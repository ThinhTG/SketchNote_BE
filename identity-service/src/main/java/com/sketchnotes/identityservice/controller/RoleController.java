package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.RoleRequest;
import com.sketchnotes.identityservice.dtos.response.RoleResponseKeycloak;
import com.sketchnotes.identityservice.service.interfaces.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/roles")
public class RoleController {
    private  final IRoleService roleService;
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<RoleResponseKeycloak>>> getAllRoles() {
        List<RoleResponseKeycloak> response = roleService.getAllRoles();
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }
    @PostMapping("")
    public ResponseEntity<ApiResponse<String>> updateRoleUser(@RequestBody RoleRequest request) {
        roleService.updateRolesForUser(request);
        return ResponseEntity.ok( ApiResponse.success( null,"Get data successful" ));
    }
}
