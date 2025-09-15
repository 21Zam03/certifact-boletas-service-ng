package com.certicom.certifact_boletas_service_ng.dto.others;

import com.certicom.certifact_boletas_service_ng.enums.ComunicacionSunatEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSunat {

    private String contentBase64;
    private String statusCode;
    private boolean success;
    private String message;
    private String nameDocument;
    private String ticket;
    private String rucEmisor;
    private ComunicacionSunatEnum estadoComunicacionSunat;
    private String indCdr;
    private String numError;
    protected int cod;

}
