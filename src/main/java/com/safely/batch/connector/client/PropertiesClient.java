package com.safely.batch.connector.client;

import com.safely.batch.connector.entity.property.PmsProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static com.safely.batch.connector.client.RealTimeRentalClient.getHeaders;


@Slf4j
@Service
public class PropertiesClient {

    @Value("${real.rent.api.uri.properties}")
    private String propertiesEndpoint;

    @Value("${real.rent.api.uri.properties.by.id}")
    private String propertiesByIdEndpoint;

    public List<PmsProperty> getAllProperties(String accessToken){

        List<PmsProperty> properties = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = getHeaders(accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<List<PmsProperty>> response;

        try {
            response = restTemplate.exchange(propertiesEndpoint, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful()) {
                properties = response.getBody();

                log.debug("Found properties: Properties: {} ", properties);

            }

        } catch (Exception e){
            log.error("Exception loading properties ", e);
        }

        return properties;
    }

    public PmsProperty[] getPropertyById(String propertyId, String accessToken){

        PmsProperty[] pmsProperty = null;

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = getHeaders(accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<PmsProperty[]> response;

        try {
            response = restTemplate.exchange(String.format(propertiesByIdEndpoint, propertyId), HttpMethod.GET, entity, PmsProperty[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                pmsProperty = response.getBody();

                log.debug("Found Property Response: ID {} Property: {}", propertyId, pmsProperty);

            }

        } catch (Exception e){
            log.error("Exception loading property ID [{}]", propertyId, e);

        }

        return pmsProperty;
    }

}
