package com.certicom.certifact_boletas_service_ng.feign.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class AnticipoRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public AnticipoRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public int deleteAnticipoById(Long anticipoId) {
        String url = String.format("%s/%d", baseUrl, anticipoId);
        try {
            restTemplate.delete(url);
            // Si llega aquí, la eliminación fue exitosa (HTTP 200 o 204)
            return 1;
        } catch (HttpClientErrorException.NotFound e) {
            // Si el anticipo no existe
            return 0;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Error 4xx o 5xx
            throw new RuntimeException("Error al eliminar anticipo: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            // Error de conexión o timeout
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

}
