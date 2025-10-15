package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AditionalFieldRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-aditionalfieldpayment-endpoint}")
    private String apiAditionalFieldEndpoint;

    public AditionalFieldRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deleteAditionalFieldPaymentById(Long aditionalPaymentId) {
        String url = getUrlEndpoint()+"/"+aditionalPaymentId;
        try {
            restTemplate.delete(url);
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al eliminar el campo adicional: " + e.getMessage(), e);
        }
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiAditionalFieldEndpoint;
    }

}
