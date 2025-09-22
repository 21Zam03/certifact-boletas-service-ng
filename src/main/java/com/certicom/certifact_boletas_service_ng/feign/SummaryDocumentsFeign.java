package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.others.RucEstadoOther;
import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "summaryDocuments")
public interface SummaryDocumentsFeign {

    @GetMapping("/api/summary-documents/sequential-number")
    public Integer getSequentialNumberInSummaryByFechaEmision(@RequestParam String rucEmisor, @RequestParam String fechaEmision);

    @PostMapping("/api/summary-documents")
    public Summary save(@RequestBody Summary summaryDto);

    @GetMapping("/api/summary-documents/state-ruc")
    public List<RucEstadoOther> getEstadoAndRucEmisorByNumeroTicket(@RequestParam String ticket);

    @GetMapping("/api/summary-documents/state")
    public String getEstadoByNumeroTicket(@RequestParam String ticket);

    @GetMapping("/api/summary-documents/ticket")
    Summary findByTicket(@RequestParam String ticket);

    @GetMapping("/api/summary-documents/id-document-summary")
    Long getIdDocumentSummaryByIdPaymentVoucher(@RequestParam Long idPaymentVoucher);

}
