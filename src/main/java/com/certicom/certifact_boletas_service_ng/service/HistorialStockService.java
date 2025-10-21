package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;

public interface HistorialStockService {

    void eliminarHistorialStockByDetail(DetailsPaymentVoucherDto detailsPaymentVoucherDto);

}
