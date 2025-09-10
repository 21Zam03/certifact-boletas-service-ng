package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.BranchOfficesDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8090", contextId = "branchOffices")
public interface BranchOfficeFeign {

    @GetMapping("/api/office")
    public BranchOfficesDto obtenerOficinaPorEmpresaIdYSerieYTipoComprobante(
            @RequestParam Integer empresaId, @RequestParam String serie, @RequestParam String tipoComprobante
    );

}
