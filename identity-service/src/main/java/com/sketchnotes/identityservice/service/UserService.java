package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.client.ProjectServiceClient;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.UserSubscription;
import com.sketchnotes.identityservice.dtos.request.UserRequest;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.dtos.response.UserProfileWithSubscriptionResponse;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {

    private final IUserRepository userRepository;
    private final ProjectServiceClient projectServiceClient;


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
    @Cacheable(value = "users", key = "#pageNo + '-' + #pageSize")
    public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize) {
        Pageable pageable =  PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.ASC, "createdAt"));
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
        return new PagedResponse<>(
                userResponses,
                users.getNumber(),
                users.getSize(),
                (int) users.getTotalElements(),
                users.getTotalPages(),
                users.isLast()
        );
    }
    
    @Override
    @CacheEvict(value = "users", allEntries = true)
    public UserResponse updateUser(Long id, UserRequest request) {
        User account = userRepository.findById(id).filter(User::isActive)
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
    @CacheEvict(value = "users", allEntries = true)
    public void deleteUser(Long id) {
        User account = userRepository.findById(id).filter(User::isActive)
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
    @Cacheable(value = "user", key = "#sub")
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

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).filter(User::isActive)
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
    public UserProfileWithSubscriptionResponse getUserProfileWithSubscription(Long userId) {
        User user = userRepository.findById(userId).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        // Get subscription info
        boolean hasActiveSubscription = user.hasActiveSubscription();
        String subscriptionType = "Free";
        LocalDateTime subscriptionEndDate = null;
        
        if (hasActiveSubscription) {
            UserSubscription activeSub = user.getActiveSubscription();
            if (activeSub != null) {
                subscriptionType = activeSub.getSubscriptionPlan().getPlanName();
                subscriptionEndDate = activeSub.getEndDate();
            }
        }
        
        // Get project quota info
        int maxProjects = user.getMaxProjects();
        Integer currentProjects = 0;
        try {
            currentProjects = projectServiceClient.getProjectCountByOwnerId(userId);
        } catch (Exception e) {
            log.error("Failed to get project count for user {}: {}", userId, e.getMessage());
        }
        
        boolean canCreateProject;
        if (maxProjects == -1) {
            canCreateProject = true; // Unlimited
        } else {
            canCreateProject = currentProjects < maxProjects;
        }
        
        return UserProfileWithSubscriptionResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                .hasActiveSubscription(hasActiveSubscription)
                .subscriptionType(subscriptionType)
                .subscriptionEndDate(subscriptionEndDate)
                .maxProjects(maxProjects)
                .currentProjects(currentProjects)
                .canCreateProject(canCreateProject)
                .build();
    }
}
