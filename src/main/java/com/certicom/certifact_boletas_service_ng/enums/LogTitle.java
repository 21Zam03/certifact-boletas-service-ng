package com.certicom.certifact_boletas_service_ng.enums;

public enum LogTitle {

    ERROR_UNEXPECTED("Error unexpected"),
    ERROR_HTTP_CLIENT("Error httpclient"),
    ERROR_HTTP_RED("Error httpred"),
    ERROR_HTTP_SERVER("Error httpserver"),
    ERROR_HTTP("Error httpgeneral"),

    //Advertencia para validaciones, no corta el flujo de la peticion pero si es una observacion a tener en cuenta si se presenta.
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
