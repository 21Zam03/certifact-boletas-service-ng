package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import org.springframework.stereotype.Service;

@Service
public class DocumentsSummaryServiceImpl implements DocumentsSummaryService {

    @Override
    public ResponsePSE generarSummaryByFechaEmisionAndRuc(String ruc, String fechaEmision, IdentificadorComprobante comprobante, String usuario) {
        return null;
    }

}
