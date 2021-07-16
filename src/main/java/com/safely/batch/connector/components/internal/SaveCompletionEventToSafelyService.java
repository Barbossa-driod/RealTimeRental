package com.safely.batch.connector.components.internal;

import com.safely.api.domain.Event;
import com.safely.api.domain.enumeration.EventSeverity;
import com.safely.api.domain.enumeration.EventStatus;
import com.safely.api.domain.enumeration.EventSubType;
import com.safely.api.domain.enumeration.EventType;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.common.services.safely.SafelyConnectorEventsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SaveCompletionEventToSafelyService {
    private static final Logger log = LoggerFactory.getLogger(SaveCompletionEventToSafelyService.class);
    private final SafelyConnectorEventsService eventsService;

    public SaveCompletionEventToSafelyService(SafelyConnectorEventsService eventsService) {
        this.eventsService = eventsService;
    }

    public void execute(JobContext jobContext, EventSeverity eventSeverity) {

        Event event = new Event();
        event.setEventType(EventType.CONNECTOR);
        event.setEventSubType(EventSubType.REALTIMERENTAL);

        event.setEventStatus(EventStatus.COMPLETE);
        event.setStartTime(jobContext.getStartTime());
        event.setEndTime(jobContext.getEndTime());

        event.setCreatedReservations(
                jobContext.getNewReservations() != null ? jobContext.getNewReservations().size() : 0);
        event.setUpdateReservations(
                jobContext.getUpdatedReservations() != null ? jobContext.getUpdatedReservations().size()
                        : 0);
        event.setCancelledReservations(0);

        if (jobContext.getOrganization() != null) {
            event.setOrganizationId(jobContext.getOrganization().getEntityId());
            event.setOrganizationName(jobContext.getOrganization().getName());
        }

        event.setSeverity(eventSeverity);
        switch (eventSeverity) {
            case INFO:
                event.setDescription("Job completed successfully.");
                break;
            case WARNING:
                event.setDescription("Job completed with errors.");
                break;
            case ERROR:
                event.setDescription("Job failed.");
                break;
            default:
                log.error("OrganizationId: {}. Unrecognized event severity: {}", jobContext.getOrganizationId(), eventSeverity);
        }

        event.setJobStatistics(jobContext.getJobStatistics());

        try {
            eventsService.create(jobContext.getSafelyToken().getIdToken(), event);
        } catch (Exception ex) {
            log.error("OrganizationId: {}. Event: [{}]", jobContext.getOrganizationId(), event);
            log.error("OrganizationId: {}. Error while writing Event to API.", jobContext.getOrganizationId(), ex);
        }

        log.info("OrganizationId: {}. Job completed: [{}]", jobContext.getOrganizationId(), event);
    }
}
