package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.converter.PaymentVoucherConverter;
import com.certicom.certifact_boletas_service_ng.dto.*;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponsePSE;
import com.certicom.certifact_boletas_service_ng.exception.ServiceException;
import com.certicom.certifact_boletas_service_ng.feign.BranchOfficeFeign;
import com.certicom.certifact_boletas_service_ng.feign.CompanyFeign;
import com.certicom.certifact_boletas_service_ng.feign.PaymentVoucherFeign;
import com.certicom.certifact_boletas_service_ng.feign.UserFeign;
import com.certicom.certifact_boletas_service_ng.formatter.PaymentVoucherFormatter;
import com.certicom.certifact_boletas_service_ng.request.PaymentVoucherRequest;
import com.certicom.certifact_boletas_service_ng.service.PaymentVoucherService;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentVoucherServiceImpl implements PaymentVoucherService {

    private final PaymentVoucherFormatter paymentVoucherFormatter;
    private final UserFeign userFeign;
    private final CompanyFeign companyFeign;
    private final BranchOfficeFeign branchOfficeFeign;
    private final PaymentVoucherFeign paymentVoucherFeign;

    @Override
    public Map<String, Object> createPaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        return generateNewDocument(paymentVoucher, idUsuario);
    }

    @Override
    public Map<String, Object> updatePaymentVoucher(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        return Map.of();
    }

    private Map<String, Object> generateNewDocument(PaymentVoucherRequest paymentVoucher, Long idUsuario) {
        Map<String, Object> resultado = new HashMap<>();
        ResponsePSE response;
        boolean status = false;

        try {
            PaymentVoucherDto paymentVoucherDto = PaymentVoucherConverter.requestToDto(paymentVoucher);
            paymentVoucherFormatter.formatPaymentVoucher(paymentVoucherDto);
            Integer estadoItem = ConstantesParameter.STATE_ITEM_PENDIENTE_ADICION;
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

        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage());
        }
        return null;
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
