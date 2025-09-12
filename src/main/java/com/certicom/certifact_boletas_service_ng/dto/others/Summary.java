package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Summary {

    private String fechaEmision;
    private Integer nroResumenDelDia;
    private List<SummaryDetail> items;

    private String rucEmisor;
    private String denominacionEmisor;
    private String nombreComercialEmisor;
    private String tipoDocumentoEmisor;

    private String estadoComprobante;
//    private Party emisor;

    public String getId() {//nombre del archivo sin la extencion y sin el ruc
        return new StringBuilder(ConstantesSunat.RESUMEN_DIARIO_BOLETAS).
                append("-").
                append(fechaEmision.replace("-", "")).
                append("-").append(nroResumenDelDia).toString();
    }

    public String getSignId() {//id de la firma
        return new StringBuilder("SRC-").
                append(fechaEmision.replace("-", "")).
                append("-").append(nroResumenDelDia).toString();
    }

    public String getUriExternalReference() {
        return new StringBuilder(rucEmisor).
                append("-RC-").
                append(fechaEmision.replace("-", "")).
                append("-").append(nroResumenDelDia).toString();
    }

}
