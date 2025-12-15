package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VoidedDocumentsRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    public VoidedDocumentsRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getEstadoByNumeroTicket(String ticket) {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/api/voided-documents/state")
                .queryParam("ticket", ticket)
                .toUriString();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, String.class
            );
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
            LogHelper.errorLog(LogMessages.currentMethod(),
                    "Error de conexión con el servicio externo", e);
            throw new ServiceException(LogMessages.ERROR_HTTP_RED, e);
        } catch (RestClientException ex) {
            LogHelper.errorLog(LogMessages.currentMethod(),
                    "Error inesperado al comunicarse con el servicio externo", ex);
            throw new ServiceException(LogMessages.ERROR_HTTP, ex);
        }
    }

}
