package com.certicom.certifact_boletas_service_ng.enums;

public enum LogTitle {

    ERROR_DATABASE("Error in database"),
    ERROR_UNEXPECTED("Error unexpected"),

    //Advertencia para validaciones
    WARN_VALIDATION("Validation"),

    //Advertencia para resultados vacios o nulos
    WARN_NOT_RESULT("No Result"),
    DEBUG("Debugging"),
    INFO("Information");

    private final String type;

    LogTitle(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
