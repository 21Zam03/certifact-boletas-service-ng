package com.certicom.certifact_boletas_service_ng.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Leyenda {

    private String descripcion;
    private String codigo;

}