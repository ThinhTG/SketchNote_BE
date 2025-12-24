package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.response.TopSellingCourseDTO;
import com.sketchnotes.learning.repository.CourseEnrollmentRepository;
import com.sketchnotes.learning.service.interfaces.IAdminCourseStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for Admin Course Statistics.
 * Handles all business logic for course statistics and analytics.
 */
@Service
@RequiredArgsConstructor
public class AdminCourseStatsService implements IAdminCourseStatsService {
    
    private final CourseEnrollmentRepository courseEnrollmentRepository;
    
    @Override
    public List<TopSellingCourseDTO> getTopSellingCourses(int limit) {
        List<Object[]> results = courseEnrollmentRepository.findTopSellingCourses();
        
        return results.stream()
                .limit(limit)
                .map(row -> TopSellingCourseDTO.builder()
                        .courseId((Long) row[0])
                        .enrollmentCount((Long) row[1])
                        .title((String) row[2])
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    public long getTotalEnrollments() {
        return courseEnrollmentRepository.countTotalEnrollments();
    }
}
