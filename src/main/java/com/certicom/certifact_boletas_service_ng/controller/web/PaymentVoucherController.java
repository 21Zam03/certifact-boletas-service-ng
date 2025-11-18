package com.certicom.certifact_boletas_service_ng.controller.web;

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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(PaymentVoucherController.API_PATH)
@RequiredArgsConstructor
@Slf4j
public class PaymentVoucherController {

    public static final String API_PATH = "/api/web/boletas";

    private final PaymentVoucherService paymentVoucherService;
    private final PaymentVoucherValidator paymentVoucherValidator;
    private final PaymentVoucherFormatter paymentVoucherFormatter;

    @PostMapping("/comprobantes-pago")
    public ResponseEntity<?> savePaymentVoucher(
            @RequestBody @Valid PaymentVoucherRequest paymentVoucherRequest,
            @RequestHeader(name = "X-User-Ruc", required = true) String userRuc,
            @RequestHeader(name = "X-User-Id", required = true) String userId,
            @RequestHeader(name = "X-User-Roles", required = true) String rol
    ) {
        paymentVoucherRequest.setRucEmisor(userRuc);

        paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherRequest);

        paymentVoucherValidator.validate(paymentVoucherRequest, false);

        Map<String, Object> result = paymentVoucherService.createPaymentVoucher(paymentVoucherRequest, Long.valueOf(userId));

        return new ResponseEntity<>(result.get(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE), HttpStatus.CREATED);
    }

    @PostMapping("/editar-comprobante")
    public ResponseEntity<?> editPaymentVoucher(
            @RequestBody @Valid PaymentVoucherRequest paymentVoucherRequest,
            @RequestHeader(name = "X-User-Ruc", required = true) String userRuc,
            @RequestHeader(name = "X-User-Id", required = true) String userId,
            @RequestHeader(name = "X-User-Roles", required = true) String rol
    ) {
        paymentVoucherRequest.setRucEmisor(userRuc);

        paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherRequest);

        paymentVoucherValidator.validate(paymentVoucherRequest, true);

        Map<String, Object> result = paymentVoucherService.updatePaymentVoucher(paymentVoucherRequest,  Long.valueOf(userId));

        return new ResponseEntity<>(result.get(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE), HttpStatus.CREATED);
    }

    @GetMapping("/comprobantes-anticipo")
    public ResponseEntity<List<PaymentVoucherDto>> comprobantesAnticipo(
            @RequestParam(name = "filtroNumDoc", required = true) String filtroNumDoc,
            @RequestHeader(name = "X-User-Ruc", required = true) String ruc,
            @RequestHeader(name = "X-User-Id", required = true) Long usuarioId,
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles) {
        return new ResponseEntity<List<PaymentVoucherDto>>(paymentVoucherService.findComprobanteByAnticipo(filtroNumDoc, ruc), HttpStatus.OK);
    }

    @GetMapping("/comprobantes-credito")
    public ResponseEntity<List<PaymentVoucherDto>> comprobantesCredito(
            @RequestParam(name = "filtroNumDoc", required = true) String filtroNumDoc,
            @RequestHeader(name = "X-User-Ruc", required = true) String ruc,
            @RequestHeader(name = "X-User-Id", required = true) Long usuarioId,
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles) {
        return new ResponseEntity<List<PaymentVoucherDto>>(paymentVoucherService.findComprobanteByCredito(filtroNumDoc, ruc), HttpStatus.OK);
    }

    @GetMapping("/comprobanteById")
    public ResponseEntity<?> comprobanteById(
            @RequestParam(name = "id", required = true) Long id,
            @RequestHeader(name = "X-User-Ruc", required = true) String ruc,
            @RequestHeader(name = "X-User-Id", required = true) Long usuarioId,
            @RequestHeader(name = "X-User-Roles", required = true) List<String> roles) {

        return new ResponseEntity<Object>(paymentVoucherService.getComprobanteById(id), HttpStatus.OK);
    }

}
