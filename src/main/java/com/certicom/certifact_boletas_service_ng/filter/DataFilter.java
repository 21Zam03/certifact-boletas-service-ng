package com.certicom.certifact_boletas_service_ng.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class DataFilter implements Filter {

    public static final String RUC_CLIENT = "X-RUC-Client";
    public static final String X_ID_USER = "X-ID-User";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String ruc = request.getHeader(RUC_CLIENT);
        String id = request.getHeader(X_ID_USER);

        if (ruc != null) {
            MDC.put("ruc", ruc);
        }

        if (id != null) {
            MDC.put("id", id);
        }

        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            MDC.clear();
        }

    }

}
