package com.certicom.certifact_boletas_service_ng.controller.web;

import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.request.VoucherAnnularRequest;
import com.certicom.certifact_boletas_service_ng.service.DocumentsVoidedService;
import com.certicom.certifact_boletas_service_ng.validation.SummaryValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(VoidedDocumentsController.API_PATH)
@RequiredArgsConstructor
@Slf4j
public class VoidedDocumentsController {

    public final static String API_PATH = "/api/web/boletas";
    private final DocumentsVoidedService documentsVoidedService;
    private final SummaryValidator summaryValidator;

    @PostMapping("/anulacion-comprobantes")
    public ResponseEntity<?> anularPaymentVoucher(
            @RequestBody List<VoucherAnnularRequest> documentosToAnular,
            @RequestHeader(name = "X-User-Ruc", required = true) String userRuc,
            @RequestHeader(name = "X-User-Id", required = true) String userId,
            @RequestHeader(name = "X-User-Roles", required = true) String rol
    ) {
        List<String> ticketsVoidedProcess = new ArrayList<>();

        String username = "demo@certifakt.com.pe";

        summaryValidator.validateSummaryByFechaEmision(userRuc,
                "2025-11-20");

        ResponsePSE resp = documentsVoidedService.anularDocuments(
                documentosToAnular,
                userRuc,
                username, ticketsVoidedProcess);

        return new ResponseEntity<>(resp, HttpStatus.OK);
    }


}
