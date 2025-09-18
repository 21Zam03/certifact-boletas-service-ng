package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;

public interface DocumentsSummaryService {

    ResponsePSE generarSummaryByFechaEmisionAndRuc(String ruc, String fechaEmision, IdentificadorComprobante comprobante, String usuario);
    ResponsePSE processSummaryTicket(String ticket, String useName, String rucEmisor);

}
