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

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-cuota-endpoint}")
    private String apiCuotaEndpoint;

    public CuotaPaymentVoucherRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deletePaymentCuotaById(Long paymentCuotaId) {
        String url = getUrlEndpoint()+"/"+paymentCuotaId;
        try {
            restTemplate.delete(url);
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al eliminar la cuota de pago: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiCuotaEndpoint;
    }

}
