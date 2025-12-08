package com.sketchnotes.identityservice.service.interfaces;

import com.sketchnotes.identityservice.dtos.request.CreateNotificationRequest;
import com.sketchnotes.identityservice.dtos.request.CreditPackageRequest;
import com.sketchnotes.identityservice.dtos.response.CreditPackageResponse;
import com.sketchnotes.identityservice.dtos.response.NotificationDto;
import com.sketchnotes.identityservice.dtos.response.PurchasePackageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface for notification management.
 * Handles notification creation, retrieval, and status updates.
 */
public interface INotificationService {
    
    /**
     * Create a new notification and push it to the user via WebSocket.
     *
     * @param request the notification creation request
     * @return the created notification DTO
     */
    NotificationDto create(CreateNotificationRequest request);
    
    /**
     * Get paginated notifications for a specific user.
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of notification DTOs
     */
    Page<NotificationDto> getNotifications(Long userId, Pageable pageable);
    
    /**
     * Mark a specific notification as read.
     *
     * @param userId the user ID (for authorization)
     * @param notificationId the notification ID
     */
    void markAsRead(Long userId, Long notificationId);
    
    /**
     * Mark all notifications for a user as read.
     *
     * @param userId the user ID
     */
    void markAllAsRead(Long userId);
    
    /**
     * Count unread notifications for a user.
     *
     * @param userId the user ID
     * @return count of unread notifications
     */
    long countUnread(Long userId);

    /**
     * Interface cho Credit Package Service
     */
    interface ICreditPackageService {

        /**
         * Tạo gói credit mới (Admin)
         */
        CreditPackageResponse createPackage(CreditPackageRequest request);

        /**
         * Cập nhật gói credit (Admin)
         */
        CreditPackageResponse updatePackage(Long id, CreditPackageRequest request);

        /**
         * Xóa gói credit (Admin) - soft delete bằng cách set isActive = false
         */
        void deletePackage(Long id);

        /**
         * Lấy thông tin một gói credit theo ID
         */
        CreditPackageResponse getPackageById(Long id);

        /**
         * Lấy tất cả gói credit đang active (cho User)
         */
        List<CreditPackageResponse> getActivePackages();

        /**
         * Lấy tất cả gói credit (cho Admin)
         */
        List<CreditPackageResponse> getAllPackages();

        /**
         * Kích hoạt/Vô hiệu hóa gói credit (Admin)
         */
        CreditPackageResponse togglePackageStatus(Long id);

        /**
         * Mua gói credit package (User)
         * @param userId ID của user
         * @param packageId ID của gói credit package
         * @return PurchasePackageResponse chứa thông tin giao dịch và số dư mới
         */
        PurchasePackageResponse purchasePackage(Long userId, Long packageId);
    }
}
