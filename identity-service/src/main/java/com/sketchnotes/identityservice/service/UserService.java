package com.sketchnotes.identityservice.service;

import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.UserSubscription;
import com.sketchnotes.identityservice.dtos.request.UserRequest;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.ultils.PagedResponse;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
        return toUserResponse(user);
    }

    @Override
    @Cacheable(value = "users", key = "#pageNo + '-' + #pageSize")
    public PagedResponse<UserResponse> getAllUsers(int pageNo, int pageSize) {
        Pageable pageable =  PageRequest.of(pageNo, pageSize);
        Page<User> users = userRepository.findAllByIsActiveTrue(pageable);
        List<UserResponse> userResponses = users.stream()
                .map(this::toUserResponse)
                .toList();
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
        return toUserResponse(account);
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
        return toUserResponse(user);
    }

    @Override
    @Cacheable(value = "user", key = "#sub")
    public UserResponse getUserByKeycloakId(String sub) {
        User user = userRepository.findByKeycloakId(sub).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toUserResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return toUserResponse(user);
    }
    
    /**
     * Helper method to convert User entity to UserResponse with subscription info
     */
    private UserResponse toUserResponse(User user) {
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
        
        return UserResponse.builder()
                .id(user.getId())
                .keycloakId(user.getKeycloakId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().toString())
                .avatarUrl(user.getAvatarUrl())
                // Subscription info
                .hasActiveSubscription(hasActiveSubscription)
                .subscriptionType(subscriptionType)
                .subscriptionEndDate(subscriptionEndDate)
                .maxProjects(user.getMaxProjects())
                .build();
    }
}
