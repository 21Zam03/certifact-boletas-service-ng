package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;
import com.certicom.certifact_boletas_service_ng.dto.SummaryFileDto;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponseSunat;
import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import com.certicom.certifact_boletas_service_ng.dto.others.SummaryDetail;
import com.certicom.certifact_boletas_service_ng.enums.EstadoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoComprobanteEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoSunatEnum;
import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.feign.CompanyFeign;
import com.certicom.certifact_boletas_service_ng.feign.PaymentVoucherFeign;
import com.certicom.certifact_boletas_service_ng.feign.SummaryDocumentsFeign;
import com.certicom.certifact_boletas_service_ng.feign.VoidedDocumentsFeign;
import com.certicom.certifact_boletas_service_ng.service.AmazonS3ClientService;
import com.certicom.certifact_boletas_service_ng.service.StatusService;
import com.certicom.certifact_boletas_service_ng.service.SunatService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.certicom.certifact_boletas_service_ng.util.UtilArchivo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatusServiceImpl implements StatusService {

    private final SunatService sunatService;
    private final SummaryDocumentsFeign summaryDocumentsFeign;
    private final VoidedDocumentsFeign voidedDocumentsFeign;
    private final CompanyFeign companyFeign;
    private final AmazonS3ClientService amazonS3ClientService;
    private final PaymentVoucherFeign paymentVoucherFeign;

    @Override
    public ResponsePSE getStatus(String numeroTicket, String tipoResumen, String userName, String rucEmisor) {
        ResponseSunat respSunat;
        ResponsePSE resp = null;
        System.out.println("NUMERO TICKET: "+numeroTicket);
        System.out.println("TIPO RESUMEN: "+tipoResumen);
        System.out.println("USERNAME: "+userName);
        System.out.println("RUC EMISOR: "+rucEmisor);
        try {
            resp = new ResponsePSE();
            respSunat = sunatService.getStatus(numeroTicket, tipoResumen, rucEmisor);
            System.out.println(respSunat);

            switch (respSunat.getEstadoComunicacionSunat()) {
                case SUCCESS:
                    comunicacionSuccess(
                            tipoResumen,
                            numeroTicket,
                            respSunat.getStatusCode(),
                            respSunat.getMessage(),
                            respSunat.getNameDocument(),
                            respSunat.getRucEmisor(),
                            respSunat.getContentBase64(),
                            userName,
                            EstadoComprobanteEnum.ACEPTADO
                    );

                    resp.setEstado(true);
                    resp.setRespuesta(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_PROCESO_OK);
                    resp.setMensaje("[" + respSunat.getStatusCode() + "] " + respSunat.getMessage());
                    break;
                case SUCCESS_WITH_WARNING:
                    comunicacionSuccess(
                            tipoResumen,
                            numeroTicket,
                            respSunat.getStatusCode(),
                            respSunat.getMessage(),
                            respSunat.getNameDocument(),
                            rucEmisor,
                            respSunat.getContentBase64(),
                            userName,
                            EstadoComprobanteEnum.ACEPTADO_ADVERTENCIA
                    );
                    resp.setEstado(true);
                    resp.setRespuesta(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_PROCESO_OK);
                    resp.setMensaje("[" + respSunat.getStatusCode() + "] " + respSunat.getMessage());
                    break;
                case SUCCESS_WITH_ERROR_CONTENT:
                    comunicacionError(
                            tipoResumen,
                            numeroTicket,
                            respSunat.getStatusCode(),
                            respSunat.getMessage(),
                            rucEmisor,
                            userName,
                            EstadoComprobanteEnum.ERROR);
                    resp.setRespuesta(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_PROCESO_ERROR);
                    resp.setMensaje("[" + respSunat.getStatusCode() + "] " + respSunat.getMessage());
                    resp.setEstado(false);
                    break;
                case SUCCESS_WITHOUT_CONTENT_CDR:
                    resp.setMensaje(respSunat.getMessage());
                    resp.setEstado(false);
                    break;
                case WITHOUT_CONNECTION:
                case PENDING:
                    comunicacionPendiente(tipoResumen, numeroTicket, resp);
                    resp.setRespuesta(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO);
                    resp.setMensaje(respSunat.getMessage());
                    resp.setEstado(false);
                    break;
                default:
//					resp.setRespuesta(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO);
                    resp.setMensaje(respSunat.getMessage());
                    resp.setEstado(false);
                    break;
            }

        } catch (IOException e) {
            resp.setEstado(false);
            resp.setMensaje(e.getMessage());

            /*
            Logger.register(TipoLogEnum.ERROR, rucEmisor, numeroTicket, operacionLog,
                    SubOperacionLogEnum.IN_PROCESS, e.getMessage(), numeroTicket, e);
            * */

        } catch (Exception e) {
            resp.setEstado(false);
            resp.setMensaje(e.getMessage());
            /*
            Logger.register(TipoLogEnum.ERROR, rucEmisor, numeroTicket, operacionLog,
                    SubOperacionLogEnum.IN_PROCESS, e.getMessage(), numeroTicket, e);
            * */

        }

        return resp;
    }

    private void comunicacionPendiente(String tipoDocumentoResumen, String numeroTicket, ResponsePSE responsePSE) {

        if (tipoDocumentoResumen.equals(ConstantesSunat.RESUMEN_DIARIO_BOLETAS)) {
            Summary summary = summaryDocumentsFeign.findByTicket(numeroTicket);
            Integer intentos = summary.getIntentosGetStatus() != null ? summary.getIntentosGetStatus() : 0;
            summary.setIntentosGetStatus(intentos + 1);
            summary = summaryDocumentsFeign.save(summary);
            responsePSE.setIntentosGetStatus(summary.getIntentosGetStatus());
        } else {
            /*
            VoidedDocumentsEntity voided = documentsVoidedRepository.getVoidedByTicket(numeroTicket);
            Integer intentos = voided.getIntentosGetStatus() != null ? voided.getIntentosGetStatus() : 0;
            if (voided.getIntentosGetStatus() == null) voided.setIntentosGetStatus(0);
            voided.setIntentosGetStatus(intentos + 1);
            voided = documentsVoidedRepository.save(voided);
            responsePSE.setIntentosGetStatus(voided.getIntentosGetStatus());
            * */
        }
    }

    private void comunicacionError(String tipoDocumentoResumen, String numeroTicket,
                                   String codeResponse, String messageResponse, String rucEmisor,
                                   String userName, EstadoComprobanteEnum estadoComprobanteError) throws Exception {

        Map<String, String> params;
        String estadoDocumentInBD;

        estadoDocumentInBD = getEstadoDocumentoResumenInBD(tipoDocumentoResumen, numeroTicket);
        if (estadoDocumentInBD.equals(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO)) {

            params = new HashMap<>();

            params.put(ConstantesParameter.PARAM_NUM_TICKET, numeroTicket);
            params.put(ConstantesParameter.PARAM_ESTADO, ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_PROCESO_ERROR);
            params.put(ConstantesParameter.PARAM_RESPONSE_CODE, codeResponse);
            params.put(ConstantesParameter.PARAM_DESCRIPTION, messageResponse);
            params.put(ConstantesParameter.PARAM_USER_NAME, userName);

            actualizarDocumentoResumenByTicket(params, tipoDocumentoResumen, null, estadoComprobanteError);
        }
    }

    private void comunicacionSuccess(String tipoDocumentoResumen, String numeroTicket,
                                     String codeResponse, String messageResponse, String nameDocument,
                                     String rucEmisor, String fileBase64, String userName, EstadoComprobanteEnum aceptado) throws Exception {

        Map<String, String> params = new HashMap<>();
        String estadoDocumentInBD;

        estadoDocumentInBD = getEstadoDocumentoResumenInBD(tipoDocumentoResumen, numeroTicket);
        if (estadoDocumentInBD.equals(ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_IN_PROCESO)) {
            System.out.println("INGRESO VOIDED 98");
            CompanyDto companyEntity = companyFeign.findCompanyByRuc(rucEmisor);
            RegisterFileUploadDto file = amazonS3ClientService.uploadFileStorage(UtilArchivo.b64ToByteArrayInputStream(fileBase64),
                    nameDocument, "summary", companyEntity);

            params.put(ConstantesParameter.PARAM_NUM_TICKET, numeroTicket);
            params.put(ConstantesParameter.PARAM_ESTADO, ConstantesParameter.STATE_SUMMARY_VOIDED_DOCUMENTS_PROCESO_OK);
            params.put(ConstantesParameter.PARAM_RESPONSE_CODE, codeResponse);
            params.put(ConstantesParameter.PARAM_DESCRIPTION, messageResponse);
            params.put(ConstantesParameter.PARAM_USER_NAME, userName);

            actualizarDocumentoResumenByTicket(params, tipoDocumentoResumen, file.getIdRegisterFileSend(), aceptado);
            System.out.println("INGRESO VOIDED 99"+tipoDocumentoResumen);
            if (tipoDocumentoResumen.equals(ConstantesSunat.RESUMEN_DIARIO_BOLETAS)) {
                try {
                    Summary summaryDocumentEntity = summaryDocumentsFeign.findByTicket(numeroTicket);
                    if (summaryDocumentEntity != null) {
                        String finalRucEmisor = rucEmisor;
                        summaryDocumentEntity.getItems().forEach(detailDocsSummaryEntity -> {
                            PaymentVoucherDto paymentVoucherEntity = paymentVoucherFeign
                                    .findByRucAndTipoAndSerieAndNumero(
                                            finalRucEmisor, detailDocsSummaryEntity.getTipoComprobante(),
                                            detailDocsSummaryEntity.getSerie(),
                                            detailDocsSummaryEntity.getNumero());
                            if (paymentVoucherEntity != null) {
                                System.out.println();
                            }
                        });
                    }
                } catch (Exception e) {
                    log.error("ERROR: {} ", e.getMessage());
                }
            }
        }
    }

    @Override
    public String getEstadoDocumentoResumenInBD(String tipoDocumento, String numeroTicket) {
        String estado;
        if (tipoDocumento.equals(ConstantesSunat.RESUMEN_DIARIO_BOLETAS)) {
            estado = summaryDocumentsFeign.getEstadoByNumeroTicket(numeroTicket);
        } else {
            estado = voidedDocumentsFeign.getEstadoByNumeroTicket(numeroTicket);
        }
        return estado;
    }

    public void actualizarDocumentoResumenByTicket(
            Map<String, String> params,
            String tipoDocumento, Long idRegisterFile,
            EstadoComprobanteEnum estadoComprobanteEnum) {

        Timestamp fechaModificacion = new Timestamp(Calendar.getInstance().getTime().getTime());
        String numeroTicket = params.get(ConstantesParameter.PARAM_NUM_TICKET);
        String estado = params.get(ConstantesParameter.PARAM_ESTADO);
        String codeResponse = params.get(ConstantesParameter.PARAM_RESPONSE_CODE);
        String description = params.get(ConstantesParameter.PARAM_DESCRIPTION);
        String usuario = params.get(ConstantesParameter.PARAM_USER_NAME);
        List<String> identificadoresComprobantes = new ArrayList<>();
        List<String> comprobantesByAnular = null;
        List<String> comprobantesByAceptar = null;
        String rucEmisor;

        StringBuilder msgLog = new StringBuilder();

        if (tipoDocumento.equals(ConstantesSunat.RESUMEN_DIARIO_BOLETAS)) {
            Summary summary;
            summary = summaryDocumentsFeign.findByTicket(numeroTicket);
            rucEmisor = summary.getRucEmisor();
            summary.setEstado(estado);
            summary.setCodeResponse(codeResponse);
            summary.setDescripcionResponse(description);
            summary.setUserNameModify(usuario);
            summary.setFechaModificacion(fechaModificacion);
            summary.setEstadoComprobante(estadoComprobanteEnum.getCodigo());

            //AGREGANDO ARCHIVO
            if (idRegisterFile != null) {
                summary.addFile(SummaryFileDto.builder()
                        .estadoArchivo(EstadoArchivoEnum.ACTIVO.name())
                        .idRegisterFileSend(idRegisterFile)
                        .tipoArchivo(TipoArchivoEnum.CDR.name())
                        .build());
            }
            summaryDocumentsFeign.save(summary);

            msgLog.append("{").append(ConstantesParameter.MSG_RESP_SUB_PROCESO_OK).append("}").
                    append("{numeroTicket:").append(numeroTicket).append("}{estado:").append(estado).
                    append("}{codeResponse:").append(codeResponse).append("}{description:").append(description).
                    append("}{fechaModificacion:").append(fechaModificacion).append("}{estadoComprobante:").
                    append(estadoComprobanteEnum.getCodigo()).append("}");
/*
            Logger.register(TipoLogEnum.INFO, rucEmisor, numeroTicket, OperacionLogEnum.STATUS_SUNAT_SUMMARY,
                    SubOperacionLogEnum.UPDATE_BD_SUMMARY, msgLog.toString());
*/
            comprobantesByAnular = new ArrayList<>();
            comprobantesByAceptar = new ArrayList<>();

            for (SummaryDetail detail : summary.getItems()) {
                if (detail.getStatusItem() == ConstantesParameter.STATE_ITEM_PENDIENTE_ADICION ||
                        detail.getStatusItem() == ConstantesParameter.STATE_ITEM_PENDIENTE_MODIFICACION) {
                    comprobantesByAceptar.add(rucEmisor + "-" + detail.getTipoComprobante() + "-"
                            + detail.getSerie() + "-" + detail.getNumero());
                } else {
                    comprobantesByAnular.add(rucEmisor + "-" + detail.getTipoComprobante() + "-"
                            + detail.getSerie() + "-" + detail.getNumero());
                }
            }

            identificadoresComprobantes = new ArrayList<String>(comprobantesByAceptar);
            identificadoresComprobantes.addAll(comprobantesByAnular);

        } else {
/*
            VoidedDocumentsEntity voided;
            identificadoresComprobantes = new ArrayList<>();
            voided = documentsVoidedRepository.getVoidedByTicket(numeroTicket);
            rucEmisor = voided.getRucEmisor();

            voided.setEstado(estado);
            voided.setCodigoRespuesta(codeResponse);
            voided.setDescripcionRespuesta(description);
            voided.setUserNameModify(usuario);
            voided.setFechaModificacion(fechaModificacion);
            voided.setEstadoComprobante(estadoComprobanteEnum.getCodigo());

            //AGREGANDO ARCHIVO
            if (idRegisterFile != null) {
                voided.addFile(VoidedFileEntity.builder()
                        .estadoArchivo(EstadoArchivoEnum.ACTIVO)
                        .registerFileUpload(RegisterFileUploadEntity.builder().idRegisterFileSend(idRegisterFile).build())
                        .tipoArchivo(TipoArchivoEnum.CDR)
                        .build());
            }
            System.out.println("seguimiento voided");
            documentsVoidedRepository.save(voided);

            msgLog.append("{").append(ConstantesParameter.MSG_RESP_SUB_PROCESO_OK).append("}").
                    append("{numeroTicket:").append(numeroTicket).append("}{estado:").append(estado).
                    append("}{codeResponse:").append(codeResponse).append("}{description:").append(description).
                    append("}{fechaModificacion:").append(fechaModificacion).append("}{estadoComprobante:").
                    append(estadoComprobanteEnum.getCodigo()).append("}");

            Logger.register(TipoLogEnum.INFO, rucEmisor, numeroTicket, OperacionLogEnum.STATUS_SUNAT_VOIDED,
                    SubOperacionLogEnum.UPDATE_BD_VOIDED, msgLog.toString());

            for (DetailDocsVoidedEntity detail : voided.getBajaDocumentos()) {
                identificadoresComprobantes.add(rucEmisor + "-" + detail.getTipoComprobante() + "-" +
                        detail.getSerieDocumento() + "-" + detail.getNumeroDocumento());
            }*/
        }

        switch (estadoComprobanteEnum) {
            case ACEPTADO:
            case ACEPTADO_ADVERTENCIA:
                if (tipoDocumento.equals(ConstantesSunat.COMUNICACION_BAJA)) {
                    /*
                    paymentVoucherFeign.updateComprobantesByBajaDocumentos(
                            identificadoresComprobantes, usuario, fechaModificacion);

                    for(String comprobante : identificadoresComprobantes) {
                        PaymentVoucherEntity paymentVoucher = paymentVoucherRepository.findByIdentificadorDocumento(comprobante);
                        User user = userRepository.findByUsername(paymentVoucher.getUserName()).orElse(null);
                        for (DetailsPaymentVoucherEntity paymentVoucherItem : paymentVoucher.getDetailsPaymentVouchers()) {
                            Optional<Long> optionalProductoId = productRepository.findProductIdByCodigoOrDescripcion(
                                    paymentVoucherItem.getCodigoProducto(), paymentVoucherItem.getDescripcion());
                            ProductEntity producto = productRepository.findById(optionalProductoId.get())
                                    .orElseThrow(() -> new ServiceException("Producto no encontrado con ID. "));
                            Long stockAntes = producto.getStock();
                            producto.setStock(producto.getStock() + paymentVoucherItem.getCantidad().longValue());
                            productRepository.save(producto);
                            historialStockService.registrarHistorialStock(
                                    producto,
                                    user,
                                    stockAntes,
                                    stockAntes + paymentVoucherItem.getCantidad().longValue(),
                                    null,
                                    paymentVoucherItem,
                                    paymentVoucher,
                                    "ANULACION"
                            );
                        }
                    }

                    msgLog.setLength(0);
                    msgLog.append("{").append(ConstantesParameter.MSG_RESP_SUB_PROCESO_OK).append("}").
                            append("{identificadoresComprobantes:").append(identificadoresComprobantes).
                            append("}{fechaModificacion:").append(fechaModificacion).append("}");

                    Logger.register(TipoLogEnum.INFO, rucEmisor, numeroTicket, OperacionLogEnum.STATUS_SUNAT_VOIDED,
                            SubOperacionLogEnum.UPDATE_BD_PAYMENT_VOUCHER, msgLog.toString());
                    * */
                } else {
                    if (!comprobantesByAceptar.isEmpty()) {
                        // LEYTER
                        System.out.println("SEGUIMIENTO 024");
                        paymentVoucherFeign.updateComprobantesBySummaryDocuments(
                                comprobantesByAceptar,
                                EstadoComprobanteEnum.ACEPTADO.getCodigo(),
                                EstadoSunatEnum.ACEPTADO.getAbreviado(),
                                usuario,
                                fechaModificacion);
                        /*
                        for(String comprobante : comprobantesByAceptar) {
                            // si el comprobante contiene '-07-' es una nota y se actualiza el stock de productos
                            if (comprobante.contains("-07-")) {
                                PaymentVoucherDto paymentVoucher = paymentVoucherFeign
                                        .findByIdentificadorDocumento(comprobante);
                                User user = userRepository.findByUsername(paymentVoucher.getUserName()).orElse(null);
                                if (paymentVoucher.getCodigoTipoNotaCredito().equals("01") ||
                                        paymentVoucher.getCodigoTipoNotaCredito().equals("02") ||
                                        paymentVoucher.getCodigoTipoNotaCredito().equals("06") ){
                                    for (DetailsPaymentVoucherEntity paymentVoucherItem : paymentVoucher.getDetailsPaymentVouchers()) {
                                        Optional<Long> optionalProductoId = productRepository.findProductIdByCodigoOrDescripcion(
                                                paymentVoucherItem.getCodigoProducto(), paymentVoucherItem.getDescripcion());
                                        ProductEntity producto = productRepository.findById(optionalProductoId.get())
                                                .orElseThrow(() -> new ServiceException("Producto no encontrado con ID. "));
                                        Long stockAntes = producto.getStock();
                                        producto.setStock(producto.getStock() + paymentVoucherItem.getCantidad().longValue());
                                        productRepository.save(producto);
                                        historialStockService.registrarHistorialStock(
                                                producto,
                                                user,
                                                stockAntes,
                                                stockAntes + paymentVoucherItem.getCantidad().longValue(),
                                                null,
                                                paymentVoucherItem,
                                                null,
                                                "DEVOLUCIÓN"
                                        );
                                    }
                                }

                            }
                        }
                        * */
                        msgLog.setLength(0);
                        msgLog.append("{").append(ConstantesParameter.MSG_RESP_SUB_PROCESO_OK).append("}").
                                append("{comprobantesByAceptar:").append(comprobantesByAceptar).
                                append("}{EstadoComprobanteEnum.ACEPTADO:").append(EstadoComprobanteEnum.ACEPTADO.getCodigo()).
                                append("}{EstadoSunatEnum.ACEPTADO").append(EstadoSunatEnum.ACEPTADO.getAbreviado()).
                                append("}{fechaModificacion").append(fechaModificacion).append("}");
                        /*
                        Logger.register(TipoLogEnum.INFO, rucEmisor, numeroTicket, OperacionLogEnum.STATUS_SUNAT_SUMMARY,
                                SubOperacionLogEnum.UPDATE_BD_PAYMENT_VOUCHER, msgLog.toString());
                        * */
                    }
                    /*
                    System.out.println("INGRESAMOS A ANULAR BOLETAS");
                    if (!comprobantesByAnular.isEmpty()) {

                        System.out.println("ANULANDO COMPROBANTES: "+comprobantesByAnular.size());
                        paymentVoucherFeign.updateComprobantesBySummaryDocuments(
                                comprobantesByAnular,
                                EstadoComprobanteEnum.ANULADO.getCodigo(),
                                EstadoSunatEnum.ANULADO.getAbreviado(),
                                usuario, fechaModificacion);
                        // LEYTER

                        System.out.println("ANULANDO COMPROBANTES-2: "+comprobantesByAnular.size());
                        for(String comprobante : comprobantesByAnular) {
                            System.out.println("ANULANDO COMPROBANTE-2: "+comprobante);
                            PaymentVoucherEntity paymentVoucher = paymentVoucherRepository
                                    .findByIdentificadorDocumento(comprobante);
                            User user = userRepository.findByUsername(paymentVoucher.getUserName()).orElse(null);
                            System.out.println("ANULANDO COMPROBANTE-3: "+paymentVoucher.getDetailsPaymentVouchers().size());
                            for (DetailsPaymentVoucherEntity paymentVoucherItem : paymentVoucher.getDetailsPaymentVouchers()) {
                                System.out.println("ANULANDO ITEM-4: "+paymentVoucherItem.getCodigoProducto()==null?"nulo":
                                        paymentVoucherItem.getCodigoProducto());
                                System.out.println("ANULANDO ITEM-5: "+paymentVoucherItem.getDescripcion());
                                Optional<Long> optionalProductoId = productRepository.findProductIdByCodigoOrDescripcion(
                                        paymentVoucherItem.getCodigoProducto(), paymentVoucherItem.getDescripcion());
                                System.out.println("ANULANDO ITEM-6: "+optionalProductoId.isPresent()!=null?
                                        optionalProductoId.get():"no encontrado");
                                ProductEntity producto = productRepository.findById(optionalProductoId.get())
                                        .orElseThrow(() -> new ServiceException("Producto no encontrado con ID. "));
                                System.out.println(producto);
                                Long stockAntes = producto.getStock();
                                producto.setStock(producto.getStock() + paymentVoucherItem.getCantidad().longValue());
                                productRepository.save(producto);
                                System.out.println(producto);
                                System.out.println(user);
                                System.out.println(stockAntes);
                                System.out.println(paymentVoucherItem);
                                System.out.println(paymentVoucherItem.getCantidad().longValue());
                                historialStockService.registrarHistorialStock(
                                        producto,
                                        user,
                                        stockAntes,
                                        stockAntes + paymentVoucherItem.getCantidad().longValue(),
                                        null,
                                        paymentVoucherItem,
                                        null,
                                        "ANULACIÓN"
                                );
                            }
                        }
                        System.out.println("ANULANDO COMPROBANTES-88: ");

                        msgLog.setLength(0);
                        msgLog.append("{").append(ConstantesParameter.MSG_RESP_SUB_PROCESO_OK).append("}").
                                append("{comprobantesByAnular:").append(comprobantesByAceptar).
                                append("}{EstadoComprobanteEnum.ANULADO:").append(EstadoComprobanteEnum.ANULADO.getCodigo()).
                                append("}{EstadoSunatEnum.ANULADO").append(EstadoSunatEnum.ANULADO.getAbreviado()).
                                append("}{fechaModificacion").append(fechaModificacion).append("}")
                                .append("{SIZE : ").append(comprobantesByAnular.size()).append("}");

                        Logger.register(TipoLogEnum.INFO, rucEmisor, numeroTicket, OperacionLogEnum.STATUS_SUNAT_SUMMARY,
                                SubOperacionLogEnum.UPDATE_BD_PAYMENT_VOUCHER, msgLog.toString());
                    }
                    * */
                }
                break;
            case ERROR:
                paymentVoucherFeign.updateComprobantesOnResumenError(
                        identificadoresComprobantes, usuario, fechaModificacion);

                msgLog.setLength(0);
                msgLog.append("{").append(ConstantesParameter.MSG_RESP_SUB_PROCESO_OK).append("}").
                        append("{identificadoresComprobantesRechazados:").append(identificadoresComprobantes).
                        append("}{fechaModificacion").append(fechaModificacion).append("}");

                /*
                Logger.register(TipoLogEnum.INFO, rucEmisor, numeroTicket, OperacionLogEnum.STATUS_SUNAT_SUMMARY,
                        SubOperacionLogEnum.UPDATE_BD_PAYMENT_VOUCHER, msgLog.toString());
                * */
                break;
            default:
                break;
        }
    }
}
