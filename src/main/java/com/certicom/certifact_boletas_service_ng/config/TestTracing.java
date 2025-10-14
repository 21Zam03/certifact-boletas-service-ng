package com.certicom.certifact_boletas_service_ng.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TestTracing {

    @Autowired
    public TestTracing(ApplicationContext ctx) {
        System.out.println("Feign Client Instrumentado: " + ctx.containsBean("tracingFeignClient"));
    }

}
