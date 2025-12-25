package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.request.SubscriptionPlanRequest;
import com.sketchnotes.identityservice.dtos.response.SubscriptionPlanResponse;
import com.sketchnotes.identityservice.dtos.response.UserResponse;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.SubscriptionPlan;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.UserSubscription;
import com.sketchnotes.identityservice.repository.ISubscriptionPlanRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.IUserSubscriptionRepository;
import com.sketchnotes.identityservice.service.interfaces.ISubscriptionPlanService;
import com.sketchnotes.identityservice.ultils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService implements ISubscriptionPlanService {

    private final ISubscriptionPlanRepository subscriptionPlanRepository;
    private final IUserSubscriptionRepository userSubscriptionRepository;
   private final IUserRepository userRepository;
    @Override
    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        log.info("Creating subscription plan: {}", request.getPlanName());

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .planName(request.getPlanName())
                .planType(request.getPlanType())
                .price(request.getPrice())
                .numberOfProjects(request.getNumberOfProjects())
                .currency(request.getCurrency())
                .durationDays(request.getDurationDays())
                .description(request.getDescription())
                .isActive(request.getIsActive())
                .build();

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(plan);

        return mapToResponse(savedPlan);
    }

    @Override
    @Transactional
    public SubscriptionPlanResponse updatePlan(Long planId, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        plan.setPlanName(request.getPlanName());
        plan.setPlanType(request.getPlanType());
        plan.setPrice(request.getPrice());
        plan.setNumberOfProjects(request.getNumberOfProjects());
        plan.setCurrency(request.getCurrency());
        plan.setDurationDays(request.getDurationDays());
        plan.setDescription(request.getDescription());
        plan.setIsActive(request.getIsActive());

        SubscriptionPlan updatedPlan = subscriptionPlanRepository.save(plan);

        return mapToResponse(updatedPlan);
    }

    @Override
    public List<SubscriptionPlanResponse> getAllActivePlans() {
        User user = userRepository.findByKeycloakId(SecurityUtils.getCurrentUserId()).filter(User::isActive)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActiveTrue();
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository
                .findActiveSubscriptionsByUser(user, LocalDateTime.now());

        if (!activeSubscriptions.isEmpty()) {
            UserSubscription latestActiveSub = activeSubscriptions.get(0);

            return plans.stream()
                    .map(plan -> mapToResponse(plan, latestActiveSub.getSubscriptionPlan().getPrice().compareTo(plan.getPrice()) < 0))
                    .sorted((Comparator.comparing(SubscriptionPlanResponse::getPrice)))
                    .collect(Collectors.toList());
        }
        return plans.stream()
                .map(this::mapToResponse)
                .sorted((Comparator.comparing(SubscriptionPlanResponse::getPrice)))
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanResponse getPlanById(Long planId) {
        log.info("Fetching subscription plan ID: {}", planId);
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return mapToResponse(plan);
    }

    @Override
    @Transactional
    public void deactivatePlan(Long planId) {
        log.info("Deactivating subscription plan ID: {}", planId);

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        plan.setIsActive(false);
        subscriptionPlanRepository.save(plan);

        log.info("Deactivated subscription plan ID: {}", planId);
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .planType(plan.getPlanType())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .durationDays(plan.getDurationDays())
                .description(plan.getDescription())
                .isBuy(true)
                .isActive(plan.getIsActive())
                .numberOfProjects(plan.getNumberOfProjects())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan, boolean isBuy) {
        return SubscriptionPlanResponse.builder()
                .planId(plan.getPlanId())
                .planName(plan.getPlanName())
                .planType(plan.getPlanType())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .isBuy(isBuy)
                .numberOfProjects(plan.getNumberOfProjects())
                .durationDays(plan.getDurationDays())
                .description(plan.getDescription())
                .isActive(plan.getIsActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
