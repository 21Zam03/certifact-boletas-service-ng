package com.certicom.certifact_boletas_service_ng.util;

public class LogMessages {

    public static String currentMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }

}
