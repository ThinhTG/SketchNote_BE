package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.RoleRequest;
import com.sketchnotes.identityservice.dtos.response.RoleResponseKeycloak;

import java.util.List;

public interface IRoleService {
    List<RoleResponseKeycloak> getAllRoles();
    void updateRolesForUser(RoleRequest request);
    void assignRoleByName(Long userId, String roleName);
}
