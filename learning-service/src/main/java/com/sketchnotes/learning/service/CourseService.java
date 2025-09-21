package com.sketchnotes.learning.service;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.entity.Course;
import com.sketchnotes.learning.mapper.CourseMapper;
import com.sketchnotes.learning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public List<Course> getAllCourses()
    {return courseRepository.findAll();}

    // 1. Tạo Course
    public Course createCourse(CourseDTO dto) {

        Course course = courseMapper.toEntity(dto);
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    }

