package com.certicom.certifact_boletas_service_ng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AditionalFieldPaymentVoucherDto {

    private Integer id;
    private String nombreCampo;
    private String valorCampo;
    private Integer typeFieldId;
    private Long idPaymentVoucher;


}
