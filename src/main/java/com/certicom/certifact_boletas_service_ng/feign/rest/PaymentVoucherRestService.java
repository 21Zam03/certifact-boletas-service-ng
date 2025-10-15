package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
        return restTemplate.getForObject(url, Integer.class);
    }

    public PaymentVoucherDto getPaymentVoucherByIdentificadorDocumento(String identificadorDocumento) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint()+"/id-document")
                .queryParam("identificadorDocumento", identificadorDocumento)
                .toUriString();
        return restTemplate.getForObject(url, PaymentVoucherDto.class);
    }

    public PaymentVoucherDto save(PaymentVoucherDto paymentVoucherDto) {
        String url = getUrlEndpoint();
        return restTemplate.postForObject(url, paymentVoucherDto, PaymentVoucherDto.class);
    }

    public PaymentVoucherDto update(PaymentVoucherDto paymentVoucherDto) {
        String url = getUrlEndpoint();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PaymentVoucherDto> requestEntity = new HttpEntity<>(paymentVoucherDto, headers);
        ResponseEntity<PaymentVoucherDto> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                requestEntity,
                PaymentVoucherDto.class
        );
        return response.getBody();
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
        ResponseEntity<List<PaymentVoucherDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PaymentVoucherDto>>() {}
        );
        return response.getBody();
    }

    public List<PaymentVoucherDto> findAllForSummaryByRucEmisorAndFechaEmision(String rucEmisor, String fechaEmision) {
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/summary-ruc-date")
                .queryParam("rucEmisor", rucEmisor)
                .queryParam("fechaEmision", fechaEmision)
                .toUriString();

        ResponseEntity<List<PaymentVoucherDto>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PaymentVoucherDto>>() {}
        );
        return response.getBody();
    }

    public void updateStateToSendSunatForSummaryDocuments(List<Long> ids, String usuario, Timestamp fechaModificacion) {
        String fechaIso = fechaModificacion.toInstant().toString();
        String url = UriComponentsBuilder.fromHttpUrl(getUrlEndpoint() + "/state")
                .queryParam("ids", ids)
                .queryParam("usuario", usuario)
                .queryParam("fechaModificacion", fechaIso)
                .toUriString();
        restTemplate.put(url, null);
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
        restTemplate.put(url, null);
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
        restTemplate.put(url, null);
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
        return restTemplate.getForObject(url, PaymentVoucherDto.class);
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiPaymentVoucherEndpoint;
    }

}
