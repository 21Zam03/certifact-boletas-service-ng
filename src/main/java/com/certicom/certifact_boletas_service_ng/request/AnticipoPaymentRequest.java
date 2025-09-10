package com.certicom.certifact_boletas_service_ng.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnticipoPaymentRequest {

    private Long idAnticipoPayment;
    private Long idPaymentVoucher;
    private String identificadorPago;
    private String serieAnticipo;
    private Integer numeroAnticipo;
    private String tipoDocAnticipo;
    private BigDecimal montoAnticipo;

}
