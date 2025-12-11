package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.UserRequest;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.UserProfileWithSubscriptionResponse;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.ultils.PagedResponse;

import java.util.List;


public interface IUserService {
     UserResponse getUserById(Long id);
     PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize);
     UserResponse updateUser(Long id,  UserRequest request);
     void deleteUser(Long id);
    UserResponse getCurrentUser();
    UserResponse getUserByKeycloakId(String sub);
    UserResponse getUserByEmail(String email);
    List<UserResponse> getUsersByRole(Role role);
    // Get user profile with subscription info
    UserProfileWithSubscriptionResponse getUserProfileWithSubscription(Long userId);
}
