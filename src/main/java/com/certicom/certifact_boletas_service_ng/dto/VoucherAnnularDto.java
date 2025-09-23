package com.certicom.certifact_boletas_service_ng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VoucherAnnularDto {

    private String tipoComprobante;
    private String serie;
    private Integer numero;
    private String rucEmisor;
    private String fechaEmision;
    private String tipoComprobanteRelacionado;
    private String motivoAnulacion;

}
