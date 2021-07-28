package com.safely.batch.connector.components.common;

import com.safely.api.domain.*;
import com.safely.api.domain.enumeration.BookingChannelType;
import com.safely.api.domain.enumeration.PhoneType;
import com.safely.api.domain.enumeration.ReservationStatus;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.entity.property.PmsProperty;
import com.safely.batch.connector.entity.reservation.PmsGuest;
import com.safely.batch.connector.entity.reservation.PmsReservation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConvertPmsReservationsToSafelyService {

    private static final Logger log = LoggerFactory.getLogger(ConvertPmsReservationsToSafelyService.class);

    private static final String CONVERTED = "converted";
    private static final String PROCESSED = "processed";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String STEP_NAME = "convert_pms_reservations_to_safely";

    private static final String ACTIVE = "active";
    private static final String CANCELED = "canceled";
    private static final String CANCELLED = "cancelled";
    private static final String APPROVED = "approved";
    private static final String EXECUTED = "executed";
    private static final String CHECKED_IN = "checked in";
    private static final String FAIL = "fail";
    private static final String FINAL = "final";
    private static final String NEW = "new";
    private static final String NON_PARTICIPATING_RESERVATION = "non participating reservation";
    private static final String OWNER_SIGNED = "owner signed";
    private static final String OWNER_RESERVATION = "owner reservation";
    private static final String PASS = "pass";
    private static final String TENANT_SIGNED = "tenant signed";
    private static final String UNKNOWN = "unknown";

    public void execute(JobContext jobContext) {

        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();
        log.info("OrganizationId: {}. Convert PMS reservations to Safely structure.", organization);

        Map<String, PmsProperty> pmsPropertiesMap = jobContext.getPmsProperties()
                        .stream()
                        .collect(Collectors.toMap(PmsProperty::getId, pmsProperty -> pmsProperty));

        List<PmsReservation> pmsReservations = jobContext.getPmsReservations();

        List<Reservation> pmsConvertedReservations = new ArrayList<>();

        List<String> failedReservationKeys = new ArrayList<>();

        for (PmsReservation pmsReservation : pmsReservations) {
            try {

                if (pmsPropertiesMap.containsKey(pmsReservation.getPropertyId())){
                    Reservation convertedReservation = convertToSafelyReservation(pmsPropertiesMap.get(pmsReservation.getPropertyId()),
                            organization, pmsReservation);
                    pmsConvertedReservations.add(convertedReservation);
                } else {
                    String message = String.format("OrganizationId: %s. Failed to find Property with referenceId %s",
                            jobContext.getOrganizationId(), pmsReservation.getPropertyId());
                    log.warn(message);
                }

            } catch (Exception e) {
                String message = String.format("OrganizationId: %s. Failed to convert Reservation with referenceId %s",
                        jobContext.getOrganizationId(), pmsReservation.getLeaseId());
                log.error(message, e);
                failedReservationKeys.add(String.valueOf(pmsReservation.getLeaseId()));
            }
        }

        jobContext.setPmsSafelyReservations(pmsConvertedReservations);
        log.info("OrganizationId: {}. Converted reservations count: {}", jobContext.getOrganizationId(), pmsConvertedReservations.size());

        stepStatistics.put(CONVERTED, pmsConvertedReservations.size());
        stepStatistics.put(PROCESSED, pmsReservations.size());
        stepStatistics.put(FAILED, failedReservationKeys.size());
        stepStatistics.put(FAILED_IDS, failedReservationKeys);
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }

    protected Reservation convertToSafelyReservation(PmsProperty pmsProperty, Organization organization,
                                                     PmsReservation pmsReservation) {

        Reservation safelyReservation = new Reservation();

        //Organization
        safelyReservation.setOrganizationId(organization.getEntityId());
        safelyReservation.setLegacyOrganizationId(organization.getLegacyOrganizationId());

        //Reservation
        safelyReservation.setReferenceId(String.valueOf(pmsReservation.getLeaseId()));

        //Category1
        safelyReservation.setCategory1(pmsReservation.getType());

        //Category2
        safelyReservation.setCategory2(pmsReservation.getStatus());

        //Dates
        setReservationDates(pmsReservation, safelyReservation);

        //Guest
        setReservationGuests(pmsReservation, safelyReservation);

        //Property information
        safelyReservation.setPropertyId(pmsReservation.getPropertyId());

        //Status
        setReservationStatus(pmsReservation, safelyReservation);

        //BookingChannelType
        setBookingChannelType(pmsProperty,safelyReservation,organization);

        return safelyReservation;
    }

    private void setReservationDates(PmsReservation pmsReservation, Reservation safelyReservation) {
        LocalDateTime now = LocalDateTime.now();
        safelyReservation.setPmsCreateDate(now);
        safelyReservation.setArrivalDate(pmsReservation.getArrival());
        safelyReservation.setDepartureDate(pmsReservation.getDeparture());
    }

    private void setReservationGuests(PmsReservation pmsReservation, Reservation safelyReservation) {

        List<Guest> safelyGuests = new ArrayList<>();

        List<PmsGuest> pmsGuests = pmsReservation.getPmsGuests();

        for (PmsGuest pmsGuest: pmsGuests){
            Guest safelyGuest = new Guest();

            safelyGuest.setReferenceId(pmsGuest.getGuestId());
            safelyGuest.setFirstName(pmsGuest.getFirstName());
            safelyGuest.setLastName(pmsGuest.getLastName());

            //Emails
            setGuestEmails(pmsGuest, safelyGuest);

            //Phone
            setGuestPhoneNumbers(pmsGuest, safelyGuest);

            safelyGuests.add(safelyGuest);
        }
        safelyReservation.setGuests(safelyGuests);
    }

    private void setGuestEmails(PmsGuest pmsGuest, Guest guest) {

        List<GuestEmail> guestEmails = new ArrayList<>();

        if (pmsGuest.getEmail() != null && !pmsGuest.getEmail().isEmpty()) {
            GuestEmail guestEmail = new GuestEmail();
            guestEmail.setPrimary(Boolean.TRUE);
            guestEmail.setEmailAddress(pmsGuest.getEmail());
            guestEmails.add(guestEmail);
        }
        guest.setGuestEmails(guestEmails);
    }

    private void setGuestPhoneNumbers(PmsGuest pmsGuest, Guest guest) {

        List<GuestPhone> guestPhones = new ArrayList<>();

        if (pmsGuest.getMobilePhone() != null && !pmsGuest.getMobilePhone().isEmpty()) {
            GuestPhone guestMobilePhone = new GuestPhone();
            guestMobilePhone.setNumber(pmsGuest.getMobilePhone());
            guestMobilePhone.setType(PhoneType.PERSONAL);
            guestMobilePhone.setPrimary(Boolean.TRUE);

            guestPhones.add(guestMobilePhone);
        }

        if (pmsGuest.getHomePhone() != null && !pmsGuest.getHomePhone().isEmpty()){
            GuestPhone guestHomePhone = new GuestPhone();
            guestHomePhone.setNumber(pmsGuest.getHomePhone());
            guestHomePhone.setType(PhoneType.HOME);
            guestHomePhone.setPrimary(Boolean.TRUE);

            guestPhones.add(guestHomePhone);
        }

        guest.setGuestPhones(guestPhones);
    }

    // TODO: 15.07.2021 Ask question about to Troy about BookingChannel's array
    private void setBookingChannelType(PmsProperty pmsProperty, Reservation safelyReservation,
                                       Organization organization) {

        List<String> bookingChannels = pmsProperty.getBookingChannels();
        String pmsChannel = bookingChannels.get(0);

        if (pmsChannel != null) {

            safelyReservation.setCategory3(pmsChannel);

            //calculate the mapped ReservationType
            BookingChannelType bookingChannelType = BookingChannelType.OTHER;
            Map<String, String> bookingChannelTypeMap = organization
                    .getPmsChannelTypesToSafelyBookingChannelTypesMapping();
            if (bookingChannelTypeMap != null) {
                String safelyBookingChannelType = BookingChannelType.OTHER.name();

                if (bookingChannelTypeMap.containsKey(pmsChannel)) {
                    safelyBookingChannelType = bookingChannelTypeMap.get(pmsChannel);

                } else {
                    log.warn("No booking channel type mapping found for PMS value {} for client {} ({})",
                            pmsChannel, organization.getName(), organization.getEntityId());
                }

                try {
                    bookingChannelType = BookingChannelType.valueOf(safelyBookingChannelType);
                } catch (Exception ex) {
                    log.error(
                            "Failed to convert Booking Channel Type string to enum. PMS value: {}. Safely value: {} for client {} ({})",
                            pmsChannel, safelyBookingChannelType, organization.getName(),
                            organization.getEntityId());
                    log.error(ex.getMessage());
                }
            } else {
                log.warn("No booking type mappings setup for client {} ({})", organization.getName(),
                        organization.getEntityId());
            }

            safelyReservation.setBookingChannelType(bookingChannelType);
        }
    }

    private void setReservationStatus(PmsReservation pmsReservation, Reservation safelyReservation) {

        ReservationStatus status = getReservationStatus(pmsReservation);

        safelyReservation.setPmsStatus(status);
        safelyReservation.setStatus(status);
    }

    private ReservationStatus getReservationStatus(PmsReservation pmsReservation) {

        switch (pmsReservation.getStatus().toLowerCase()) {
            case FAIL:
            case CANCELLED:
            case CANCELED:
            case UNKNOWN:
            case NON_PARTICIPATING_RESERVATION:
                return ReservationStatus.CANCELLED;
            case NEW:
            case ACTIVE:
            case OWNER_SIGNED:
            case TENANT_SIGNED:
            case APPROVED:
            case CHECKED_IN:
                return ReservationStatus.ACTIVE;
            case FINAL:
            case EXECUTED:
            case PASS:
                return ReservationStatus.COMPLETE;
            case OWNER_RESERVATION:
                return ReservationStatus.INACTIVE;
            default:
                if (pmsReservation.getDeparture() != null
                        && pmsReservation.getDeparture().isBefore(LocalDate.now()))
                    return ReservationStatus.COMPLETE;
                else {
                    log.warn("Unknown status for reservation {} ({})", pmsReservation.getLeaseId(),
                            pmsReservation.getStatus());
                    return ReservationStatus.ACTIVE;
                }
        }
    }
}
