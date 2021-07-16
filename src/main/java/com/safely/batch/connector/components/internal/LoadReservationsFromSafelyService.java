package com.safely.batch.connector.components.internal;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorReservationsService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadReservationsFromSafelyService {

    private static final Logger log = LoggerFactory.getLogger(LoadReservationsFromSafelyService.class);

    private static final String STEP_NAME = "load_reservations_from_safely";
    private static final String LOADED = "loaded";
    private final SafelyConnectorReservationsService reservationsService;

    public LoadReservationsFromSafelyService(SafelyConnectorReservationsService reservationsService) {
        this.reservationsService = reservationsService;
    }

    public void execute(JobContext jobContext) throws Exception {

        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();

        JWTToken token = jobContext.getSafelyToken();

        List<Reservation> currentSafelyReservations = reservationsService
                .getAll(token.getIdToken(), organization.getEntityId());

        log.info("OrganizationId: {}. Loaded {} Safely reservations for organization with name: {}",
                organization.getEntityId(), currentSafelyReservations.size(), organization.getName());

        jobContext.setCurrentSafelyReservations(currentSafelyReservations);

        stepStatistics.put(LOADED, currentSafelyReservations.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }
}
