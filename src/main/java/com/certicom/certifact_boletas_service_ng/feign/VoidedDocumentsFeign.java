package com.certicom.certifact_boletas_service_ng.feign;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "voidedDocuments")
public interface VoidedDocumentsFeign {

    String getEstadoByNumeroTicket(String ticket);

}
