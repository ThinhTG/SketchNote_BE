package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dto.request.UserRequest;
import com.sketchnotes.identityservice.dto.response.UserResponse;
import com.sketchnotes.identityservice.ultils.PagedResponse;


public interface IUserService {
     UserResponse getUserById(Long id);
     PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize);
     UserResponse updateUser(Long id,  UserRequest request);
     void deleteUser(Long id);
    UserResponse getCurrentUser();
    UserResponse getUserByKeycloakId(String sub);
}
