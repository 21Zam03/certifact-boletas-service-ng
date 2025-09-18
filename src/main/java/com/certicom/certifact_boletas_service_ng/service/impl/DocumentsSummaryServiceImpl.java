package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.*;
import com.certicom.certifact_boletas_service_ng.dto.others.*;
import com.certicom.certifact_boletas_service_ng.enums.EstadoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoComprobanteEnum;
import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.exception.SignedException;
import com.certicom.certifact_boletas_service_ng.exception.TemplateException;
import com.certicom.certifact_boletas_service_ng.feign.CompanyFeign;
import com.certicom.certifact_boletas_service_ng.feign.PaymentVoucherFeign;
import com.certicom.certifact_boletas_service_ng.feign.SummaryDocumentsFeign;
import com.certicom.certifact_boletas_service_ng.service.*;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.certicom.certifact_boletas_service_ng.util.UtilArchivo;
import com.certicom.certifact_boletas_service_ng.util.UtilFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentsSummaryServiceImpl implements DocumentsSummaryService {

    //private final PaymentVoucherService paymentVoucherService;
    private final TemplateService templateService;
    private final CompanyFeign companyFeign;
    private final SunatService sunatService;
    private final AmazonS3ClientService amazonS3ClientService;
    private final SummaryDocumentsFeign summaryDocumentsFeign;
    private final PaymentVoucherFeign paymentVoucherFeign;
    private final StatusService statusService;

    @Override
    public ResponsePSE generarSummaryByFechaEmisionAndRuc(String rucEmisor, String fechaEmision, IdentificadorComprobante comprobante, String usuario) {
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
            resultGetSummary = getSummaryDocumentsByFechaEmision(fechaEmision, rucEmisor, comprobante);

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
            templateGenerated = getTemplateGenerated(rucEmisor,summary);

            fileXMLZipBase64 = templateGenerated.get(ConstantesParameter.PARAM_FILE_ZIP_BASE64);
            nameDocument = templateGenerated.get(ConstantesParameter.PARAM_NAME_DOCUMENT);
            nameDocumentComplete = nameDocument + "." + ConstantesParameter.TYPE_FILE_ZIP;

            System.out.println("NOMBRE DOCUMENTO SUMM "+nameDocument);
            responseSunat = sunatService.sendSummary(nameDocumentComplete, fileXMLZipBase64, rucEmisor);

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
            CompanyDto companyEntity = companyFeign.findCompanyByRuc(rucEmisor);
            RegisterFileUploadDto file = amazonS3ClientService.uploadFileStorage(UtilArchivo.b64ToByteArrayInputStream(fileXMLZipBase64),
                    nameDocument, "summary", companyEntity);

            //actualzia el estado
            summary.setEstadoComprobante(EstadoComprobanteEnum.PROCESO_ENVIO.getCodigo());

            registrarSummaryDocuments(
                    summary,
                    file.getIdRegisterFileSend(),
                    usuario,
                    responseSunat.getTicket(),
                    ids);

            responsePSE.setEstado(true);
            responsePSE.setMensaje(messageBuilder.toString() + ConstantesParameter.MSG_RESP_OK);
            responsePSE.setRespuesta(responseSunat.getMessage());
            responsePSE.setTicket(responseSunat.getTicket());

            //Excepciones
        } catch (TemplateException | SignedException ex) {
            responsePSE.setMensaje(ex.getMessage());
            responsePSE.setRespuesta("RUC:[" + rucEmisor + "], Fecha emision:[" + fechaEmision + "]");
        } catch (Exception ex) {
            responsePSE.setMensaje(ex.getMessage());
            responsePSE.setRespuesta("RUC:[" + rucEmisor + "], Fecha emision:[" + fechaEmision + "]");
        }
        return responsePSE;
    }

    @Override
    public ResponsePSE processSummaryTicket(String ticket, String useName, String rucEmisor) {
        ResponsePSE responsePSE = null;
        List<RucEstadoOther> data;

        String estado = null;
        data = summaryDocumentsFeign.getEstadoAndRucEmisorByNumeroTicket(ticket);
        System.out.println("DATA: "+data);
        rucEmisor = data.get(0).getRucEmisor();
        estado = data.get(0).getEstado();

        if (estado.equals(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO)) {
            responsePSE = statusService.getStatus(ticket, ConstantesSunat.RESUMEN_DIARIO_BOLETAS, useName, rucEmisor);
            System.out.println("RESPONSE: "+ responsePSE);
        } else {
            responsePSE = new ResponsePSE();
            responsePSE.setRespuesta(estado);
            responsePSE.setEstado(false);
            responsePSE.setMensaje("El ticket[" + ticket + "] ya ha sido procesado y se encuentra en estado[" + estado + "]");
        }
        return responsePSE;
    }

    public Map<String, Object> getSummaryDocumentsByFechaEmision(String fechaEmision, String rucEmisor, IdentificadorComprobante comprobante) {
        Map<String, Object> result = new HashMap<>();
        List<Long> ids = new ArrayList<>();
        log.info("SUMMARY 1 {} {} {}", fechaEmision, rucEmisor, comprobante);

        Summary summary = buildSummaryDocumentsByFechaEmision(fechaEmision, rucEmisor, comprobante, ids);
        log.info("SUMMARY RESULT: {}", summary);
        result.put(ConstantesParameter.PARAM_BEAN_SUMMARY, summary);
        result.put(ConstantesParameter.PARAM_LIST_IDS, ids);
        return result;
    }

    private Summary buildSummaryDocumentsByFechaEmision(String fechaEmision, String rucEmisor, IdentificadorComprobante comprobante, List<Long> ids) {
        Summary summaryByDay = null;
        Integer correlativoSummary = null;
        Integer correlativoSummaryDto = null;
        CompanyDto company = companyFeign.findCompanyByRuc(rucEmisor);
        String tipoDocumentoEmisor = ConstantesSunat.TIPO_DOCUMENTO_IDENTIDAD_RUC;
        String denominacionEmisor = company.getRazon();
        System.out.println(company.getRazon());
        List<PaymentVoucherDto> comprobantes = new ArrayList<>();
        List<PaymentVoucherDto> comprobantesDto = new ArrayList<>();

        if(comprobante != null && comprobante.getTipo() != null && comprobante.getSerie() != null && comprobante.getNumero() != null) {
            comprobantesDto = paymentVoucherFeign.findListSpecificForSummary(
                    rucEmisor, fechaEmision, comprobante.getTipo(), comprobante.getSerie(), comprobante.getNumero());
            System.out.println("COMPROBANTE A ENVIAR POR RESUMEN: "+comprobantesDto);
        } else {
            List<PaymentVoucherDto> listPaymentVoucherDto = paymentVoucherFeign
                    .findAllForSummaryByRucEmisorAndFechaEmision(rucEmisor, fechaEmision);
            System.out.println(listPaymentVoucherDto.size());
            comprobantesDto = listPaymentVoucherDto.subList(0, Math.min(listPaymentVoucherDto.size(), 400));
        }

        if (comprobantesDto != null && !comprobantesDto.isEmpty()) {
            correlativoSummaryDto = summaryDocumentsFeign.getSequentialNumberInSummaryByFechaEmision(rucEmisor,
                    fechaEmision);
            correlativoSummaryDto++;
            int numeroLinea = 0;

            List<SummaryDetail> details = new ArrayList<SummaryDetail>();

            summaryByDay = Summary.builder()
                    .fechaEmision(fechaEmision)
                    .nroResumenDelDia(correlativoSummaryDto)
                    .rucEmisor(rucEmisor)
                    .denominacionEmisor(denominacionEmisor)
                    .tipoDocumentoEmisor(tipoDocumentoEmisor)
                    .build();

            List<PaymentVoucherDto> comprobantesTemp;
            comprobantesTemp = comprobantesDto.stream().filter(com -> com.getBoletaAnuladaSinEmitir() !=
                    null && com.getBoletaAnuladaSinEmitir()).toList();

            for (PaymentVoucherDto payment : comprobantesTemp) {

                SummaryDetail detail = new SummaryDetail();
                numeroLinea++;

                detail.setNumeroItem(numeroLinea);
                detail.setSerie(payment.getSerie());
                detail.setNumero(payment.getNumero());
                detail.setTipoComprobante(payment.getTipoComprobante());
                detail.setCodigoMoneda(payment.getCodigoMoneda());
                detail.setTipoDocumentoReceptor(payment.getTipoDocumentoReceptor());
                detail.setNumeroDocumentoReceptor(payment.getNumeroDocumentoReceptor());
                if (payment.getCodigoTipoNotaCredito() != null || payment.getCodigoTipoNotaDebito() != null) {
                    detail.setSerieAfectado(payment.getSerieAfectado());
                    detail.setNumeroAfectado(payment.getNumeroAfectado());
                    detail.setTipoComprobanteAfectado(payment.getTipoComprobanteAfectado());
                }
                detail.setStatusItem(ConstantesParameter.STATE_ITEM_PENDIENTE_ADICION);
                detail.setImporteTotalVenta(payment.getImporteTotalVenta());
                detail.setSumatoriaOtrosCargos(payment.getSumatoriaOtrosCargos());
                detail.setTotalIGV(payment.getTotalIgv());
                detail.setTotalISC(payment.getTotalIsc());
                detail.setTotalOtrosTributos(payment.getTotalOtrostributos());
                detail.setTotalValorVentaOperacionExportacion(payment.getTotalValorVentaExportacion());
                detail.setTotalValorVentaOperacionGravada(payment.getTotalValorVentaGravada());
                detail.setTotalValorVentaOperacionInafecta(payment.getTotalValorVentaInafecta());
                detail.setTotalValorVentaOperacionExonerado(payment.getTotalValorVentaExonerada());
                detail.setTotalValorVentaOperacionGratuita(payment.getTotalValorVentaGratuita());

                details.add(detail);
            }
            for (PaymentVoucherDto payment : comprobantesDto) {
                SummaryDetail detail = new SummaryDetail();
                numeroLinea++;
                payment.getEstadoItem();
                detail.setNumeroItem(numeroLinea);
                detail.setSerie(payment.getSerie());
                detail.setNumero(payment.getNumero());
                detail.setTipoComprobante(payment.getTipoComprobante());
                detail.setCodigoMoneda(payment.getCodigoMoneda());
                detail.setTipoDocumentoReceptor(payment.getTipoDocumentoReceptor());
                detail.setNumeroDocumentoReceptor(payment.getNumeroDocumentoReceptor());
                if (payment.getCodigoTipoNotaCredito() != null || payment.getCodigoTipoNotaDebito() != null) {
                    detail.setSerieAfectado(payment.getSerieAfectado());
                    detail.setNumeroAfectado(payment.getNumeroAfectado());
                    detail.setTipoComprobanteAfectado(payment.getTipoComprobanteAfectado());
                }
                detail.setStatusItem(payment.getEstadoItem());
                detail.setImporteTotalVenta(payment.getImporteTotalVenta());
                detail.setSumatoriaOtrosCargos(payment.getSumatoriaOtrosCargos());
                detail.setTotalIGV(payment.getTotalIgv());
                detail.setTotalISC(payment.getTotalIsc());
                detail.setTotalOtrosTributos(payment.getTotalOtrostributos());
                detail.setTotalValorVentaOperacionExportacion(payment.getTotalValorVentaExportacion());
                detail.setTotalValorVentaOperacionGravada(payment.getTotalValorVentaGravada());
                detail.setTotalValorVentaOperacionInafecta(payment.getTotalValorVentaInafecta());
                detail.setTotalValorVentaOperacionExonerado(payment.getTotalValorVentaExonerada());
                detail.setTotalValorVentaOperacionGratuita(payment.getTotalValorVentaGratuita());
                details.add(detail);
                ids.add(payment.getIdPaymentVoucher());
            }

            summaryByDay.setItems(details);
        } else throw new ServiceException("No existen comprobantes para generar este resumen [" + fechaEmision + "]");
        System.out.println("SUMMARY BY DAY: "+ summaryByDay);
        return summaryByDay;
    }

    private void registrarSummaryDocuments(Summary summary, Long idRegisterFileSend, String usuario, String ticket, List<Long> ids) {
        System.out.println("INICIO: ");
        log.info("SUMMARY: {}, idRegisterFileSend: {}, usuario: {}, ticket: {}, ids: {}", summary, idRegisterFileSend, usuario, ticket, ids);
        Date fechaActual = Calendar.getInstance().getTime();
        Timestamp fechaEjecucion = new Timestamp(fechaActual.getTime());

        summary.setEstado(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO);
        summary.setFechaGeneracion(UtilFormat.fecha(fechaActual, "yyyy-MM-dd"));
        summary.setFechaGereracionResumen(fechaEjecucion);
        summary.setIdDocument(summary.getId());
        summary.setTicketSunat(ticket);
        summary.setUserName(usuario);

        /*
        summaryEntity.setCorrelativoDia(summary.getNroResumenDelDia());
        summaryEntity.setEstado(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO);
        summaryEntity.setFechaEmision(summary.getFechaEmision());
        summaryEntity.setFechaGeneracion(UtilFormat.fecha(fechaActual, "yyyy-MM-dd"));
        summaryEntity.setFechaGeneracionResumen(fechaEjecucion);
        summaryEntity.setIdDocument(summary.getId());
        summaryEntity.setTicketSunat(ticket);
        summaryEntity.setUserName(usuario);
        summaryEntity.setRucEmisor(summary.getRucEmisor());
        summaryEntity.setEstadoComprobante(summary.getEstadoComprobante());
*/
        //AGREGANDO ARCHIVO
        if (idRegisterFileSend != null) {
            summary.addFile(SummaryFileDto.builder()
                    .estadoArchivo(EstadoArchivoEnum.ACTIVO.name())
                    .idRegisterFileSend(idRegisterFileSend)
                    .tipoArchivo(TipoArchivoEnum.XML.name())
                    .build());
        }

        for (SummaryDetail item : summary.getItems()) {
            item.setEstado(ConstantesParameter.REGISTRO_ACTIVO);
            /*
            DetailDocsSummaryEntity detail = new DetailDocsSummaryEntity();
            detail.setEstado(ConstantesParameter.REGISTRO_ACTIVO);
            detail.setEstadoItem(item.getStatusItem());

            detail.setNumeroItem(item.getNumeroItem());
            detail.setSerieDocumento(item.getSerie());
            detail.setNumeroDocumento(item.getNumero());
            detail.setTipoComprobante(item.getTipoComprobante());
            detail.setNumDocReceptor(item.getNumeroDocumentoReceptor());
            detail.setTipoDocIdentReceptor(item.getTipoDocumentoReceptor());
            detail.setSerieAfectado(item.getSerieAfectado());
            detail.setNumeroAfectado(item.getNumeroAfectado());
            detail.setTipoComprobanteAfectado(item.getTipoComprobanteAfectado());
            detail.setImporteTotalVenta(item.getImporteTotalVenta());
            detail.setSumatoriaOtrosCargos(item.getSumatoriaOtrosCargos());

            detail.setTotalValorVentaOperacionExonerado(item.getTotalValorVentaOperacionExonerado());
            detail.setTotalValorVentaOperacionExportacion(item.getTotalValorVentaOperacionExportacion());
            detail.setTotalValorVentaOperacionGratuita(item.getTotalValorVentaOperacionGratuita());
            detail.setTotalValorVentaOperacionGravada(item.getTotalValorVentaOperacionGravada());
            detail.setTotalValorVentaOperacionInafecta(item.getTotalValorVentaOperacionInafecta());

            detail.setTotalIGV(item.getTotalIGV());
            detail.setTotalISC(item.getTotalISC());
            detail.setTotalOtrosTributos(item.getTotalOtrosTributos());

            summaryEntity.addDetailDocsSummary(detail);
            */
        }
        System.out.println("TICKET SUNAT: {}"+summary.getTicketSunat());
        summaryDocumentsFeign.save(summary);
        paymentVoucherFeign.updateStateToSendSunatForSummaryDocuments(ids, usuario, fechaEjecucion);
    }

    private Map<String, String> getTemplateGenerated(String rucEmisor, Summary summary) throws IOException, NoSuchAlgorithmException {
        System.out.println("RUC EMISOR: "+rucEmisor);
        OseDto ose = companyFeign.findOseByRucInter(rucEmisor);
        System.out.println("OSE: "+ose);
        if (ose != null && ose.getId()==1) {
            return templateService.buildSummaryDailySignOse(summary);
        } else if (ose != null && (ose.getId()==10||ose.getId()==12)) {
            return templateService.buildSummaryDailySignCerti(summary);
        } else {
            return templateService.buildSummaryDailySign(summary);
        }
    }

}
