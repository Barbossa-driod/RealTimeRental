package com.safely.batch.connector.components.common;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.api.domain.PropertyContact;
import com.safely.api.domain.enumeration.PropertyContactType;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.entity.property.PmsProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConvertPmsPropertiesToSafelyService {

    private static final Logger log = LoggerFactory.getLogger(ConvertPmsPropertiesToSafelyService.class);

    private static final String CONVERTED = "converted";
    private static final String PROCESSED = "processed";
    private static final String FAILED = "failed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String STEP_NAME = "convert_pms_properties_to_safely";


    public void execute(JobContext jobContext){
        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();
        log.info("OrganizationId: {}. Convert PMS properties to Safely structure.", organization);

        List<PmsProperty> pmsProperties = jobContext.getPmsProperties();

        List<Property> pmsConvertedProperties = new ArrayList<>();

        List<String> failedPropertyKeys = new ArrayList<>();


        for (PmsProperty pmsProperty: pmsProperties){
           try {
               Property convertedProperty = convertToSafelyProperty(organization, pmsProperty);
               pmsConvertedProperties.add(convertedProperty);

           }catch (Exception e){
               String message = String.format("OrganizationId: %s. Failed to convert property with Id %s",
                       jobContext.getOrganizationId(), pmsProperty.getId());
               log.error(message,e);

               failedPropertyKeys.add(pmsProperty.getId());
           }
        }

        jobContext.setPmsSafelyProperties(pmsConvertedProperties);

        log.info("OrganizationId: {}. Converted properties count: {}", jobContext.getOrganizationId(), pmsConvertedProperties.size());

        stepStatistics.put(CONVERTED, pmsConvertedProperties.size());
        stepStatistics.put(PROCESSED, pmsProperties.size());
        stepStatistics.put(FAILED, failedPropertyKeys.size());
        stepStatistics.put(FAILED_IDS, failedPropertyKeys);

        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }


    protected Property convertToSafelyProperty(Organization organization, PmsProperty pmsProperty){

        Property safelyProperty = new Property();

        //Organization
        safelyProperty.setOrganizationId(organization.getEntityId());
        safelyProperty.setLegacyOrganizationId(organization.getLegacyOrganizationId());

        //Property
        safelyProperty.setReferenceId(String.valueOf(pmsProperty.getId()));

        // TODO: 15.07.2021 Wait Steve's response about it
        if (pmsProperty.getPropertyName() == null){
            safelyProperty.setName(pmsProperty.getOwnerName());
        }else {
            safelyProperty.setName(pmsProperty.getPropertyName());
        }

        //Address
        safelyProperty.setStreetLine1(pmsProperty.getStreet());
        safelyProperty.setStateCode(pmsProperty.getState());
        safelyProperty.setCity(pmsProperty.getCity());
        safelyProperty.setPostalCode(pmsProperty.getZipCode());
        safelyProperty.setCountryCode(pmsProperty.getCountry());

        //Owner
        setPropertyContacts(pmsProperty,safelyProperty);

        //HashCode
        safelyProperty.setPmsObjectHashcode(pmsProperty.hashCode());

        //DateTime
        LocalDateTime now = LocalDateTime.now();
        safelyProperty.setPmsCreateDate(now);
        safelyProperty.setPmsUpdateDate(now);

        return safelyProperty;
    }


    private void setPropertyContacts(PmsProperty pmsProperty, Property safelyProperty){
        List<PropertyContact> contacts = new ArrayList<>();
        contacts.add(convertPropertyOwner(pmsProperty));
        safelyProperty.setPropertyContacts(contacts);
    }

    private PropertyContact convertPropertyOwner(PmsProperty pmsProperty) {

        if (pmsProperty == null) {
            return null;
        }

        PropertyContact owner = new PropertyContact();
        owner.setContactType(PropertyContactType.OWNER);

        owner.setFirstName(pmsProperty.getOwnerName());

        owner.setPhone(pmsProperty.getOwnerPhone());
        owner.setEmail(pmsProperty.getOwnerEmail());

        owner.setStreetLine1(pmsProperty.getOwnerStreet());
        owner.setCity(pmsProperty.getOwnerCity());
        owner.setStateCode(pmsProperty.getOwnerState());
        owner.setPostalCode(pmsProperty.getOwnerZip());
        owner.setCountryCode(pmsProperty.getOwnerCountry());

        return owner;
    }
}
