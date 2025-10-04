package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dto.ApiResponse;
import com.sketchnotes.identityservice.dto.request.UserRequest;
import com.sketchnotes.identityservice.dto.response.UserResponse;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/users")
public class UserController {
    private final IUserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>>getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }
    @GetMapping("/keycloak/{sub}")
    public ResponseEntity<ApiResponse<UserResponse>>getUserById(@PathVariable String sub) {
        UserResponse response = userService.getUserByKeycloakId(sub);
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>>getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PagedResponse<UserResponse> response = userService.getAllUsers(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest request) {
        UserResponse response =  userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success( response,"Update successful"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success( null,"Delete successful"));
    }
}
