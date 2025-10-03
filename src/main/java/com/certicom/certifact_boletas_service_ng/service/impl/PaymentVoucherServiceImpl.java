package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.converter.PaymentVoucherConverter;
import com.certicom.certifact_boletas_service_ng.dto.*;
import com.certicom.certifact_boletas_service_ng.dto.others.*;
import com.certicom.certifact_boletas_service_ng.enums.EstadoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoComprobanteEnum;
import com.certicom.certifact_boletas_service_ng.enums.EstadoSunatEnum;
import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.exception.SignedException;
import com.certicom.certifact_boletas_service_ng.exception.TemplateException;
import com.certicom.certifact_boletas_service_ng.feign.*;
import com.certicom.certifact_boletas_service_ng.formatter.PaymentVoucherFormatter;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.service.AmazonS3ClientService;
import com.certicom.certifact_boletas_service_ng.service.DocumentsSummaryService;
import com.certicom.certifact_boletas_service_ng.service.PaymentVoucherService;
import com.certicom.certifact_boletas_service_ng.service.TemplateService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.certicom.certifact_boletas_service_ng.util.UUIDGen;
import com.certicom.certifact_boletas_service_ng.util.UtilArchivo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVoucherServiceImpl implements PaymentVoucherService {

    private final PaymentVoucherFormatter paymentVoucherFormatter;
    private final UserFeign userFeign;
    private final CompanyFeign companyFeign;
    private final BranchOfficeFeign branchOfficeFeign;
    private final PaymentVoucherFeign paymentVoucherFeign;
    private final TemplateService templateService;
    private final AmazonS3ClientService amazonS3ClientService;
    private final DocumentsSummaryService documentsSummaryService;

    @Value("${urlspublicas.descargaComprobante}")
    private String urlServiceDownload;

    @Override
    public Map<String, Object> createPaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        return generateNewDocument(paymentVoucher, idUsuario);
    }

    @Override
    public Map<String, Object> updatePaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        return Map.of();
    }

    /*
    @Override
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
    * */

    /*
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
        return summaryByDay;
    }


* */

    private Map<String, Object> generateNewDocument(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        Map<String, Object> resultado = new HashMap<>();
        ResponsePSE response;
        boolean status = false;
        PaymentVoucherDto comprobanteCreado = null;
        SendBoletaDto sendBoletaDto = null;
        String messageResponse = ConstantesParameter.MSG_REGISTRO_DOCUMENTO_OK;

        try {
            PaymentVoucherDto paymentVoucherDto = PaymentVoucherConverter.requestToDto(paymentVoucher);
            paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherDto);
            UserDto userLogged = userFeign.findUserById(idUsuario);
            CompanyDto companyDto = completarDatosEmisor(paymentVoucherDto);
            setCodigoTipoOperacionCatalog(paymentVoucherDto);
            setOficinaId(paymentVoucherDto, companyDto);
            setLeyenda(paymentVoucherDto);

            if ((companyDto.getSimultaneo() != null && companyDto.getSimultaneo())) {
                Integer proximoNumero;
                proximoNumero = getProximoNumero(paymentVoucherDto.getTipoComprobante(), paymentVoucherDto.getSerie(), paymentVoucherDto.getRucEmisor());
                if (proximoNumero > paymentVoucherDto.getNumero()) {
                    paymentVoucherDto.setNumero(proximoNumero);
                }
            }

            Map<String, String> plantillaGenerado = generarPlantillaXml(companyDto, paymentVoucherDto);
            paymentVoucherDto.setCodigoHash(plantillaGenerado.get(ConstantesParameter.CODIGO_HASH));

            RegisterFileUploadDto archivoSubido = subirXmlComprobante(companyDto, plantillaGenerado);

            comprobanteCreado = saveVoucher(paymentVoucherDto, archivoSubido.getIdRegisterFileSend(), userLogged.getNombreUsuario());
            System.out.println("COMPROBANTE CREADO: "+comprobanteCreado);
            sendBoletaDto = createSendBoleta(companyDto, paymentVoucherDto);

            resultado.put(ConstantesParameter.PARAM_BEAN_SEND_BOLETA, sendBoletaDto);
            status = true;
        } catch (TemplateException | SignedException e) {
            messageResponse = "Error al generar plantilla del documento[" + paymentVoucher.getIdentificadorDocumento() + "] " + e.getMessage();
        } catch (Exception e) {
            messageResponse = e.getMessage();
        }
        if(!status) {
            throw new ServiceException(messageResponse);
        }
        response = createResponsePse(messageResponse, status, comprobanteCreado);

        resultado.put("idPaymentVoucher", comprobanteCreado.getIdPaymentVoucher());
        resultado.put(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE, response);

        validateAutomaticDelivery((SendBoletaDto) resultado.get(ConstantesParameter.PARAM_BEAN_SEND_BOLETA));
        return resultado;
    }

    private Map<String, Object> updateExistingDocument(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        Map<String, Object> resultado = new HashMap<>();
        ResponsePSE response;
        boolean status = false;
        PaymentVoucherDto comprobanteCreado = null;
        SendBoletaDto sendBoletaDto = null;
        String messageResponse = ConstantesParameter.MSG_REGISTRO_DOCUMENTO_OK;

        try {
            PaymentVoucherDto paymentVoucherDto = PaymentVoucherConverter.requestToDto(paymentVoucher);
            paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherDto);
            UserDto userLogged = userFeign.findUserById(idUsuario);
            CompanyDto companyDto = completarDatosEmisor(paymentVoucherDto);
            setCodigoTipoOperacionCatalog(paymentVoucherDto);
            setOficinaId(paymentVoucherDto, companyDto);
            setLeyenda(paymentVoucherDto);

            PaymentVoucherDto paymentVoucherDtoOld = paymentVoucherFeign.findPaymentVoucherByRucAndTipoComprobanteAndSerieAndNumero(
                    paymentVoucherDto.getRucEmisor(), paymentVoucherDto.getTipoComprobante(), paymentVoucherDto.getSerie(), paymentVoucherDto.getNumero());
            if (paymentVoucherDtoOld == null)
                throw new ServiceException("Este comprobante que desea editar, no existe en la base de datos del PSE");

            Map<String, String> plantillaGenerado = generarPlantillaXml(companyDto, paymentVoucherDto);
            paymentVoucherDto.setCodigoHash(plantillaGenerado.get(ConstantesParameter.CODIGO_HASH));

            RegisterFileUploadDto archivoSubido = subirXmlComprobante(companyDto, plantillaGenerado);

            comprobanteCreado = updateVoucher(paymentVoucherDto, paymentVoucherDtoOld, archivoSubido.getIdRegisterFileSend(), userLogged.getNombreUsuario());
            System.out.println("COMPROBANTE CREADO: "+comprobanteCreado);
            sendBoletaDto = createSendBoleta(companyDto, paymentVoucherDto);

            resultado.put(ConstantesParameter.PARAM_BEAN_SEND_BOLETA, sendBoletaDto);
            status = true;
        } catch (TemplateException | SignedException e) {
            messageResponse = "Error al generar plantilla del documento[" + paymentVoucher.getIdentificadorDocumento() + "] " + e.getMessage();
        } catch (Exception e) {
            messageResponse = e.getMessage();
        }
        if(!status) {
            throw new ServiceException(messageResponse);
        }
        response = createResponsePse(messageResponse, status, comprobanteCreado);

        resultado.put("idPaymentVoucher", comprobanteCreado.getIdPaymentVoucher());
        resultado.put(ConstantesParameter.PARAM_BEAN_RESPONSE_PSE, response);

        validateAutomaticDelivery((SendBoletaDto) resultado.get(ConstantesParameter.PARAM_BEAN_SEND_BOLETA));
        return resultado;
    }

    private void validateAutomaticDelivery(SendBoletaDto sendBoletaDto) {
        System.out.println("SEND BOLETA DTO: "+sendBoletaDto);
        if(sendBoletaDto!=null && sendBoletaDto.getEnvioDirecto()) {
            ResponsePSE responsePSE;
            try {
                responsePSE = documentsSummaryService.generarSummaryByFechaEmisionAndRuc(
                        sendBoletaDto.getRuc(),
                        sendBoletaDto.getFechaEmision(),
                        sendBoletaDto.getNameDocument(),
                        sendBoletaDto.getUser()
                );
                if (responsePSE.getEstado()) {
                    System.out.println("Se envio boleta por resumen diario de manera automatica");
                    //messageProducer.produceProcessSummary(responsePSE.getTicket(), sendBoletaDto.getRuc());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void transformarUrlsAResponse(ResponsePSE response, PaymentVoucherDto paymentVoucherDto) {
        if (paymentVoucherDto != null) {
            String urlTicket = urlServiceDownload + "descargapdfuuid/" + paymentVoucherDto.getIdPaymentVoucher() + "/" + paymentVoucherDto.getUuid() + "/ticket/" + paymentVoucherDto.getIdentificadorDocumento();
            String urlA4 = urlServiceDownload + "descargapdfuuid/" + paymentVoucherDto.getIdPaymentVoucher() + "/" + paymentVoucherDto.getUuid() + "/a4/" + paymentVoucherDto.getIdentificadorDocumento();
            String urlXml = urlServiceDownload + "descargaxmluuid/" + paymentVoucherDto.getIdPaymentVoucher() + "/" + paymentVoucherDto.getUuid() + "/" + paymentVoucherDto.getIdentificadorDocumento();
            response.setUrlPdfTicket(urlTicket);
            response.setUrlPdfA4(urlA4);
            response.setUrlXml(urlXml);
            response.setCodigoHash(paymentVoucherDto.getCodigoHash());
        }
    }

    private ResponsePSE createResponsePse(
            String messageResponse, boolean status,
            PaymentVoucherDto paymentVoucherModel) {
        ResponsePSE response = ResponsePSE.builder()
                .mensaje(messageResponse)
                .estado(status)
                .nombre(paymentVoucherModel.getIdentificadorDocumento())
                .build();
        transformarUrlsAResponse(response, paymentVoucherModel);
        return response;
    }

    private SendBoletaDto createSendBoleta(CompanyDto companyDto, PaymentVoucherDto paymentVoucherDto) {
        System.out.println("COMPANY: "+companyDto);
        if(companyDto.getEnvioAutomaticoSunat()!= null && companyDto.getEnvioAutomaticoSunat()) {
            IdentificadorComprobante identificadorComprobante = IdentificadorComprobante.builder()
                    .tipo(paymentVoucherDto.getTipoComprobante())
                    .serie(paymentVoucherDto.getSerie())
                    .numero(paymentVoucherDto.getNumero())
                    .build();
            SendBoletaDto sendBoletaDto = SendBoletaDto.builder()
                    .ruc(paymentVoucherDto.getRucEmisor())
                    .fechaEmision(paymentVoucherDto.getFechaEmision())
                    .nameDocument(identificadorComprobante)
                    .user(ConstantesParameter.USER_API_SCHEDULER)
                    .envioDirecto(companyDto.getEnvioAutomaticoSunat() != null && companyDto.getEnvioAutomaticoSunat())
                    .build();
            System.out.println("sendboleta: "+sendBoletaDto);
            return sendBoletaDto;
        } else {
            return null;
        }
    }

    private PaymentVoucherDto saveVoucher(PaymentVoucherDto paymentVoucherDto, Long idRegisterFile, String nombreUsuario) {
        paymentVoucherDto.setIdentificadorDocumento(paymentVoucherDto.getRucEmisor()+ "-" +paymentVoucherDto.getTipoComprobante()+ "-" +
                paymentVoucherDto.getSerie()+ "-" +paymentVoucherDto.getNumero());
        paymentVoucherDto.setEstado(EstadoComprobanteEnum.REGISTRADO.getCodigo());
        paymentVoucherDto.setEstadoAnterior(EstadoComprobanteEnum.REGISTRADO.getCodigo());
        paymentVoucherDto.setEstadoItem(ConstantesParameter.STATE_ITEM_PENDIENTE_ADICION);
        paymentVoucherDto.setEstadoSunat(EstadoSunatEnum.NO_ENVIADO.getAbreviado());
        paymentVoucherDto.setMensajeRespuesta(ConstantesParameter.MSG_REGISTRO_DOCUMENTO_OK);
        paymentVoucherDto.setFechaRegistro(new Timestamp(Calendar.getInstance().getTime().getTime()));
        paymentVoucherDto.setUserName(nombreUsuario);
        paymentVoucherDto.setFechaModificacion(null);
        paymentVoucherDto.setUserNameModificacion(nombreUsuario);

        if (idRegisterFile != null) {
            paymentVoucherDto.addPaymentVoucherFile(PaymentVoucherFileDto.builder()
                    .orden(1)
                    .estadoArchivo(EstadoArchivoEnum.ACTIVO.name())
                    .idRegisterFileSend(idRegisterFile)
                    .tipoArchivo(TipoArchivoEnum.XML.name())
                    .build());
        }

        for (DetailsPaymentVoucherDto item : paymentVoucherDto.getItems()) {
            item.setEstado(ConstantesParameter.REGISTRO_ACTIVO);
        }

        if (nombreUsuario != null && paymentVoucherDto.getOficinaId() == null) {
            if (!nombreUsuario.equals(ConstantesSunat.SUPERADMIN)) {
                UserDto user = userFeign.findUserByUsername(nombreUsuario);
                paymentVoucherDto.setOficinaId(user.getIdOficina());
            }
        }

        paymentVoucherDto.setUuid(UUIDGen.generate());
        paymentVoucherDto.setFechaEmisionDate(new Date());
        paymentVoucherDto.setCuentaFinancieraBeneficiario("");

        return paymentVoucherFeign.save(paymentVoucherDto);
    }

    private PaymentVoucherDto updateVoucher(PaymentVoucherDto paymentVoucherDto, PaymentVoucherDto paymentVoucherDtoOld, Long idRegisterFile, String nombreUsuario) {
        return null;
    }

    private RegisterFileUploadDto subirXmlComprobante(CompanyDto companyDto, Map<String, String> plantillaGenerado) {
        String nombreDocumento = plantillaGenerado.get(ConstantesParameter.PARAM_NAME_DOCUMENT);
        String fileXMLZipBase64 = plantillaGenerado.get(ConstantesParameter.PARAM_FILE_ZIP_BASE64);
        RegisterFileUploadDto archivo = amazonS3ClientService.uploadFileStorage(UtilArchivo.b64ToByteArrayInputStream(fileXMLZipBase64),
                nombreDocumento, "invoice", companyDto);
        log.info("ARVHIVO SUBIDO: {}", archivo.toString());
        return archivo;
    }

    private Map<String, String> generarPlantillaXml(CompanyDto companyDto, PaymentVoucherDto paymentVoucherDto) throws IOException, NoSuchAlgorithmException {
        System.out.println("GENERANDO PLANTILLA");
        Map<String, String> plantillaGenerado = new HashMap<>();
        if (companyDto.getOseId() != null && companyDto.getOseId() == 1) {
            plantillaGenerado = templateService.buildPaymentVoucherSignOse(paymentVoucherDto);
        } else if (companyDto.getOseId() != null && companyDto.getOseId() == 2) {
            plantillaGenerado = templateService.buildPaymentVoucherSignOseBliz(paymentVoucherDto);
        } else if (companyDto.getOseId() != null && (companyDto.getOseId() == 10 || companyDto.getOseId() == 12)) {
            plantillaGenerado = templateService.buildPaymentVoucherSignCerti(paymentVoucherDto);
        } else {
            plantillaGenerado = templateService.buildPaymentVoucherSign(paymentVoucherDto);
        }
        log.info("PLANTILLA GENERADA: {}", plantillaGenerado.get(ConstantesParameter.PARAM_FILE_XML_BASE64));
        return plantillaGenerado;
    }

    private Integer getProximoNumero(String tipoDocumento, String serie, String ruc) {
        Integer ultimoComprobante = paymentVoucherFeign.obtenerSiguienteNumeracionPorTipoComprobanteYSerieYRucEmisor(tipoDocumento, serie, ruc);
        if (ultimoComprobante != null) {
            return ultimoComprobante + 1;
        } else {
            return 1;
        }
    }

    private void setLeyenda(PaymentVoucherDto paymentVoucherModel) {
        if(paymentVoucherModel.getCodigoTipoOperacion() != null) {
            if (paymentVoucherModel.getCodigoTipoOperacion().equals("1001") || paymentVoucherModel.getCodigoTipoOperacion().equals("1002") ||
                    paymentVoucherModel.getCodigoTipoOperacion().equals("1003") || paymentVoucherModel.getCodigoTipoOperacion().equals("1004")) {
                LeyendaDto leyendaDto = LeyendaDto.builder()
                        .descripcion("Operación sujeta al Sistema de Pago de Obligaciones Tributarias con el Gobierno Central")
                        .codigo("2006")
                        .build();
                paymentVoucherModel.setLeyendas(new ArrayList<>());
                paymentVoucherModel.getLeyendas().add(leyendaDto);
            }
        }
    }

    private void setOficinaId(PaymentVoucherDto comprobante, CompanyDto companyModel) {
        if (Boolean.TRUE.equals(companyModel.getAllowSaveOficina()) && comprobante.getOficinaId() == null) {
            try {
                BranchOfficesDto branchOfficesModel = branchOfficeFeign.obtenerOficinaPorEmpresaIdYSerieYTipoComprobante(
                        companyModel.getId(), comprobante.getSerie(), comprobante.getTipoComprobante());
                System.out.println("ID OFICINA: "+branchOfficesModel);
                if(branchOfficesModel !=null) {
                    if (branchOfficesModel.getId() != null) {
                        comprobante.setOficinaId(branchOfficesModel.getId());
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private static final Map<String, String> OPERACION_MAP = Map.of(
            "01", "0101",
            "02", "0200",
            "04", "0502"
    );

    private void setCodigoTipoOperacionCatalog(PaymentVoucherDto paymentVoucher) {
        String codigo = paymentVoucher.getCodigoTipoOperacion();
        if (codigo != null) {
            codigo = codigo.trim();
            if (codigo.length() == 4) {
                paymentVoucher.setCodigoTipoOperacionCatalogo51(codigo);
            } else {
                paymentVoucher.setCodigoTipoOperacionCatalogo51(
                        OPERACION_MAP.getOrDefault(codigo, "0101")
                );
            }
        } else {
            paymentVoucher.setCodigoTipoOperacionCatalogo51("0101");
        }
    }

    private CompanyDto completarDatosEmisor(PaymentVoucherDto paymentVoucher) {
        if (paymentVoucher.getRucEmisor() == null) {
            throw new IllegalArgumentException("El RUC del emisor no puede ser nulo");
        }
        CompanyDto companyDto = companyFeign.findCompanyByRuc(paymentVoucher.getRucEmisor());
        if (companyDto == null) {
            throw new ServiceException("No se encontró la empresa con RUC: " + paymentVoucher.getRucEmisor());
        }
        paymentVoucher.setRucEmisor(companyDto.getRuc());
        paymentVoucher.setDenominacionEmisor(companyDto.getRazon());
        paymentVoucher.setTipoDocumentoEmisor(ConstantesSunat.TIPO_DOCUMENTO_IDENTIDAD_RUC);
        paymentVoucher.setNombreComercialEmisor(companyDto.getNombreComer());
        paymentVoucher.setUblVersion(companyDto.getUblVersion() != null ? companyDto.getUblVersion() : ConstantesSunat.UBL_VERSION_2_0);
        return companyDto;
    }

}
