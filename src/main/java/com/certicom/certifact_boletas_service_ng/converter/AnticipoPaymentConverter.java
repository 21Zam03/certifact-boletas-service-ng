package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.AnticipoPaymentDto;
import com.certicom.certifact_boletas_service_ng.request.AnticipoPaymentRequest;

import java.util.ArrayList;
import java.util.List;

public class AnticipoPaymentConverter {

    public static List<AnticipoPaymentDto> requestListToDtoList(List<AnticipoPaymentRequest> anticipoPaymentRequest) {
        List<AnticipoPaymentDto> anticipoPaymentDtoList = new ArrayList<>();
        if(anticipoPaymentRequest != null && !anticipoPaymentRequest.isEmpty()) {
            for (AnticipoPaymentRequest paymentRequest : anticipoPaymentRequest) {
                anticipoPaymentDtoList.add(AnticipoPaymentConverter.requestToDto(paymentRequest));
            }
        } else {
            return null;
        }
        return anticipoPaymentDtoList;
    }
    public static AnticipoPaymentDto requestToDto(AnticipoPaymentRequest anticipoPaymentRequest) {
        return AnticipoPaymentDto.builder().build();
    }

}
