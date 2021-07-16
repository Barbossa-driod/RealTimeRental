package com.safely.batch.connector.components.external;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.ReservationsClient;
import com.safely.batch.connector.entity.reservation.PmsReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoadPmsReservationsService {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsReservationsService.class);

    private static final String STEP_NAME = "load_reservations_from_pms";
    private static final String LOADED = "loaded";
    private final ReservationsClient reservationsClient;

    public LoadPmsReservationsService(ReservationsClient reservationsClient) {
        this.reservationsClient = reservationsClient;
    }

    public void execute(JobContext jobContext){

        Map<String, Object> stepStatistics = new HashMap<>();
        log.info("OrganizationId: {}. Loading reservations from PMS", jobContext.getOrganizationId());

        String pmsToken = jobContext.getToken();
        List<PmsReservation> allReservations = reservationsClient.getAllReservations(pmsToken);

        log.info("OrganizationId: {}. Loaded {} reservations from PMS.", jobContext.getOrganizationId(), allReservations.size());

        stepStatistics.put(LOADED, allReservations.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

        jobContext.setPmsReservations(allReservations);
    }
}
