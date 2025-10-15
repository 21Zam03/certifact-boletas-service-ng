package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class RegisterFileUploadRestService {

    private final RestTemplate restTemplate;

    @Value("${app.api.url}")
    private String baseUrl;

    public RegisterFileUploadRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public RegisterFileUploadDto findFirst1ByPaymentVoucherIdPaymentVoucherAndTipoArchivoAndEstadoArchivoOrderByOrdenDesc(
            Long idPayment,
            String tipoArchivo,
            String estadoArchivo) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/register-file-upload")
                .queryParam("idPayment", idPayment)
                .queryParam("tipoArchivo", tipoArchivo)
                .queryParam("estadoArchivo", estadoArchivo)
                .toUriString();
        return restTemplate.getForObject(url, RegisterFileUploadDto.class);
    }

    public RegisterFileUploadDto saveRegisterFileUpload(RegisterFileUploadDto registerFileUploadModelDto) {
        String url = baseUrl + "/api/register-file-upload";
        return restTemplate.postForObject(url, registerFileUploadModelDto, RegisterFileUploadDto.class);
    }

    public RegisterFileUploadDto getDataForCdr(Long id, String uuid, String tipo) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/register-file-upload/cdr-info")
                .queryParam("id", id)
                .queryParam("uuid", uuid)
                .queryParam("tipo", tipo)
                .toUriString();
        return restTemplate.getForObject(url, RegisterFileUploadDto.class);
    }

    public RegisterFileUploadDto getDataForXml(Long id, String uuid, String tipo) {
        String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/register-file-upload/xml-info")
                .queryParam("id", id)
                .queryParam("uuid", uuid)
                .queryParam("tipo", tipo)
                .toUriString();
        return restTemplate.getForObject(url, RegisterFileUploadDto.class);
    }

}
