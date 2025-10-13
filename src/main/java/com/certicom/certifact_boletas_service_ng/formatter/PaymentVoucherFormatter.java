package com.certicom.certifact_boletas_service_ng.formatter;

import com.certicom.certifact_boletas_service_ng.dto.AnticipoPaymentDto;
import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.AnticipoPaymentRequest;
import com.certicom.certifact_boletas_service_ng.request.DetailsPaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentVoucherFormatter {

    private final PaymentVoucherDetailFormatter paymentVoucherDetailFormatter;

    public void formatPaymentVoucher(PaymentVoucherRequest paymentVoucherDto) {
        calculateTotalValorVenta(paymentVoucherDto);
        formatTotalValorVenta(paymentVoucherDto);
        formatItems(paymentVoucherDto);
        formatAnticipos(paymentVoucherDto.getAnticipos());
        formatData(paymentVoucherDto);
    }

    private void calculateTotalValorVenta(PaymentVoucherRequest paymentVoucherDto) {
        if (isNullOrZero(paymentVoucherDto.getTotalValorVentaGravada())
                && isNullOrZero(paymentVoucherDto.getTotalValorVentaExportacion())
                && isNullOrZero(paymentVoucherDto.getTotalValorVentaExonerada())
                && isNullOrZero(paymentVoucherDto.getTotalImpOperGratuita())
                && isNullOrZero(paymentVoucherDto.getTotalValorVentaInafecta())) {
            for (DetailsPaymentVoucherRequest line: paymentVoucherDto.getItems() ) {
                switch (line.getCodigoTipoAfectacionIGV()){
                    case "20":
                        if(paymentVoucherDto.getTotalValorVentaExonerada()==null) {
                            paymentVoucherDto.setTotalValorVentaExonerada(BigDecimal.ZERO);
                        }
                        paymentVoucherDto.setTotalValorVentaExonerada(paymentVoucherDto.getTotalValorVentaExonerada().add(line.getValorVenta()));
                        break;
                }
            }
        }
    }

    private void formatTotalValorVenta(PaymentVoucherRequest paymentVoucherModel) {
        if (isNotNullAndZero(paymentVoucherModel.getTotalValorVentaGravada())) {
            paymentVoucherModel.setTotalValorVentaGravada(null);
        }
        if (isNotNullAndZero(paymentVoucherModel.getTotalValorVentaGratuita())) {
            paymentVoucherModel.setTotalValorVentaGratuita(null);
        }
        if (isNotNullAndZero(paymentVoucherModel.getTotalValorVentaExonerada())) {
            paymentVoucherModel.setTotalValorVentaExonerada(null);
        }
        if (isNotNullAndZero(paymentVoucherModel.getTotalValorVentaExportacion() )) {
            paymentVoucherModel.setTotalValorVentaExportacion(null);
        }
        if (isNotNullAndZero(paymentVoucherModel.getTotalValorVentaInafecta())) {
            paymentVoucherModel.setTotalValorVentaInafecta(null);
        }
        if (isNotNullAndZero(paymentVoucherModel.getTotalIgv())) {
            paymentVoucherModel.setTotalIgv(null);
        }
        if (paymentVoucherModel.getMontoDetraccion() != null) {
            paymentVoucherModel.setMontoDetraccion(paymentVoucherModel.getMontoDetraccion().setScale(2, RoundingMode.CEILING));
        }
        if (paymentVoucherModel.getTipoTransaccion() == null) {
            paymentVoucherModel.setTipoTransaccion(BigDecimal.ONE);
        }
    }

    private void formatItems(PaymentVoucherRequest paymentVoucherModel) {
        for (DetailsPaymentVoucherRequest line: paymentVoucherModel.getItems() ) {
            paymentVoucherDetailFormatter.format(line);
        }
    }

    private void formatAnticipos(List<AnticipoPaymentRequest> anticipos) {
        int correlativo = 1;
        if (anticipos != null && !anticipos.isEmpty()) {
            for (int i=0; i<anticipos.size(); i++) {
                if (correlativo < 10) {
                    anticipos.get(i).setIdentificadorPago("0" + correlativo);
                } else {
                    anticipos.get(i).setIdentificadorPago(Integer.toString(correlativo));
                }
                correlativo++;
            }
        }
    }

    private void formatData(PaymentVoucherRequest paymentVoucherModel) {
        paymentVoucherModel.setRucEmisor(StringUtils.trimToNull(paymentVoucherModel.getRucEmisor()));
        paymentVoucherModel.setSerie(paymentVoucherModel.getSerie().toUpperCase());
        paymentVoucherModel.setHoraEmision(StringUtils.trimToNull(paymentVoucherModel.getHoraEmision()));
        paymentVoucherModel.setCodigoMoneda(StringUtils.trimToNull(paymentVoucherModel.getCodigoMoneda()));
        paymentVoucherModel.setCodigoLocalAnexoEmisor(StringUtils.trimToNull(paymentVoucherModel.getCodigoLocalAnexoEmisor()));
        paymentVoucherModel.setDenominacionReceptor(StringUtils.trimToNull(paymentVoucherModel.getDenominacionReceptor()));

        paymentVoucherModel.setCodigoTipoOtroDocumentoRelacionado(StringUtils.trimToNull(
                paymentVoucherModel.getCodigoTipoOtroDocumentoRelacionado()));
        paymentVoucherModel.setSerieNumeroOtroDocumentoRelacionado(StringUtils.trimToNull(
                paymentVoucherModel.getSerieNumeroOtroDocumentoRelacionado()));
        paymentVoucherModel.setCodigoTipoOperacion(StringUtils.trimToNull(paymentVoucherModel.getCodigoTipoOperacion()));
        paymentVoucherModel.setMotivoNota(StringUtils.trimToNull(paymentVoucherModel.getMotivoNota()));
        //paymentVoucher.setIdentificadorDocumento(identificadorDocumento);

        if (StringUtils.isBlank(paymentVoucherModel.getDenominacionReceptor())) {
            paymentVoucherModel.setDenominacionReceptor("-");
        }
        if (StringUtils.isBlank(paymentVoucherModel.getNumeroDocumentoReceptor())) {
            paymentVoucherModel.setNumeroDocumentoReceptor("-");
        }
        if (StringUtils.isBlank(paymentVoucherModel.getTipoDocumentoReceptor())) {
            paymentVoucherModel.setTipoDocumentoReceptor("-");
        }
        if(paymentVoucherModel.getUblVersion() == null) {
            paymentVoucherModel.setUblVersion("2.1");
        }
    }

    private boolean isNotNullAndZero(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isNullOrZero(BigDecimal value) {
        return value == null || value.compareTo(BigDecimal.ZERO) == 0;
    }

}
