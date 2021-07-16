package com.safely.batch.connector.components.common;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Reservation;
import com.safely.batch.connector.JobContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ComputeReservationsChangeListService {

    private static final Logger log = LoggerFactory.getLogger(ComputeReservationsChangeListService.class);

    private static final String UPDATED = "updated";
    private static final String CREATED = "created";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String PROCESSED = "processed";
    private static final String STEP_NAME = "compute_reservations_change_list";

    public void execute(JobContext jobContext) {
        Organization organization = jobContext.getOrganization();

        log.info("OrganizationId: {}. Processing reservations to find changes for organization with name: {}",
                organization.getEntityId(), organization.getName());

        Map<String, Object> stepStatistics = new HashMap<>();

        List<Reservation> safelyReservations = jobContext.getCurrentSafelyReservations();

        List<Reservation> pmsReservations = jobContext.getPmsSafelyReservations();

        //Add all the safely reservation to a Map so we can look them up by Reference ID
        Map<String, Reservation> safelyReservationLookup = new HashMap<>();
        for (Reservation safelyReservation : safelyReservations) {
            safelyReservationLookup.put(safelyReservation.getReferenceId(), safelyReservation);
        }

        //Find all new reservations
        List<Reservation> newReservations = new ArrayList<>();
        List<Reservation> updatedReservations = new ArrayList<>();
        List<String> erroredReservations = new ArrayList<>();

        for (Reservation pmsReservation : pmsReservations) {
            try {
                Reservation safelyReservation = safelyReservationLookup
                        .get(pmsReservation.getReferenceId());
                if (safelyReservation == null) {
                    newReservations.add(pmsReservation);
                } else if (!safelyReservation.equals(pmsReservation)) {
                    updateReservation(safelyReservation, pmsReservation);
                    updatedReservations.add(safelyReservation);
                }
            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to compute updates for reservation with referenceId %s",
                        jobContext.getOrganizationId(), pmsReservation.getReferenceId());
                log.error(message, e);
                erroredReservations.add(pmsReservation.getReferenceId());
            }
        }

        log.info("OrganizationId: {}. Found {} new reservations.", jobContext.getOrganizationId(), newReservations.size());
        log.info("OrganizationId: {}. Found {} updated reservations.", jobContext.getOrganizationId(), updatedReservations.size());

        // TODO: Implement any custom logic for handling missing or deleted reservations from the PMS.

        jobContext.setNewReservations(newReservations);
        jobContext.setUpdatedReservations(updatedReservations);

        stepStatistics.put(CREATED, newReservations.size());
        stepStatistics.put(UPDATED, updatedReservations.size());
        stepStatistics.put(PROCESSED, pmsReservations.size());
        stepStatistics.put(FAILED_IDS, erroredReservations);
        stepStatistics.put(FAILED, erroredReservations.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    }

    protected Reservation updateReservation(Reservation safelyReservation,
                                            Reservation pmsReservation) {

        safelyReservation.setOrganizationId(pmsReservation.getOrganizationId());
        safelyReservation.setLegacyOrganizationId(pmsReservation.getLegacyOrganizationId());
        safelyReservation.setReferenceId(pmsReservation.getReferenceId());

        // property data
        safelyReservation.setPropertyReferenceId(pmsReservation.getPropertyReferenceId());
        safelyReservation.setPropertyName(pmsReservation.getPropertyName());

        // category values
        safelyReservation.setCategory1(pmsReservation.getCategory1());
        safelyReservation.setCategory2(pmsReservation.getCategory2());
        safelyReservation.setCategory3(pmsReservation.getCategory3());
        safelyReservation.setCategory4(pmsReservation.getCategory4());

        // guest counts
        safelyReservation.setAdults(pmsReservation.getAdults());
        safelyReservation.setChildren(pmsReservation.getChildren());
        safelyReservation.setInfants(pmsReservation.getInfants());
        safelyReservation.setPets(pmsReservation.getPets());
        safelyReservation.setSmoker(pmsReservation.getSmoker());
        safelyReservation.setGuests(pmsReservation.getGuests());

        // price values
        safelyReservation.setCurrency(pmsReservation.getCurrency());
        safelyReservation.setPriceNightly(pmsReservation.getPriceNightly());
        safelyReservation.setPriceTotal(pmsReservation.getPriceTotal());

        // classification types
        safelyReservation.setReservationType(pmsReservation.getReservationType());
        safelyReservation.setBookingChannelType(pmsReservation.getBookingChannelType());

        // reservation dates
        safelyReservation.setArrivalDate(pmsReservation.getArrivalDate());
        safelyReservation.setDepartureDate(pmsReservation.getDepartureDate());
        safelyReservation.setBookingDate(pmsReservation.getBookingDate());
        safelyReservation.setPmsCreateDate(pmsReservation.getPmsCreateDate());
        safelyReservation.setPmsUpdateDate(pmsReservation.getPmsUpdateDate());

        // reservation status
        safelyReservation.setStatus(pmsReservation.getStatus());
        safelyReservation.setPmsStatus(pmsReservation.getPmsStatus());

        safelyReservation.setPmsObjectHashcode(pmsReservation.getPmsObjectHashcode());

        return safelyReservation;
    }
}
