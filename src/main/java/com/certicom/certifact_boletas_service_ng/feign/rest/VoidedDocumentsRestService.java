package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VoidedDocumentsRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public VoidedDocumentsRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getEstadoByNumeroTicket(String ticket) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/voided-documents/state")
                .queryParam("ticket", ticket)
                .toUriString();
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, null, String.class
        );
        return response.getBody();
    }

}
