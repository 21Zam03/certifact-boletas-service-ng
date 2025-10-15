package com.certicom.certifact_boletas_service_ng.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogHelper {

    public static void warnLog(String title, String currentMethod, String detail) {
        log.warn(LogTemplates.WARN_LOG_TEMPLATE,
                title,
                currentMethod,
                detail);
    }

    public static void infoLog(String title, String currentMethod, String detail) {
        log.info(LogTemplates.INFO_LOG_TEMPLATE,
                title,
                currentMethod,
                detail);
    }

    public static void errorLog(String title, String currentMethod, String detail, Throwable e) {
        log.error(LogTemplates.ERROR_LOG_TEMPLATE,
                title,
                currentMethod,
                detail,
                e);
    }

}
