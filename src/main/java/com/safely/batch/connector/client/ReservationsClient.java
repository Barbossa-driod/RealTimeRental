package com.safely.batch.connector.client;

import com.safely.batch.connector.entity.reservation.PmsReservation;
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
public class ReservationsClient {

    @Value("${real.rent.api.uri.reservations}")
    private String reservationEndpoint;

    public List<PmsReservation> getAllReservations(String accessToken){

        List<PmsReservation> reservations = new ArrayList<>();

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = getHeaders(accessToken);

        HttpEntity<String> entity = new HttpEntity<>("", headers);

        ResponseEntity<List<PmsReservation>> response;

        try{
            response = restTemplate.exchange(reservationEndpoint, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

            if (response.getStatusCode().is2xxSuccessful()) {
                reservations = response.getBody();

                log.debug("Found reservations: Properties: {} ", reservations);

            }
        }catch (Exception e){
            log.error("Exception loading reservations ", e);
        }

        return reservations;
    }
}
