package com.sketchnotes.learning.service.impl;

import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.service.interfaces.IAdminCourseStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of IAdminCourseStatsService.
 * Provides admin course statistics functionality.
 */
@Service
@RequiredArgsConstructor
public class AdminCourseStatsServiceImpl implements IAdminCourseStatsService {
    
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    
    @Override
    public List<Map<String, Object>> getTopSellingCourses(int limit) {
        List<Object[]> results = courseEnrollmentRepository.findTopSellingCourses();
        return results.stream()
                .limit(limit)
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("courseId", row[0]);
                    map.put("enrollmentCount", row[1]);
                    map.put("title", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public long getTotalEnrollments() {
        return courseEnrollmentRepository.countTotalEnrollments();
    }
}
