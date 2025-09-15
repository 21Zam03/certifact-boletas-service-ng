package com.certicom.certifact_boletas_service_ng.dto.others;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherFileDto;
import com.certicom.certifact_boletas_service_ng.dto.SummaryFileDto;
import com.certicom.certifact_boletas_service_ng.enums.EstadoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.ArrayList;
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

    private String idDocument;
    private String estado;
    private String fechaGeneracion;
    private Timestamp fechaGereracionResumen;
    private String ticketSunat;
    private String userName;
    private List<SummaryFileDto> summaryFileDtoList;

//    private Party emisor;

    @JsonIgnore
    public List<SummaryFileDto> getOrCreateSummaryFileDtoList() {
        if (this.summaryFileDtoList == null) {
            this.summaryFileDtoList = new ArrayList<SummaryFileDto>();
        }
        return this.summaryFileDtoList;
    }

    @JsonIgnore
    public void addFile(SummaryFileDto summaryFileDto) {
        summaryFileDto.setOrden(getOrCreateSummaryFileDtoList().size()+1);
        if(summaryFileDto.getTipoArchivo().equals(TipoArchivoEnum.XML.name())) {
            getOrCreateSummaryFileDtoList().forEach(f -> {
                if(f.getTipoArchivo().equals(TipoArchivoEnum.XML.name())) {
                    f.setEstadoArchivo(EstadoArchivoEnum.INACTIVO.name());
                }
            });
        }
        System.out.println("LISTA: "+getOrCreateSummaryFileDtoList().size());
        getOrCreateSummaryFileDtoList().add(summaryFileDto);
    }

    @JsonIgnore
    public String getId() {//nombre del archivo sin la extencion y sin el ruc
        return new StringBuilder(ConstantesSunat.RESUMEN_DIARIO_BOLETAS).
                append("-").
                append(fechaEmision.replace("-", "")).
                append("-").append(nroResumenDelDia).toString();
    }

    @JsonIgnore
    public String getSignId() {//id de la firma
        return new StringBuilder("SRC-").
                append(fechaEmision.replace("-", "")).
                append("-").append(nroResumenDelDia).toString();
    }

    @JsonIgnore
    public String getUriExternalReference() {
        return new StringBuilder(rucEmisor).
                append("-RC-").
                append(fechaEmision.replace("-", "")).
                append("-").append(nroResumenDelDia).toString();
    }

}
