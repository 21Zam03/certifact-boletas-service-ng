package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SendBoletaDto {

    private String ruc;
    private String fechaEmision;
    private IdentificadorComprobante nameDocument;
    private String user;
    private Boolean envioDirecto;

}
