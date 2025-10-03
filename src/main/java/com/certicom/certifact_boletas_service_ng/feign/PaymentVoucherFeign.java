package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.PaymentVoucherDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8092", contextId = "paymentVoucher")
public interface PaymentVoucherFeign {

    @GetMapping("/api/payment-voucher/number")
    public Integer obtenerSiguienteNumeracionPorTipoComprobanteYSerieYRucEmisor(
            @RequestParam String tipoComprobante, @RequestParam String serie, @RequestParam String ruc
    );

    @GetMapping("/api/payment-voucher/id-document")
    public PaymentVoucherDto getPaymentVoucherByIdentificadorDocumento(@RequestParam String identificadorDocumento);

    @PostMapping("/api/payment-voucher")
    public PaymentVoucherDto save(@RequestBody PaymentVoucherDto paymentVoucherDto);

    @GetMapping("/api/payment-voucher/specific-summary")
    public List<PaymentVoucherDto> findListSpecificForSummary(
            @RequestParam String rucEmisor,
            @RequestParam String fechaEmision,
            @RequestParam String tipo,
            @RequestParam String serie,
            @RequestParam Integer numero);

    @GetMapping("/api/payment-voucher/summary-ruc-date")
    public List<PaymentVoucherDto> findAllForSummaryByRucEmisorAndFechaEmision(
            @RequestParam String rucEmisor, @RequestParam String fechaEmision);

    @PutMapping("/api/payment-voucher/state")
    public void updateStateToSendSunatForSummaryDocuments(
            @RequestParam("ids") List<Long> ids,
            @RequestParam("usuario") String usuario,
            @RequestParam("fechaModificacion") Timestamp fechaModificacion
    );

    @PutMapping("/api/payment-voucher/payment-summary-accept")
    void updateComprobantesBySummaryDocuments(
            @RequestParam List<String> comprobantesByAceptar,
            @RequestParam String codigo,
            @RequestParam String abreviado,
            @RequestParam String usuario,
            @RequestParam Timestamp fechaModificacion);

    @PutMapping("/api/payment-voucher/payment-summary-error")
    void updateComprobantesOnResumenError(
            @RequestParam List<String> identificadoresComprobantes,
            @RequestParam String usuario,
            @RequestParam Timestamp fechaModificacion);

    @GetMapping("/api/payment-voucher/ruc-type-serie-number")
    PaymentVoucherDto findByRucAndTipoAndSerieAndNumero(
            @RequestParam String finalRucEmisor,
            @RequestParam String tipoComprobante,
            @RequestParam String serie,
            @RequestParam Integer numero);

    PaymentVoucherDto findPaymentVoucherByRucAndTipoComprobanteAndSerieAndNumero(String rucEmisor, String tipoComprobante, String serie, Integer numero);
}
