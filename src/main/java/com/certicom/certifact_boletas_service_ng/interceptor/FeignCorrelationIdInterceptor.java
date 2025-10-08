package com.certicom.certifact_boletas_service_ng.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeignCorrelationIdInterceptor implements RequestInterceptor {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String SERVICE_NAME_HEADER = "X-Service-Name";
    private static final String SERVICE_NAME = "boletas-service-ng";

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String correlationId = MDC.get(CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString(); // fallback si no existe
        }

        String requestId = UUID.randomUUID().toString();

        requestTemplate.header(CORRELATION_ID_HEADER, correlationId);
        requestTemplate.header(REQUEST_ID_HEADER, requestId);
        requestTemplate.header(SERVICE_NAME_HEADER, SERVICE_NAME);

    }

}
