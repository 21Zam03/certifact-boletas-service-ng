package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.DetailsPaymentVoucherRequest;

import java.util.ArrayList;
import java.util.List;

public class DetailsPaymentVoucherConverter {

    public static DetailsPaymentVoucherDto requestToDto(DetailsPaymentVoucherRequest detailsPaymentVoucherRequest) {
        return DetailsPaymentVoucherDto.builder().build();
    }

    public static List<DetailsPaymentVoucherDto> requestListToDtoList(List<DetailsPaymentVoucherRequest> detailsPaymentVoucherRequests) {
        List<DetailsPaymentVoucherDto> detailsPaymentVoucherDtos = new ArrayList<>();
        for (DetailsPaymentVoucherRequest detailsPaymentVoucherRequest : detailsPaymentVoucherRequests) {
            detailsPaymentVoucherDtos.add(requestToDto(detailsPaymentVoucherRequest));
        }
        return detailsPaymentVoucherDtos;
    }

}
