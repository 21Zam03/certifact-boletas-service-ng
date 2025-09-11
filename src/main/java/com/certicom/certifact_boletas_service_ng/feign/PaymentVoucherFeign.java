package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "paymentVoucher")
public interface PaymentVoucherFeign {

    @GetMapping("/api/payment-voucher/number")
    public Integer obtenerSiguienteNumeracionPorTipoComprobanteYSerieYRucEmisor(
            @RequestParam String tipoComprobante, @RequestParam String serie, @RequestParam String ruc
    );

    @GetMapping("/api/payment-voucher/id-document")
    public PaymentVoucherDto getPaymentVoucherByIdentificadorDocumento(@RequestParam String identificadorDocumento);

    @PostMapping("/api/payment-voucher")
    public PaymentVoucherDto save(@RequestBody PaymentVoucherDto paymentVoucherDto);

}
