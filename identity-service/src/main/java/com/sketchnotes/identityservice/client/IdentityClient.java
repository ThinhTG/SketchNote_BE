package com.sketchnotes.identityservice.client;

import com.sketchnotes.identityservice.dto.identity.*;
import feign.Headers;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import feign.QueryMap;

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
}
