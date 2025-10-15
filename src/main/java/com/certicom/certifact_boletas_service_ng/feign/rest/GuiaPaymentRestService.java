package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GuiaPaymentRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-guiapayment-endpoint}")
    private String apiGuiaPaymentEndpoint;

    public GuiaPaymentRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deleteGuiaPaymentById(Long guiaPaymentId) {
        String url = getUrlEndpoint() + "/" + guiaPaymentId;
        restTemplate.delete(url);
        return 1;
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiGuiaPaymentEndpoint;
    }

}
