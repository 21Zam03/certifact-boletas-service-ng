package com.certicom.certifact_boletas_service_ng.controller;

import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.service.AmazonS3ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(BoletaFileController.API_PATH_TEST)
@RequiredArgsConstructor
public class BoletaFileController {

    public static final String API_PATH = "/api/internal/file";
    public static final String API_PATH_TEST = "/api";

    private final AmazonS3ClientService amazonS3ClientService;

    @GetMapping("/descargacdruuid/{id}/{uuid}")
    public ResponseEntity<?> downloadSummaryCDR(
            @PathVariable Long id, @PathVariable String uuid) throws IOException {
        ByteArrayResource resource = amazonS3ClientService.downloadFileInvoice(id, uuid, TipoArchivoEnum.CDR);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }

    /*DESCARGA EL XML DEL COMPROBANTE UNICO, no el xml del resumen diario*/
    @GetMapping("/descargaxmluuid/{id}/{uuid}/{nameDocument}")
    public ResponseEntity<?> downloadXML(@PathVariable Long id, @PathVariable String uuid, @PathVariable String nameDocument) throws IOException {
        ByteArrayResource resource = amazonS3ClientService.downloadFileInvoice(id, uuid, TipoArchivoEnum.XML);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(resource);
    }


}
