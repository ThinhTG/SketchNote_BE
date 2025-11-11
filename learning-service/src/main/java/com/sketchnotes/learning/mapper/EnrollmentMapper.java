package com.sketchnotes.learning.mapper;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.dto.EnrollmentDTO;
import com.sketchnotes.learning.entity.CourseEnrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CourseMapper.class})
public interface EnrollmentMapper {
    @Mapping(source = "course.courseId", target = "courseId")
    @Mapping(source = "course", target = "course")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "progressPercent", target = "progressPercent")
    EnrollmentDTO toDTO(CourseEnrollment entity);
}

