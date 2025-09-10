package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;

public class PaymentVoucherConverter {

    public static PaymentVoucherDto requestToDto(PaymentVoucherRequest paymentVoucherRequest) {
        return PaymentVoucherDto.builder()
                .ublVersion("2.1")
                .rucEmisor(paymentVoucherRequest.getRucEmisor())
                .tipoComprobante(paymentVoucherRequest.getTipoComprobante())
                .serie(paymentVoucherRequest.getSerie())
                .numero(paymentVoucherRequest.getNumero())
                .fechaEmision(paymentVoucherRequest.getFechaEmision())
                .horaEmision(paymentVoucherRequest.getHoraEmision())
                .fechaEmision(paymentVoucherRequest.getFechaEmision())
                .codigoMoneda(paymentVoucherRequest.getCodigoMoneda())
                .fechaVencimiento(paymentVoucherRequest.getFechaVencimiento())
                .codigoTipoOperacion(paymentVoucherRequest.getCodigoTipoOperacion())
                .tipoDocumentoEmisor(paymentVoucherRequest.getTipoDocumentoEmisor())
                .tipoDocumentoReceptor(paymentVoucherRequest.getTipoDocumentoReceptor())
                .numeroDocumentoReceptor(paymentVoucherRequest.getNumeroDocumentoReceptor())
                .denominacionReceptor(paymentVoucherRequest.getDenominacionReceptor())
                .direccionReceptor(paymentVoucherRequest.getDireccionReceptor())
                .emailReceptor(paymentVoucherRequest.getEmailReceptor())
                .totalValorVentaGravada(paymentVoucherRequest.getTotalValorVentaGravada())
                .totalIgv(paymentVoucherRequest.getTotalIgv())
                .importeTotalVenta(paymentVoucherRequest.getImporteTotalVenta())
                .anticipos(AnticipoPaymentConverter.requestListToDtoList(paymentVoucherRequest.getAnticipos()))
                .camposAdicionales(AditionalFieldPaymentVoucherConverter.requestListToDtoList(paymentVoucherRequest.getCamposAdicionales()))
                .cuotas(PaymentCuotasConverter.requestListToDtoList(paymentVoucherRequest.getCuotas()))
                .items(DetailsPaymentVoucherConverter.requestListToDtoList(paymentVoucherRequest.getItems()))
                .guiasRelacionadas(GuiaPaymentVoucherConverter.requestListToDtoList(paymentVoucherRequest.getGuiasRelacionadas()))
                .build();
    }

}
