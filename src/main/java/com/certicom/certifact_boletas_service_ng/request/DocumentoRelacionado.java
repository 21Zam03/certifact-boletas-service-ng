package com.certicom.certifact_boletas_service_ng.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentoRelacionado {

    private String numero;
    private String tipoDocumento;

}
