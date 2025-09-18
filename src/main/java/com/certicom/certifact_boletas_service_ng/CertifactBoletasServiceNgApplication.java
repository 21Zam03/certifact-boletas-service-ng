package com.certicom.certifact_boletas_service_ng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class CertifactBoletasServiceNgApplication {

	public static void main(String[] args) {
		SpringApplication.run(CertifactBoletasServiceNgApplication.class, args);
	}

}
