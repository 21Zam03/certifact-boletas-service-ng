package com.certicom.certifact_boletas_service_ng.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "detailPaymentVoucher")
public interface DetailPaymentVoucherFeign {

    @DeleteMapping("/api/detail-payment-voucher/{id}")
    public int deleteDetailPaymentVoucherById(@PathVariable("id") Long detailPaymentVoucherId);

}
