package com.sketchnotes.learning.controller;

import com.sketchnotes.learning.client.IdentityClient;
import com.sketchnotes.learning.dto.ApiResponse;
import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.UpdateCourseRatingRequest;
import com.sketchnotes.learning.service.interfaces.ICourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning/courses")
@RequiredArgsConstructor
public class CourseController {
    private final ICourseService courseService;
    private final IdentityClient identityClient;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(ApiResponse.success(courses, "Courses retrieved successfully"));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseById(@PathVariable Long id) {
        CourseDTO course = courseService.getCourseById(id);
        return ResponseEntity.ok(ApiResponse.success(course, "Course retrieved successfully"));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<CourseDTO>> createCourse(@RequestBody CourseDTO dto) {
        CourseDTO created = courseService.createCourse(dto);
        return new ResponseEntity<>(
                ApiResponse.success(created, "Course created successfully"),
                HttpStatus.CREATED
        );
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CourseDTO>> updateCourse(@PathVariable Long id, @RequestBody CourseDTO dto) {
        CourseDTO updated = courseService.updateCourse(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated, "Course updated successfully"));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Course deleted successfully"));
    }

    @GetMapping("/enrolled/me")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getEnrolledCourses() {
        var user = identityClient.getCurrentUser().getResult();
        List<CourseDTO> enrolledCourses = courseService.getEnrolledCourses(user.getId());
        return ResponseEntity.ok(ApiResponse.success(enrolledCourses, "Enrolled courses retrieved successfully"));
    }

    @GetMapping("/not-enrolled/me")
    public ResponseEntity<ApiResponse<List<CourseDTO>>> getNotEnrolledCourses() {
        var user = identityClient.getCurrentUser().getResult();
        List<CourseDTO> notEnrolledCourses = courseService.getNotEnrolledCourses(user.getId());
        return ResponseEntity.ok(ApiResponse.success(notEnrolledCourses, "Not enrolled courses retrieved successfully"));
    }

    /**
     * Cập nhật rating cho khóa học
     * Endpoint này được gọi từ identity-service khi có feedback mới
     */
    @PutMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<Void>> updateCourseRating(
            @PathVariable Long id,
            @RequestBody UpdateCourseRatingRequest request) {
        courseService.updateCourseRating(id, request.getAvgRating(), request.getRatingCount());
        return ResponseEntity.ok(ApiResponse.success(null, "Course rating updated successfully"));
    }

    /**
     * Internal endpoint: Lấy thông tin rating của khóa học
     */
    @GetMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<CourseDTO>> getCourseRating(@PathVariable Long id) {
        CourseDTO rating = courseService.getCourseRating(id);
        return ResponseEntity.ok(ApiResponse.success(rating, "Course rating retrieved successfully"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(400, ex.getMessage(), null));
    }


}