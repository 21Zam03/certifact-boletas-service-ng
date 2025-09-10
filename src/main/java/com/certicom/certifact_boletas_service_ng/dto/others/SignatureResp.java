package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignatureResp {

    private Boolean status;
    private ByteArrayOutputStream signatureFile;
    private String digestValue;

}
