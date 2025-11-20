package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.ProductDto;
import com.certicom.certifact_boletas_service_ng.dto.UserDto;
import com.certicom.certifact_boletas_service_ng.feign.rest.HistorialStockRestService;
import com.certicom.certifact_boletas_service_ng.service.HistorialStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HistorialStockServiceImpl implements HistorialStockService {

    private final HistorialStockRestService historialStockRestService;

    @Autowired
    public HistorialStockServiceImpl(HistorialStockRestService historialStockRestService) {
        this.historialStockRestService = historialStockRestService;
    }

    @Override
    public void eliminarHistorialStockByDetail(DetailsPaymentVoucherDto detailsPaymentVoucherDto) {
        historialStockRestService.deleteByDetailsGuia(detailsPaymentVoucherDto.getIdComprobanteDetalle());
    }

    @Override
    public void registrarHistorialStock(ProductDto producto, UserDto userLogged, Long stockAntes, Long stockDespues, Object o, DetailsPaymentVoucherDto item, PaymentVoucherDto paymentVoucherDto, String evento) {

    }

}
