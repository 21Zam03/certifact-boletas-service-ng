package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import com.certicom.certifact_boletas_service_ng.dto.others.Voided;
import com.certicom.certifact_boletas_service_ng.exception.SignedException;
import com.certicom.certifact_boletas_service_ng.exception.TemplateException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

public interface TemplateService {

    public Map<String, String> buildPaymentVoucherSignOse(PaymentVoucherDto paymentVoucherModel);
    public Map<String, String> buildPaymentVoucherSignOseBliz(PaymentVoucherDto paymentVoucherModel);
    public Map<String, String> buildPaymentVoucherSignCerti(PaymentVoucherDto paymentVoucherModel) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException;
    public Map<String, String> buildPaymentVoucherSign(PaymentVoucherDto paymentVoucherModel);

    public Map<String, String> buildVoidedDocumentsSign(Voided voided) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException;
    public Map<String, String> buildVoidedDocumentsSignCerti(Voided voided) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException;

    public Map<String, String> buildSummaryDailySign(Summary summary) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException;
    public Map<String, String> buildSummaryDailySignOse(Summary summary) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException;
    public Map<String, String> buildSummaryDailySignCerti(Summary summary) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException;

}
