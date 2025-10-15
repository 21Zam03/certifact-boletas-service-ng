package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.BranchOfficesDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class BranchOfficeRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("external.services.boleta-service-sp.endpoints.api-office-endpoint")
    private String apiOfficeEndpoint;

    public BranchOfficeRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BranchOfficesDto obtenerOficinaPorEmpresaIdYSerieYTipoComprobante(
            Integer empresaId, String serie, String tipoComprobante) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl(getUrlEndpoint())
                .queryParam("empresaId", empresaId)
                .queryParam("serie", serie)
                .queryParam("tipoComprobante", tipoComprobante);
        try {
            ResponseEntity<BranchOfficesDto> response =
                    restTemplate.getForEntity(uriBuilder.toUriString(), BranchOfficesDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new RuntimeException("Error al obtener la oficina: " + e.getMessage(), e);
        } catch (ResourceAccessException e) {
            throw new RuntimeException("No se pudo conectar al servicio boletas-service-sp", e);
        }
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiOfficeEndpoint;
    }

}
