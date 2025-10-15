package com.certicom.certifact_boletas_service_ng.validation;

import com.certicom.certifact_boletas_service_ng.deserializer.InputField;
import com.certicom.certifact_boletas_service_ng.exception.ValidationException;
import com.certicom.certifact_boletas_service_ng.feign.rest.CompanyRestService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.UtilFormat;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SummaryValidator extends InputField<Object> {

    //private final CompanyFeign companyRepository;

    private final CompanyRestService companyRestService;

    public void validateSummaryByFechaEmision(String rucEmisor, String fechaEmision)  {
        validateRucActivo(rucEmisor);
        validateFechaEmision(fechaEmision);
    }

    private void validateRucActivo(String rucEmisor) {
        System.out.println("RUC EMISOR: "+rucEmisor);
        String mensajeValidacion = null;
        String estado = companyRestService.getStateFromCompanyByRuc(rucEmisor);
        if (estado!=null){
            if (!estado.equals(ConstantesParameter.REGISTRO_ACTIVO)) {
                throw new ValidationException("El ruc emisor [" + rucEmisor + "] No se encuentra habilitado para "
                        + "ejecutar operaciones al API-REST.");
            }
        }else {
            throw new ValidationException("El ruc emisor [" + rucEmisor + "] No existe "
                    + "ejecutar operaciones al API-REST.");
        }
    }

    private void validateFechaEmision(String fechaEmision) {
        String mensajeValidacion = null;
        if (StringUtils.isBlank(fechaEmision)) {
            throw new ValidationException("El campo [" + fechaEmisionLabel + "] es obligatorio.");
        }
        if (UtilFormat.fechaDate(fechaEmision) == null) {
            throw new ValidationException("El campo [" + fechaEmisionLabel + "] debe tener el formato yyyy-MM-dd");
        }
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }

}
