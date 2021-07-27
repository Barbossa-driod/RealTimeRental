package com.safely.batch.connector.components.internal;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorPropertiesService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SavePropertiesToSafelyService {

    private static final Logger log = LoggerFactory.getLogger(SavePropertiesToSafelyService.class);

    private static final String UPDATED = "updated";
    private static final String CREATED = "created";
    private static final String FAILED = "failed";
    private static final String PROCESSED = "processed";
    private static final String FAILED_IDS = "failed_ids";
    private static final String STEP_NAME = "save_reservations_to_safely";
    private final SafelyConnectorPropertiesService propertiesService;

    public SavePropertiesToSafelyService(SafelyConnectorPropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    public void execute(JobContext jobContext) throws Exception {

        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();

        JWTToken token = jobContext.getSafelyToken();

        List<Property> newProperties = jobContext.getNewProperties();

        List<String> failedIds = new ArrayList<>();

        log.info("OrganizationId: {}. Writing {} new properties for organization with name: {}",
                organization.getEntityId(), newProperties.size(), organization.getName());

        int successfullyCreated = 0;

        for (Property property : newProperties) {
            try {
                propertiesService.create(token.getIdToken(), property);
                successfullyCreated++;
            } catch (Exception e) {
                log.error("OrganizationId: {}. Failed to create property with ReferenceID {}. Error message: {}",
                        organization.getEntityId(), property.getReferenceId(), e.getMessage());
                failedIds.add(property.getReferenceId());
            }
        }

        List<Property> updatedProperties = jobContext.getUpdatedProperties();
        int updatedSuccessfully = 0;
        log.info("OrganizationId: {}. Writing {} updated properties for organization with name: {}",
                organization.getEntityId(), updatedProperties.size(), organization.getName());

        for (Property property : updatedProperties) {
            try {
                propertiesService.save(token.getIdToken(), property);
                updatedSuccessfully++;
            } catch (Exception e) {
                log.error("OrganizationId: {}. Failed to update property with ReferenceID {}. Error message: {}",
                        organization.getEntityId(), property.getReferenceId(), e.getMessage());
                failedIds.add(property.getReferenceId());
            }

        }

        log.info("OrganizationId: {}. Found {} successfully created properties.", jobContext.getOrganizationId(), successfullyCreated);
        log.info("OrganizationId: {}. Found {} successfully updated properties.", jobContext.getOrganizationId(), updatedSuccessfully);
        log.info("OrganizationId: {}. Found {} failed properties.", jobContext.getOrganizationId(), failedIds.size());

        stepStatistics.put(CREATED, successfullyCreated);
        stepStatistics.put(UPDATED, updatedSuccessfully);
        stepStatistics.put(FAILED, failedIds.size());
        stepStatistics.put(PROCESSED, newProperties.size() + updatedProperties.size());
        stepStatistics.put(FAILED_IDS, failedIds);
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);
    }
}
