package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GetStatusCdrDto {

    private String ruc;
    private String tipoComprobante;
    private String serie;
    private Integer numero;
    private Long idPaymentVoucher;

}
