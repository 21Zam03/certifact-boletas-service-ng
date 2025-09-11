package com.certicom.certifact_boletas_service_ng.converter;

import com.certicom.certifact_boletas_service_ng.dto.DetailsPaymentVoucherDto;
import com.certicom.certifact_boletas_service_ng.request.DetailsPaymentVoucherRequest;

import java.util.ArrayList;
import java.util.List;

public class DetailsPaymentVoucherConverter {

    public static DetailsPaymentVoucherDto requestToDto(DetailsPaymentVoucherRequest detailsPaymentVoucherRequest) {
        return DetailsPaymentVoucherDto.builder()
                .idComprobanteDetalle(detailsPaymentVoucherRequest.getIdComprobanteDetalle())
                .numeroItem(detailsPaymentVoucherRequest.getNumeroItem())
                .codigoUnidadMedida(detailsPaymentVoucherRequest.getCodigoUnidadMedida())
                .cantidad(detailsPaymentVoucherRequest.getCantidad())
                .descripcion(detailsPaymentVoucherRequest.getDescripcion())
                .codigoProducto(detailsPaymentVoucherRequest.getCodigoProducto())
                .codigoProductoGS1(detailsPaymentVoucherRequest.getCodigoProductoGS1())
                .valorUnitario(detailsPaymentVoucherRequest.getValorUnitario())
                .valorVenta(detailsPaymentVoucherRequest.getValorVenta())
                .descuento(detailsPaymentVoucherRequest.getDescuento())
                .codigoDescuento(detailsPaymentVoucherRequest.getCodigoDescuento())
                .precioVentaUnitario(detailsPaymentVoucherRequest.getPrecioVentaUnitario())
                .valorReferencialUnitario(detailsPaymentVoucherRequest.getValorReferencialUnitario())
                .montoBaseIgv(detailsPaymentVoucherRequest.getMontoBaseIgv())
                .montoBaseIvap(detailsPaymentVoucherRequest.getMontoBaseIvap())
                .montoBaseExportacion(detailsPaymentVoucherRequest.getMontoBaseExportacion())
                .montoBaseExonerado(detailsPaymentVoucherRequest.getMontoBaseExonerado())
                .montoBaseInafecto(detailsPaymentVoucherRequest.getMontoBaseInafecto())
                .montoBaseGratuito(detailsPaymentVoucherRequest.getMontoBaseGratuito())
                .montoBaseIsc(detailsPaymentVoucherRequest.getMontoBaseIsc())
                .montoBaseOtrosTributos(detailsPaymentVoucherRequest.getMontoBaseOtrosTributos())
                .igv(detailsPaymentVoucherRequest.getIgv())
                .isc(detailsPaymentVoucherRequest.getIsc())
                .montoIcbper(detailsPaymentVoucherRequest.getMontoIcbper())
                .ivap(detailsPaymentVoucherRequest.getIvap())
                .impuestoVentaGratuita(detailsPaymentVoucherRequest.getImpuestoVentaGratuita())
                .otrosTributos(detailsPaymentVoucherRequest.getOtrosTributos())
                .porcentajeIgv(detailsPaymentVoucherRequest.getPorcentajeIgv())
                .porcentajeIvap(detailsPaymentVoucherRequest.getPorcentajeIvap())
                .porcentajeIsc(detailsPaymentVoucherRequest.getPorcentajeIsc())
                .porcentajeOtrosTributos(detailsPaymentVoucherRequest.getPorcentajeOtrosTributos())
                .porcentajeTributoVentaGratuita(detailsPaymentVoucherRequest.getPorcentajeTributoVentaGratuita())
                .codigoTipoAfectacionIGV(detailsPaymentVoucherRequest.getCodigoTipoAfectacionIGV())
                .codigoTipoCalculoISC(detailsPaymentVoucherRequest.getCodigoTipoCalculoISC())
                .productoId(detailsPaymentVoucherRequest.getProductoId())
                .detalleViajeDetraccion(detailsPaymentVoucherRequest.getDetalleViajeDetraccion())
                .ubigeoOrigenDetraccion(detailsPaymentVoucherRequest.getUbigeoOrigenDetraccion())
                .direccionOrigenDetraccion(detailsPaymentVoucherRequest.getDireccionOrigenDetraccion())
                .ubigeoDestinoDetraccion(detailsPaymentVoucherRequest.getUbigeoDestinoDetraccion())
                .direccionDestinoDetraccion(detailsPaymentVoucherRequest.getDireccionDestinoDetraccion())
                .valorServicioTransporte(detailsPaymentVoucherRequest.getValorServicioTransporte())
                .valorCargaEfectiva(detailsPaymentVoucherRequest.getValorCargaEfectiva())
                .valorCargaUtil(detailsPaymentVoucherRequest.getValorCargaUtil())
                .hidroMatricula(detailsPaymentVoucherRequest.getHidroMatricula())
                .hidroEmbarcacion(detailsPaymentVoucherRequest.getHidroEmbarcacion())
                .hidroDescripcionTipo(detailsPaymentVoucherRequest.getHidroDescripcionTipo())
                .hidroLugarDescarga(detailsPaymentVoucherRequest.getHidroLugarDescarga())
                .hidroFechaDescarga(detailsPaymentVoucherRequest.getHidroFechaDescarga())
                .hidroCantidad(detailsPaymentVoucherRequest.getHidroCantidad())
                .unidadManejo(detailsPaymentVoucherRequest.getUnidadManejo())
                .instruccionesEspeciales(detailsPaymentVoucherRequest.getInstruccionesEspeciales())
                .marca(detailsPaymentVoucherRequest.getMarca())
                .adicional(detailsPaymentVoucherRequest.getAdicional())
                .estado(detailsPaymentVoucherRequest.getEstado())
                .build();
    }

    public static List<DetailsPaymentVoucherDto> requestListToDtoList(List<DetailsPaymentVoucherRequest> detailsPaymentVoucherRequests) {
        List<DetailsPaymentVoucherDto> detailsPaymentVoucherDtos = new ArrayList<>();
        for (DetailsPaymentVoucherRequest detailsPaymentVoucherRequest : detailsPaymentVoucherRequests) {
            detailsPaymentVoucherDtos.add(requestToDto(detailsPaymentVoucherRequest));
        }
        return detailsPaymentVoucherDtos;
    }

}
