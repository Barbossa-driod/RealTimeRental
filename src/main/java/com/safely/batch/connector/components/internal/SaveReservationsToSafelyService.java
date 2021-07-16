package com.safely.batch.connector.components.internal;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorReservationsService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SaveReservationsToSafelyService {

    private static final Logger log = LoggerFactory.getLogger(SaveReservationsToSafelyService.class);

    private static final String UPDATED = "updated";
    private static final String CREATED = "created";
    private static final String FAILED = "failed";
    private static final String PROCESSED = "processed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String STEP_NAME = "save_reservations_to_safely";

    private final SafelyConnectorReservationsService reservationsService;

    public SaveReservationsToSafelyService(SafelyConnectorReservationsService reservationsService) {
        this.reservationsService = reservationsService;
    }

    public void execute(JobContext jobContext) {

        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();
        JWTToken token = jobContext.getSafelyToken();

        int successfullyCreated = 0;

        List<String> failedIds = new ArrayList<>();

        List<Reservation> newReservations = jobContext.getNewReservations();
        log.info("OrganizationId: {}. Writing {} new reservations for organization with name: {}",
                organization.getEntityId(), newReservations.size(), organization.getName());
        for (Reservation reservation : newReservations) {
            try {
                reservationsService.create(token.getIdToken(), reservation);
                successfullyCreated++;
            } catch (Exception e) {
                log.error("OrganizationId: {}. Failed to create reservation with referenceId {}. Error message: {}",
                        organization.getEntityId(), reservation.getReferenceId(), e.getMessage());
                failedIds.add(reservation.getReferenceId());
            }
        }

        List<Reservation> updatedReservations = jobContext.getUpdatedReservations();
        log.info("OrganizationId: {}. Writing {} updated reservations for organization with name: {}",
                organization.getEntityId(), updatedReservations.size(), organization.getName());

        int successfullyUpdated = 0;

        for (Reservation reservation : updatedReservations) {
            try {
                reservationsService.save(token.getIdToken(), reservation);
                successfullyUpdated++;
            } catch (Exception e) {
                log.error("OrganizationId: {}. Failed to update reservation with referenceId {}. Error message: {}",
                        organization.getEntityId(), reservation.getReferenceId(), e.getMessage());
                failedIds.add(reservation.getReferenceId());
            }
        }

        log.info("OrganizationId: {}.  Found {} successfully created reservations.", jobContext.getOrganizationId(), successfullyCreated);
        log.info("OrganizationId: {}. Found {} successfully updated reservations.", jobContext.getOrganizationId(), successfullyUpdated);
        log.info("OrganizationId: {}. Found {} failed reservations.", jobContext.getOrganizationId(), failedIds.size());

        stepStatistics.put(CREATED, successfullyCreated);
        stepStatistics.put(UPDATED, successfullyUpdated);
        stepStatistics.put(FAILED, failedIds.size());
        stepStatistics.put(PROCESSED, newReservations.size() + updatedReservations.size());
        stepStatistics.put(FAILED_IDS, failedIds);
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }
}

