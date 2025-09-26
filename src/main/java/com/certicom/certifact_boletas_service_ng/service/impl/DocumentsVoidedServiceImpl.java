package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.converter.VoucherAnnularConverter;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.VoidedDocumentsDto;
import com.certicom.certifact_boletas_service_ng.dto.VoucherAnnularDto;
import com.certicom.certifact_boletas_service_ng.dto.others.IdentificadorComprobante;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.dto.others.Voided;
import com.certicom.certifact_boletas_service_ng.enums.EstadoComprobanteEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoSunatEnum;
import com.certicom.certifact_boletas_service_ng.exception.ValidationException;
import com.certicom.certifact_boletas_service_ng.feign.PaymentVoucherFeign;
import com.certicom.certifact_boletas_service_ng.jms.SqsProducer;
import com.certicom.certifact_boletas_service_ng.request.VoucherAnnularRequest;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.service.DocumentsVoidedService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.certicom.certifact_boletas_service_ng.validation.VoucherAnnularValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentsVoidedServiceImpl implements DocumentsVoidedService {

    private final VoucherAnnularValidator voucherAnnularValidator;
    private final PaymentVoucherFeign paymentVoucherFeign;
    private final DocumentsSummaryService documentsSummaryService;
    private final SqsProducer sqsProducer;

    @Override
    public VoidedDocumentsDto registrarVoidedDocuments(Voided voided, Long idRegisterFile, String usuario, String ticket) {
        return null;
    }

    @Override
    public ResponsePSE anularDocuments(List<VoucherAnnularRequest> documents, String rucEmisor, String userName, List<String> ticketsVoidedProcess) {
        ResponsePSE respuesta = new ResponsePSE();
        Map<String, List<VoucherAnnularRequest>> documentosBajaByFechaEmisionFacturasMap = new HashMap<>();
        List<VoucherAnnularRequest> documentosVoidedByFechaEmision;
        List<VoucherAnnularRequest> documentosSummary = new ArrayList<>();
        StringBuilder messageBuilder = null;
        List<VoucherAnnularRequest> documentsanular = new ArrayList<>();

        //voucherAnnularValidator.validateVoucherAnnular(documents, rucEmisor);

        try {
            for (VoucherAnnularRequest documento : documents) {
                String identificadorDocumento = rucEmisor + "-" + documento.getTipoComprobante() + "-" +
                        documento.getSerie().toUpperCase() + "-" + documento.getNumero();
                System.out.println("INDENTIFICADOR : "+identificadorDocumento);

                PaymentVoucherDto entity = paymentVoucherFeign.getPaymentVoucherByIdentificadorDocumento(identificadorDocumento);
                System.out.println("Paymente: "+entity);
                if (documento.getRucEmisor()==null){
                    documento.setRucEmisor(rucEmisor);
                }
                documento.setFechaEmision(entity.getFechaEmision());
                documento.setSerie(documento.getSerie().toUpperCase());
                documentsanular.add(documento);
            }

            for (VoucherAnnularRequest document : documents) {
                String identificadorDocumento = rucEmisor + "-" + document.getTipoComprobante() + "-" +
                        document.getSerie().toUpperCase() + "-" + document.getNumero();
                System.out.println(identificadorDocumento);
                boolean noExiste = false;
                PaymentVoucherDto entity = paymentVoucherFeign.getPaymentVoucherByIdentificadorDocumento(identificadorDocumento);
                if (entity==null){
                    noExiste=true;
                }

                if (noExiste){
                    if (messageBuilder == null) {
                        messageBuilder = new StringBuilder();
                    }
                    messageBuilder.append("500");
                    messageBuilder.append("No existe documento de referencia");
                }else{
                    switch (document.getTipoComprobante()) {
                        case ConstantesSunat.TIPO_DOCUMENTO_BOLETA:
                            documentosSummary.add(document);
                            break;
                        case ConstantesSunat.TIPO_DOCUMENTO_NOTA_CREDITO:
                        case ConstantesSunat.TIPO_DOCUMENTO_NOTA_DEBITO:
                            if (document.getTipoComprobanteRelacionado().equals(ConstantesSunat.TIPO_DOCUMENTO_FACTURA)) {
                                if (documentosBajaByFechaEmisionFacturasMap.get(document.getFechaEmision()) != null) {
                                    documentosVoidedByFechaEmision = documentosBajaByFechaEmisionFacturasMap
                                            .get(document.getFechaEmision());
                                } else {
                                    documentosVoidedByFechaEmision = new ArrayList<>();
                                }
                                documentosVoidedByFechaEmision.add(document);
                                documentosBajaByFechaEmisionFacturasMap.put(
                                        document.getFechaEmision(),
                                        documentosVoidedByFechaEmision);
                            } else {
                                documentosSummary.add(document);
                            };
                            break;
                    }
                }
            }
            for (VoucherAnnularRequest document : documentosSummary) {
                document.setRucEmisor(rucEmisor);
                VoucherAnnularDto voucherAnnularDto = VoucherAnnularConverter.requestToDto(document);
                annularDocumentSendFromSummaryDocuments(voucherAnnularDto, userName);
            }
            respuesta.setEstado(true);
            if (messageBuilder != null) {
                respuesta.setMensaje(messageBuilder.toString());
            } else {
                respuesta.setMensaje(ConstantesParameter.MSG_RESP_OK);
            }
        } catch (Exception e) {
            respuesta.setEstado(false);
            respuesta.setMensaje(e.getMessage());
            System.out.println("ERROR: " + e.getMessage());
        }
        generateVoidSummary(VoucherAnnularConverter.requestListToDtoList(documents), userName);
        return respuesta;
    }

    private void generateVoidSummary(List<VoucherAnnularDto> documentosToAnular, String username) {
        ResponsePSE responsePSE = null;
        try {
            IdentificadorComprobante comprobante = new IdentificadorComprobante(documentosToAnular.get(0).getTipoComprobante(),documentosToAnular.get(0).getSerie(),documentosToAnular.get(0).getRucEmisor(),documentosToAnular.get(0).getNumero());
            responsePSE = documentsSummaryService.generarSummaryByFechaEmisionAndRuc(
                    documentosToAnular.get(0).getRucEmisor(),
                    documentosToAnular.get(0).getFechaEmision(),
                    comprobante,
                    username
            );
            System.out.println(responsePSE.toString());
            if (responsePSE.getEstado()) {
                sqsProducer.produceProcessSummary(responsePSE.getTicket(), documentosToAnular.get(0).getRucEmisor());
            }
        } catch (ValidationException e) {
            responsePSE = new ResponsePSE();
            responsePSE.setEstado(false);
            responsePSE.setMensaje(e.getMessage());
        }
    }

    public void annularDocumentSendFromSummaryDocuments(VoucherAnnularDto voucherInput, String userName) {
        Timestamp fechaModificacion;
        String identificador;

        fechaModificacion = new Timestamp(Calendar.getInstance().getTime().getTime());
        identificador = voucherInput.getRucEmisor() + "-" + voucherInput.getTipoComprobante() + "-"
                + voucherInput.getSerie().toUpperCase() + "-" + voucherInput.getNumero();

        PaymentVoucherDto boletaOrNoteBoleta = paymentVoucherFeign.getPaymentVoucherByIdentificadorDocumento(identificador);
        //SI QUIERO ANULAR UN COMPROBANTE ANTES DE QUE ESTE ACEPTADO EN SUNAT, SETEO UN FLAG
        if (!boletaOrNoteBoleta.getEstadoSunat().equals(EstadoSunatEnum.ACEPTADO.getAbreviado())) {
            boletaOrNoteBoleta.setBoletaAnuladaSinEmitir(true);
        }

        boletaOrNoteBoleta.setEstado(EstadoComprobanteEnum.PENDIENTE_ANULACION.getCodigo());
        boletaOrNoteBoleta.setEstadoSunat(EstadoSunatEnum.NO_ENVIADO.getAbreviado());
        boletaOrNoteBoleta.setMotivoAnulacion(voucherInput.getMotivoAnulacion());
        boletaOrNoteBoleta.setUserNameModificacion(userName);
        boletaOrNoteBoleta.setFechaModificacion(fechaModificacion);
        boletaOrNoteBoleta.setEstadoItem(ConstantesParameter.STATE_ITEM_PENDIENTE_ANULACION);

        paymentVoucherFeign.save(boletaOrNoteBoleta);

        /*
        Logger.register(TipoLogEnum.INFO, voucherInput.getRucEmisor(), identificador, OperacionLogEnum.REGISTER_ANULAR_VOUCHER,
                SubOperacionLogEnum.UPDATE_BD_PAYMENT_VOUCHER, ConstantesParameter.MSG_RESP_SUB_PROCESO_OK + ".[" + voucherInput.toString() + "]["
                        + "EstadoComprobante:" + EstadoComprobanteEnum.PENDIENTE_ANULACION.getCodigo() + ","
                        + "EstadoSunat:" + EstadoSunatEnum.NO_ENVIADO.getAbreviado() + ","
                        + "MotivoAnulacion:" + voucherInput.getMotivoAnulacion() + ","
                        + "fechaModificacion:" + fechaModificacion + "]");
        * */

    }

}
