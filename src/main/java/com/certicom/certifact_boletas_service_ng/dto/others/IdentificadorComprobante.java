package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentificadorComprobante {

    private String tipo;
    private String serie;
    private String ruc;
    private Integer numero;

}
