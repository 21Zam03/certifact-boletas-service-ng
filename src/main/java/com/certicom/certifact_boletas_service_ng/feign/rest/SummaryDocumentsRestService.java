package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.others.RucEstadoOther;
import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class SummaryDocumentsRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public SummaryDocumentsRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Integer getSequentialNumberInSummaryByFechaEmision(String rucEmisor, String fechaEmision) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/summary-documents/sequential-number")
                .queryParam("rucEmisor", rucEmisor)
                .queryParam("fechaEmision", fechaEmision)
                .toUriString();
        ResponseEntity<Integer> response = restTemplate.exchange(
                url, HttpMethod.GET, null, Integer.class);
        return response.getBody();
    }

    public Summary save(Summary summaryDto) {
        String url = baseUrl + "/api/summary-documents";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Summary> request = new HttpEntity<>(summaryDto, headers);
        ResponseEntity<Summary> response = restTemplate.exchange(
                url, HttpMethod.POST, request, Summary.class);
        return response.getBody();
    }

    public List<RucEstadoOther> getEstadoAndRucEmisorByNumeroTicket(String ticket) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/summary-documents/state-ruc")
                .queryParam("ticket", ticket)
                .toUriString();
        ResponseEntity<List<RucEstadoOther>> response = restTemplate.exchange(
                url, HttpMethod.GET, null, new ParameterizedTypeReference<List<RucEstadoOther>>() {});
        return response.getBody();
    }

    public String getEstadoByNumeroTicket(String ticket) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/summary-documents/state")
                .queryParam("ticket", ticket)
                .toUriString();
        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, null, String.class);
        return response.getBody();
    }

    public Summary findByTicket(String ticket) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/summary-documents/ticket")
                .queryParam("ticket", ticket)
                .toUriString();
        ResponseEntity<Summary> response = restTemplate.exchange(
                url, HttpMethod.GET, null, Summary.class);
        return response.getBody();
    }

    public Long getIdDocumentSummaryByIdPaymentVoucher(Long idPaymentVoucher) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/summary-documents/id-document-summary")
                .queryParam("idPaymentVoucher", idPaymentVoucher)
                .toUriString();
        ResponseEntity<Long> response = restTemplate.exchange(
                url, HttpMethod.GET, null, Long.class);
        return response.getBody();
    }

}
