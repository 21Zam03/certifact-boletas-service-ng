package com.certicom.certifact_boletas_service_ng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GuiaPaymentVoucherDto {

    private Long idguiaremision;
    private String codigoTipoGuia;
    private String serieNumeroGuia;
    private Long idPaymentVoucher;

}
