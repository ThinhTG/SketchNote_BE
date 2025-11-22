package com.sketchnotes.order_service.dtos.admin;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardResponseDTO {
    private UserStatsDTO userStats;
    private RevenueStatsDTO revenueStats;

    public AdminDashboardResponseDTO() {}

    public AdminDashboardResponseDTO(UserStatsDTO userStats, RevenueStatsDTO revenueStats, OverviewStatsDTO overviewStats, List<TopItemDTO> topSellingCourses, List<TopItemDTO> topSellingResources, List<TopDesignerDTO> topDesigners, List<SubscriptionStatDTO> subscriptionStats) {
        this.userStats = userStats;
        this.revenueStats = revenueStats;
        this.overviewStats = overviewStats;
        this.topSellingCourses = topSellingCourses;
        this.topSellingResources = topSellingResources;
        this.topDesigners = topDesigners;
        this.subscriptionStats = subscriptionStats;
    }

    private OverviewStatsDTO overviewStats;
    private List<TopItemDTO> topSellingCourses;
    private List<TopItemDTO> topSellingResources;
    private List<TopDesignerDTO> topDesigners;
    private List<SubscriptionStatDTO> subscriptionStats;

    public OverviewStatsDTO getOverviewStats() { return overviewStats; }
    public void setOverviewStats(OverviewStatsDTO overviewStats) { this.overviewStats = overviewStats; }

    public List<TopItemDTO> getTopSellingCourses() { return topSellingCourses; }
    public void setTopSellingCourses(List<TopItemDTO> topSellingCourses) { this.topSellingCourses = topSellingCourses; }

    public List<TopItemDTO> getTopSellingResources() { return topSellingResources; }
    public void setTopSellingResources(List<TopItemDTO> topSellingResources) { this.topSellingResources = topSellingResources; }

    public List<TopDesignerDTO> getTopDesigners() { return topDesigners; }
    public void setTopDesigners(List<TopDesignerDTO> topDesigners) { this.topDesigners = topDesigners; }

    public List<SubscriptionStatDTO> getSubscriptionStats() { return subscriptionStats; }
    public void setSubscriptionStats(List<SubscriptionStatDTO> subscriptionStats) { this.subscriptionStats = subscriptionStats; }

    public static class Builder {
        private UserStatsDTO userStats;
        private RevenueStatsDTO revenueStats;
        private OverviewStatsDTO overviewStats;
        private List<TopItemDTO> topSellingCourses;
        private List<TopItemDTO> topSellingResources;
        private List<TopDesignerDTO> topDesigners;
        private List<SubscriptionStatDTO> subscriptionStats;

        public Builder userStats(UserStatsDTO userStats) { this.userStats = userStats; return this; }
        public Builder revenueStats(RevenueStatsDTO revenueStats) { this.revenueStats = revenueStats; return this; }
        public Builder overviewStats(OverviewStatsDTO overviewStats) { this.overviewStats = overviewStats; return this; }
        public Builder topSellingCourses(List<TopItemDTO> topSellingCourses) { this.topSellingCourses = topSellingCourses; return this; }
        public Builder topSellingResources(List<TopItemDTO> topSellingResources) { this.topSellingResources = topSellingResources; return this; }
        public Builder topDesigners(List<TopDesignerDTO> topDesigners) { this.topDesigners = topDesigners; return this; }
        public Builder subscriptionStats(List<SubscriptionStatDTO> subscriptionStats) { this.subscriptionStats = subscriptionStats; return this; }

        public AdminDashboardResponseDTO build() {
            return new AdminDashboardResponseDTO(userStats, revenueStats, overviewStats, topSellingCourses, topSellingResources, topDesigners, subscriptionStats);
        }
    }

    public static class OverviewStatsDTO {
        private long totalOrders;
        private long totalEnrollments;

        public OverviewStatsDTO() {}
        public OverviewStatsDTO(long totalOrders, long totalEnrollments) {
            this.totalOrders = totalOrders;
            this.totalEnrollments = totalEnrollments;
        }
        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
        public long getTotalEnrollments() { return totalEnrollments; }
        public void setTotalEnrollments(long totalEnrollments) { this.totalEnrollments = totalEnrollments; }
    }

    public static class TopItemDTO {
        private Long id;
        private String name; // Optional, if we fetch it
        private long count;

        public TopItemDTO() {}
        public TopItemDTO(Long id, String name, long count) {
            this.id = id;
            this.name = name;
            this.count = count;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class TopDesignerDTO {
        private Long designerId;
        private BigDecimal totalRevenue;

        public TopDesignerDTO() {}
        public TopDesignerDTO(Long designerId, BigDecimal totalRevenue) {
            this.designerId = designerId;
            this.totalRevenue = totalRevenue;
        }
        public Long getDesignerId() { return designerId; }
        public void setDesignerId(Long designerId) { this.designerId = designerId; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    public static class SubscriptionStatDTO {
        private Long subscriptionId;
        private long count;

        public SubscriptionStatDTO() {}
        public SubscriptionStatDTO(Long subscriptionId, long count) {
            this.subscriptionId = subscriptionId;
            this.count = count;
        }
        public Long getSubscriptionId() { return subscriptionId; }
        public void setSubscriptionId(Long subscriptionId) { this.subscriptionId = subscriptionId; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static Builder builder() {
        return new Builder();
    }

    public UserStatsDTO getUserStats() {
        return userStats;
    }

    public void setUserStats(UserStatsDTO userStats) {
        this.userStats = userStats;
    }

    public RevenueStatsDTO getRevenueStats() {
        return revenueStats;
    }

    public void setRevenueStats(RevenueStatsDTO revenueStats) {
        this.revenueStats = revenueStats;
    }



    public static class UserStatsDTO {
        private long totalUsers;
        private long customers;
        private long designers;

        public UserStatsDTO() {}

        public UserStatsDTO(long totalUsers, long customers, long designers) {
            this.totalUsers = totalUsers;
            this.customers = customers;
            this.designers = designers;
        }

        public static Builder builder() {
            return new Builder();
        }

        public long getTotalUsers() {
            return totalUsers;
        }

        public void setTotalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
        }

        public long getCustomers() {
            return customers;
        }

        public void setCustomers(long customers) {
            this.customers = customers;
        }

        public long getDesigners() {
            return designers;
        }

        public void setDesigners(long designers) {
            this.designers = designers;
        }

        public static class Builder {
            private long totalUsers;
            private long customers;
            private long designers;

            public Builder totalUsers(long totalUsers) {
                this.totalUsers = totalUsers;
                return this;
            }

            public Builder customers(long customers) {
                this.customers = customers;
                return this;
            }

            public Builder designers(long designers) {
                this.designers = designers;
                return this;
            }

            public UserStatsDTO build() {
                return new UserStatsDTO(totalUsers, customers, designers);
            }
        }
    }

    public static class RevenueStatsDTO {
        private List<RevenueDataPoint> courseRevenue;
        private List<RevenueDataPoint> subscriptionRevenue;
        private List<RevenueDataPoint> resourceCommissionRevenue;
        private BigDecimal totalCourseRevenue;
        private BigDecimal totalSubscriptionRevenue;
        private BigDecimal totalResourceCommissionRevenue;

        public RevenueStatsDTO() {}

        public RevenueStatsDTO(List<RevenueDataPoint> courseRevenue, List<RevenueDataPoint> subscriptionRevenue, List<RevenueDataPoint> resourceCommissionRevenue, BigDecimal totalCourseRevenue, BigDecimal totalSubscriptionRevenue, BigDecimal totalResourceCommissionRevenue) {
            this.courseRevenue = courseRevenue;
            this.subscriptionRevenue = subscriptionRevenue;
            this.resourceCommissionRevenue = resourceCommissionRevenue;
            this.totalCourseRevenue = totalCourseRevenue;
            this.totalSubscriptionRevenue = totalSubscriptionRevenue;
            this.totalResourceCommissionRevenue = totalResourceCommissionRevenue;
        }

        public static Builder builder() {
            return new Builder();
        }

        public List<RevenueDataPoint> getCourseRevenue() {
            return courseRevenue;
        }

        public void setCourseRevenue(List<RevenueDataPoint> courseRevenue) {
            this.courseRevenue = courseRevenue;
        }

        public List<RevenueDataPoint> getSubscriptionRevenue() {
            return subscriptionRevenue;
        }

        public void setSubscriptionRevenue(List<RevenueDataPoint> subscriptionRevenue) {
            this.subscriptionRevenue = subscriptionRevenue;
        }

        public List<RevenueDataPoint> getResourceCommissionRevenue() {
            return resourceCommissionRevenue;
        }

        public void setResourceCommissionRevenue(List<RevenueDataPoint> resourceCommissionRevenue) {
            this.resourceCommissionRevenue = resourceCommissionRevenue;
        }

        public BigDecimal getTotalCourseRevenue() {
            return totalCourseRevenue;
        }

        public void setTotalCourseRevenue(BigDecimal totalCourseRevenue) {
            this.totalCourseRevenue = totalCourseRevenue;
        }

        public BigDecimal getTotalSubscriptionRevenue() {
            return totalSubscriptionRevenue;
        }

        public void setTotalSubscriptionRevenue(BigDecimal totalSubscriptionRevenue) {
            this.totalSubscriptionRevenue = totalSubscriptionRevenue;
        }

        public BigDecimal getTotalResourceCommissionRevenue() {
            return totalResourceCommissionRevenue;
        }

        public void setTotalResourceCommissionRevenue(BigDecimal totalResourceCommissionRevenue) {
            this.totalResourceCommissionRevenue = totalResourceCommissionRevenue;
        }

        public static class Builder {
            private List<RevenueDataPoint> courseRevenue;
            private List<RevenueDataPoint> subscriptionRevenue;
            private List<RevenueDataPoint> resourceCommissionRevenue;
            private BigDecimal totalCourseRevenue;
            private BigDecimal totalSubscriptionRevenue;
            private BigDecimal totalResourceCommissionRevenue;

            public Builder courseRevenue(List<RevenueDataPoint> courseRevenue) {
                this.courseRevenue = courseRevenue;
                return this;
            }

            public Builder subscriptionRevenue(List<RevenueDataPoint> subscriptionRevenue) {
                this.subscriptionRevenue = subscriptionRevenue;
                return this;
            }

            public Builder resourceCommissionRevenue(List<RevenueDataPoint> resourceCommissionRevenue) {
                this.resourceCommissionRevenue = resourceCommissionRevenue;
                return this;
            }

            public Builder totalCourseRevenue(BigDecimal totalCourseRevenue) {
                this.totalCourseRevenue = totalCourseRevenue;
                return this;
            }

            public Builder totalSubscriptionRevenue(BigDecimal totalSubscriptionRevenue) {
                this.totalSubscriptionRevenue = totalSubscriptionRevenue;
                return this;
            }

            public Builder totalResourceCommissionRevenue(BigDecimal totalResourceCommissionRevenue) {
                this.totalResourceCommissionRevenue = totalResourceCommissionRevenue;
                return this;
            }

            public RevenueStatsDTO build() {
                return new RevenueStatsDTO(courseRevenue, subscriptionRevenue, resourceCommissionRevenue, totalCourseRevenue, totalSubscriptionRevenue, totalResourceCommissionRevenue);
            }
        }
    }

    public static class RevenueDataPoint {
        private String period;
        private BigDecimal amount;

        public RevenueDataPoint() {}

        public RevenueDataPoint(String period, BigDecimal amount) {
            this.period = period;
            this.amount = amount;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
