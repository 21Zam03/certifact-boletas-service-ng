package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.OseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
public class CompanyRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-company-endpoint}")
    private String apiCompanyEndpoint;

    public CompanyRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getStateFromCompanyByRuc(String rucEmisor) {
        String url = getUrlEndpoint()+"/state?rucEmisor=" + rucEmisor;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al obtener el estado de la empresa: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

    public CompanyDto findCompanyByRuc(String ruc) {
        String url = getUrlEndpoint()+"/"+ruc;
        try {
            ResponseEntity<CompanyDto> response = restTemplate.getForEntity(url, CompanyDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al obtener la empresa: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

    public OseDto findOseByRucInter(String ruc) {
        String url = getUrlEndpoint()+"/ose?ruc="+ruc;
        try {
            ResponseEntity<OseDto> response = restTemplate.getForEntity(url, OseDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al obtener el OSE: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiCompanyEndpoint;
    }
}
