package com.sketchnotes.order_service.dtos.admin;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardResponseDTO {
    private UserStatsDTO userStats;
    private RevenueStatsDTO revenueStats;

    public AdminDashboardResponseDTO() {}

    public AdminDashboardResponseDTO(UserStatsDTO userStats, RevenueStatsDTO revenueStats) {
        this.userStats = userStats;
        this.revenueStats = revenueStats;
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

    public static class Builder {
        private UserStatsDTO userStats;
        private RevenueStatsDTO revenueStats;

        public Builder userStats(UserStatsDTO userStats) {
            this.userStats = userStats;
            return this;
        }

        public Builder revenueStats(RevenueStatsDTO revenueStats) {
            this.revenueStats = revenueStats;
            return this;
        }

        public AdminDashboardResponseDTO build() {
            return new AdminDashboardResponseDTO(userStats, revenueStats);
        }
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
