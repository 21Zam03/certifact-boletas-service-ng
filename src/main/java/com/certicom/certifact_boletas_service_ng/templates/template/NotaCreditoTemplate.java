package com.certicom.certifact_boletas_service_ng.templates.template;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.exception.TemplateException;
import org.springframework.stereotype.Component;

@Component
public class NotaCreditoTemplate {

    public String buildCreditNote(PaymentVoucherDto creditNote) throws TemplateException {
        return "";
    }

}
