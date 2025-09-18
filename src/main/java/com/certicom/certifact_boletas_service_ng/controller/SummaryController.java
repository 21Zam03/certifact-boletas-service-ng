package com.certicom.certifact_boletas_service_ng.controller;

import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.validation.SummaryValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(SummaryController.API_PATH)
@RestController
@RequiredArgsConstructor
public class SummaryController {

    public static final String API_PATH = "/api/sunat";
    private final SummaryValidator summaryValidator;
/*
    public ResponseEntity<?> summaryByFechaEmision(
            @PathVariable("fechaEmision") String fechaEmision,
            @RequestBody(required = false) IdentificadorComprobante comprobante
    ) {
        ResponsePSE responsePSE;
        String ruc = "";
        try {
            summaryValidator.validateSummaryByFechaEmision(user.getRuc(), fechaEmision);
            responsePSE = documentsSummaryService.generarSummaryByFechaEmisionAndRuc(
                    user.getRuc(),
                    fechaEmision,
                    comprobante,
                    user.getUsername()
            );

            if (responsePSE.getEstado()) {
                messageProducer.produceProcessSummary(responsePSE.getTicket(), user.getRuc());
            }
        } catch (ValidatorFieldsException e) {

            responsePSE = new ResponsePSE();
            responsePSE.setEstado(false);
            responsePSE.setMensaje(e.getMensajeValidacion());
        }

        return new ResponseEntity<ResponsePSE>(responsePSE, HttpStatus.OK);

    }
*/
}
