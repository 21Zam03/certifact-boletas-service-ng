package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class CuotaPaymentVoucherRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public CuotaPaymentVoucherRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deletePaymentCuotaById(Long paymentCuotaId) {
        String url = String.format("%s/api/payment_cuotas/%d", baseUrl, paymentCuotaId);
        try {
            restTemplate.delete(url);
            // Si llega aquí, la eliminación fue exitosa (HTTP 200 o 204)
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            // Si no se encuentra el recurso
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al eliminar la cuota de pago: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

}
