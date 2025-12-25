package com.sketchnotes.identityservice.service.implement;

import com.sketchnotes.identityservice.client.ProjectServiceClient;
import com.sketchnotes.identityservice.dtos.request.PurchaseSubscriptionRequest;
import com.sketchnotes.identityservice.dtos.response.SubscriptionPlanResponse;
import com.sketchnotes.identityservice.dtos.response.SubscriptionUpgradeCheckResponse;
import com.sketchnotes.identityservice.dtos.response.UserQuotaResponse;
import com.sketchnotes.identityservice.dtos.response.UserSubscriptionResponse;
import com.sketchnotes.identityservice.enums.PlanType;
import com.sketchnotes.identityservice.enums.Role;
import com.sketchnotes.identityservice.enums.SubscriptionStatus;
import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.enums.PaymentStatus;
import com.sketchnotes.identityservice.exception.AppException;
import com.sketchnotes.identityservice.exception.ErrorCode;
import com.sketchnotes.identityservice.model.SubscriptionPlan;
import com.sketchnotes.identityservice.model.User;
import com.sketchnotes.identityservice.model.UserSubscription;
import com.sketchnotes.identityservice.model.Wallet;
import com.sketchnotes.identityservice.model.Transaction;
import com.sketchnotes.identityservice.repository.ISubscriptionPlanRepository;
import com.sketchnotes.identityservice.repository.IUserRepository;
import com.sketchnotes.identityservice.repository.IUserSubscriptionRepository;
import com.sketchnotes.identityservice.repository.IWalletRepository;
import com.sketchnotes.identityservice.repository.ITransactionRepository;
import com.sketchnotes.identityservice.service.interfaces.IUserService;
import com.sketchnotes.identityservice.service.interfaces.IUserSubscriptionService;
import com.sketchnotes.identityservice.service.interfaces.IRoleService;
import com.sketchnotes.identityservice.service.interfaces.IWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionService implements IUserSubscriptionService {

    private final IUserSubscriptionRepository userSubscriptionRepository;
    private final ISubscriptionPlanRepository subscriptionPlanRepository;
    private final IUserRepository userRepository;
    private final IWalletRepository walletRepository;
    private final ITransactionRepository transactionRepository;
    private final ProjectServiceClient projectServiceClient;
    private final IRoleService roleService;
    private final IUserService userService;
    private final IWalletService walletService;

    @Override
    public SubscriptionUpgradeCheckResponse checkUpgrade(Long userId, Long planId) {
        log.info("Checking upgrade eligibility for user {} to plan {}", userId, planId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        
        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));
        
        if (!newPlan.getIsActive()) {
            throw new AppException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }
        
        // Check for active subscriptions - get the latest one
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository
                .findActiveSubscriptionsByUser(user, LocalDateTime.now());
        
        SubscriptionUpgradeCheckResponse.NewPlanInfo newPlanInfo = SubscriptionUpgradeCheckResponse.NewPlanInfo.builder()
                .planId(newPlan.getPlanId())
                .planName(newPlan.getPlanName())
                .planType(newPlan.getPlanType().name())
                .price(newPlan.getPrice())
                .durationDays(newPlan.getDurationDays())
                .build();
        
        if (activeSubscriptions.isEmpty()) {
            // No active subscription - can purchase freely
            return SubscriptionUpgradeCheckResponse.builder()
                    .canUpgrade(true)
                    .hasActiveSubscription(false)
                    .warningMessage(null)
                    .currentSubscription(null)
                    .newPlan(newPlanInfo)
                    .build();
        }
        
        // Has active subscription - get the latest one
        UserSubscription activeSub = activeSubscriptions.get(0);
        int remainingDays = (int) ChronoUnit.DAYS.between(LocalDateTime.now(), activeSub.getEndDate());
        
        SubscriptionUpgradeCheckResponse.CurrentSubscriptionInfo currentInfo = 
                SubscriptionUpgradeCheckResponse.CurrentSubscriptionInfo.builder()
                        .subscriptionId(activeSub.getSubscriptionId())
                        .planName(activeSub.getSubscriptionPlan().getPlanName())
                        .planType(activeSub.getSubscriptionPlan().getPlanType().name())
                        .endDate(activeSub.getEndDate())
                        .remainingDays(Math.max(0, remainingDays))
                        .build();
        
        // Check if trying to buy the same plan
        if (activeSub.getSubscriptionPlan().getPlanId().equals(planId)) {
            return SubscriptionUpgradeCheckResponse.builder()
                    .canUpgrade(false)
                    .hasActiveSubscription(true)
                    .warningMessage("You already have this subscription plan active. Please wait until it expires or cancel it first.")
                    .currentSubscription(currentInfo)
                    .newPlan(newPlanInfo)
                    .build();
        }
        
        // Can upgrade but with warning
        String warningMessage = String.format(
                "Your current subscription '%s' has not expired yet (%d days remaining). " +
                "If you proceed, your current subscription will be cancelled and replaced with the new plan '%s'. " +
                "Are you sure you want to upgrade?",
                activeSub.getSubscriptionPlan().getPlanName(),
                Math.max(0, remainingDays),
                newPlan.getPlanName()
        );
        
        return SubscriptionUpgradeCheckResponse.builder()
                .canUpgrade(true)
                .hasActiveSubscription(true)
                .warningMessage(warningMessage)
                .currentSubscription(currentInfo)
                .newPlan(newPlanInfo)
                .build();
    }

    @Override
    @Transactional
    public UserSubscriptionResponse purchaseSubscription(Long userId, PurchaseSubscriptionRequest request) {
        log.info("User {} purchasing subscription plan {}", userId, request.getPlanId());

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Get subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getPlanId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND));

        if (!plan.getIsActive()) {
            throw new AppException(ErrorCode.SUBSCRIPTION_PLAN_NOT_FOUND);
        }

        // Check for active subscriptions - get all and cancel them if user confirms upgrade
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository
                .findActiveSubscriptionsByUser(user, LocalDateTime.now());
        
        if (!activeSubscriptions.isEmpty()) {
            // Check if trying to buy the same plan (check against the latest active sub)
            UserSubscription latestActiveSub = activeSubscriptions.get(0);
            if (latestActiveSub.getSubscriptionPlan().getPlanId().equals(request.getPlanId())) {
                throw new AppException(ErrorCode.SUBSCRIPTION_ALREADY_ACTIVE);
            }
            
            // If user hasn't confirmed upgrade, return error with warning
            if (request.getConfirmUpgrade() == null || !request.getConfirmUpgrade()) {
                throw new AppException(ErrorCode.SUBSCRIPTION_UPGRADE_CONFIRMATION_REQUIRED);
            }
            
            // User confirmed upgrade - cancel ALL old active subscriptions
            log.info("User {} confirmed upgrade. Cancelling {} old subscription(s)", 
                    userId, activeSubscriptions.size());
            for (UserSubscription activeSub : activeSubscriptions) {
                activeSub.setStatus(SubscriptionStatus.CANCELLED);
                activeSub.setAutoRenew(false);
                userSubscriptionRepository.save(activeSub);
                log.info("Cancelled subscription {}", activeSub.getSubscriptionId());
            }
        }

        // Get user's wallet
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Check wallet balance
        if (wallet.getBalance().compareTo(plan.getPrice()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // Deduct from wallet
        wallet.setBalance(wallet.getBalance().subtract(plan.getPrice()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Create transaction record
        log.info("Creating transaction for subscription purchase. Amount: {}, Wallet ID: {}", 
                plan.getPrice(), wallet.getWalletId());
        
        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(plan.getPrice())
                .balance(wallet.getBalance())
                .type(TransactionType.SUBSCRIPTION)
                .status(PaymentStatus.SUCCESS)
                .provider("WALLET")
                .createdAt(LocalDateTime.now())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        log.info("Transaction saved successfully. Transaction ID: {}", savedTransaction.getTransactionId());
        
        if (savedTransaction.getTransactionId() == null) {
            log.error("Transaction ID is null after save!");
            throw new RuntimeException("Failed to create transaction - ID is null");
        }

        // Create subscription
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(plan.getDurationDays());
        
        String transactionIdStr = savedTransaction.getTransactionId().toString();
        log.info("Setting transaction ID in subscription: {}", transactionIdStr);

        UserSubscription subscription = UserSubscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(now)
                .endDate(endDate)
                .autoRenew(request.getAutoRenew())
                .transactionId(transactionIdStr)
                .build();
        user.setMaxProjects(user.getMaxProjects() + plan.getNumberOfProjects());
        userRepository.save(user);
        UserSubscription savedSubscription = userSubscriptionRepository.save(subscription);

        // Upgrade role if Designer plan
        if (plan.getPlanType() == PlanType.DESIGNER && user.getRole() != Role.DESIGNER) {
            user.setRole(Role.DESIGNER);
            userRepository.save(user);

            // Also update role in Keycloak
            try {
                roleService.assignRoleByName(userId, "DESIGNER");
            } catch (Exception e) {
               throw new AppException(ErrorCode.ROLE_ASSIGNMENT_FAILED);
            }
        }

        // cộng tiền cho admin
        var Admin = userService.getUsersByRole(Role.ADMIN);
        if (Admin.isEmpty()) {
            log.error("No admin user found to receive course fee");
        } else {
            var adminUser = Admin.get(0); // Lấy admin đầu tiên
            Wallet adminWallet = walletService.getWalletByUserId(adminUser.getId());
            if (adminWallet == null) {
                // Tạo wallet nếu chưa có
                adminWallet = walletService.createWallet(adminUser.getId());
            }
            walletService.depositWithType(
                adminWallet.getWalletId(), 
                plan.getPrice(), 
                com.sketchnotes.identityservice.enums.TransactionType.SUBSCRIPTION, 
                "Admin revenue from subscription: " + plan.getName()
            );
        }

        return mapToResponse(savedSubscription);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long userId, Long subscriptionId) {

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (!subscription.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setAutoRenew(false);
        userSubscriptionRepository.save(subscription);
    }

    @Override
    public UserSubscriptionResponse getActiveSubscription(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<UserSubscription> activeSubscriptions = userSubscriptionRepository
                .findActiveSubscriptionsByUser(user, LocalDateTime.now());
        
        if (activeSubscriptions.isEmpty()) {
            return null;
        }
        
        // Return the latest active subscription
        return mapToResponse(activeSubscriptions.get(0));
    }

    @Override
    public List<UserSubscriptionResponse> getUserSubscriptions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<UserSubscription> subscriptions = userSubscriptionRepository.findByUser(user);
        return subscriptions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserQuotaResponse getUserQuota(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Get current project count from project-service
        Integer currentProjects = 0;
        try {
            currentProjects = projectServiceClient.getProjectCountByOwnerId(userId);
        } catch (Exception e) {
            log.error("Failed to get project count for user {}: {}", userId, e.getMessage());
        }

        // Check if user has active subscription
        boolean hasActiveSubscription = user.hasActiveSubscription();
        int maxProjects = user.getMaxProjects(); // -1 for unlimited, 3 for free

        String subscriptionType = "Free";
        if (hasActiveSubscription) {
            UserSubscription activeSub = user.getActiveSubscription();
            if (activeSub != null) {
                subscriptionType = activeSub.getSubscriptionPlan().getPlanName();
            }
        }

        boolean canCreateProject;
        Integer remainingProjects = null;

        if (maxProjects == -1) {
            // Unlimited
            canCreateProject = true;
            remainingProjects = null;
        } else {
            // Limited (free tier: 3 projects)
            remainingProjects = Math.max(0, maxProjects - currentProjects);
            canCreateProject = currentProjects < maxProjects;
        }

        return UserQuotaResponse.builder()
                .maxProjects(maxProjects)
                .currentProjects(currentProjects)
                .remainingProjects(remainingProjects)
                .subscriptionType(subscriptionType)
                .hasActiveSubscription(hasActiveSubscription)
                .canCreateProject(canCreateProject)
                .build();
    }

    @Override
    public boolean checkProjectQuota(Long userId) {
        UserQuotaResponse quota = getUserQuota(userId);
        return quota.getCanCreateProject();
    }
    
    @Override
    public boolean hasActiveSubscription(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElse(null);
            if (user == null) {
                return false;
            }
            return user.hasActiveSubscription();
        } catch (Exception e) {
            log.error("Error checking active subscription for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * *") // Run daily at midnight
    @Transactional
    public void processExpiredSubscriptions() {
        log.info("Processing expired subscriptions...");

        List<UserSubscription> expiredSubscriptions = 
                userSubscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());

        for (UserSubscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            userSubscriptionRepository.save(subscription);
            log.info("Marked subscription {} as EXPIRED", subscription.getSubscriptionId());
        }

        log.info("Processed {} expired subscriptions", expiredSubscriptions.size());
    }

    private UserSubscriptionResponse mapToResponse(UserSubscription subscription) {
        SubscriptionPlanResponse planResponse = SubscriptionPlanResponse.builder()
                .planId(subscription.getSubscriptionPlan().getPlanId())
                .planName(subscription.getSubscriptionPlan().getPlanName())
                .planType(subscription.getSubscriptionPlan().getPlanType())
                .price(subscription.getSubscriptionPlan().getPrice())
                .currency(subscription.getSubscriptionPlan().getCurrency())
                .durationDays(subscription.getSubscriptionPlan().getDurationDays())
                .description(subscription.getSubscriptionPlan().getDescription())
                .isActive(subscription.getSubscriptionPlan().getIsActive())
                .createdAt(subscription.getSubscriptionPlan().getCreatedAt())
                .numberOfProjects(subscription.getSubscriptionPlan().getNumberOfProjects())
                .updatedAt(subscription.getSubscriptionPlan().getUpdatedAt())
                .build();

        return UserSubscriptionResponse.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .userId(subscription.getUser().getId())
                .plan(planResponse)
                .status(subscription.getStatus())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .autoRenew(subscription.getAutoRenew())
                .transactionId(subscription.getTransactionId())
                .createdAt(subscription.getCreatedAt())
                .isCurrentlyActive(subscription.isCurrentlyActive())
                .build();
    }
}
