package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.ParameterDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ParameterRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public ParameterRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ParameterDto findByName(String name) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/parameter/name")
                .queryParam("name", name)
                .toUriString();
        return restTemplate.getForObject(url, ParameterDto.class);
    }

}
