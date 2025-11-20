package com.certicom.certifact_boletas_service_ng.controller.web;

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

    public static final String API_PATH = "/api/web/boletas";
    private final SummaryValidator summaryValidator;
    private final DocumentsSummaryService documentsSummaryService;
    private final SqsProducer sqsProducer;

    @PostMapping("/resumen-diario/{fechaEmision}")
    public ResponseEntity<?> summaryByFechaEmision(
            @PathVariable("fechaEmision") String fechaEmision,
            @RequestBody(required = false) IdentificadorComprobante comprobante,
            @RequestHeader(name = "X-User-Ruc", required = true) String userRuc,
            @RequestHeader(name = "X-User-Id", required = true) String userId,
            @RequestHeader(name = "X-User-Roles", required = true) String rol
    ) {
        ResponsePSE responsePSE;

        String username = "demo@certifakt.com.pe";
        try {
            summaryValidator.validateSummaryByFechaEmision(userRuc, fechaEmision);
            responsePSE = documentsSummaryService.generarSummaryByFechaEmisionAndRuc(
                    userRuc,
                    fechaEmision,
                    comprobante,
                    username
            );
            if (responsePSE.getEstado()) {
                sqsProducer.produceProcessSummary(responsePSE.getTicket(), userRuc);
            }
        } catch (ValidationException e) {
            responsePSE = new ResponsePSE();
            responsePSE.setEstado(false);
            responsePSE.setMensaje(e.getMessage());
        }
        return new ResponseEntity<>(responsePSE, HttpStatus.OK);
    }

}
