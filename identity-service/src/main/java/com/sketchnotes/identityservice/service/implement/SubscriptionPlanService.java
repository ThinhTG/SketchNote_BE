package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.dtos.request.SubscriptionPlanRequest;
import com.sketchnotes.identityservice.dtos.response.SubscriptionPlanResponse;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.SubscriptionPlan;
import com.sketchnotes.identityservice.repository.ISubscriptionPlanRepository;
import com.sketchnotes.identityservice.service.interfaces.ISubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService implements ISubscriptionPlanService {

    private final ISubscriptionPlanRepository subscriptionPlanRepository;

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
        log.info("Fetching all active subscription plans");
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findByIsActiveTrue();
        return plans.stream()
                .map(this::mapToResponse)
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
                .isActive(plan.getIsActive())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
