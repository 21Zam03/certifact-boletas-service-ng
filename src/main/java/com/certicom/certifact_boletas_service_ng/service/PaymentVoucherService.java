package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;

import java.util.Map;

public interface PaymentVoucherService {

    Map<String, Object> createPaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario);
    Map<String, Object> updatePaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario);

    public Map<String, Object> getSummaryDocumentsByFechaEmision(String fechaEmision, String rucEmisor, IdentificadorComprobante comprobante);

}
