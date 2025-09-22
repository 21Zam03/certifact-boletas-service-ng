package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;
import com.certicom.certifact_boletas_service_ng.enums.TipoArchivoEnum;
import org.springframework.core.io.ByteArrayResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface AmazonS3ClientService {

    RegisterFileUploadDto uploadFileStorage(InputStream inputStream, String nameFile, String folder, CompanyDto companyDto);
    ByteArrayResource downloadFileInvoice(Long id, String uuid, TipoArchivoEnum tipoArchivoEnum) throws IOException;
    ByteArrayInputStream downloadFileStorage(RegisterFileUploadDto fileStorage);

}
