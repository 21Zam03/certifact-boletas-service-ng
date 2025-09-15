package com.certicom.certifact_boletas_service_ng.service;

import com.certicom.certifact_boletas_service_ng.dto.others.ResponseSunat;

public interface SunatService {

    ResponseSunat sendSummary(String fileName, String contentFileBase64, String rucEmisor);

}
