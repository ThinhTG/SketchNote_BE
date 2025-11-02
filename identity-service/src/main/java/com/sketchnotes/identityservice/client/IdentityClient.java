package com.sketchnotes.identityservice.client;

import com.sketchnotes.identityservice.dtos.identity.*;
import com.sketchnotes.identityservice.dtos.request.RoleKeycloakRequest;
import com.sketchnotes.identityservice.dtos.response.RoleResponseKeycloak;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "identity-client", url = "${idp.url}")
public interface IdentityClient {
    @PostMapping(
            value = "/realms/${idp.client-id}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    TokenExchangeResponse exchangeClientToken(TokenExchangeParam param);

    @PostMapping(value = "/admin/realms/${idp.client-id}/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(@RequestHeader("authorization") String token, @RequestBody UserCreationParam param);
//login
    @PostMapping(value = "/realms/${idp.client-id}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    LoginExchangeResponse login( LoginParam param);
    // Refresh token
    @PostMapping(value = "/realms/${idp.client-id}/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    LoginExchangeResponse refreshToken(RefreshTokenParam param);
    //login with google
    @PostMapping(value = "/realms/${idp.client-id}/protocol/openid-connect  /token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Headers("Content-Type: application/x-www-form-urlencoded")
    LoginExchangeResponse loginWithGoogle(GoogleLoginParam param);

    /**
     * Lấy danh sách role của user (realm-level)
     */
    @GetMapping("/admin/realms/${idp.client-id}/users/{userId}/role-mappings/realm")
   List<RoleResponseKeycloak> getUserRoles(
            @RequestHeader("authorization") String token,
            @PathVariable("userId") String userId
    );
    @GetMapping("/admin/realms/${idp.client-id}/roles")
   List<RoleResponseKeycloak> getRealmRoles(
            @RequestHeader("authorization") String token
    );
    /**
     * Gán role mới cho user (realm-level)
     */
    @PostMapping(value = "/admin/realms/${idp.client-id}/users/{userId}/role-mappings/realm",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> assignRolesToUser(
            @RequestHeader("authorization") String token,
            @PathVariable("userId") String userId,
            @RequestBody List<RoleKeycloakRequest> roles
    );
}
