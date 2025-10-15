package com.certicom.certifact_boletas_service_ng.filter;

import com.certicom.certifact_boletas_service_ng.enums.LogTitle;
import com.certicom.certifact_boletas_service_ng.util.LogHelper;
import com.certicom.certifact_boletas_service_ng.util.LogMessages;
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
        long startTime = System.currentTimeMillis();

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
            LogHelper.infoLog(LogTitle.INFO.getType(), LogMessages.currentMethod(), "Incoming request ["+request.getMethod()+" "+request.getRequestURI()+"]");
            filterChain.doFilter(servletRequest, servletResponse);
            LogHelper.infoLog(LogTitle.INFO.getType(), LogMessages.currentMethod(), "Completed request: duration="+(System.currentTimeMillis() - startTime)+"ms");
        } finally {
            MDC.clear();
        }

    }

}
