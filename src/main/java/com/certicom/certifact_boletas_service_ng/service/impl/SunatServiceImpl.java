package com.certicom.certifact_boletas_service_ng.service.impl;

import com.certicom.certifact_boletas_service_ng.dto.OseDto;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponseServer;
import com.certicom.certifact_boletas_service_ng.dto.others.ResponseSunat;
import com.certicom.certifact_boletas_service_ng.enums.ComunicacionSunatEnum;
import com.certicom.certifact_boletas_service_ng.feign.CompanyFeign;
import com.certicom.certifact_boletas_service_ng.service.SunatService;
import com.certicom.certifact_boletas_service_ng.templates.template.RequestSunatTemplate;
import com.certicom.certifact_boletas_service_ng.util.ConstantesParameter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;

import static com.certicom.certifact_boletas_service_ng.util.UtilXml.formatXML;
import static com.certicom.certifact_boletas_service_ng.util.UtilXml.parseXmlFile;

@Service
@RequiredArgsConstructor
public class SunatServiceImpl implements SunatService {

    private final CompanyFeign companyFeign;
    private final RequestSunatTemplate requestSunatTemplate;

    @Value("${sunat.endpoint}")
    private String endPointSunat;
    @Value("${sunat.endpointOtrosCpe}")
    private String endPointSunatOtrosCpe;
    @Value("${sunat.endpointGuiaRemision}")
    private String endPointSunatGuiaRemision;
    @Value("${sunat.endpointGuiaRemisionRest}")
    private String endPointSunatGuiaRemisionRest;
    @Value("${sunat.endpointConsultaCDR}")
    private String endPointConsultaCDR;

    private String endPoint="";
    private String endPointSunatOld = "https://www.sunat.gob.pe/ol-ti-itcpfegem/billService";
    final String baseurlget =
            "http://200.41.86.242:8077/api/sendose?endpoint=https://osetesting.bizlinks.com.pe/ol-ti-itcpe/billService&tagOperacionOK=ticket";
    final String baseurlgetContent =
            "http://200.41.86.242:8077/api/sendose?endpoint=https://osetesting.bizlinks.com.pe/ol-ti-itcpe/billService&tagOperacionOK=content";
    final String baseurlgetApply =
            "http://200.41.86.242:8077/api/sendose?endpoint=https://osetesting.bizlinks.com.pe/ol-ti-itcpe/billService&tagOperacionOK=applicationResponse";
    final String baseurlgetGuiaApply =
            "http://200.41.86.242:8077/api/sendguiaose?endpoint=https://osetesting.bizlinks.com.pe/ol-ti-itcpe/billService&tagOperacionOK=applicationResponse";

    @Override
    public ResponseSunat sendSummary(String fileName, String contentFileBase64, String rucEmisor) {
        ResponseSunat responseSunat = new ResponseSunat();
        String formatSoap;
        Document document;
        NodeList nodeFaultcode;
        NodeList nodeFaultstring;
        Node node;

        try {
            System.out.println("ANTES FORMAT SOAT");
            formatSoap = obtenerFormatBuildSendSumary(rucEmisor,fileName, contentFileBase64);

            System.out.println("FORMAT SOAT 2 SEGUIMIENTO");

            ResponseServer responseServer = null;
            OseDto ose = companyFeign.findOseByRucInter(rucEmisor);
            if (ose != null) {
                if (ose.getId()==1){
                    responseServer = send(formatSoap, obtenerEndPointSunat(rucEmisor),
                            ConstantesParameter.TAG_SEND_SUMMARY_TICKET);
                }   else if (ose.getId()==2 || ose.getId()==12) {
                    RestTemplate template = new RestTemplate();
                    URI uriget = new URI(ose.getUrlFacturas()+ConstantesParameter.TAG_SEND_SUMMARY_TICKET);
                    HttpHeaders requestHeaders = new HttpHeaders();
                    System.out.println("SOAP ANULACION");
                    System.out.println(formatSoap);
                    HttpEntity<String> requestEntity = new HttpEntity<>(formatSoap, requestHeaders);
                    ResponseEntity<ResponseServer> entity = template.exchange(uriget, HttpMethod.POST, requestEntity, ResponseServer.class);
                    System.out.println(entity);
                    if (entity.getStatusCode() == HttpStatus.OK) {
                        responseServer = entity.getBody();
                        System.out.println("user response retrieved ");
                    }
                }else if (ose.getId()==10 ) {
                    System.out.println("Enviar boleta resumen 10");
                    System.out.println(formatSoap);
                    responseServer = send(formatSoap, obtenerEndPointSunat(rucEmisor),
                            ConstantesParameter.TAG_SEND_SUMMARY_TICKET);
                }
            } else {
                System.out.println("Enviar anulacion 0");
                responseServer = send(formatSoap, obtenerEndPointSunat(rucEmisor),
                        ConstantesParameter.TAG_SEND_SUMMARY_TICKET);
            }
            document = parseXmlFile(responseServer.getContent());

            if (responseServer.isSuccess()) {

                NodeList nodeTicket = document.getElementsByTagName(ConstantesParameter.TAG_SEND_SUMMARY_TICKET);
                String valueTicket = nodeTicket.item(0).getTextContent();
                responseSunat.setSuccess(true);
                responseSunat.setTicket(valueTicket);
                responseSunat.setEstadoComunicacionSunat(ComunicacionSunatEnum.SUCCESS);
            } else {

                String valueFaultcode = null;
                String valueFaultstring = null;

                nodeFaultcode = document.getElementsByTagName("faultcode");
                nodeFaultstring = document.getElementsByTagName("faultstring");
                node = nodeFaultcode.item(0);

                if (node != null && StringUtils.isNotBlank(node.getTextContent())) {
                    valueFaultcode = (node.getTextContent()).replaceAll("[^0-9]", "");
                    valueFaultstring = nodeFaultstring.item(0).getTextContent();
                    if (valueFaultcode.equals("")) {
                        responseSunat.setEstadoComunicacionSunat(ComunicacionSunatEnum.WITHOUT_CONNECTION);
                    } else {
                        responseSunat.setEstadoComunicacionSunat(ComunicacionSunatEnum.SUCCESS_WITH_ERROR_CONTENT);
                    }
                    responseSunat.setStatusCode(valueFaultcode);
                    responseSunat.setMessage(valueFaultstring);
                }
                responseSunat.setSuccess(false);
            }
        } catch (IOException e) {
            responseSunat.setMessage("Error al comunicarse con la Sunat." + e.getMessage());
            responseSunat.setEstadoComunicacionSunat(ComunicacionSunatEnum.ERROR_INTERNO_WS_API);
            responseSunat.setSuccess(false);
        } catch (Exception ex) {
            responseSunat.setMessage(ex.getMessage());
            responseSunat.setEstadoComunicacionSunat(ComunicacionSunatEnum.ERROR_INTERNO_WS_API);
            responseSunat.setSuccess(false);
        }
        return responseSunat;
    }

    private String obtenerEndPointSunat(String rucEmisor) {
        OseDto ose = companyFeign.findOseByRucInter(rucEmisor);
        if (ose != null && ose.getId()!=10) {
            return ose.getUrlFacturas();
        } else {
            return endPointSunat;
        }
    }

    private ResponseServer send(String xml, String endpoint, String tagOperacionOK)
            throws IOException {

        ResponseServer responseServer = new ResponseServer();
        CloseableHttpResponse responsePost;
        String formattedSOAPResponse;
        StringEntity entity = null;
        HttpPost httpPost = null;
        String inputLine;
        int responseCode = 0;
        System.out.println(endpoint);
        try(CloseableHttpClient client = HttpClients.createDefault()) {
            httpPost = new HttpPost(endpoint);
            httpPost.setHeader("Prama", "no-cache");
            httpPost.setHeader("Cache-Control", "no-cache");

            entity = new StringEntity(xml, "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-type", "text/xml");

            System.out.println("XML: " + xml);
            System.out.println("Endpoint: " + endpoint);
            System.out.println("Httppost: " + httpPost);
            System.out.println("Entity:" + entity);
            responsePost = client.execute(httpPost);
            System.out.println("HttpResponse: " + responsePost);
            responseCode = responsePost.getStatusLine().getStatusCode();
            responseServer.setServerCode(responseCode);

            StringBuilder response = new StringBuilder();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(responsePost.getEntity().getContent()));
            while ((inputLine = in.readLine()) != null) {
                String inp = inputLine.replace("S:", "soap-env:");
                inp = inp.replace(":S=", ":soap-env=");
                inp = inp.replace("SOAP-ENV:", "soap-env:");
                inp = inp.replace("ns2:", "br:");
                inp = inp.replace(":ns2", ":br");
                response.append(inp);
            }
            System.out.println("response");
            System.out.println(response);
            formattedSOAPResponse = formatXML(response.toString());
            responseServer.setContent(formattedSOAPResponse);
        }


        if (formattedSOAPResponse.contains("<" + tagOperacionOK + ">")) {
            responseServer.setSuccess(true);
        } else {
            responseServer.setSuccess(false);
        }

        return responseServer;
    }

    private String obtenerFormatBuildSendSumary(String ruc, String fileName, String contentFileBase64) {
        OseDto ose = companyFeign.findOseByRucInter(ruc);
        String formato = "";
        if (ose != null) {
            if (ose.getId()==1){
                formato =  requestSunatTemplate.buildOseSendSummary(fileName, contentFileBase64);
            }else if (ose.getId()==2){
                formato =  requestSunatTemplate.buildOseBlizSendSummary(fileName, contentFileBase64);
            }else if (ose.getId()==12){
                formato =  requestSunatTemplate.buildOseBlizSendSummary12(fileName, contentFileBase64);
            }else if (ose.getId()==10){
                formato =  requestSunatTemplate.buildSendSummaryCerti(fileName, contentFileBase64);
            }
        } else {
            formato =  requestSunatTemplate.buildSendSummary(fileName, contentFileBase64);
        }
        return formato;
    }
}
