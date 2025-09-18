package com.certicom.certifact_boletas_service_ng.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeErrorEnum {

    ERROR("ERROR"),
    WARNING("OBSERV");
    private final String type;

}

