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

    @Value("${app.api.url}")
    private String baseUrl;

    public DetailPaymentVoucherRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deleteDetailPaymentVoucherById(Long detailPaymentVoucherId) {
        String url = String.format("%s/api/detail-payment-voucher/%d", baseUrl, detailPaymentVoucherId);
        try {
            restTemplate.delete(url);
            // Si llega aquí, la eliminación fue exitosa (HTTP 200 o 204)
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            // Si el registro no se encontró
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Errores HTTP 4xx o 5xx
            throw new RuntimeException("Error al eliminar el detalle de pago: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            // Error de conexión o timeout
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

}
