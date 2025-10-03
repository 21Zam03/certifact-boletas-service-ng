package com.certicom.certifact_boletas_service_ng.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "additionalFieldPayment")
public interface AditionalFieldFeign {

    @GetMapping("/api/aditional-field-payment-voucher")
    public Integer findAditionalFieldIdByValorCampo(@RequestParam String nombreCampo);

    @DeleteMapping("/api/aditional-field-payment-voucher/{id}")
    public int deleteAditionalFieldPaymentById(@PathVariable("id") Long aditionalPaymentId);

}
