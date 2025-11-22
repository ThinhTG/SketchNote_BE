package com.sketchnotes.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "learning-service")
public interface LearningClient {

    @GetMapping("/api/courses/admin/stats/top-selling")
    List<Map<String, Object>> getTopSellingCourses(@RequestParam(defaultValue = "5") int limit);

    @GetMapping("/api/courses/admin/stats/total-enrollments")
    long getTotalEnrollments();
}
