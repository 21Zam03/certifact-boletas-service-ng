package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;

import java.util.List;
import java.util.Map;

public interface PaymentVoucherService {

    Map<String, Object> createPaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario);

    Map<String, Object> updatePaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario);

    List<PaymentVoucherDto> findComprobanteByAnticipo(String filtroNumDoc, String ruc);

    List<PaymentVoucherDto> findComprobanteByCredito(String filtroNumDoc, String ruc);

    PaymentVoucherDto getComprobanteById(Long id);

    //public Map<String, Object> getSummaryDocumentsByFechaEmision(String fechaEmision, String rucEmisor, IdentificadorComprobante comprobante);

}
