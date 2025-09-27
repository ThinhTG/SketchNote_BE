package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dto.request.UserRequest;
import com.sketchnotes.identityservice.dto.response.UserResponse;
import com.sketchnotes.identityservice.ultils.PagedResponse;


public interface IUserService {
    public UserResponse getUserById(Long id);
    public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize);
    public UserResponse updateUser(Long id,  UserRequest request);
    public void deleteUser(Long id);
}
