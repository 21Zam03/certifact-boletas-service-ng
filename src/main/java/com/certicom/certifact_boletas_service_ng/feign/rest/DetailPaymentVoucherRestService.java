package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class DetailPaymentVoucherRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-detailpayment-endpoint}")
    private String apiDetailPaymentEndpoint;

    public DetailPaymentVoucherRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deleteDetailPaymentVoucherById(Long detailPaymentVoucherId) {
        String url = getUrlEndpoint()+"/"+detailPaymentVoucherId;
        try {
            restTemplate.delete(url);
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al eliminar el detalle de pago: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiDetailPaymentEndpoint;
    }

}
