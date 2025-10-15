package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GuiaPaymentRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public GuiaPaymentRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deleteGuiaPaymentById(Long guiaPaymentId) {
        String url = baseUrl + "/api/guia-payment-voucher/" + guiaPaymentId;
        restTemplate.delete(url);
        return 1; // Simula el retorno del Feign (si se elimina correctamente)
    }

}
