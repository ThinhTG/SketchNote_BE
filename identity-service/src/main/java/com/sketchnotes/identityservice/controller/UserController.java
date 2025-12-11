package com.sketchnotes.identityservice.controller;

import com.sketchnotes.identityservice.dtos.ApiResponse;
import com.sketchnotes.identityservice.dtos.request.UserRequest;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.UserProfileWithSubscriptionResponse;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserResponse>>getUserByEmail(@PathVariable String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok( ApiResponse.success( response,"Get data successful" ));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ApiResponse<UserResponse>>getUserByIdPublic(@PathVariable Long id) {
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
    
    /**
     * Get current user profile with subscription information
     * This endpoint returns detailed subscription info including:
     * - Active subscription status
     * - Subscription type and expiry date
     * - Project quota (max, current, can create)
     */
    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserProfileWithSubscriptionResponse>> getCurrentUserProfile() {
        UserResponse currentUser = userService.getCurrentUser();
        UserProfileWithSubscriptionResponse profile = userService.getUserProfileWithSubscription(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(profile, "Get user profile with subscription successful"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {
        PagedResponse<UserResponse> response = userService.getAllUsers(pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success( response,"Get data successful"));
    }
    @GetMapping("/role")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@RequestParam Role role) {
        List<UserResponse> response = userService.getUsersByRole(role);
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
