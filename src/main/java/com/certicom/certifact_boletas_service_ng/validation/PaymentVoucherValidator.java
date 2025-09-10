package com.certicom.certifact_boletas_service_ng.validation;

import com.certicom.certifact_boletas_service_ng.deserializer.InputField;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PaymentVoucherValidator extends InputField<Object> {

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }

    public void validate(PaymentVoucherRequest paymentVoucherRequest, boolean isEdit) {

    }
}
