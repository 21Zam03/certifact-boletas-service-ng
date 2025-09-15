package com.certicom.certifact_boletas_service_ng.dto.others;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseServer {

    protected String content;
    protected int serverCode;
    protected boolean success;
    protected String message;

}
