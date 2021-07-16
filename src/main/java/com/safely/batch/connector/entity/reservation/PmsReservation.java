package com.safely.batch.connector.entity.reservation;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsReservation {

    @JsonProperty("PropertyID")
    private String propertyId;

    @JsonProperty("LeaseID")
    private String leaseId;

    @JsonProperty("ArrivalDate")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDate arrival;

    @JsonProperty("DepartureDate")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDate departure;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("Type")
    private String type;

    @JsonProperty("Guests")
    private List<PmsGuest> pmsGuests;

}
