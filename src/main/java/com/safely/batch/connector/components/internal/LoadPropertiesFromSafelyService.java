package com.safely.batch.connector.components.internal;

import com.safely.api.domain.Organization;
import com.safely.api.domain.Property;
import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.common.domain.safely.auth.JWTToken;
import com.safely.batch.connector.common.services.safely.SafelyConnectorPropertiesService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoadPropertiesFromSafelyService {

    private static final Logger log = LoggerFactory.getLogger(LoadPropertiesFromSafelyService.class);

    private static final String STEP_NAME = "load_properties_from_safely";
    private static final String LOADED = "loaded";
    private final SafelyConnectorPropertiesService propertiesService;

    public LoadPropertiesFromSafelyService(SafelyConnectorPropertiesService propertiesService) {
        this.propertiesService = propertiesService;
    }

    public void execute(JobContext jobContext) throws Exception {

        Map<String, Object> stepStatistics = new HashMap<>();

        Organization organization = jobContext.getOrganization();

        JWTToken token = jobContext.getSafelyToken();

        List<Property> currentSafelyProperties = propertiesService
                .getAll(token.getIdToken(), organization.getEntityId());
        log.info("OrganizationId: {}. Loaded {} Safely properties for organization with name: {}",
                organization.getEntityId(), currentSafelyProperties.size(), organization.getName());

        jobContext.setCurrentSafelyProperties(currentSafelyProperties);

        stepStatistics.put(LOADED, currentSafelyProperties.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

    }
}
