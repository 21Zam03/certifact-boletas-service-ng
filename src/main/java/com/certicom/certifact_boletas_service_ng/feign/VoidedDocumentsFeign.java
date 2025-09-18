package com.certicom.certifact_boletas_service_ng.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "voidedDocuments")
public interface VoidedDocumentsFeign {

    @GetMapping("/api/voided-documents/state")
    String getEstadoByNumeroTicket(@RequestParam String ticket);

}
