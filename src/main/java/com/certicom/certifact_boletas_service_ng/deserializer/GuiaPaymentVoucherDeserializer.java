package com.certicom.certifact_boletas_service_ng.deserializer;

import com.certicom.certifact_boletas_service_ng.exception.DeserializerException;
import com.certicom.certifact_boletas_service_ng.request.GuiaPaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GuiaPaymentVoucherDeserializer extends InputField<GuiaPaymentVoucherRequest> {

    @Override
    public GuiaPaymentVoucherRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        GuiaPaymentVoucherRequest objectResult;
        JsonNode trama;
        JsonNode campoTrama;

        String codigoTipoGuia = null;
        String serieNumeroGuia = null;
        Long idguiaremision = null;

        String mensajeError;

        trama = jsonParser.getCodec().readTree(jsonParser);

        campoTrama = trama.get(codigoTipoGuiaLabel);
        if (campoTrama != null) {
            if (!campoTrama.isTextual()) {
                mensajeError = ConstantesParameter.MSG_RESP_ERROR_DESERIALIZACION_STRING + "24[" + codigoTipoGuiaLabel + "]";
                throw new DeserializerException(mensajeError);
            } else {
                codigoTipoGuia = campoTrama.textValue();
            }
        }
        campoTrama = trama.get(serieNumeroGuiaLabel);
        if (campoTrama != null && !campoTrama.isNull()) {
            if (!campoTrama.isTextual()) {
                mensajeError = ConstantesParameter.MSG_RESP_ERROR_DESERIALIZACION_STRING + "25[" + serieNumeroGuiaLabel + "]";
                throw new DeserializerException(mensajeError);
            } else {
                serieNumeroGuia = campoTrama.textValue();
            }
        }
        campoTrama = trama.get(idguiaremisionLabel);
        if (campoTrama != null && !campoTrama.isNull()) {
            if (campoTrama.isNumber()) {
                idguiaremision = campoTrama.longValue();
            } else {
                mensajeError = ConstantesParameter.MSG_RESP_ERROR_DESERIALIZACION_STRING + "26[" + idguiaremisionLabel + "]";
                throw new DeserializerException(mensajeError);
            }
        }

        objectResult = new GuiaPaymentVoucherRequest();
        objectResult.setCodigoTipoGuia(codigoTipoGuia);
        objectResult.setSerieNumeroGuia(serieNumeroGuia);
        objectResult.setIdguiaremision(idguiaremision);


        return objectResult;
    }

}
