package com.safely.batch.connector;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import com.safely.batch.connector.entity.property.PmsProperty;
import com.safely.batch.connector.entity.reservation.PmsReservation;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.aws.messaging.listener.Visibility;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Data
public class JobContext {

    private static final Logger log = LoggerFactory.getLogger(SqsListeningService.class);

    final int maxSeconds = 60 * 60 * 12; // 12 hours is max allowed by sqs

    private final static String BASE_URL = "BASE_URL";
    private final static String SERVER_KEY = "SERVER_KEY";
    private final static String SERVER_SECRET = "SERVER_SECRET";

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int inboundQueueVisibility;
    private Visibility visibility;

    // authentication toke for Safely API
    private JWTToken safelyToken;

    // the organization this job is being performed for
    private Organization organization;
    private String organizationId;

    // properties loaded from PMS
    private List<PmsProperty> pmsProperties;
    // reservations loaded from PMS
    private List<PmsReservation> pmsReservations;

    // PMS properties that have been converted to the safely domain model
    private List<Property> pmsSafelyProperties;
    // PMS reservations that have been converted to the safely domain model
    private List<Reservation> pmsSafelyReservations;

    // Current properties for the organization in safely
    private List<Property> currentSafelyProperties;
    // Current reservations for the organization in safely
    private List<Reservation> currentSafelyReservations;

    // Reservations that need to be created/updated/removed from safely
    private List<Reservation> newReservations;
    private List<Reservation> updatedReservations;
    private List<Reservation> removedReservations;

    // Properties that need to be created/updated/removed from safely
    private List<Property> newProperties;
    private List<Property> updatedProperties;
    private List<Property> removedProperties;

    Map<String, Map<String, Object>> jobStatistics = new HashMap<>();

    // PmsToken
    private String token;

    public void refreshVisibility(int additionalSeconds) throws Exception {
        log.info("OrganizationId: {}. Preparing to refresh message visibility.", organizationId);
        LocalDateTime now = LocalDateTime.now();
        int lengthOfJobInSeconds = (int) ChronoUnit.SECONDS.between(this.getStartTime(), now);
        int secondsLeftInVisibility = inboundQueueVisibility - lengthOfJobInSeconds;

        if (secondsLeftInVisibility <= 0) {
            String msg = String.format("OrganizationId: %s. Job has taken longer than message visibility. StartTime: '%s' Now: '%s' Length of Job: %s Seconds In Visibility: %s", organizationId, this.getStartTime(), now, lengthOfJobInSeconds, inboundQueueVisibility);
            log.error(msg);
            throw new Exception(msg);
        }

        int maxAllowedSecondsToAdd = maxSeconds - secondsLeftInVisibility;
        int finalSecondsToAdd = Math.max(Math.min(maxAllowedSecondsToAdd, additionalSeconds), 0);
        log.info("OrganizationId: {}. Message visibility timeout refresh. Current Length of Job: {} Seconds Left in Visibility: {}, Seconds to Add: {} Final Seconds to Add: {}", organizationId, lengthOfJobInSeconds, secondsLeftInVisibility, additionalSeconds, finalSecondsToAdd);
        if (finalSecondsToAdd > secondsLeftInVisibility) {
            visibility.extend(finalSecondsToAdd).get();
            inboundQueueVisibility += finalSecondsToAdd;
        }
    }
}
