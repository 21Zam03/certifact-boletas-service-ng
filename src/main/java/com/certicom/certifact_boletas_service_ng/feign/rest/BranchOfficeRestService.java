package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.BranchOfficesDto;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
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
        } catch (HttpClientErrorException e) {
            LogHelper.warnLog(LogTitle.ERROR_HTTP_CLIENT.getType(), LogMessages.currentMethod(),
                    "Error "+e.getStatusCode()+" al comunicarse con el servicio externo, "+e.getMessage());
            return null;
        } catch (HttpServerErrorException e) {
            LogHelper.errorLog(LogTitle.ERROR_HTTP_SERVER.getType(), LogMessages.currentMethod(),
                    "Error "+e.getStatusCode()+" al comunicarse con el servicio externo, "+ e.getMessage());
            throw new ServiceException(LogMessages.ERROR_HTTP_SERVER, e);
        } catch (ResourceAccessException e) {
            LogHelper.errorLog(LogTitle.ERROR_HTTP_RED.getType(), LogMessages.currentMethod(),
                    "Error de conexión con el servicio externo", e);
            throw new ServiceException(LogMessages.ERROR_HTTP_RED, e);
        } catch (RestClientException ex) {
            LogHelper.errorLog(LogTitle.ERROR_HTTP.getType(), LogMessages.currentMethod(),
                    "Error inesperado al comunicarse con el servicio externo", ex);
            throw new ServiceException(LogMessages.ERROR_HTTP, ex);
        }
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiOfficeEndpoint;
    }

}
