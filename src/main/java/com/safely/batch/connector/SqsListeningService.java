package com.safely.batch.connector;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.safely.api.domain.enumeration.EventSeverity;
import com.safely.batch.connector.components.common.ComputePropertiesChangeListService;
import com.safely.batch.connector.components.common.ComputeReservationsChangeListService;
import com.safely.batch.connector.components.common.ConvertPmsPropertiesToSafelyService;
import com.safely.batch.connector.components.common.ConvertPmsReservationsToSafelyService;
import com.safely.batch.connector.components.external.LoadPmsAuthTokenService;
import com.safely.batch.connector.components.internal.LoadOrganizationService;
import com.safely.batch.connector.components.external.LoadPmsPropertiesService;
import com.safely.batch.connector.components.external.LoadPmsReservationsService;
import com.safely.batch.connector.components.internal.LoadPropertiesFromSafelyService;
import com.safely.batch.connector.components.internal.LoadReservationsFromSafelyService;
import com.safely.batch.connector.components.internal.LoadSafelyAuthService;
import com.safely.batch.connector.components.internal.SaveCompletionEventToSafelyService;
import com.safely.batch.connector.components.internal.SavePropertiesToSafelyService;
import com.safely.batch.connector.components.internal.SaveReservationsToSafelyService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.Acknowledgment;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.Visibility;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Service;

@SuppressWarnings("checkstyle:LineLength")
@Service
public class SqsListeningService {

    private static final Logger log = LoggerFactory.getLogger(SqsListeningService.class);

    private final int scheduledDelayValue = 60;

    private final LoadPmsAuthTokenService loadPmsAuthTokenService;
    private final LoadSafelyAuthService loadSafelyAuthService;
    private final LoadOrganizationService loadOrganizationService;
    private final LoadPmsPropertiesService loadPmsPropertiesService;
    private final LoadPmsReservationsService loadPmsReservationsService;
    private final ConvertPmsPropertiesToSafelyService convertPmsPropertiesToSafelyService;
    private final ConvertPmsReservationsToSafelyService convertPmsReservationsToSafelyService;
    private final LoadPropertiesFromSafelyService loadPropertiesFromSafelyService;
    private final LoadReservationsFromSafelyService loadReservationsFromSafelyService;
    private final ComputePropertiesChangeListService computePropertiesChangeListService;
    private final ComputeReservationsChangeListService computeReservationsChangeListService;
    private final SavePropertiesToSafelyService savePropertiesToSafelyService;
    private final SaveReservationsToSafelyService saveReservationsToSafelyService;
    private final SaveCompletionEventToSafelyService saveCompletionEventToSafelyService;
    private final ObjectMapper objectMapper;
    private final AmazonSQSAsync amazonSqsAsync;

    @Value("${safely.api.username}")
    private String apiUsername;
    @Value("${safely.api.password}")
    private String apiPassword;
    @Value("${safely.queue.inbound.visibility}")
    private int inboundQueueVisibility;
    @Value("${safely.queue.outbound.name}")
    private String outboundQueueName;

    public SqsListeningService(LoadSafelyAuthService loadSafelyAuthService,
                               LoadOrganizationService loadOrganizationService,
                               LoadPmsPropertiesService loadPmsPropertiesService,
                               LoadPmsReservationsService loadPmsReservationsService,
                               ConvertPmsPropertiesToSafelyService convertPmsPropertiesToSafelyService,
                               ConvertPmsReservationsToSafelyService convertPmsReservationsToSafelyService,
                               LoadPropertiesFromSafelyService loadPropertiesFromSafelyService,
                               LoadReservationsFromSafelyService loadReservationsFromSafelyService,
                               ComputePropertiesChangeListService computePropertiesChangeListService,
                               ComputeReservationsChangeListService computeReservationsChangeListService,
                               SavePropertiesToSafelyService savePropertiesToSafelyService,
                               SaveReservationsToSafelyService saveReservationsToSafelyService,
                               SaveCompletionEventToSafelyService saveCompletionEventToSafelyService,
                               ObjectMapper objectMapper, AmazonSQSAsync amazonSqsAsync,
                               LoadPmsAuthTokenService loadPmsAuthTokenService) {
        this.loadSafelyAuthService = loadSafelyAuthService;
        this.loadOrganizationService = loadOrganizationService;
        this.loadPmsPropertiesService = loadPmsPropertiesService;
        this.loadPmsReservationsService = loadPmsReservationsService;
        this.convertPmsPropertiesToSafelyService = convertPmsPropertiesToSafelyService;
        this.convertPmsReservationsToSafelyService = convertPmsReservationsToSafelyService;
        this.loadPropertiesFromSafelyService = loadPropertiesFromSafelyService;
        this.loadReservationsFromSafelyService = loadReservationsFromSafelyService;
        this.computePropertiesChangeListService = computePropertiesChangeListService;
        this.computeReservationsChangeListService = computeReservationsChangeListService;
        this.savePropertiesToSafelyService = savePropertiesToSafelyService;
        this.saveReservationsToSafelyService = saveReservationsToSafelyService;
        this.saveCompletionEventToSafelyService = saveCompletionEventToSafelyService;
        this.objectMapper = objectMapper;
        this.amazonSqsAsync = amazonSqsAsync;
        this.loadPmsAuthTokenService = loadPmsAuthTokenService;
    }

    @SqsListener(value = "${safely.queue.inbound.name}", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void receiveMessage(String messageJson, Visibility visibility, Acknowledgment acknowledgment) {
        ScheduledFuture<?> scheduledTask = null;
        LocalDateTime startTime = LocalDateTime.now();

        String organizationId = null;

        JobContext jobContext = new JobContext();

        jobContext.setStartTime(startTime);

        jobContext.setInboundQueueVisibility(inboundQueueVisibility);
        jobContext.setVisibility(visibility);

        EventSeverity eventSeverity = EventSeverity.INFO;

        try {

            // get organizationId from message
            ConnectorMessage message = objectMapper.readValue(messageJson, ConnectorMessage.class);

            organizationId = message.getOrganizationId();

            jobContext.setOrganizationId(organizationId);

            log.info("OrganizationId: {}. Processing message at UTC: {}. Message created on: {}",
                    organizationId, jobContext.getStartTime(), message.getCreateDate());
            AtomicReference<Exception> refreshVisibilityException = new AtomicReference<>();
            scheduledTask = getScheduledTask(organizationId, jobContext, refreshVisibilityException);

            // setup for this run
            log.info("OrganizationId: {}. Authentication in safelyAPI and loading organization data.", organizationId);
            loadSafelyAuthService.execute(jobContext, apiUsername, apiPassword);
            loadOrganizationService.execute(jobContext, organizationId);

            // load previous data
            log.info("OrganizationId: {}. Preparing to load property data from Safely.", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            loadPropertiesFromSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to load reservation data from Safely.", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            loadReservationsFromSafelyService.execute(jobContext);

            // load data from the PMS API
            checkRefreshVisibilityException(refreshVisibilityException);
            loadPmsAuthTokenService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to load property data from PMS.", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            loadPmsPropertiesService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to load reservation data from PMS.", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            loadPmsReservationsService.execute(jobContext);


            // convert PMS data to Safely format
            log.info("OrganizationId: {}. Preparing to convert PMS properties to Safely structure", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            convertPmsPropertiesToSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to convert PMS reservations to Safely structure", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            convertPmsReservationsToSafelyService.execute(jobContext);


            // compare previous data to new data for changes
            log.info("OrganizationId: {}. Preparing to compute properties change list", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            computePropertiesChangeListService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to compute reservations change list", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            computeReservationsChangeListService.execute(jobContext);

            // save any changes
            log.info("OrganizationId: {}. Preparing to save properties to Safely", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            savePropertiesToSafelyService.execute(jobContext);
            log.info("OrganizationId: {}. Preparing to save reservations to Safely", organizationId);
            checkRefreshVisibilityException(refreshVisibilityException);
            saveReservationsToSafelyService.execute(jobContext);


            // if any step reported any failures, mark severity as warning
            if (jobContext.getJobStatistics() != null) {
                for (Map.Entry<String, Map<String, Object>> entry : jobContext.getJobStatistics().entrySet()) {
                    Map<String, Object> map = entry.getValue();
                    if (map.containsKey("failed") && (int) map.get("failed") > 0) {
                        eventSeverity = EventSeverity.WARNING;
                        break;
                    }
                }
            }

            // send a message to legacy sync
            sendMessageToQueue(organizationId);

        } catch (Exception ex) {
            log.error("OrganizationId: {}, Error message: {}, Error: {}", organizationId, ex.getMessage(), ex);
            eventSeverity = EventSeverity.ERROR;
        } finally {
            // exceptions in save event are handled
            log.info("OrganizationId: {}. Preparing to save completion event to Safely", organizationId);
            jobContext.setEndTime(LocalDateTime.now());
            saveCompletionEventToSafelyService.execute(jobContext, eventSeverity);
            if (scheduledTask != null) {
                scheduledTask.cancel(true);
            }        }

        try {
            switch (eventSeverity) {
                case INFO:
                case WARNING:
                    log.info("OrganizationId: {}. Job completed with status {}. Removing message from queue.", organizationId, eventSeverity);
                    acknowledgment.acknowledge().get();
                    break;
                case ERROR:
                    log.info("OrganizationId: {}. Job completed with status {}. Allowing the message to time out back into the queue.",
                            organizationId, eventSeverity);
                    break;
                default:
                    log.error("OrganizationId: {}. Unrecognized event severity: {}", organizationId, eventSeverity);
            }
        } catch (Exception ex) {
            log.error("OrganizationId: {}. Error while trying to clean up a message. Error message: {}", organizationId, ex.getMessage());
        }
    }

    private void sendMessageToQueue(String organizationId) {

        log.info("OrganizationId: {}. Sending message to queue: '{}'", organizationId,
                outboundQueueName);

        LocalDateTime createDate = LocalDateTime.now();
        try {
            ConnectorMessage message = new ConnectorMessage();
            message.setOrganizationId(organizationId);
            message.setCreateDate(createDate);

            String messageAsJsonString = objectMapper.writeValueAsString(message);

            SendMessageRequest sendMessageRequest = new SendMessageRequest()
                    .withQueueUrl(outboundQueueName)
                    .withMessageBody(messageAsJsonString);

            this.amazonSqsAsync.sendMessage(sendMessageRequest);
        } catch (Exception ex) {
            log.error("OrganizationId: {}. Error while sending a message to queue. Error message: {}", organizationId, ex);
        }
    }

    private ScheduledFuture<?> getScheduledTask(String organizationId, JobContext jobContext, AtomicReference<Exception> refreshVisibilityException) {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        return scheduledExecutorService.scheduleAtFixedRate(
                () -> {
                    try {
                        jobContext.refreshVisibility(scheduledDelayValue + 1);
                    } catch (Exception e) {
                        log.error("OrganizationId: {}. Error occurred while trying to refresh message visibility. Error message: {}", organizationId, e.getMessage());
                        refreshVisibilityException.set(e);
                    }
                }, BigDecimal.ZERO.longValue(), scheduledDelayValue, TimeUnit.SECONDS);
    }

    private void checkRefreshVisibilityException(AtomicReference<Exception> refreshVisibilityException) throws Exception {
        if (refreshVisibilityException.get() != null) {
            throw refreshVisibilityException.get();
        }
    }
}
