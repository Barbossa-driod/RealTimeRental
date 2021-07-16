package com.safely.batch.connector.components.external;

import com.safely.batch.connector.JobContext;
import com.safely.batch.connector.client.PropertiesClient;
import com.safely.batch.connector.entity.property.PmsProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LoadPmsPropertiesService {

    private static final Logger log = LoggerFactory.getLogger(LoadPmsPropertiesService.class);

    private static final String STEP_NAME = "load_properties_from_pms";
    private static final String LOADED = "loaded";

    private final PropertiesClient propertiesClient;

    public LoadPmsPropertiesService(PropertiesClient propertiesClient) {
        this.propertiesClient = propertiesClient;
    }

    public void execute(JobContext jobContext){

        Map<String, Object> stepStatistics = new HashMap<>();
        log.info("OrganizationId: {}. Loading properties from PMS", jobContext.getOrganizationId());

        String pmsToken = jobContext.getToken();
        List<PmsProperty> allProperties = propertiesClient.getAllProperties(pmsToken);

        log.info("OrganizationId: {}. Loaded {} properties from PMS.", jobContext.getOrganizationId(), allProperties.size());

        stepStatistics.put(LOADED, allProperties.size());
        jobContext.getJobStatistics().put(STEP_NAME, stepStatistics);

        jobContext.setPmsProperties(allProperties);
    }

}
