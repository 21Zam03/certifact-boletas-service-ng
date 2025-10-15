package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class AditionalFieldRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public AditionalFieldRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Integer findAditionalFieldIdByValorCampo(String nombreCampo) {
        String url = String.format("%s?nombreCampo=%s", baseUrl, nombreCampo);
        try {
            ResponseEntity<Integer> response = restTemplate.getForEntity(url, Integer.class);
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Manejo de error similar al de Feign
            throw new ServiceException("Error al obtener el campo adicional: " + e.getMessage());
        }
    }

    public int deleteAditionalFieldPaymentById(Long aditionalPaymentId) {
        String url = String.format("%s/%d", baseUrl, aditionalPaymentId);
        try {
            restTemplate.delete(url);
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al eliminar el campo adicional: " + e.getMessage(), e);
        }
    }

}
