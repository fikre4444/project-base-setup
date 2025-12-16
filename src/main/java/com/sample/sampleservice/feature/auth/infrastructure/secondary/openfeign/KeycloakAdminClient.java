package com.sample.sampleservice.feature.auth.infrastructure.secondary.openfeign;

import com.sample.sampleservice.feature.auth.domain.model.UserDetails;
import com.sample.sampleservice.feature.auth.domain.model.UserRequest;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.Credential;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.RoleRepresentation;
import com.sample.sampleservice.feature.auth.infrastructure.secondary.model.UserRequestDto;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "keycloak-admin-client",
        url = "${keycloak.auth-server-url}",
        path = "/admin/realms/${keycloak.realm}",
        configuration = FeignClientsConfiguration.class)
public interface KeycloakAdminClient {

    @GetMapping(value = "/users")
    List<UserDetails> getUserByUsername(@RequestHeader(value = "Authorization") String bearerToken, @RequestParam(value = "username") String username, @RequestParam(value = "email") String email);

    @PutMapping(value = "/users/{id}/reset-password")
    void resetPassword(@RequestHeader(value = "Authorization") String bearerToken, @PathVariable(value = "id") String id, @RequestBody @Valid Credential credential);

    @PutMapping(value = "/users/{id}/reset-password-email")
    void forgotPassword(@RequestHeader(value = "Authorization") String bearerToken, @PathVariable(value = "id") String id);

    @GetMapping(value = "/users/{userId}")
    UserDetails getUser(@RequestHeader("Authorization") final String token, @PathVariable("userId") final String id);

    @RequestMapping(value = "/users", method = RequestMethod.POST)
    Boolean registerUser(@RequestHeader("Authorization") final String token,
                                         @RequestBody final UserRequestDto user);

    @PostMapping(value = "/users/{id}/logout")
    void logout(@RequestHeader(value = "Authorization") String bearerToken, @PathVariable(value = "id") String id);

    @PutMapping(value = "/users/{id}/send-verify-email")
    void sendVerifyEmail(@RequestHeader(value = "Authorization") String bearerToken, @PathVariable(value = "id") String id);

    @PostMapping(value = "/users/{userId}/role-mappings/realm")
    ResponseEntity<?> setRoles(@RequestHeader("Authorization") final String token,
                               @PathVariable("userId") final String id,
                               @RequestBody final List<RoleRepresentation> roles);

    @GetMapping(value = "/users/{userId}/role-mappings/realm/available")
    List<RoleRepresentation> roles(@RequestHeader("Authorization") final String token,
                                                   @PathVariable("userId") final String id);

    @GetMapping(value = "/roles")
    List<RoleRepresentation> roles(@RequestHeader("Authorization") final String token,
                                   @RequestParam(value = "first") Integer first,
                                   @RequestParam(value = "max") Integer max,
                                   @RequestParam(value = "search") String search);

    @GetMapping(value = "/users/{userId}/role-mappings/realm")
    List<RoleRepresentation> myRoles(@RequestHeader("Authorization") final String token,
                                     @PathVariable("userId") final String id);

    @DeleteMapping(value = "/users/{userId}/role-mappings/realm")
    void removeRoles(@RequestHeader("Authorization") final String token,
                     @PathVariable("userId") final String id);

    @GetMapping(value = "/users")
    List<UserDetails> search(@RequestHeader(value = "Authorization") String bearerToken, @RequestParam(value = "emailVerified") Boolean emailVerified, @RequestParam(value = "enabled") Boolean enabled, @RequestParam(value = "exact") Boolean exact, @RequestParam(value = "first") Integer first, @RequestParam(value = "max") Integer max, @RequestParam(value = "search") String search, @RequestParam(value = "briefRepresentation") Boolean briefRepresentation);

    @GetMapping(value = "/roles/{role-name}/users")
    List<UserDetails> search(@RequestHeader(value = "Authorization") String bearerToken, @PathVariable(value = "role-name") String roleName, @RequestParam(value = "first") Integer first, @RequestParam(value = "max") Integer max, @RequestParam(value = "briefRepresentation") Boolean briefRepresentation);

    @GetMapping(value = "/users/count")
    Integer count(@RequestHeader(value = "Authorization") String bearerToken, @RequestParam(value = "emailVerified") Boolean emailVerified, @RequestParam(value = "enabled") Boolean enabled, @RequestParam(value = "exact") Boolean exact, @RequestParam(value = "search") String search);
      
    @GetMapping(value = "/users/{userId}")
    UserDetails getUser(@RequestHeader("Authorization") final String token, @PathVariable("userId") final String id, @RequestParam(value = "briefRepresentation") Boolean briefRepresentation);

    @PutMapping(value = "/users/{userId}")
    UserDetails updateUser(@RequestHeader("Authorization") final String token,
                           @PathVariable("userId") final String id,
                           @RequestBody UserRequest user);

    @GetMapping(value = "/users")
    List<UserDetails> getUsers(@RequestHeader(value = "Authorization") String bearerToken,
                           @RequestParam(value = "first") int first,
                           @RequestParam(value = "max") int max,
                           @RequestParam(value = "briefRepresentation") boolean briefRepresentation);

    @GetMapping(value = "/roles")
    List<RoleRepresentation> getAllRoles(@RequestHeader(value = "Authorization") String bearerToken);

    @GetMapping(value = "/roles/{roleName}/users")
    List<UserDetails> getUsersInRole(@RequestHeader(value = "Authorization") String bearerToken,
                                    @PathVariable("roleName") String roleName,
                                    @RequestParam("first") int first,
                                    @RequestParam("max") int max);
                                    
    @RequestMapping(method = RequestMethod.DELETE, value = "/users/{userId}/role-mappings/realm")
    void removeRoles(@RequestHeader("Authorization") final String token,
                    @PathVariable("userId") final String id,
                    @RequestBody final List<RoleRepresentation> roles);

    @GetMapping(value = "/attack-detection/brute-force/users/{userId}")
    Map<String, Object> getBruteForceStatus(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable("userId") String userId
    );
}
