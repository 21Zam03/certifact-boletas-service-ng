package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "facturas-service-sp", url = "http://localhost:8092", contextId = "registerFileUpload")
public interface RegisterFileUploadFeign {

    @GetMapping("/api/register-file-upload")
    public RegisterFileUploadDto findFirst1ByPaymentVoucherIdPaymentVoucherAndTipoArchivoAndEstadoArchivoOrderByOrdenDesc
            (@RequestParam Long idPayment, @RequestParam String tipoArchivo, @RequestParam String estadoArchivo);

    @PostMapping("/api/register-file-upload")
    public RegisterFileUploadDto saveRegisterFileUpload(@RequestBody RegisterFileUploadDto registerFileUploadModelDto);

    @GetMapping("/api/register-file-upload/id&uuid&tipo")
    public RegisterFileUploadDto getDataForCdr(@RequestParam Long id);

}
