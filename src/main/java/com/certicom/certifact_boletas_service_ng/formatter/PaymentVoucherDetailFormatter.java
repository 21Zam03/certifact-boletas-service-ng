package com.certicom.certifact_boletas_service_ng.formatter;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.DetailsPaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentVoucherDetailFormatter {

    public void format(DetailsPaymentVoucherRequest item) {
        calculateValorReferencial(item);

        item.setCodigoUnidadMedida(StringUtils.trimToNull(item.getCodigoUnidadMedida()));
        item.setCodigoProducto(StringUtils.trimToNull(item.getCodigoProducto()));
        item.setCodigoProductoSunat(StringUtils.trimToNull(item.getCodigoProductoSunat()));
        item.setDescripcion(StringUtils.trimToNull(item.getDescripcion()));
        item.setCodigoTipoAfectacionIGV(StringUtils.trimToNull(item.getCodigoTipoAfectacionIGV()));
        item.setCodigoTipoCalculoISC(StringUtils.trimToNull(item.getCodigoTipoCalculoISC()));
    }

    private void calculateValorReferencial(DetailsPaymentVoucherRequest item) {
        if ((item.getCodigoTipoAfectacionIGV().equals(ConstantesSunat.TIPO_AFCETACION_IGV_EXONERADO))&&item.getValorReferencialUnitario()==null){
            item.setValorReferencialUnitario(item.getValorUnitario());
            item.setMontoBaseExportacion(null);
            item.setImpuestoVentaGratuita(BigDecimal.ZERO);
            if (item.getCodigoTipoAfectacionIGV().equals(ConstantesSunat.TIPO_AFCETACION_IGV_EXONERADO)){
                item.setMontoBaseExonerado(item.getValorVenta());
            }
        }
    }

}
