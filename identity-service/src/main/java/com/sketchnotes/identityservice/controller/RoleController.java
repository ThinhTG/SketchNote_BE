package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dto.ApiResponse;
import com.sketchnotes.identityservice.dto.request.RoleRequest;
import com.sketchnotes.identityservice.dto.response.RoleResponseKeycloak;
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<List<RoleResponseKeycloak>>> getAllRoles() {
        List<RoleResponseKeycloak> response = roleService.getAllRoles();
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }
    @PostMapping("/{id}")
    public ResponseEntity<ApiResponse<List<RoleResponseKeycloak>>> updateRoleUser(@RequestBody RoleRequest request) {
        List<RoleResponseKeycloak> response = roleService.getAllRoles();
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }
}
