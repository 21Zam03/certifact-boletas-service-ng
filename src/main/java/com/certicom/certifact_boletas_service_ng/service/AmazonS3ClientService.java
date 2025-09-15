package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.CompanyDto;
import com.certicom.certifact_boletas_service_ng.dto.RegisterFileUploadDto;

import java.io.InputStream;

public interface AmazonS3ClientService {

    RegisterFileUploadDto uploadFileStorage(InputStream inputStream, String nameFile, String folder, CompanyDto companyDto);

}
