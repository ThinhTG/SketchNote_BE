package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.dto.request.UserRequest;
import com.sketchnotes.identityservice.dto.response.UserResponse;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserRepository userRepository;


    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize) {
        Pageable pageable =  PageRequest.of(pageNo, pageSize);
        Page<User> users = userRepository.findAllByIsActiveTrue(pageable);
        List<UserResponse> userResponses = users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                .build()).toList();
        return new PagedResponse(
                userResponses,
                users.getNumber(),
                users.getSize(),
                (int) users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
    }
    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User account = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        account.setFirstName(request.getFirstName());
        account.setLastName(request.getLastName());
        account.setAvatarUrl(request.getAvatarUrl());
        account.setUpdateAt(LocalDateTime.now());
        account = userRepository.save(account);
        return UserResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .firstName(account.getFirstName())
                .lastName(account.getLastName())
                .role(account.getRole().toString())
                .avatarUrl(account.getAvatarUrl())
                .build();
    }

    @Override
    public void deleteUser(Long id) {
        User account = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        account.setActive(false);
        account.setUpdateAt(LocalDateTime.now());
         userRepository.save(account);
    }

    @Override
    public UserResponse getCurrentUser() {
        User user = userRepository.findByKeycloakId(SecurityUtils.getCurrentUserId()).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public UserResponse getUserByKeycloakId(String sub) {
        User user = userRepository.findByKeycloakId(sub).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
