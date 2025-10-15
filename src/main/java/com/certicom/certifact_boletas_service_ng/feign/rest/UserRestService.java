package com.certicom.certifact_boletas_service_ng.feign.rest;

import com.certicom.certifact_boletas_service_ng.dto.UserDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class UserRestService {

    private final RestTemplate restTemplate;

    @Value("${external.services.boleta-service-sp.base-url}")
    private String baseUrl;

    @Value("${external.services.boleta-service-sp.endpoints.api-user-endpoint}")
    private String apiUserEndpoint;

    public UserRestService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserDto findUserById(Long idUser) {
        String url = UriComponentsBuilder
                .fromHttpUrl(getUrlEndpoint()+ "/idUser")
                .queryParam("idUser", idUser)
                .toUriString();
        ResponseEntity<UserDto> response = restTemplate.exchange(
                url, HttpMethod.GET, null, UserDto.class);
        return response.getBody();
    }

    public UserDto findUserByUsername(String username) {
        String url = UriComponentsBuilder
                .fromHttpUrl(getUrlEndpoint() + "/username")
                .queryParam("username", username)
                .toUriString();
        ResponseEntity<UserDto> response = restTemplate.exchange(
                url, HttpMethod.GET, null, UserDto.class);
        return response.getBody();
    }

    private String getUrlEndpoint() {
        return this.baseUrl+this.apiUserEndpoint;
    }

}
