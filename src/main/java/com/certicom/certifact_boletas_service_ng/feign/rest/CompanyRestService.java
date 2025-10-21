package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.OseDto;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

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

    public CompanyDto findCompanyByRuc(String ruc) {
        String url = getUrlEndpoint()+"/"+ruc;
        try {
            ResponseEntity<CompanyDto> response = restTemplate.getForEntity(url, CompanyDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            LogHelper.warnLog(LogTitle.ERROR_HTTP_CLIENT.getType(), LogMessages.currentMethod(),
                    "Error 4xx al comunicarse con el servicio externo", e);
            return null;
        } catch (HttpServerErrorException e) {
            LogHelper.errorLog(LogTitle.ERROR_HTTP_SERVER.getType(), LogMessages.currentMethod(),
                    "Error 5xx al comunicarse con el servicio externo", e);
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

    public OseDto findOseByRucInter(String ruc) {
        String url = getUrlEndpoint()+"/ose?ruc="+ruc;
        try {
            ResponseEntity<OseDto> response = restTemplate.getForEntity(url, OseDto.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            LogHelper.warnLog(LogTitle.ERROR_HTTP_CLIENT.getType(), LogMessages.currentMethod(),
                    "Error 4xx al comunicarse con el servicio externo", e);
            return null;
        } catch (HttpServerErrorException e) {
            LogHelper.errorLog(LogTitle.ERROR_HTTP_SERVER.getType(), LogMessages.currentMethod(),
                    "Error 5xx al comunicarse con el servicio externo", e);
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
        return this.baseUrl+this.apiCompanyEndpoint;
    }
}
