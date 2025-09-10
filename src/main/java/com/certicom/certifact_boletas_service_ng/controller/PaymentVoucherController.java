package com.certicom.certifact_boletas_service_ng.controller;

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
@RequestMapping(PaymentVoucherController.API_PATH)
@RequiredArgsConstructor
@Slf4j
public class PaymentVoucherController {

    public static final String API_PATH = "/api/v1/boletas";
    private final PaymentVoucherService paymentVoucherService;
    private final PaymentVoucherValidator paymentVoucherValidator;

    @PostMapping
    public ResponseEntity<?> savePaymentVoucher(@RequestBody @Valid PaymentVoucherRequest paymentVoucherRequest) {
        Long idUsuario = 2L;
        watchLog(paymentVoucherRequest);
        paymentVoucherValidator.validate(paymentVoucherRequest, false);
        Map<String, Object> result = paymentVoucherService.createPaymentVoucher(paymentVoucherRequest, idUsuario);
        return new ResponseEntity<>(result.get(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<?> editPaymentVoucher(@RequestBody @Valid PaymentVoucherRequest paymentVoucherRequest) {
        Long idUsuario = 2L;
        watchLog(paymentVoucherRequest);
        paymentVoucherValidator.validate(paymentVoucherRequest, true);
        Map<String, Object> result = paymentVoucherService.updatePaymentVoucher(paymentVoucherRequest,  idUsuario);
        return new ResponseEntity<>(result.get(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE), HttpStatus.CREATED);
    }

    private void watchLog(Object object) {
        log.info("ComprobanteController - watchLog - [object={}]", object.toString());
    }

}
