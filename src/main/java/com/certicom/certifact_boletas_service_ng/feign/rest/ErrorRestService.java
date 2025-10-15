package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.ErrorDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ErrorRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public ErrorRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ErrorDto findFirst1ByCodeAndDocument(String codigoRespuesta, String tipoDocumento) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/error")
                .queryParam("codigoRespuesta", codigoRespuesta)
                .queryParam("tipoDocumento", tipoDocumento)
                .toUriString();

        return restTemplate.getForObject(url, ErrorDto.class);
    }

}
