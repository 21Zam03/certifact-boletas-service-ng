package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.sql.Timestamp;
import java.util.List;

@Service
public class PaymentVoucherRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-paymentvoucher-endpoint}")
    private String apiPaymentVoucherEndpoint;

    public PaymentVoucherRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Integer obtenerSiguienteNumeracionPorTipoComprobanteYSerieYRucEmisor(String tipoComprobante, String serie, String ruc) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint()+"/number")
                .queryParam("tipoComprobante", tipoComprobante)
                .queryParam("serie", serie)
                .queryParam("ruc", ruc)
                .toUriString();
        try {
            return restTemplate.getForObject(url, Integer.class);
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

    public PaymentVoucherDto getPaymentVoucherByIdentificadorDocumento(String identificadorDocumento) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint()+"/id-document")
                .queryParam("identificadorDocumento", identificadorDocumento)
                .toUriString();
        try {
          return restTemplate.getForObject(url, PaymentVoucherDto.class);
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

    public PaymentVoucherDto save(PaymentVoucherDto paymentVoucherDto) {
        String url = getUrlEndpoint();
        try {
            return restTemplate.postForObject(url, paymentVoucherDto, PaymentVoucherDto.class);
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

    public PaymentVoucherDto update(PaymentVoucherDto paymentVoucherDto) {
        System.out.println("UPDATE");
        String url = getUrlEndpoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentVoucherDto> requestEntity = new HttpEntity<>(paymentVoucherDto, headers);
        try {
            ResponseEntity<PaymentVoucherDto> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    PaymentVoucherDto.class
            );
            if(response.getBody() != null) {
                LogHelper.infoLog(LogMessages.currentMethod(), "La consulta se realizo exitosamente");
            }
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

    public List<PaymentVoucherDto> findListSpecificForSummary(
            String rucEmisor,
            String fechaEmision,
            String tipo,
            String serie,
            Integer numero) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/specific-summary")
                .queryParam("rucEmisor", rucEmisor)
                .queryParam("fechaEmision", fechaEmision)
                .queryParam("tipo", tipo)
                .queryParam("serie", serie)
                .queryParam("numero", numero)
                .toUriString();
        try {
            ResponseEntity<List<PaymentVoucherDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PaymentVoucherDto>>() {}
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

    public List<PaymentVoucherDto> findAllForSummaryByRucEmisorAndFechaEmision(String rucEmisor, String fechaEmision) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/summary-ruc-date")
                .queryParam("rucEmisor", rucEmisor)
                .queryParam("fechaEmision", fechaEmision)
                .toUriString();
        try {
            ResponseEntity<List<PaymentVoucherDto>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PaymentVoucherDto>>() {
                    }
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

    public void updateStateToSendSunatForSummaryDocuments(List<Long> ids, String usuario, Timestamp fechaModificacion) {
        String fechaIso = fechaModificacion.toInstant().toString();
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/state")
                .queryParam("ids", ids)
                .queryParam("usuario", usuario)
                .queryParam("fechaModificacion", fechaIso)
                .toUriString();
        try {
            restTemplate.put(url, null);
        } catch (HttpClientErrorException e) {
            LogHelper.warnLog(LogTitle.ERROR_HTTP_CLIENT.getType(), LogMessages.currentMethod(),
                    "Error "+e.getStatusCode()+" al comunicarse con el servicio externo, "+e.getMessage());
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

    public void updateComprobantesBySummaryDocuments(
            List<String> comprobantesByAceptar,
            String codigo,
            String abreviado,
            String usuario,
            Timestamp fechaModificacion) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/payment-summary-accept")
                .queryParam("comprobantesByAceptar", comprobantesByAceptar)
                .queryParam("codigo", codigo)
                .queryParam("abreviado", abreviado)
                .queryParam("usuario", usuario)
                .queryParam("fechaModificacion", fechaModificacion)
                .toUriString();
        try {
            restTemplate.put(url, null);
        } catch (HttpClientErrorException e) {
            LogHelper.warnLog(LogTitle.ERROR_HTTP_CLIENT.getType(), LogMessages.currentMethod(),
                    "Error "+e.getStatusCode()+" al comunicarse con el servicio externo, "+e.getMessage());
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

    public void updateComprobantesOnResumenError(
            List<String> identificadoresComprobantes,
            String usuario,
            Timestamp fechaModificacion) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/payment-summary-error")
                .queryParam("identificadoresComprobantes", identificadoresComprobantes)
                .queryParam("usuario", usuario)
                .queryParam("fechaModificacion", fechaModificacion)
                .toUriString();
        try {
            restTemplate.put(url, null);
        } catch (HttpClientErrorException e) {
            LogHelper.warnLog(LogTitle.ERROR_HTTP_CLIENT.getType(), LogMessages.currentMethod(),
                    "Error "+e.getStatusCode()+" al comunicarse con el servicio externo, "+e.getMessage());
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

    public PaymentVoucherDto findByRucAndTipoAndSerieAndNumero(
            String finalRucEmisor,
            String tipoComprobante,
            String serie,
            Integer numero) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/ruc-type-serie-number")
                .queryParam("finalRucEmisor", finalRucEmisor)
                .queryParam("tipoComprobante", tipoComprobante)
                .queryParam("serie", serie)
                .queryParam("numero", numero)
                .toUriString();
        try {
            return restTemplate.getForObject(url, PaymentVoucherDto.class);
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

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiPaymentVoucherEndpoint;
    }

    public List<PaymentVoucherDto> findAllByTipoComprobanteInAndNumDocIdentReceptorAndRucEmisorAndTipoOperacionAndEstadoOrderByNumDocIdentReceptor(
            List<String> tipoComprobante, String numDocIdentReceptor, String rucEmisor, String tipoOperacion, String estado) {
        try {
            // Construcción de la URL base
            String url = getUrlEndpoint() + "/anticipo";

            // Construimos los parámetros de la URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("numDocIdentReceptor", numDocIdentReceptor)
                    .queryParam("rucEmisor", rucEmisor)
                    .queryParam("tipoOperacion", tipoOperacion)
                    .queryParam("estado", estado);

            // Agregamos los valores múltiples del parámetro tipoComprobante
            for (String tipo : tipoComprobante) {
                builder.queryParam("tipoComprobante", tipo);
            }

            String uri = builder.toUriString();

            // Llamada GET
            ResponseEntity<List<PaymentVoucherDto>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PaymentVoucherDto>>() {}
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

    public List<PaymentVoucherDto> getPaymentVocuherByCredito(String numDocIdentReceptor, String rucEmisor) {
        try {
            String url = getUrlEndpoint() + "/credito";

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("numDocIdentReceptor", numDocIdentReceptor)
                    .queryParam("rucEmisor", rucEmisor);

            String uri = builder.toUriString();

            ResponseEntity<List<PaymentVoucherDto>> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<PaymentVoucherDto>>() {}
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

    public PaymentVoucherDto findPaymentVoucherById(Long id) {
        String url = getUrlEndpoint()  + "/" +id;
        try {
            ResponseEntity<PaymentVoucherDto> response = restTemplate.getForEntity(url, PaymentVoucherDto.class);
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
