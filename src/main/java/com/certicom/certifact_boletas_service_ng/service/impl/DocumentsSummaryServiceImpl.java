package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponseSunat;
import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.service.PaymentVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentsSummaryServiceImpl implements DocumentsSummaryService {

    private final PaymentVoucherService paymentVoucherService;

    @Override
    public ResponsePSE generarSummaryByFechaEmisionAndRuc(String ruc, String fechaEmision, IdentificadorComprobante comprobante, String usuario) {
        ResponsePSE responsePSE = new ResponsePSE();
        Map<String, String> templateGenerated;
        Map<String, String> params;
        Map<String, Object> resultGetSummary;
        ResponseSunat responseSunat;
        String nameDocumentComplete;
        List<Long> ids;
        String fileXMLZipBase64;
        String nameDocument = null;
        responsePSE.setEstado(false);
        StringBuilder messageBuilder = new StringBuilder();
        Summary summary;

        try {

            resultGetSummary = paymentVoucherService.getSummaryDocumentsByFechaEmision(fechaEmision, rucEmisor, comprobante);
            Logger.register(TipoLogEnum.INFO, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.GENERATE_BEAN,
                    resultGetSummary.toString());
            //Verificación de RD vacio
            if (resultGetSummary.get(ConstantesParameter.PARAM_BEAN_SUMMARY) != null) {
                summary = (Summary) resultGetSummary.get(ConstantesParameter.PARAM_BEAN_SUMMARY);
                ids = (List<Long>) resultGetSummary.get(ConstantesParameter.PARAM_LIST_IDS);
            } else {
                responsePSE.setEstado(false);
                responsePSE.setMensaje(ConstantesParameter.MSG_SUMMARY_VACIO +
                        "RUC:[" + rucEmisor + "], Fecha emision:[" + fechaEmision + "]");
                responsePSE.setRespuesta(null);
                return responsePSE;
            }
            //Generación del template
            templateGenerated = getTemplateGenerated(rucEmisor,summary);

            Logger.register(TipoLogEnum.INFO, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.GENERATE_TEMPLATE,
                    ConstantesParameter.MSG_RESP_SUB_PROCESO_OK);
            //Envio del resumen a Sunat
            //fileXMLZipBase64, nameDocument
            fileXMLZipBase64 = templateGenerated.get(ConstantesParameter.PARAM_FILE_ZIP_BASE64);
            nameDocument = templateGenerated.get(ConstantesParameter.PARAM_NAME_DOCUMENT);
            nameDocumentComplete = nameDocument + "." + ConstantesParameter.TYPE_FILE_ZIP;
            System.out.println("NOMBRE DOCUMENTO SUMM "+nameDocument);
            responseSunat = sendSunat.sendSummary(nameDocumentComplete, fileXMLZipBase64, rucEmisor);

            Logger.register(TipoLogEnum.INFO, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.SEND_SUNAT,
                    responseSunat.toString());

            messageBuilder.append("[").append(rucEmisor).append("]");
            messageBuilder.append("[").append(fechaEmision).append("]");

            //Respuestas de Sunat
            if (responseSunat.getEstadoComunicacionSunat() == null) {
                throw new ServiceException("Error al comunicarse con Sunat");
            }

            switch (responseSunat.getEstadoComunicacionSunat()) {
                case SUCCESS_WITH_ERROR_CONTENT:
                    messageBuilder.append("[").append(responseSunat.getStatusCode()).append("]");
                    messageBuilder.append(responseSunat.getMessage());
                    break;
                case WITHOUT_CONNECTION:
                case ERROR_INTERNO_WS_API:
                    messageBuilder.append(responseSunat.getMessage());
                    break;
                default:
            }

            if (!responseSunat.isSuccess()) {
                throw new Exception(messageBuilder.toString());
            }

            params = new HashMap<String, String>();
            params.put(ConstantesParameter.PARAM_RUC_EMISOR, rucEmisor);
            params.put(ConstantesParameter.PARAM_NAME_DOCUMENT, nameDocument);
            params.put(ConstantesParameter.PARAM_TIPO_ARCHIVO, ConstantesSunat.RESUMEN_DIARIO_BOLETAS);
            params.put(ConstantesParameter.PARAM_STATUS_REGISTRO,
                    ConstantesParameter.REGISTRO_STATUS_NUEVO);

            //subida del Resumen Diario y registro en la bd
            CompanyInterDto companyEntity = companyRepository.getCompanyByRuc(rucEmisor);
            RegisterFileUploadEntity file = amazonS3ClientService.uploadFileStorage(UtilArchivo.b64ToByteArrayInputStream(fileXMLZipBase64),
                    nameDocument, "summary", companyEntity);

            Logger.register(TipoLogEnum.INFO, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.STORAGE_FILE,
                    params.toString());

            //actualzia el estado
            summary.setEstadoComprobante(EstadoComprobanteEnum.PROCESO_ENVIO.getCodigo());

            registrarSummaryDocuments(
                    summary,
                    file.getIdRegisterFileSend(),
                    usuario,
                    responseSunat.getTicket(),
                    ids);
            Logger.register(TipoLogEnum.INFO, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.INSERT_BD,
                    ConstantesParameter.MSG_RESP_SUB_PROCESO_OK);

            responsePSE.setEstado(true);
            responsePSE.setMensaje(messageBuilder.toString() + ConstantesParameter.MSG_RESP_OK);
            responsePSE.setRespuesta(responseSunat.getMessage());
            responsePSE.setTicket(responseSunat.getTicket());

            //Excepciones
        } catch (TemplateException | SignedException ex) {

            responsePSE.setMensaje(ex.getMessage());
            responsePSE.setRespuesta("RUC:[" + rucEmisor + "], Fecha emision:[" + fechaEmision + "]");

            Logger.register(TipoLogEnum.ERROR, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.GENERATE_TEMPLATE,
                    ex.getMessage(), responsePSE.getRespuesta().toString(), ex);

        } catch (Exception ex) {

            responsePSE.setMensaje(ex.getMessage());
            responsePSE.setRespuesta("RUC:[" + rucEmisor + "], Fecha emision:[" + fechaEmision + "]");

            Logger.register(TipoLogEnum.ERROR, rucEmisor, fechaEmision,
                    OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS, SubOperacionLogEnum.IN_PROCESS,
                    ex.getMessage(), responsePSE.getRespuesta().toString(), ex);

        }
        //Fin del registro del proceso
        Logger.register(TipoLogEnum.INFO, rucEmisor, fechaEmision, OperacionLogEnum.REGISTER_SUMMARY_DOCUMENTS,
                SubOperacionLogEnum.COMPLETED, responsePSE.toString());

        return responsePSE;
    }

}
