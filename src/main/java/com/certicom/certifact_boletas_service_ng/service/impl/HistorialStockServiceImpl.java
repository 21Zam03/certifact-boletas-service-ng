package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.feign.rest.HistorialStockRestService;
import com.certicom.certifact_boletas_service_ng.service.HistorialStockService;
import org.springframework.stereotype.Service;

@Service
public class HistorialStockServiceImpl implements HistorialStockService {

    private HistorialStockRestService historialStockRestService;

    @Override
    public void eliminarHistorialStockByDetail(DetailsPaymentVoucherDto detailsPaymentVoucherDto) {
        historialStockRestService.deleteByDetailsGuia(detailsPaymentVoucherDto.getIdComprobanteDetalle());
    }

}
