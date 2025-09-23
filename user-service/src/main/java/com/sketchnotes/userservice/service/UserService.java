package com.sketchnotes.userservice.service;

import com.sketchnotes.userservice.model.User;
import com.sketchnotes.userservice.pojo.request.UserRequest;
import com.sketchnotes.userservice.pojo.response.UserResponse;
import com.sketchnotes.userservice.repository.IUserRepository;
import com.sketchnotes.userservice.service.interfaces.IUserService;
import com.sketchnotes.userservice.ultils.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService, IUserService {

    private final IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        return this.userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }

    @Override
    public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize) {
        Pageable pageable = (Pageable) PageRequest.of(pageNo, pageSize);
        Page<User> users = userRepository.findAll(pageable);
        List<UserResponse> userResponses = users.stream().map(user -> UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
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
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));

        account.setFullName(request.getFullName());
        account.setAvatarUrl(request.getAvatarUrl());
        account.setUpdateAt(LocalDateTime.now());
        account = userRepository.save(account);
        return UserResponse.builder()
                .id(account.getId())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .role(account.getRole().toString())
                .avatarUrl(account.getAvatarUrl())
                .build();
    }

    @Override
    public void deleteUser(Long id) {
        User account = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Not found"));
        account.setActive(false);
        account.setUpdateAt(LocalDateTime.now());
         userRepository.save(account);
    }
}
