package com.certicom.certifact_boletas_service_ng.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8090", contextId = "paymentVoucher")
public interface PaymentVoucherFeign {

    @GetMapping("/api/payment-voucher/number")
    public Integer obtenerSiguienteNumeracionPorTipoComprobanteYSerieYRucEmisor(
            @RequestParam String tipoComprobante, @RequestParam String serie, @RequestParam String ruc
    );

}
