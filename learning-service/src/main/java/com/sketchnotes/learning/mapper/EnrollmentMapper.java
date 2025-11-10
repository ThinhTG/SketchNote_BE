package com.sketchnotes.learning.mapper;

import com.sketchnotes.learning.dto.EnrollmentDTO;
import com.sketchnotes.learning.entity.CourseEnrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {
    @Mapping(source = "course.courseId", target = "courseId")
    @Mapping(source = "status", target = "status")
    EnrollmentDTO toDTO(CourseEnrollment entity);
}

