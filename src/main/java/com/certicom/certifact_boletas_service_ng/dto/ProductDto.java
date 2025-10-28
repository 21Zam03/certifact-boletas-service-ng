package com.certicom.certifact_boletas_service_ng.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProductDto {

    private Long id;
    private String codigo;
    private String descripcion;
    private String moneda;
    private String unidadMedida;
    private String tipoAfectacion;
    private BigDecimal valorVentaSinIgv;
    private BigDecimal valorVentaConIgv;
    private String codigoSunat;
    private String unidadManejo;
    private String instruccionesEspeciales;
    private String marca;
    private Long stock;
    private Boolean notificarStockBajo;
    private LocalDateTime fechaModificacionStock;
    private String serie;
    private String lote;
    private LocalDate fechaVencimiento;
    private String codigoBarras;
    private String codigoFamilia;
    private String codigoQr;
    private Long imagenId;
    private Long codCompany;
    private boolean estado;
    private Date createdOn;
    private String createdBy;
    private String updatedBy;
    private Date updatedOn;

}
