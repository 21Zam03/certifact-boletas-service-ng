package com.certicom.certifact_boletas_service_ng.validation;

import com.certicom.certifact_boletas_service_ng.deserializer.InputField;
import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.exception.ValidationException;
import com.certicom.certifact_boletas_service_ng.feign.rest.CompanyRestService;
import com.certicom.certifact_boletas_service_ng.feign.rest.PaymentVoucherRestService;
import com.certicom.certifact_boletas_service_ng.request.AnticipoPaymentRequest;
import com.certicom.certifact_boletas_service_ng.request.DetailsPaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.util.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentVoucherValidator extends InputField<Object> {

    //private final PaymentVoucherFeign paymentVoucherFeign;
    //private final CompanyFeign companyFeign;

    private final PaymentVoucherRestService paymentVoucherRestService;
    private final CompanyRestService companyRestService;

    private final PaymentVoucherDetailValidator paymentVoucherDetailValidator;
    private final AnticipoValidator anticipoValidator;

    @Value("${urlspublicas.descargaComprobante}")
    private String urlServiceDownload;

    public void validate(PaymentVoucherRequest paymentVoucherRequest, boolean isEdit) {
        boolean datosReceptorObligatorio;
        validateRucAtivo(paymentVoucherRequest.getRucEmisor());
        validateTipoComprobante(paymentVoucherRequest.getTipoComprobante());
        if (isNotaCreditoODebito(paymentVoucherRequest.getTipoComprobante())) {
            validateCamposNotaCreditoDebito(paymentVoucherRequest);
        } else {
            validateTipoDocumentoReceptorBoleta(paymentVoucherRequest.getTipoDocumentoReceptor());
        }
        validateSerie(paymentVoucherRequest.getSerie(), paymentVoucherRequest.getTipoComprobante(), paymentVoucherRequest.getTipoComprobanteAfectado());
        validateNumero(paymentVoucherRequest.getNumero());
        validateNumeracion(paymentVoucherRequest.getTipoComprobante(), paymentVoucherRequest.getSerie(), paymentVoucherRequest.getRucEmisor(), paymentVoucherRequest.getNumero());
        validateIdentificadorDocumento(paymentVoucherRequest.getRucEmisor(), paymentVoucherRequest.getTipoComprobante(), paymentVoucherRequest.getSerie(), paymentVoucherRequest.getNumero(), isEdit);
        validateFechaEmision(paymentVoucherRequest.getFechaEmision());
        validateHoraEmision(paymentVoucherRequest.getHoraEmision(), paymentVoucherRequest.getFechaEmision());
        validateTipoMoneda(paymentVoucherRequest.getCodigoMoneda());
        validateImporteTotal(paymentVoucherRequest.getImporteTotalVenta());
        datosReceptorObligatorio = validateObligatoriedadDatosCliente(paymentVoucherRequest.getTipoComprobante(),
                paymentVoucherRequest.getTipoComprobanteAfectado(), paymentVoucherRequest.getImporteTotalVenta());
        validateTipoDocumentoReceptor(paymentVoucherRequest.getTipoDocumentoReceptor(), datosReceptorObligatorio);
        validateNumeroDocumentoReceptor(paymentVoucherRequest.getNumeroDocumentoReceptor(),
                paymentVoucherRequest.getTipoDocumentoReceptor(), datosReceptorObligatorio);
        validateDenominacionReceptor(paymentVoucherRequest.getDenominacionReceptor(), datosReceptorObligatorio);
        validateDomicilioFiscalEmisor(paymentVoucherRequest.getCodigoLocalAnexoEmisor());
        validateTipoDocumentoRelacionado(paymentVoucherRequest.getCodigoTipoOtroDocumentoRelacionado());
        validateNumeroDocumentoRelacionado(paymentVoucherRequest.getSerieNumeroOtroDocumentoRelacionado(),
                paymentVoucherRequest.getCodigoTipoOtroDocumentoRelacionado());
        validateAnticipos(paymentVoucherRequest.getAnticipos());
        validateItems(paymentVoucherRequest.getItems(), paymentVoucherRequest.getTipoComprobante(), paymentVoucherRequest.getUblVersion(), paymentVoucherRequest.getRucEmisor());
        if(paymentVoucherRequest.getUblVersion().equals(ConstantesSunat.UBL_VERSION_2_1)) {
            validateTotalIsc(paymentVoucherRequest.getTotalValorBaseIsc(), paymentVoucherRequest.getTotalIsc(), paymentVoucherRequest.getItems());
            validateTotalGratuita(paymentVoucherRequest.getTotalValorVentaGratuita(), paymentVoucherRequest.getTotalImpOperGratuita(), paymentVoucherRequest.getItems());
            validateTotalGravada(paymentVoucherRequest.getTotalValorVentaGravada(), paymentVoucherRequest.getTotalIgv(), paymentVoucherRequest.getItems());
            validateTotalOtrosTributos(paymentVoucherRequest.getTotalValorBaseOtrosTributos(), paymentVoucherRequest.getTotalOtrostributos(), paymentVoucherRequest.getItems());
        }
        validateDetracciones(paymentVoucherRequest);
    }

    private void validateRucAtivo(String rucEmisor) {
        try {
            String estado = companyRestService.getStateFromCompanyByRuc(rucEmisor);
            if(!estado.equals(ConstantesParameter.REGISTRO_ACTIVO)) {
                throw new ValidationException(
                        "El ruc emisor [" + rucEmisor + "] No se encuentra habilitado para ejecutar operaciones al API-REST."
                );
            }
        } catch (FeignException fe) {
            throw new ValidationException(
                    "No se pudo validar el estado del RUC emisor [" + rucEmisor + "]. Error al comunicarse con el servicio de validación: " + fe.getMessage()
            );
        } catch (Exception e) {
            throw new ValidationException(
                    "Error inesperado al validar el estado del RUC emisor [" + rucEmisor + "]: " + e.getMessage()
            );
        }
    }

    private void validateTipoComprobante(String tipoComprobante) {
        if (StringUtils.isBlank(tipoComprobante)) {
            throw new ValidationException("El campo [" + tipoComprobanteLabel + "] es obligatorio.");
        }
        if(!tipoComprobante.equals(ConstantesSunat.TIPO_DOCUMENTO_BOLETA) &&
                !tipoComprobante.equals(ConstantesSunat.TIPO_DOCUMENTO_NOTA_CREDITO) &&
                !tipoComprobante.equals(ConstantesSunat.TIPO_DOCUMENTO_NOTA_DEBITO)) {
            throw new ValidationException(
                    "El campo ["+tipoComprobanteLabel+"] contiene un valor no valido o no es de tipo boleta, nota de credito, nota de debito - ["+tipoComprobante+"]."
            );
        }
    }

    private boolean isNotaCreditoODebito(String tipoComprobante) {
        return ConstantesSunat.TIPO_DOCUMENTO_NOTA_CREDITO.equals(tipoComprobante)
                || ConstantesSunat.TIPO_DOCUMENTO_NOTA_DEBITO.equals(tipoComprobante);
    }

    private void validateCamposNotaCreditoDebito(PaymentVoucherRequest paymentVoucherModel) {
        validateTipoComprobanteAfectado(paymentVoucherModel.getTipoComprobanteAfectado());
        validateSerieAfectado(paymentVoucherModel.getSerieAfectado());
        validateNumeroAfectado(paymentVoucherModel.getNumeroAfectado());
        validateCodigoTipoNotaCredito(paymentVoucherModel.getTipoComprobante(),
                paymentVoucherModel.getCodigoTipoNotaCredito());
        validateCodigoTipoNotaDebito(paymentVoucherModel.getTipoComprobante(),
                paymentVoucherModel.getCodigoTipoNotaDebito());
        validateMotivoNota(paymentVoucherModel.getMotivoNota());
    }

    private void validateTipoComprobanteAfectado(String tipoComprobanteAfectado) {
        if (StringUtils.isBlank(tipoComprobanteAfectado)) {
            throw new ValidationException("El campo [" + tipoComprobanteAfectadoLabel + "] es obligatorio.");
        }
        if (!StringUtils.isNumeric(tipoComprobanteAfectado)) {
            throw new ValidationException("El campo [" + tipoComprobanteAfectadoLabel + "] debe tener "
                    + "caracteres numericos.");
        }
        if (!tipoComprobanteAfectado.equals(ConstantesSunat.TIPO_DOCUMENTO_BOLETA)) {
            throw new ValidationException(
                    "El campo [" + tipoComprobanteAfectadoLabel + "] debe corresponder al siguiente comprobante: 03 Boleta"
            );
        }
    }

    private void validateSerieAfectado(String serieAfectado) {
        if (StringUtils.isBlank(serieAfectado)) {
            throw new ValidationException("El campo [" + serieAfectadoLabel + "] es obligatorio.");
        }
        if (!StringUtils.isAlphanumeric(serieAfectado)) {
            throw new ValidationException("El campo [" + serieAfectadoLabel + "] recibe caracteres alfabeticos y numericos.");
        }
    }

    private void validateNumeroAfectado(Integer numeroAfectado) {
        if (numeroAfectado == null) {
            throw new ValidationException("El campo [" + numeroAfectadoLabel + "] es obligatorio.");
        }
        if (numeroAfectado < 1) {
            throw new ValidationException("El campo [" + numeroAfectadoLabel + "] debe ser mayor que cero.");
        }
    }

    private void validateCodigoTipoNotaCredito(String tipoComprobante, String tipoNotaCredito)  {
        if (tipoComprobante.equals(ConstantesSunat.TIPO_DOCUMENTO_NOTA_CREDITO)) {
            if (StringUtils.isBlank(tipoNotaCredito)) {
                throw new ValidationException("El campo [" + tipoNotaCreditoLabel + "] es obligatorio.");
            }
            if (!StringUtils.isNumeric(tipoNotaCredito)) {
                throw new ValidationException("El campo [" + tipoNotaCreditoLabel + "] debe tener "
                        + "caracteres numericos.");
            }
            if (StringUtils.length(tipoNotaCredito) != 2) {
                throw new ValidationException("El campo [" + tipoNotaCreditoLabel + "] debe tener 2 caracteres numericos.");
            }
        }
    }

    private void validateCodigoTipoNotaDebito(String tipoComprobante, String tipoNotaDebito) {
        if (tipoComprobante.equals(ConstantesSunat.TIPO_DOCUMENTO_NOTA_DEBITO)) {
            if (StringUtils.isBlank(tipoNotaDebito)) {
                throw new ValidationException("El campo [" + tipoNotaDebitoLabel + "] es obligatorio.");
            }
            if (!StringUtils.isNumeric(tipoNotaDebito)) {
                throw new ValidationException("El campo [" + tipoNotaDebitoLabel + "] debe tener "
                        + "caracteres numericos.");
            }
            if (StringUtils.length(tipoNotaDebito) != 2) {
                throw new ValidationException("El campo [" + tipoNotaDebitoLabel + "] debe tener 2 caracteres numericos.");
            }
        }
    }

    private void validateMotivoNota(String motivo) {
        if (StringUtils.isBlank(motivo)) {
            throw new ValidationException("El campo [" + motivoNotaLabel + "] es obligatorio.");
        }
        if (250 < StringUtils.length(motivo)) {
            throw new ValidationException("El campo [" + motivoNotaLabel + "] debe tener como longitud maxima de 250 caracteres.");
        }
    }

    private void validateTipoDocumentoReceptorBoleta(String tipoDocumentoReceptor) {
        if (StringUtils.isBlank(tipoDocumentoReceptor)) {
            throw new ValidationException("El campo [" + tipoDocumentoReceptorLabel + "] es obligatorio.");
        }
    }

    private void validateSerie(String serie, String tipoComprobante, String tipoComprobanteAfectado) {
        if (StringUtils.isBlank(serie)) {
            throw new ValidationException("El campo [" + serieLabel + "] es obligatorio.");
        }
        if (!StringUtils.isAlphanumeric(serie)) {
            throw new ValidationException("El campo [" + serieLabel + "] recibe caracteres del alfabeto y números.");
        }
        if (StringUtils.length(serie) != 4) {
            throw new ValidationException("El campo [" + serieLabel + "] debe ser alfanumerico de 4 caracteres.");
        }

        switch (tipoComprobante) {
            case ConstantesSunat.TIPO_DOCUMENTO_BOLETA:
                validateSerieBoleta(serie);
                break;
            case ConstantesSunat.TIPO_DOCUMENTO_NOTA_CREDITO:
            case ConstantesSunat.TIPO_DOCUMENTO_NOTA_DEBITO:
                if (tipoComprobanteAfectado.equals(ConstantesSunat.TIPO_DOCUMENTO_BOLETA)) {
                    validateSerieBoleta(serie);
                }
        }
    }

    private void validateSerieBoleta(String serie) {
        String primeraLetra;
        primeraLetra = StringUtils.substring(serie, 0, 1).toUpperCase();
        if (!primeraLetra.equals("B") && !StringUtils.isNumeric(serie)) {
            throw new ValidationException("El campo [" + serieLabel + "] puede ser númerico ó empezar con el caracter B.");
        }
    }

    private void validateNumero(Integer numero) {
        if (numero == null) {
            throw new ValidationException("El campo [" + numeroLabel + "] es obligatorio.");
        }
        if (numero < 1) {
            throw new ValidationException("El campo [" + numeroLabel + "] debe ser mayor que cero.");
        }
        if (StringUtils.length(numero.toString()) > 8) {
            throw new ValidationException("El campo [" + numeroLabel + "] debe tener como maximo 8 digitos.");
        }
    }

    private void validateNumeracion(String tipoComprobante, String serie, String rucEmisor, Integer numero) {
        try {
            int proximo = paymentVoucherRestService.
                    obtenerSiguienteNumeracionPorTipoComprobanteYSerieYRucEmisor(tipoComprobante, serie, rucEmisor);
            if (proximo > 1){
                int diferencia = numero - proximo;
                if (diferencia > 120){
                    LogHelper.warnLog(LogTitle.WARN_VALIDATION.getType(),
                            LogMessages.currentMethod(), "El numero [" + numero + "] difiere de su antecesor en " + proximo + " posiciones.");
                    throw new ValidationException(
                            "El numero [" + numero + "] difiere de su antecesor en " + proximo + " posiciones."
                    );
                }
            }
        } catch (FeignException fe) {
            LogHelper.errorLog(LogTitle.ERROR_HTTP.getType(), LogMessages.currentMethod(),
                    "Error al comunicarse con el servicio externo ", fe);
            throw new ServiceException(LogMessages.ERROR_HTTP, fe);
        } catch (Exception e) {
            LogHelper.errorLog(LogTitle.ERROR_UNEXPECTED.getType(), LogMessages.currentMethod(), "Ocurrio un error inesperado", e);
            throw new ServiceException(LogMessages.ERROR_UNEXPECTED, e);
        }
    }

    private void validateIdentificadorDocumento(String rucEmisor, String tipoComprobante, String serie, Integer numero, boolean isEdit) {
        try {
            String idDocumento = rucEmisor + "-" + tipoComprobante + "-" + serie + "-" + numero;
            PaymentVoucherDto identificadorEntity = paymentVoucherRestService.getPaymentVoucherByIdentificadorDocumento(idDocumento);
            if (identificadorEntity != null && !isEdit) {
                throw new ValidationException(
                        "El comprobante ya ha sido registrado [" + rucEmisorLabel + ":" + rucEmisor + "; "
                                + tipoComprobanteLabel + ":" + tipoComprobante + "; " + serieLabel + ":" + serie + "; " + numeroLabel + ":"
                                + numero + "; " + fechaEmisionLabel + ":" + identificadorEntity.getFechaEmision() + "; fecha_registro:"
                                + identificadorEntity.getFechaRegistro() + "]|"+urlServiceDownload + "descargapdfuuid/" + identificadorEntity.getIdPaymentVoucher() + "/"
                                + identificadorEntity.getUuid() + "/a4/" + identificadorEntity.getIdentificadorDocumento()+"|"
                                +urlServiceDownload + "descargapdfuuid/" + identificadorEntity.getIdPaymentVoucher() + "/" +
                                identificadorEntity.getUuid() + "/ticket/" + identificadorEntity.getIdentificadorDocumento()+"|"
                                +urlServiceDownload + "descargaxmluuid/" + identificadorEntity.getIdPaymentVoucher() + "/" + identificadorEntity.getUuid() + "/" +
                                identificadorEntity.getIdentificadorDocumento()+"|"+identificadorEntity.getCodigoHash()
                );
            }
        } catch (FeignException fe) {
            throw new ValidationException("No se pudo validar si el comprobante ya ha sido registrado. Error al comunicarse con el servicio de validación: " + fe.getMessage());
        }
    }

    private void validateFechaEmision(String fechaEmision) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date fechaEmisionDate;

        if (StringUtils.isBlank(fechaEmision)) {
            throw new ValidationException("El campo [" + fechaEmisionLabel + "] es obligatorio.");
        }
        if (UtilFormat.fechaDate(fechaEmision) == null) {
            throw new ValidationException("El campo [" + fechaEmisionLabel + "] debe tener el formato yyyy-MM-dd");
        }
        try {
            fechaEmisionDate = formatter.parse(fechaEmision);
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -8);
            if (calendar.getTime().compareTo(fechaEmisionDate) > 0) {
                throw new ValidationException("El campo [" + fechaEmisionLabel + "] no debe tener más de 7 días de anterioridad");
            }
        } catch (ParseException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    private void validateHoraEmision(String horaEmision, String fechaEmision) {
        if (StringUtils.isBlank(horaEmision)) {
            throw new ValidationException("El campo [" + horaEmisionLabel + "] es obligatorio.");
        }
        if (StringUtils.isBlank(UtilFormat.hora(fechaEmision + " " + horaEmision))) {
            throw new ValidationException("El campo [" + horaEmisionLabel + "] debe tener el formato hh:mm:ss");
        }
    }

    private void validateTipoMoneda(String tipoMoneda) {
        if (StringUtils.isBlank(tipoMoneda)) {
            throw new ValidationException("El campo [" + codigoMonedaLabel + "] es obligatorio.");
        }
        if (!StringUtils.isAlphanumeric(tipoMoneda)) {
            throw new ValidationException("El campo [" + codigoMonedaLabel + "] es alfanumerico.");
        }
        if (StringUtils.length(tipoMoneda) != 3) {
            throw new ValidationException("El campo [" + codigoMonedaLabel + "] debe tener 3 caracteres.");
        }
    }

    private void validateImporteTotal(BigDecimal importeTotal) {
        if (importeTotal == null) {
            throw new ValidationException("El campo [" + importeTotalLabel + "] es obligatorio.");
        }
    }

    private boolean validateObligatoriedadDatosCliente(String tipoComprobante, String tipoComprobanteAfectado, BigDecimal importeTotalVenta) {
        return true;
    }

    private void validateTipoDocumentoReceptor(String tipoDocumentoReceptor, boolean datosReceptorObligatorio) {
        if (StringUtils.isBlank(tipoDocumentoReceptor) && datosReceptorObligatorio) {
            throw new ValidationException("El campo [" + tipoDocumentoReceptorLabel + "] es obligatorio.");
        }
        if (StringUtils.isNotBlank(tipoDocumentoReceptor) && !StringUtils.isAlphanumeric(tipoDocumentoReceptor)) {
            throw new ValidationException("El campo [" + tipoDocumentoReceptorLabel + "] debe ser alfanumerico.");
        }
        if (StringUtils.isNotBlank(tipoDocumentoReceptor) && StringUtils.length(tipoDocumentoReceptor) > 1) {
            throw new ValidationException("El campo [" + tipoDocumentoReceptorLabel + "] debe tener un solo caracter.");
        }
    }

    private void validateNumeroDocumentoReceptor(String numeroDocumentoReceptor, String tipoDocumentoReceptor, boolean datosReceptorObligatorio) {
        if (datosReceptorObligatorio) {
            if (StringUtils.isBlank(numeroDocumentoReceptor)) {
                throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] es obligatorio.");
            }
        } else {
            if (StringUtils.isNotBlank(numeroDocumentoReceptor) || StringUtils.isNotBlank(tipoDocumentoReceptor)) {
                if (StringUtils.isBlank(numeroDocumentoReceptor)) {
                    throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] es obligatorio, si ingresa el campo [" + tipoDocumentoReceptorLabel + "]");
                }
                if (StringUtils.isBlank(tipoDocumentoReceptor)) {
                    throw new ValidationException("El campo [" + tipoDocumentoReceptorLabel + "] es obligatorio, si ingresa el campo [" + numeroDocumentoReceptorLabel + "]");
                }
            } else {
                return;
            }
        }

        switch (tipoDocumentoReceptor) {
            case ConstantesSunat.TIPO_DOCUMENTO_IDENTIDAD_DNI:
                if (!StringUtils.isNumeric(numeroDocumentoReceptor)) {
                    throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] debe contener solo digitos numericos.");
                }
                if (StringUtils.length(numeroDocumentoReceptor) != 8) {
                    throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] debe tener 8 digitos.");
                }
                break;
            case ConstantesSunat.TIPO_DOCUMENTO_IDENTIDAD_RUC:
                if (!StringUtils.isNumeric(numeroDocumentoReceptor)) {
                    throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] debe contener solo digitos numericos.");
                }
                if (StringUtils.length(numeroDocumentoReceptor) != 11) {
                    throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] debe tener 11 digitos.");
                }
                break;
            default:
                if (!tipoDocumentoReceptor.equals(ConstantesSunat.TIPO_DOCUMENTO_NO_DOMI_SIN_RUC)) {
                    if (!StringUtils.isAlphanumeric(numeroDocumentoReceptor)) {
                        throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] debe contener digitos alfanumericos.");
                    }
                    if (StringUtils.length(numeroDocumentoReceptor) > 15) {
                        throw new ValidationException("El campo [" + numeroDocumentoReceptorLabel + "] debe tener 15 caracteres como maximo.");
                    }
                }
        }
    }

    private void validateDenominacionReceptor(String denominacionReceptor, boolean datosReceptorObligatorio) {
        if (datosReceptorObligatorio) {
            if (StringUtils.isBlank(denominacionReceptor)) {
                throw new ValidationException("El campo [" + denominacionReceptorLabel + "] es obligatorio.");
            }
        }
        if (StringUtils.isNotBlank(denominacionReceptor) && StringUtils.length(denominacionReceptor) > 1500) {
            throw new ValidationException("El campo [" + denominacionReceptorLabel + "] debe tener un maximo de 1500 caracteres.");
        }
    }

    private void validateDomicilioFiscalEmisor(String codigoDomicilioFiscal) {
        if (StringUtils.isNotBlank(codigoDomicilioFiscal)) {
            if (!StringUtils.isAlphanumeric(codigoDomicilioFiscal)) {
                throw new ValidationException("El campo [" + codigoLocalAnexoEmisorLabel + "] debe ser alfanumerico.");
            }
            if (StringUtils.length(codigoDomicilioFiscal) != 4) {
                throw new ValidationException("El campo [" + codigoLocalAnexoEmisorLabel + "] debe tener 4 caracteres.");
            }
        }
    }

    private void validateTipoDocumentoRelacionado(String tipoDocumentoRelacionado) {
        if (StringUtils.isNotBlank(tipoDocumentoRelacionado)) {
            if (!StringUtils.isAlphanumeric(tipoDocumentoRelacionado)) {
                throw new ValidationException("El campo [" + tipoDocumentoRelacionadoLabel + "] debe ser alfanumerico.");
            }
            if (StringUtils.length(StringUtils.trim(tipoDocumentoRelacionado)) != 2) {
                throw new ValidationException("El campo [" + tipoDocumentoRelacionadoLabel + "] debe tener 2 caracteres.");
            }
        }
    }

    private void validateNumeroDocumentoRelacionado(String numeroDocumentoRelacionado, String tipoDocumentoRelacionado) {
        if (StringUtils.isNotBlank(numeroDocumentoRelacionado)) {
            if (StringUtils.isBlank(tipoDocumentoRelacionado)) {
                throw new ValidationException("El campo [" + tipoDocumentoRelacionadoLabel + "] es obligatorio, en el caso de ingresar "
                        + "el campo [" + numeroDocumentoRelacionadoLabel + "]");
            }
            if (StringUtils.length(numeroDocumentoRelacionado) > 30) {
                throw new ValidationException("El campo [" + numeroDocumentoRelacionadoLabel + "] debe a lo mas 30 caracteres.");
            }
        } else {
            if (StringUtils.isNotBlank(tipoDocumentoRelacionado)) {
                throw new ValidationException("Se ha ingresado el campo [" + tipoDocumentoRelacionadoLabel + "] por lo cual tambien "
                        + "debe ingresar el campo [" + numeroDocumentoRelacionadoLabel + "]");
            }
        }
    }

    private void validateAnticipos(List<AnticipoPaymentRequest> anticipos) {
        if (anticipos != null && !anticipos.isEmpty()) {
            for (AnticipoPaymentRequest anticipo : anticipos) {
                anticipoValidator.validateAnticipo(anticipo);
            }
        }
    }

    private void validateItems(List<DetailsPaymentVoucherRequest> items, String tipoComprobante, String ublVersion, String ruc) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("El campo [" + itemsLabel + "] es obligatorio, debe contener al menos un item.");
        }
        for (DetailsPaymentVoucherRequest item : items) {
            paymentVoucherDetailValidator.validate(item, tipoComprobante, ublVersion, ruc);
        }
    }

    private void validateTotalIsc(BigDecimal montoBaseIsc, BigDecimal montoIsc, List<DetailsPaymentVoucherRequest> items) {
        boolean existeAlmenosUno = false;
        if (montoBaseIsc != null || montoIsc != null) {
            if (montoBaseIsc == null) {
                throw new ValidationException("El campo [" + totalValorBaseIscLabel + "] es obligatorio, si ingresa valor en [" + totalIscLabel + "]");
            }
            if (montoIsc == null) {
                throw new ValidationException("El campo [" + totalIscLabel + "] es obligatorio, si ingresa valor en [" + totalValorBaseIscLabel + "]");
            }
            for (DetailsPaymentVoucherRequest line : items) {
                existeAlmenosUno = paymentVoucherDetailValidator.validateOperacionISC(line.getMontoBaseIsc(), line.getIsc(), line.getPorcentajeIsc(), line.getCodigoTipoCalculoISC());
                if (existeAlmenosUno) {
                    break;
                }
            }
            if (!existeAlmenosUno) {
                throw new ValidationException("Debe ingresar al menos un item con datos del tributo ISC.");
            }
        }
    }

    private void validateTotalGratuita(BigDecimal montoBaseGratuita, BigDecimal montoGratuita, List<DetailsPaymentVoucherRequest> items) {
        boolean existeAlmenosUno = false;
        boolean existeProductoGratuito = false;
        if (montoBaseGratuita != null || montoGratuita != null) {
            if(montoBaseGratuita.doubleValue() > 0){
                if (montoBaseGratuita == null) {
                    throw new ValidationException("El campo [" + totalValorVentaGratuitaLabel + "] es obligatorio, si ingresa valor en [" + totalImpOperGratuitaLabel + "]");
                }
                for (DetailsPaymentVoucherRequest line : items) {
                    existeProductoGratuito = false;
                    if(line.getMontoBaseGratuito() != null) {
                        existeProductoGratuito = paymentVoucherDetailValidator.validateOperacionGratuita(line.getMontoBaseGratuito(), line.getImpuestoVentaGratuita(), line.getPorcentajeTributoVentaGratuita(), line.getValorReferencialUnitario());
                    }
                    if (existeProductoGratuito) {
                        existeAlmenosUno = true;
                        line.setValorUnitario(BigDecimal.ZERO);
                    }
                }
                if (!existeAlmenosUno) {
                    throw new ValidationException("Debe ingresar al menos un item con datos de venta gratuita.");
                }
            }

        }
    }

    private void validateTotalGravada(BigDecimal montoBaseGravada, BigDecimal montoIgv, List<DetailsPaymentVoucherRequest> items) {
        boolean existeAlmenosUno = false;
        if (montoBaseGravada != null || montoIgv != null) {
            if (montoBaseGravada == null) {
                throw new ValidationException("El campo [" + totalValorVentaGravadaLabel + "] es obligatorio, si ingresa valor en [" + totalIgvLabel + "]");
            }
            if (montoIgv == null) {
                throw new ValidationException("El campo [" + totalIgvLabel + "] es obligatorio, si ingresa valor en [" + totalValorVentaGravadaLabel + "]");
            }
            for (DetailsPaymentVoucherRequest line : items) {
                // Agrega lógica para contemplar el tipo de afectación IGV "30" (Inafecto - Operación Onerosa)
                if ("30".equals(line.getCodigoTipoAfectacionIGV())) {
                    existeAlmenosUno = true;
                    break;
                }
                existeAlmenosUno = paymentVoucherDetailValidator.validateOperacionGravada(
                        line.getMontoBaseIgv(),
                        line.getIgv(),
                        line.getPorcentajeIgv(),
                        line.getCodigoTipoAfectacionIGV()
                );

                if (existeAlmenosUno) {
                    break;
                }
            }
            if (!existeAlmenosUno) {
                throw new ValidationException("Debe ingresar al menos un item con datos del tributo con afectación gravada.");
            }
        }
    }

    private void validateTotalOtrosTributos(BigDecimal montoBaseOtrosTributos, BigDecimal montoOtrosTributos, List<DetailsPaymentVoucherRequest> items) {
        boolean existeAlmenosUno = false;
        if (montoBaseOtrosTributos != null || montoOtrosTributos != null) {
            if (montoBaseOtrosTributos == null) {
                throw new ValidationException("El campo [" + totalValorBaseOtrosTributosLabel + "] es obligatorio, si ingresa valor en [" + totalOtrostributosLabel + "]");
            }
            if (montoOtrosTributos == null) {
                throw new ValidationException("El campo [" + totalOtrostributosLabel + "] es obligatorio, si ingresa valor en [" + totalValorBaseOtrosTributosLabel + "]");
            }
            for (DetailsPaymentVoucherRequest line : items) {
                existeAlmenosUno = paymentVoucherDetailValidator.validateOperacionOtrosTributos(line.getMontoBaseOtrosTributos(), line.getOtrosTributos(), line.getPorcentajeOtrosTributos());
                if (existeAlmenosUno) {
                    break;
                }
            }
            if (!existeAlmenosUno) {
                throw new ValidationException("Debe ingresar al menos un item con datos de Otros tributos.");
            }
        }
    }

    private void validateDetracciones(PaymentVoucherRequest paymentVoucherModel) {
        boolean existeCodigoBien = false;

        if (paymentVoucherModel.getTipoComprobante().equals(ConstantesSunat.TIPO_DOCUMENTO_FACTURA)) {
            if (paymentVoucherModel.getCodigoBienDetraccion() != null) {
                existeCodigoBien = true;
            }
            if (existeCodigoBien) {
                if (paymentVoucherModel.getCodigoTipoOperacion().equals("1001") || paymentVoucherModel.getCodigoTipoOperacion().equals("1004")) {
                    if (StringUtils.length(StringUtils.trim(paymentVoucherModel.getCodigoBienDetraccion())) != 3) {
                        throw new ValidationException("El campo [" + codigoBienDetraccionLabel + "] debe tener 3 digitos.");
                    }
                    if (paymentVoucherModel.getCuentaFinancieraBeneficiario() == null) {
                        throw new ValidationException("El campo [" + cuentaFinancieraBeneficiarioLabel + "] es obligatorio.");
                    }
                    if (StringUtils.length(StringUtils.trim(paymentVoucherModel.getCuentaFinancieraBeneficiario())) > 100) {
                        throw new ValidationException("El campo [" + cuentaFinancieraBeneficiarioLabel
                                + "] debe tener como maximo 100 digitos.");
                    }
                    if (paymentVoucherModel.getMontoDetraccion() == null) {
                        throw new ValidationException("El campo [" + montoDetraccionLabel + "] es obligarorio.");
                    }
                    if (!StringUtils.isAlphanumeric(paymentVoucherModel.getCodigoMedioPago())) {
                        throw new ValidationException("El campo [" + codigoMedioPagoLabel
                                + "] recibe caracteres alfabeticos y numericos.");
                    }
                    if (StringUtils.length(StringUtils.trim(paymentVoucherModel.getCodigoMedioPago())) != 3) {
                        throw new ValidationException("El campo [" + codigoMedioPagoLabel + "] debe tener 3 digitos.");
                    }
                    if (paymentVoucherModel.getPorcentajeDetraccion() == null) {
                        throw new ValidationException("El campo [" + porcentajeDetraccionLabel + "] es obligarorio.");
                    }
                    if (paymentVoucherModel.getCodigoBienDetraccion().equals("027")) {
                        for (DetailsPaymentVoucherRequest item : paymentVoucherModel.getItems()) {
                            if (StringUtils.isBlank(item.getDetalleViajeDetraccion())) {
                                throw new ValidationException("El campo [" + detalleViajeDetraccionLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (StringUtils.isBlank(item.getUbigeoOrigenDetraccion())) {
                                throw new ValidationException("El campo [" + ubigeoOrigenDetraccionLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (StringUtils.isBlank(item.getDireccionOrigenDetraccion())) {
                                throw new ValidationException("El campo [" + direccionOrigenDetraccionLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (StringUtils.isBlank(item.getUbigeoDestinoDetraccion())) {
                                throw new ValidationException("El campo [" + ubigeoDestinoDetraccionLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (StringUtils.isBlank(item.getDireccionDestinoDetraccion())) {
                                throw new ValidationException("El campo [" + direccionDestinoDetraccionLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (item.getUbigeoOrigenDetraccion().length() != 6) {
                                throw new ValidationException("El campo [" + ubigeoOrigenDetraccionLabel + "] debe tener 6 caracteres.");
                            }
                            if (item.getUbigeoDestinoDetraccion().length() != 6) {
                                throw new ValidationException("El campo [" + ubigeoDestinoDetraccionLabel + "] debe tener 6 caracteres.");
                            }
                            if (item.getValorServicioTransporte() == null) {
                                throw new ValidationException("El campo [" + valorServicioTransporteLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (item.getValorCargaEfectiva() == null) {
                                throw new ValidationException("El campo [" + valorCargaEfectivaLabel + "] es obligarorio, para codigo bien 027.");
                            }
                            if (item.getValorCargaUtil() == null) {
                                throw new ValidationException("El campo [" + valorCargaUtilLabel + "] es obligarorio, para codigo bien 027.");
                            }
                        }
                    }
                } else {
                    throw new ValidationException("Las Detracciones, no corresponden al valor de Tipo Operacion Ingresado.");
                }
            }
        }
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        return null;
    }

}
