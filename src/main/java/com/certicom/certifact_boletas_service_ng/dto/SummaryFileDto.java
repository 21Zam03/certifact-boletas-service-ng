package com.certicom.certifact_boletas_service_ng.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SummaryFileDto {

    private Long id;
    private String tipoArchivo;
    private String estadoArchivo;
    private Long idRegisterFileSend;
    private Long idDocumentSummary;
    private Integer orden;

}
