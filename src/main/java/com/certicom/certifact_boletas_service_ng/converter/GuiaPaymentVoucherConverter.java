package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.GuiaPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.GuiaPaymentVoucherRequest;

import java.util.ArrayList;
import java.util.List;

public class GuiaPaymentVoucherConverter {

    public static GuiaPaymentVoucherDto requestToDto(GuiaPaymentVoucherRequest guiaPaymentVoucherRequest) {
        return GuiaPaymentVoucherDto.builder().build();
    }

    public static List<GuiaPaymentVoucherDto> requestListToDtoList(List<GuiaPaymentVoucherRequest> guiaPaymentVoucherRequests) {
        List<GuiaPaymentVoucherDto> guiaPaymentVoucherDtos = new ArrayList<>();
        for (GuiaPaymentVoucherRequest guiaPaymentVoucherRequest : guiaPaymentVoucherRequests) {
            guiaPaymentVoucherDtos.add(requestToDto(guiaPaymentVoucherRequest));
        }
        return guiaPaymentVoucherDtos;
    }

}
