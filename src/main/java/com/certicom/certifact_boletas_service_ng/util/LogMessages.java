package com.certicom.certifact_boletas_service_ng.util;

public class LogMessages {

    public static String currentMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

    public static final String ERROR_UNEXPECTED = "Error inesperado";
    public static final String ERROR_HTTP = "Error al consultar servicio externo";

}
