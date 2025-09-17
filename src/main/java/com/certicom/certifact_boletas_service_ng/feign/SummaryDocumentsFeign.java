package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "summaryDocuments")
public interface SummaryDocumentsFeign {

    @GetMapping("/api/summary-documents/sequential-number")
    public Integer getSequentialNumberInSummaryByFechaEmision(@RequestParam String rucEmisor, @RequestParam String fechaEmision);

    @PostMapping
    public Summary save(@RequestBody Summary summaryDto);

}
