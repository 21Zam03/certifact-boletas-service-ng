package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.converter.VoucherAnnularConverter;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.dto.VoidedDocumentsDto;
import com.certicom.certifact_boletas_service_ng.dto.VoucherAnnularDto;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.dto.others.Voided;
import com.certicom.certifact_boletas_service_ng.enums.EstadoComprobanteEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoSunatEnum;
import com.certicom.certifact_boletas_service_ng.feign.PaymentVoucherFeign;
import com.certicom.certifact_boletas_service_ng.request.VoucherAnnularRequest;
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
        return respuesta;
    }

    public void annularDocumentSendFromSummaryDocuments(VoucherAnnularDto voucherInput, String userName) {
        Timestamp fechaModificacion;
        String identificador;

        fechaModificacion = new Timestamp(Calendar.getInstance().getTime().getTime());
        identificador = voucherInput.getRucEmisor() + "-" + voucherInput.getTipoComprobante() + "-"
                + voucherInput.getSerie().toUpperCase() + "-" + voucherInput.getNumero();

        PaymentVoucherDto boletaOrNoteBoleta = paymentVoucherFeign.getPaymentVoucherByIdentificadorDocumento(identificador);
        //SI QUIERO ANULAR UN COMPROBANTE ANTES DE QUE ESTE ACEPTADO EN SUNAT, SETEO UN FLAG
        System.out.println("aqui");
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
