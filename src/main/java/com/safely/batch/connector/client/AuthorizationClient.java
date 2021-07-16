package com.safely.batch.connector.client;

import com.safely.batch.connector.entity.AuthenticationResponseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class AuthorizationClient {

    @Value("${real.rent.api.uri.token}")
    private String TokenEndpoint;

    public AuthenticationResponseToken getPmsToken(String username, String password) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        map.add("grant_type", "password");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        AuthenticationResponseToken responseToken;

        log.info("Loading Authentication Token from RealTimeRental.");
        try {

            ResponseEntity<AuthenticationResponseToken> response = restTemplate.postForEntity(
                    TokenEndpoint, request , AuthenticationResponseToken.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Authentication Token retrieval failed! Error Code: {}", response.getStatusCodeValue());
                throw new Exception();
            }
            responseToken = response.getBody();

        } catch (Exception e){
            log.error("Exception while retrieving Authentication token from RealTimeRental!", e);
            throw e;
        }
        return responseToken;
    }
}
