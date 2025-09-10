package com.certicom.certifact_boletas_service_ng.feign;

import com.certicom.certifact_boletas_service_ng.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "boletas-service-sp", url = "http://localhost:8090", contextId = "user")
public interface UserFeign {

    @GetMapping("/api/user/idUser")
    UserDto findUserById(@RequestParam Long idUser);

    @GetMapping("/api/user/username")
    public UserDto findUserByUsername(@RequestParam String username);

}
