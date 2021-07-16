package com.safely.batch.connector.entity.property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PmsProperty {

    @JsonProperty("PropertyName")
    private String propertyName;

    @JsonProperty("PropertyID")
    private String id;

    @JsonProperty("Street")
    private String street;

    @JsonProperty("City")
    private String city;

    @JsonProperty("State")
    private String state;

    @JsonProperty("Zip")
    private String zipCode;

    @JsonProperty("Country")
    private String country;

    @JsonProperty("Website")
    private String webSite;

    @JsonProperty("BookingChannels")
    private List<String> bookingChannels;

    @JsonProperty("OwnerName")
    private String ownerName;

    @JsonProperty("OwnerStreet")
    private String ownerStreet;

    @JsonProperty("OwnerCity")
    private String ownerCity;

    @JsonProperty("OwnerState")
    private String ownerState;

    @JsonProperty("OwnerZip")
    private String ownerZip;

    @JsonProperty("OwnerCountry")
    private String ownerCountry;

    @JsonProperty("OwnerEmail")
    private String ownerEmail;

    @JsonProperty("OwnerPhone")
    private String ownerPhone;

}
