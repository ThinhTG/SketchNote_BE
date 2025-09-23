package com.sketchnotes.userservice.service.interfaces;

import com.sketchnotes.userservice.pojo.request.UserRequest;
import com.sketchnotes.userservice.pojo.response.UserResponse;
import com.sketchnotes.userservice.ultils.PagedResponse;


public interface IUserService {
    public UserResponse getUserById(Long id);
    public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize);
    public UserResponse updateUser(Long id,  UserRequest request);
    public void deleteUser(Long id);
}
