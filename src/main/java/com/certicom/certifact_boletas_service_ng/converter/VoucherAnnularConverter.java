package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.VoucherAnnularDto;
import com.certicom.certifact_boletas_service_ng.request.VoucherAnnularRequest;

public class VoucherAnnularConverter {

    public static VoucherAnnularDto requestToDto(VoucherAnnularRequest voucherAnnularRequest) {
        if(voucherAnnularRequest != null) {
            return VoucherAnnularDto.builder()
                    .tipoComprobante(voucherAnnularRequest.getTipoComprobante())
                    .serie(voucherAnnularRequest.getSerie())
                    .numero(voucherAnnularRequest.getNumero())
                    .rucEmisor(voucherAnnularRequest.getRucEmisor())
                    .fechaEmision(voucherAnnularRequest.getFechaEmision())
                    .tipoComprobanteRelacionado(voucherAnnularRequest.getTipoComprobanteRelacionado())
                    .motivoAnulacion(voucherAnnularRequest.getMotivoAnulacion())
                    .build();
        } return null;
    }

}
