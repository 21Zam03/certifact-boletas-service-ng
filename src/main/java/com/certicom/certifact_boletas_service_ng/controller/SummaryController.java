package com.certicom.certifact_boletas_service_ng.controller;

import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.exception.ValidationException;
import com.certicom.certifact_boletas_service_ng.jms.SqsProducer;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.validation.SummaryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping(SummaryController.API_PATH)
@RestController
@RequiredArgsConstructor
public class SummaryController {

    public static final String API_PATH = "/api/v1/summary";
    private final SummaryValidator summaryValidator;
    private final DocumentsSummaryService documentsSummaryService;
    private final SqsProducer sqsProducer;

    @PostMapping("/{fechaEmision}")
    public ResponseEntity<?> summaryByFechaEmision(
            @PathVariable("fechaEmision") String fechaEmision,
            @RequestBody(required = false) IdentificadorComprobante comprobante
    ) {
        ResponsePSE responsePSE;
        String ruc = "20204040303";
        String username = "demo@certifakt.com.pe";
        try {
            summaryValidator.validateSummaryByFechaEmision(ruc, fechaEmision);
            responsePSE = documentsSummaryService.generarSummaryByFechaEmisionAndRuc(
                    ruc,
                    fechaEmision,
                    comprobante,
                    username
            );
            if (responsePSE.getEstado()) {
                sqsProducer.produceProcessSummary(responsePSE.getTicket(), ruc);
            }
        } catch (ValidationException e) {
            responsePSE = new ResponsePSE();
            responsePSE.setEstado(false);
            responsePSE.setMensaje(e.getMessage());
        }
        return new ResponseEntity<>(responsePSE, HttpStatus.OK);
    }

}
