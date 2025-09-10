package com.certicom.certifact_boletas_service_ng.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AditionalFieldPaymentVoucherRequest implements Serializable {

    private Integer id;
    private String nombreCampo;
    private String valorCampo;
    private Integer typeFieldId;
    private Long idPaymentVoucher;

}
