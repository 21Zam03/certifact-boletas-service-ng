package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.others.SignatureResp;
import com.certicom.certifact_boletas_service_ng.dto.others.Voided;
import com.certicom.certifact_boletas_service_ng.exception.SignedException;
import com.certicom.certifact_boletas_service_ng.exception.TemplateException;
import com.certicom.certifact_boletas_service_ng.service.TemplateService;
import com.certicom.certifact_boletas_service_ng.signed.Signed;
import com.certicom.certifact_boletas_service_ng.templates.template.BoletaTemplate;
import com.certicom.certifact_boletas_service_ng.templates.template.NotaCreditoTemplate;
import com.certicom.certifact_boletas_service_ng.templates.template.NotaDebitoTemplate;
import com.certicom.certifact_boletas_service_ng.templates.template21.BoletaTemplate21;
import com.certicom.certifact_boletas_service_ng.templates.template21.NotaCreditoTemplate21;
import com.certicom.certifact_boletas_service_ng.templates.template21.NotaDebitoTemplate21;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.certicom.certifact_boletas_service_ng.util.UtilArchivo;
import com.certicom.certifact_boletas_service_ng.util.UtilConversion;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class TemplateServiceImpl implements TemplateService {

    private final Signed signed;
    private final BoletaTemplate boletaTemplate;
    private final BoletaTemplate21 boletaTemplate21;
    private final NotaCreditoTemplate notaCreditoTemplate;
    private final NotaCreditoTemplate21 notaCreditoTemplate21;
    private final NotaDebitoTemplate notaDebitoTemplate;
    private final NotaDebitoTemplate21 notaDebitoTemplate21;

    @Override
    public Map<String, String> buildPaymentVoucherSignOse(PaymentVoucherDto paymentVoucherModel) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> buildPaymentVoucherSignOseBliz(PaymentVoucherDto paymentVoucherModel) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> buildPaymentVoucherSignCerti(PaymentVoucherDto paymentVoucherModel) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException {
        String xmlGenerado = null;
        String idFirma;
        String nombreDocumento;
        Map<String, String> resp;
        SignatureResp signatureResp;

        switch (paymentVoucherModel.getTipoComprobante()) {
            case ConstantesSunat.TIPO_DOCUMENTO_BOLETA:
                if(paymentVoucherModel.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_0)) {
                    xmlGenerado = boletaTemplate.buildBoleta(paymentVoucherModel);
                }else if(paymentVoucherModel.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_1)) {
                    xmlGenerado = boletaTemplate21.buildBoleta21(paymentVoucherModel);
                }
                break;
            case ConstantesSunat.TIPO_DOCUMENTO_NOTA_CREDITO:
                if(paymentVoucherModel.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_0)) {
                    xmlGenerado = notaCreditoTemplate.buildCreditNote(paymentVoucherModel);

                }else if(paymentVoucherModel.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_1)) {
                    xmlGenerado = notaCreditoTemplate21.buildCreditNote(paymentVoucherModel);
                }
                break;
            default:
                if(paymentVoucherModel.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_0)) {
                    xmlGenerado = notaDebitoTemplate.buildDebitNote(paymentVoucherModel);
                }else if(paymentVoucherModel.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_1)) {
                    xmlGenerado = notaDebitoTemplate21.buildDebitNote(paymentVoucherModel);
                }
                break;
        }

        idFirma = "S" + paymentVoucherModel.getTipoComprobante() + paymentVoucherModel.getSerie() + "-" + paymentVoucherModel.getNumero();
        signatureResp = signed.signCerticom(xmlGenerado, idFirma);
        nombreDocumento = paymentVoucherModel.getRucEmisor() + "-" + paymentVoucherModel.getTipoComprobante() + "-" +
                paymentVoucherModel.getSerie() + "-" + paymentVoucherModel.getNumero();

        resp = buildDataTemplate(signatureResp, nombreDocumento);
        resp.put(ConstantesParameter.CODIGO_HASH, UtilArchivo.generarCodigoHash(signatureResp.toString()));

        return resp;
    }

    @Override
    public Map<String, String> buildPaymentVoucherSign(PaymentVoucherDto paymentVoucherModel) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> buildVoidedDocumentsSign(Voided voided) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> buildVoidedDocumentsSignCerti(Voided voided) throws TemplateException, SignedException, IOException, NoSuchAlgorithmException {
        return Collections.emptyMap();
    }

    private Map<String, String> buildDataTemplate(SignatureResp signatureResp, String nombreDocumento) throws SignedException, IOException, NoSuchAlgorithmException {

        Map<String, String> resp;
        File zipeado;

        zipeado = UtilArchivo.comprimir(signatureResp.getSignatureFile(),
                ConstantesParameter.TYPE_FILE_XML, nombreDocumento);
        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        String shaChecksum = getFileChecksum(shaDigest, zipeado);
        resp = new HashMap<>();
        resp.put(ConstantesParameter.PARAM_NAME_DOCUMENT, nombreDocumento);
        try {

            byte encoded[] = Base64.getEncoder().encode(signatureResp.getSignatureFile().toByteArray());
            String xmlBase64 = new String(encoded);

            resp.put(ConstantesParameter.PARAM_FILE_ZIP_BASE64, UtilConversion.encodeFileToBase64(zipeado));
            resp.put(ConstantesParameter.PARAM_FILE_XML_BASE64, xmlBase64);
            resp.put(ConstantesParameter.PARAM_STRING_HASH, shaChecksum);
        } catch (IOException e) {
            throw new SignedException(e.getMessage());
        }

        return resp;
    }

    private String getFileChecksum(MessageDigest digest, File file) throws IOException {
        //Get file input stream for reading the file content


        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        try(FileInputStream fis = new FileInputStream(file)) {
            //Read file data and update in message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }
        //Get the hash's bytes
        byte[] bytes = digest.digest();

        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        //return complete hash
        return sb.toString();
    }

}
