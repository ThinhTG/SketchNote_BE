package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dto.request.RoleRequest;
import com.sketchnotes.identityservice.dto.response.RoleResponseKeycloak;

import java.util.List;

public interface IRoleService {
    List<RoleResponseKeycloak> getAllRoles();
    void updateRolesForUser(RoleRequest request);
}
