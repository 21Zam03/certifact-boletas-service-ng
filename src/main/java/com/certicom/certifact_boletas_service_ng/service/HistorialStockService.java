package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.ProductDto;
import com.certicom.certifact_boletas_service_ng.dto.UserDto;

public interface HistorialStockService {

    void eliminarHistorialStockByDetail(DetailsPaymentVoucherDto detailsPaymentVoucherDto);

    void registrarHistorialStock(ProductDto producto, UserDto userLogged, Long stockAntes, Long stockDespues, Object o, DetailsPaymentVoucherDto item, PaymentVoucherDto paymentVoucherDto, String evento);

}
