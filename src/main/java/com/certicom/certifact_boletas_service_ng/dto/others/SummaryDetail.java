package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SummaryDetail {

    private Integer numeroItem;
    private String serie;
    private Integer numero;
    private String tipoComprobante;
    private String codigoMoneda;
    private String tipoDocumentoReceptor;
    private String numeroDocumentoReceptor;
    private String serieAfectado;
    private Integer numeroAfectado;
    private String tipoComprobanteAfectado;
    private Integer statusItem;
    private BigDecimal importeTotalVenta;
    private BigDecimal sumatoriaOtrosCargos;

    private BigDecimal totalIGV;
    private BigDecimal totalISC;
    private BigDecimal totalOtrosTributos;

    private BigDecimal totalValorVentaOperacionExonerado;
    private BigDecimal totalValorVentaOperacionExportacion;
    private BigDecimal totalValorVentaOperacionGratuita;
    private BigDecimal totalValorVentaOperacionGravada;
    private BigDecimal totalValorVentaOperacionInafecta;

    private String estado;
    private Long idPaymentVoucher;

}
