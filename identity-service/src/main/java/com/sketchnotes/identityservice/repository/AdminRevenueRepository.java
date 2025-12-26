package com.sketchnotes.identityservice.repository;

import com.sketchnotes.identityservice.enums.TransactionType;
import com.sketchnotes.identityservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository chuyên dụng cho Admin Revenue Dashboard
 * Chỉ truy vấn các giao dịch tạo ra doanh thu cho Admin (Subscription, Token)
 */
@Repository
public interface AdminRevenueRepository extends JpaRepository<Transaction, Long> {

    // ==================== TỔNG DOANH THU ====================
    
    /**
     * Tổng doanh thu từ Subscription trong khoảng thời gian
     * Bao gồm cả SUBSCRIPTION và PURCHASE_SUBSCRIPTION để cover tất cả cases
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION') AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalSubscriptionRevenue(@Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end);
    
    /**
     * Tổng doanh thu từ Token/AI Credits trong khoảng thời gian
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type = 'PURCHASE_AI_CREDITS' AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalTokenRevenue(@Param("start") LocalDateTime start, 
                                    @Param("end") LocalDateTime end);
    
    /**
     * Tổng doanh thu từ bán khóa học trong khoảng thời gian
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalCourseRevenue(@Param("start") LocalDateTime start, 
                                     @Param("end") LocalDateTime end);
    
    /**
     * Tổng doanh thu từ tất cả nguồn (Subscription + Token + Course)
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION', 'PURCHASE_AI_CREDITS', 'COURSE_FEE') AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start, 
                               @Param("end") LocalDateTime end);
    
    // ==================== ĐẾM GIAO DỊCH ====================
    
    /**
     * Đếm số giao dịch subscription thành công
     * CHỈ đếm transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION') AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    Long countSubscriptionTransactions(@Param("start") LocalDateTime start, 
                                       @Param("end") LocalDateTime end);
    
    /**
     * Đếm số giao dịch mua token thành công
     * CHỈ đếm transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type = 'PURCHASE_AI_CREDITS' AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    Long countTokenTransactions(@Param("start") LocalDateTime start, 
                                @Param("end") LocalDateTime end);
    
    /**
     * Đếm số giao dịch mua khóa học thành công
     * CHỈ đếm transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COUNT(t) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN " +
           "AND t.createdAt BETWEEN :start AND :end")
    Long countCourseTransactions(@Param("start") LocalDateTime start, 
                                 @Param("end") LocalDateTime end);
    
    // ==================== DOANH THU THEO NGÀY ====================
    
    /**
     * Doanh thu subscription theo ngày
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM-DD') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION') AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getSubscriptionRevenueByDay(@Param("start") LocalDateTime start, 
                                               @Param("end") LocalDateTime end);
    
    /**
     * Doanh thu token theo ngày
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM-DD') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type = 'PURCHASE_AI_CREDITS' AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getTokenRevenueByDay(@Param("start") LocalDateTime start, 
                                        @Param("end") LocalDateTime end);
    
    /**
     * Tổng doanh thu theo ngày (Subscription + Token + Course)
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM-DD') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION', 'PURCHASE_AI_CREDITS', 'COURSE_FEE') AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getTotalRevenueByDay(@Param("start") LocalDateTime start, 
                                        @Param("end") LocalDateTime end);
    
    /**
     * Doanh thu khóa học theo ngày
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM-DD') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getCourseRevenueByDay(@Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);
    
    // ==================== DOANH THU THEO THÁNG ====================
    
    /**
     * Doanh thu subscription theo tháng
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION') AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getSubscriptionRevenueByMonth(@Param("start") LocalDateTime start, 
                                                 @Param("end") LocalDateTime end);
    
    /**
     * Doanh thu token theo tháng
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type = 'PURCHASE_AI_CREDITS' AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getTokenRevenueByMonth(@Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
    
    /**
     * Tổng doanh thu theo tháng
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION', 'PURCHASE_AI_CREDITS', 'COURSE_FEE') AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getTotalRevenueByMonth(@Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
    
    /**
     * Doanh thu khóa học theo tháng
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY-MM') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getCourseRevenueByMonth(@Param("start") LocalDateTime start, 
                                           @Param("end") LocalDateTime end);
    
    // ==================== DOANH THU THEO NĂM ====================
    
    /**
     * Doanh thu subscription theo năm
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION') AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getSubscriptionRevenueByYear(@Param("start") LocalDateTime start, 
                                                @Param("end") LocalDateTime end);
    
    /**
     * Doanh thu token theo năm
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type = 'PURCHASE_AI_CREDITS' AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getTokenRevenueByYear(@Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);
    
    /**
     * Tổng doanh thu theo năm
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION', 'PURCHASE_AI_CREDITS', 'COURSE_FEE') AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getTotalRevenueByYear(@Param("start") LocalDateTime start, 
                                         @Param("end") LocalDateTime end);
    
    /**
     * Doanh thu khóa học theo năm
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query(value = "SELECT to_char(t.created_at, 'YYYY') as period, " +
                   "COALESCE(SUM(t.amount), 0) as amount, COUNT(*) as count " +
                   "FROM transaction t " +
                   "JOIN wallet w ON t.wallet_id = w.wallet_id " +
                   "JOIN users u ON w.user_id = u.id " +
                   "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
                   "AND u.role = 'ADMIN' " +
                   "AND t.created_at BETWEEN :start AND :end " +
                   "GROUP BY period ORDER BY period", nativeQuery = true)
    List<Object[]> getCourseRevenueByYear(@Param("start") LocalDateTime start, 
                                          @Param("end") LocalDateTime end);
    
    // ==================== WALLET OVERVIEW (THAM KHẢO) ====================
    
    /**
     * Tổng tiền user đã deposit (để tham khảo, không phải revenue)
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.type = 'DEPOSIT' AND t.status = 'SUCCESS'")
    BigDecimal getTotalUserDeposits();
    
    /**
     * Tổng tiền user đã withdraw (để tham khảo)
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.type = 'WITHDRAW' AND t.status = 'SUCCESS'")
    BigDecimal getTotalUserWithdrawals();
    
    /**
     * Tổng doanh thu từ tất cả thời gian (All-time revenue)
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION', 'PURCHASE_AI_CREDITS', 'COURSE_FEE') AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN")
    BigDecimal getAllTimeRevenue();
    
    /**
     * Tổng doanh thu subscription từ tất cả thời gian
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type IN ('SUBSCRIPTION', 'PURCHASE_SUBSCRIPTION') AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN")
    BigDecimal getAllTimeSubscriptionRevenue();
    
    /**
     * Tổng doanh thu token từ tất cả thời gian
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type = 'PURCHASE_AI_CREDITS' AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN")
    BigDecimal getAllTimeTokenRevenue();
    
    /**
     * Tổng doanh thu khóa học từ tất cả thời gian
     * CHỈ lấy transaction cộng vào ví của user có role ADMIN
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "JOIN t.wallet w JOIN w.user u " +
           "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
           "AND u.role = com.sketchnotes.identityservice.enums.Role.ADMIN")
    BigDecimal getAllTimeCourseRevenue();
    
    /**
     * Lấy top token/credit packages được mua nhiều nhất
     * Join với credit_transactions và credit_packages để lấy thông tin chi tiết
     * referenceId format: "PKG-{packageId}"
     * Note: total_revenue tính theo giá hiện tại của package * số lượng mua
     */
    @Query(value = "SELECT cp.id as package_id, cp.name as package_name, " +
           "COUNT(ct.id) as purchase_count, " +
           "COALESCE(SUM(cp.discounted_price), 0) as total_revenue " +
           "FROM credit_transactions ct " +
           "JOIN credit_packages cp ON CAST(SUBSTRING(ct.reference_id FROM 5) AS BIGINT) = cp.id " +
           "WHERE ct.type = 'PACKAGE_PURCHASE' " +
           "AND ct.reference_id LIKE 'PKG-%' " +
           "GROUP BY cp.id, cp.name " +
           "ORDER BY purchase_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopTokenPackages(@Param("limit") int limit);
    
    /**
     * Lấy top khóa học được mua nhiều nhất
     * Parse course name từ description (format: "Payment for course: {courseName}")
     * Vì không có course entity trong identity-service, ta group by description
     */
    @Query(value = "SELECT " +
           "COALESCE(NULLIF(TRIM(SUBSTRING(t.description FROM 'Payment for course: (.+)')), ''), t.description) as course_name, " +
           "COUNT(*) as purchase_count, " +
           "COALESCE(SUM(t.amount), 0) as total_revenue " +
           "FROM transaction t " +
           "WHERE t.type = 'COURSE_FEE' AND t.status = 'SUCCESS' " +
           "GROUP BY course_name " +
           "ORDER BY purchase_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> getTopCourses(@Param("limit") int limit);
}
