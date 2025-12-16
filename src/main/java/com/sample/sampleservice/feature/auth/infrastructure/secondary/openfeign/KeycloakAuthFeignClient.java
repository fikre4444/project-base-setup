package com.sample.sampleservice.feature.auth.infrastructure.secondary.openfeign;

import com.sample.sampleservice.feature.auth.domain.model.OAuth2TokenResult;
import com.sample.sampleservice.feature.auth.domain.model.UserLogin;
import com.sample.sampleservice.shared.openfeign.infrastructure.secondary.FeignFormEncoderConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@FeignClient(name = "keycloak-auth-client",
        url = "${keycloak.auth-server-url}",
        path = "/realms/${keycloak.realm}",
        configuration = FeignFormEncoderConfiguration.class)
public interface KeycloakAuthFeignClient {

    @RequestMapping(value = "/protocol/openid-connect/token", method = POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    OAuth2TokenResult refresh(UserLogin authRequestDTO);

    @PostMapping(value = "/users/{id}/logout")
    void logout(@RequestHeader(value = "Authorization") String bearerToken, @PathVariable(value = "id") String id);
}
