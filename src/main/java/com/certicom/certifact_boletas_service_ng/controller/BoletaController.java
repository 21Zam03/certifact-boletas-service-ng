package com.certicom.certifact_boletas_service_ng.controller;

import com.certicom.certifact_boletas_service_ng.formatter.PaymentVoucherFormatter;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.service.PaymentVoucherService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.validation.PaymentVoucherValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(BoletaController.API_PATH_TEST)
@RequiredArgsConstructor
@Slf4j
public class BoletaController {

    public static final String API_PATH = "/api/v1/boletas";
    public static final String API_PATH_TEST = "/api";

    private final PaymentVoucherService paymentVoucherService;
    private final PaymentVoucherValidator paymentVoucherValidator;
    private final PaymentVoucherFormatter paymentVoucherFormatter;

    @PostMapping("/comprobantes-pago")
    public ResponseEntity<?> savePaymentVoucher(@RequestBody @Valid PaymentVoucherRequest paymentVoucherRequest) {
        //Id usuario por defecto va ir en duro hasta saber como identificar al usuario que haral a peticion desde el gateway
        Long idUsuario = 2L;
        String rucEmisor = "20204040303";
        paymentVoucherRequest.setRucEmisor(rucEmisor);

        paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherRequest);
        paymentVoucherValidator.validate(paymentVoucherRequest, false);
        Map<String, Object> result = paymentVoucherService.createPaymentVoucher(paymentVoucherRequest, idUsuario);
        return new ResponseEntity<>(result.get(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE), HttpStatus.OK);
    }

    @PostMapping("/editar-comprobante")
    public ResponseEntity<?> editPaymentVoucher(@RequestBody @Valid PaymentVoucherRequest paymentVoucherRequest) {
        Long idUsuario = 2L;

        paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherRequest);
        paymentVoucherValidator.validate(paymentVoucherRequest, true);
        Map<String, Object> result = paymentVoucherService.updatePaymentVoucher(paymentVoucherRequest,  idUsuario);
        return new ResponseEntity<>(result.get(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE), HttpStatus.CREATED);
    }

}
