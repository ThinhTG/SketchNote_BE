package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.client.IdentityClient;
import com.sketchnotes.identityservice.dtos.identity.TokenExchangeParam;
import com.sketchnotes.identityservice.dtos.identity.TokenExchangeResponse;
import com.sketchnotes.identityservice.dtos.request.RoleKeycloakRequest;
import com.sketchnotes.identityservice.dtos.request.RoleRequest;
import com.sketchnotes.identityservice.dtos.response.RoleResponseKeycloak;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.exception.ErrorNormalizer;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IRoleService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final IdentityClient identityClient;
    private final IUserRepository userRepository;
    private final ErrorNormalizer errorNormalizer;
    @Value("${idp.client-id}")
    @NonFinal
    String clientId;

    @Value("${idp.client-secret}")
    @NonFinal
    String clientSecret;
    @Override
    public List<RoleResponseKeycloak> getAllRoles() {
        try {
            TokenExchangeResponse token = identityClient.exchangeClientToken(TokenExchangeParam.builder()
                    .grant_type("client_credentials")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .scope("openid")
                    .build());
            return identityClient.getRealmRoles("Bearer " + token.getAccessToken());
        } catch (FeignException exception) {
            throw errorNormalizer.handleKeyCloakException(exception);
        }
    }

    @Override
    public void updateRolesForUser(RoleRequest request) {
        try {
            User user = userRepository.findById(request.getUserId()).filter(User::isActive)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            TokenExchangeResponse token = identityClient.exchangeClientToken(TokenExchangeParam.builder()
                    .grant_type("client_credentials")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .scope("openid")
                    .build());
            List<RoleResponseKeycloak> roles = identityClient.getRealmRoles("Bearer " + token.getAccessToken());
            RoleResponseKeycloak roleToAssign = roles.stream()
                    .filter(role -> role.getId().equals(request.getRoleId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

            RoleKeycloakRequest credential = RoleKeycloakRequest.builder()
                    .id(roleToAssign.getId())
                    .name(roleToAssign.getName())
                    .build();

            identityClient.assignRolesToUser("Bearer " + token.getAccessToken(), user.getKeycloakId(), List.of(credential));
            user.setRole(Role.valueOf(roleToAssign.getName()));
            userRepository.save(user);
        } catch (FeignException exception) {
            throw errorNormalizer.handleKeyCloakException(exception);
        }
    }
}
