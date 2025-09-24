package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.VoucherAnnularDto;
import com.certicom.certifact_boletas_service_ng.request.VoucherAnnularRequest;

import java.util.ArrayList;
import java.util.List;

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

    public static List<VoucherAnnularDto> requestListToDtoList(List<VoucherAnnularRequest> voucherAnnularRequests) {
        if(voucherAnnularRequests != null && !voucherAnnularRequests.isEmpty()) {
            List<VoucherAnnularDto> voucherAnnularDtos = new ArrayList<>();
            for (VoucherAnnularRequest voucherAnnularRequest : voucherAnnularRequests) {
                voucherAnnularDtos.add(VoucherAnnularConverter.requestToDto(voucherAnnularRequest));
            }
            return voucherAnnularDtos;
        } else return null;
    }

}
