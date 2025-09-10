package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.AditionalFieldPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.AditionalFieldPaymentVoucherRequest;

import java.util.ArrayList;
import java.util.List;

public class AditionalFieldPaymentVoucherConverter {

    public static List<AditionalFieldPaymentVoucherDto> requestListToDtoList(List<AditionalFieldPaymentVoucherRequest> aditionalFieldPaymentVoucherRequests) {
        List<AditionalFieldPaymentVoucherDto> aditionalFieldPaymentVoucherDtos = new ArrayList<>();
        for (AditionalFieldPaymentVoucherRequest aditionalFieldPaymentVoucherRequest : aditionalFieldPaymentVoucherRequests) {
            aditionalFieldPaymentVoucherDtos.add(requestToDto(aditionalFieldPaymentVoucherRequest));
        }
        return aditionalFieldPaymentVoucherDtos;
    }

    public static AditionalFieldPaymentVoucherDto requestToDto(AditionalFieldPaymentVoucherRequest aditionalFieldPaymentVoucherRequest) {
        return AditionalFieldPaymentVoucherDto.builder().build();
    }

}
