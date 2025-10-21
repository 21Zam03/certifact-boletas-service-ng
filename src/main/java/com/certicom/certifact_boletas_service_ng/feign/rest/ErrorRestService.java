package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.ErrorDto;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class ErrorRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-error-catalog}")
    private String apiErrorCatalog;

    public ErrorRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ErrorDto findFirst1ByCodeAndDocument(String codigoRespuesta, String tipoDocumento) {
        String url = UriComponentsBuilder
                .fromHttpUrl(getUrlEndpoint())
                .queryParam("codigoRespuesta", codigoRespuesta)
                .queryParam("tipoDocumento", tipoDocumento)
                .toUriString();
        try {
            return restTemplate.getForObject(url, ErrorDto.class);
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
        return this.baseUrl+this.apiErrorCatalog;
    }

}
