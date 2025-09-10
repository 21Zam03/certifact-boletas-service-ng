package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.PaymentCuotasDto;
import com.certicom.certifact_boletas_service_ng.request.PaymentCuotasRequest;

import java.util.ArrayList;
import java.util.List;

public class PaymentCuotasConverter {

    public static List<PaymentCuotasDto> requestListToDtoList(List<PaymentCuotasRequest> paymentCuotasRequests) {
        List<PaymentCuotasDto> paymentCuotasDtos = new ArrayList<>();
        for (PaymentCuotasRequest paymentCuotasRequest : paymentCuotasRequests) {
            paymentCuotasDtos.add(requestToDto(paymentCuotasRequest));
        }
        return paymentCuotasDtos;
    }

    public static PaymentCuotasDto requestToDto(PaymentCuotasRequest paymentCuotasRequest) {
        return PaymentCuotasDto.builder().build();
    }

}
