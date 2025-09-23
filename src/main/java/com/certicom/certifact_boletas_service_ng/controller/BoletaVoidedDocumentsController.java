package com.certicom.certifact_boletas_service_ng.controller;

import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.request.VoucherAnnularRequest;
import com.certicom.certifact_boletas_service_ng.service.DocumentsVoidedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(BoletaVoidedDocumentsController.API_PATH)
@RequiredArgsConstructor
@Slf4j
public class BoletaVoidedDocumentsController {

    public final static String API_PATH = "/api/v1/voided";
    private final DocumentsVoidedService documentsVoidedService;

    @PostMapping
    public ResponseEntity<?> anularPaymentVoucher(@RequestBody List<VoucherAnnularRequest> documentosToAnular) {
        List<String> ticketsVoidedProcess = new ArrayList<>();
        String rucEmisor = "20204040303";
        String username = "demo@certifakt.com.pe";
        ResponsePSE resp = documentsVoidedService.anularDocuments(
                documentosToAnular,
                rucEmisor,
                username, ticketsVoidedProcess);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }


}
