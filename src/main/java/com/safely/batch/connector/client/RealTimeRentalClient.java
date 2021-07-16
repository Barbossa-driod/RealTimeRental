package com.safely.batch.connector.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class RealTimeRentalClient {

    private static final String AUTHENTICATION_BEARER_FORMAT = "Bearer %s";

    protected static HttpHeaders getHeaders(String accessToken){

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        headers.add("Authorization",String.format(AUTHENTICATION_BEARER_FORMAT, accessToken));

        return headers;
    }
}
