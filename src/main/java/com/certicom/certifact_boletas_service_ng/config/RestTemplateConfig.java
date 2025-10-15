package com.certicom.certifact_boletas_service_ng.config;

import com.certicom.certifact_boletas_service_ng.interceptor.RestTemplateInteceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class RestTemplateConfig {

    private final RestTemplateInteceptor restTemplateInteceptor;

    public RestTemplateConfig(RestTemplateInteceptor restTemplateInteceptor) {
        this.restTemplateInteceptor = restTemplateInteceptor;
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder.build();
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add(restTemplateInteceptor);
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

}
