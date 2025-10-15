package com.certicom.certifact_boletas_service_ng.templates.template;

import com.certicom.certifact_boletas_service_ng.dto.others.Summary;
import com.certicom.certifact_boletas_service_ng.dto.others.SummaryDetailDto;
import com.certicom.certifact_boletas_service_ng.dto.others.Tipo;
import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.exception.TemplateException;
import com.certicom.certifact_boletas_service_ng.util.ConstantesSunat;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;

import static com.certicom.certifact_boletas_service_ng.util.UtilFormat.format;
import static com.certicom.certifact_boletas_service_ng.util.UtilXml.*;

@Component
@Slf4j
public class SummaryTemplate {

    public String buildSummary(Summary summary) throws TemplateException {
        String xml;
        DOMSource source;
        StringWriter writer;
        BigDecimal montoZero = BigDecimal.ZERO;

        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            Document doc = docBuilder.newDocument();

            Element summaryRoot = doc.createElement("SummaryDocuments");
            doc.appendChild(summaryRoot);

            Attr xmlns = doc.createAttribute("xmlns");
            xmlns.setValue("urn:sunat:names:specification:ubl:peru:schema:xsd:SummaryDocuments-1");
            summaryRoot.setAttributeNode(xmlns);

            Attr xmlnscac = doc.createAttribute("xmlns:cac");
            xmlnscac.setValue("urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            summaryRoot.setAttributeNode(xmlnscac);

            Attr xmlnscbc = doc.createAttribute("xmlns:cbc");
            xmlnscbc.setValue("urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            summaryRoot.setAttributeNode(xmlnscbc);

            Attr xmlnsds = doc.createAttribute("xmlns:ds");
            xmlnsds.setValue("http://www.w3.org/2000/09/xmldsig#");
            summaryRoot.setAttributeNode(xmlnsds);

            Attr xmlnsext = doc.createAttribute("xmlns:ext");
            xmlnsext.setValue("urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            summaryRoot.setAttributeNode(xmlnsext);

            Attr xmlnssac = doc.createAttribute("xmlns:sac");
            xmlnssac.setValue("urn:sunat:names:specification:ubl:peru:schema:xsd:SunatAggregateComponents-1");
            summaryRoot.setAttributeNode(xmlnssac);

            Attr xmlnsxsi = doc.createAttribute("xmlns:xsi");
            xmlnsxsi.setValue("http://www.w3.org/2001/XMLSchema-instance");
            summaryRoot.setAttributeNode(xmlnsxsi);

            appendChild(doc, summaryRoot, "ext:UBLExtensions", " ");

            appendChild(doc, summaryRoot, "cbc:UBLVersionID", "2.0");
            appendChild(doc, summaryRoot, "cbc:CustomizationID", "1.1");
            appendChild(doc, summaryRoot, "cbc:ID", concat(summary.getId()));
            appendChild(doc, summaryRoot, "cbc:ReferenceDate", summary.getFechaEmision());
            //Fecha de generacion podria ser mayor o igual a la fecha de emision
            //actualmente la sunat no esta validando correctamente, por lo cual
            //como solucion temporal se esta colocando la misma fecha
            appendChild(doc, summaryRoot, "cbc:IssueDate", summary.getFechaEmision());

            Element cacSignature = appendChild(doc, summaryRoot, "cac:Signature");
            appendChild(doc, cacSignature, "cbc:ID", "IDSignKG");

            Element cacSignatoryParty = appendChild(doc, cacSignature, "cac:SignatoryParty");

            Element cacPartyIdentification = appendChild(doc, cacSignatoryParty, "cac:PartyIdentification");
            appendChild(doc, cacPartyIdentification, "cbc:ID", summary.getRucEmisor());

            Element cacPartyName = appendChild(doc, cacSignatoryParty, "cac:PartyName");
            appendChild(doc, cacPartyName, "cbc:Name", summary.getDenominacionEmisor());

            Element cacDigitalSignatureAttachment = appendChild(doc, cacSignature, "cac:DigitalSignatureAttachment");
            Element cacExternalReference = appendChild(doc, cacDigitalSignatureAttachment, "cac:ExternalReference");
            appendChild(doc, cacExternalReference, "cbc:URI", "#SignatureKG");

            Element cacAccountingSupplierParty = appendChild(doc, summaryRoot, "cac:AccountingSupplierParty");
            appendChild(doc, cacAccountingSupplierParty, "cbc:CustomerAssignedAccountID", summary.getRucEmisor());
            appendChild(doc, cacAccountingSupplierParty, "cbc:AdditionalAccountID", summary.getTipoDocumentoEmisor());

            Element cacParty = appendChild(doc, cacAccountingSupplierParty, "cac:Party");
            Element cacPartyLegalEntity = appendChild(doc, cacParty, "cac:PartyLegalEntity");
            appendChild(doc, cacPartyLegalEntity, "cbc:RegistrationName", summary.getDenominacionEmisor());

            for (SummaryDetailDto item : summary.getItems()) {

                Element sacSummaryDocumentsLine = appendChild(doc, summaryRoot, "sac:SummaryDocumentsLine");
                appendChild(doc, sacSummaryDocumentsLine, "cbc:LineID", item.getNumeroItem());
                appendChild(doc, sacSummaryDocumentsLine, "cbc:DocumentTypeCode", item.getTipoComprobante());
                appendChild(doc, sacSummaryDocumentsLine, "cbc:ID", item.getSerie() + "-" + item.getNumero());

                if (StringUtils.isNotBlank(item.getNumeroDocumentoReceptor()) || StringUtils.isNotBlank(item.getTipoDocumentoReceptor())) {
                    Element cacAccountingCustomerParty = appendChild(doc, sacSummaryDocumentsLine, "cac:AccountingCustomerParty");
                    if (StringUtils.isNotBlank(item.getNumeroDocumentoReceptor())) {
                        appendChild(doc, cacAccountingCustomerParty, "cbc:CustomerAssignedAccountID", item.getNumeroDocumentoReceptor());
                    }
                    if (StringUtils.isNotBlank(item.getTipoDocumentoReceptor())) {
                        appendChild(doc, cacAccountingCustomerParty, "cbc:AdditionalAccountID", item.getTipoDocumentoReceptor());
                    }
                }

                if (item.getTipoComprobanteAfectado() != null &&
                        item.getSerieAfectado() != null && item.getNumeroAfectado() != null) {

                    Element billingReferenceElement = appendChild(doc, sacSummaryDocumentsLine,
                            "cac:BillingReference");
                    Element invoiceDocumentReferenceElement = appendChild(doc, billingReferenceElement,
                            "cac:InvoiceDocumentReference");
                    appendChild(doc, invoiceDocumentReferenceElement,
                            "cbc:ID", item.getSerieAfectado() + "-" + item.getNumeroAfectado());
                    appendChild(doc, invoiceDocumentReferenceElement,
                            "cbc:DocumentTypeCode", item.getTipoComprobanteAfectado());

                }

                Element cacStatus = appendChild(doc, sacSummaryDocumentsLine, "cac:Status");
                appendChild(doc, cacStatus, "cbc:ConditionCode", item.getStatusItem());

                appendChild(doc, sacSummaryDocumentsLine, "sac:TotalAmount",
                        format(item.getImporteTotalVenta())).
                        setAttribute("currencyID", item.getCodigoMoneda());

                if (item.getTotalValorVentaOperacionExportacion() != null && item.getTotalValorVentaOperacionExportacion().compareTo(BigDecimal.ZERO) == 1) {
                    appendChildBillingPayment(doc, sacSummaryDocumentsLine,
                            item.getTotalValorVentaOperacionExportacion(),
                            ConstantesSunat.TIPO_VALOR_VENTA_EXPORTACION,
                            item.getCodigoMoneda());
                }

                if (item.getTotalValorVentaOperacionExonerado() != null && item.getTotalValorVentaOperacionExonerado().compareTo(BigDecimal.ZERO) == 1) {
                    appendChildBillingPayment(doc, sacSummaryDocumentsLine,
                            item.getTotalValorVentaOperacionExonerado(),
                            ConstantesSunat.TIPO_VALOR_VENTA_EXONERADO,
                            item.getCodigoMoneda());
                }

                if (item.getTotalValorVentaOperacionGratuita() != null && item.getTotalValorVentaOperacionGratuita().compareTo(BigDecimal.ZERO) == 1) {
                    appendChildBillingPayment(doc, sacSummaryDocumentsLine,
                            item.getTotalValorVentaOperacionGratuita(),
                            ConstantesSunat.TIPO_VALOR_VENTA_GRATUITA,
                            item.getCodigoMoneda());
                }

                if (item.getTotalValorVentaOperacionGravada() != null && item.getTotalValorVentaOperacionGravada().compareTo(BigDecimal.ZERO) == 1) {
                    appendChildBillingPayment(doc, sacSummaryDocumentsLine,
                            item.getTotalValorVentaOperacionGravada(),
                            ConstantesSunat.TIPO_VALOR_VENTA_GRAVADO,
                            item.getCodigoMoneda());
                }

                if (item.getTotalValorVentaOperacionInafecta() != null && item.getTotalValorVentaOperacionInafecta().compareTo(BigDecimal.ZERO) == 1) {
                    appendChildBillingPayment(doc, sacSummaryDocumentsLine,
                            item.getTotalValorVentaOperacionInafecta(),
                            ConstantesSunat.TIPO_VALOR_VENTA_INAFECTO,
                            item.getCodigoMoneda());
                }


                if (item.getSumatoriaOtrosCargos() != null) {

                    Element allowanceChargeElement = appendChild(doc, sacSummaryDocumentsLine, "cac:AllowanceCharge");
                    appendChild(doc, allowanceChargeElement, "cbc:ChargeIndicator", "true");
                    appendChild(doc, allowanceChargeElement, "cbc:Amount",
                            format(item.getSumatoriaOtrosCargos())).
                            setAttribute("currencyID", item.getCodigoMoneda());

                }
                BigDecimal montoTributoIGV = (item.getTotalIGV() != null) ? item.getTotalIGV() : montoZero;
                appendChildSumatoria(doc, sacSummaryDocumentsLine, montoTributoIGV,
                        ConstantesSunat.CODIGO_TRIBUTO_IGV, item.getCodigoMoneda());
                appendChildSumatoria(doc, sacSummaryDocumentsLine, item.getTotalISC(),
                        ConstantesSunat.CODIGO_TRIBUTO_ISC, item.getCodigoMoneda());
                appendChildSumatoria(doc, sacSummaryDocumentsLine, item.getTotalOtrosTributos(),
                        ConstantesSunat.CODIGO_TRIBUTO_OTROS, item.getCodigoMoneda());
            }
            source = new DOMSource(doc);
            Transformer transformer = getTransformer();

            writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            transformer.transform(source, result);

            xml = formatXML(writer.toString());
        } catch (Exception ex) {
            //ex.printStackTrace();
            LogHelper.errorLog(LogTitle.ERROR_UNEXPECTED.getType(),
                    LogMessages.currentMethod(), "Ocurrio un error inesperado al momento de generar la plantilla xml", ex);
            throw new TemplateException(ex.getMessage());
        }
        return xml;
    }
    private Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Bloquear el acceso a DTDs y entidades externas
            transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException | TransformerConfigurationException e) {
            log.error("Error al activar la seguridad en el transformador", e);
            throw new TransformerConfigurationException("No se pudo configurar la seguridad XML.", e);
        }
        return transformerFactory.newTransformer();
    }
    private void appendChildBillingPayment(Document doc, Element sacSummaryDocumentsLine,
                                           BigDecimal ventaTotal, String tipoValorVenta, String currency) {

        if (ventaTotal != null) {

            Element sacBillingPayment = appendChild(doc, sacSummaryDocumentsLine, "sac:BillingPayment");
            appendChild(doc, sacBillingPayment, "cbc:PaidAmount", format(ventaTotal)).
                    setAttribute("currencyID", currency);
            appendChild(doc, sacBillingPayment, "cbc:InstructionID", tipoValorVenta);
        }
    }

    private void appendChildSumatoria(Document document, Element elementRoot,
                                      BigDecimal sumatoriaTributo, String codigoTipoTributo, String codigoMoneda) {

        if (sumatoriaTributo != null) {

            Element taxTotalElement = appendChild(document, elementRoot, "cac:TaxTotal");
            appendChild(document, taxTotalElement, "cbc:TaxAmount", format(sumatoriaTributo))
                    .setAttribute(ConstantesSunat.ATTRIBUTE_CURRENCY_ID, codigoMoneda);

            Element taxSubTotalElement = appendChild(document, taxTotalElement, "cac:TaxSubtotal");
            appendChild(document, taxSubTotalElement, "cbc:TaxAmount", format(sumatoriaTributo))
                    .setAttribute(ConstantesSunat.ATTRIBUTE_CURRENCY_ID, codigoMoneda);

            Element taxCategoryElement = appendChild(document, taxSubTotalElement, "cac:TaxCategory");
            Element taxSchemeElement = appendChild(document, taxCategoryElement, "cac:TaxScheme");
            Tipo tipoTributo = null;

            switch (codigoTipoTributo) {

                case ConstantesSunat.CODIGO_TRIBUTO_IGV:
                    tipoTributo = ConstantesSunat.TRIBUTO_IGV;
                    break;
                case ConstantesSunat.CODIGO_TRIBUTO_ISC:
                    tipoTributo = ConstantesSunat.TRIBUTO_ISC;
                    break;
                case ConstantesSunat.CODIGO_TRIBUTO_OTROS:
                    tipoTributo = ConstantesSunat.TRIBUTO_OTROS;
                    break;

            }
            appendChild(document, taxSchemeElement, "cbc:ID", tipoTributo.getId());
            appendChild(document, taxSchemeElement, "cbc:Name", tipoTributo.getName());
            appendChild(document, taxSchemeElement, "cbc:TaxTypeCode", tipoTributo.getTypeCode());
        }
    }

}
