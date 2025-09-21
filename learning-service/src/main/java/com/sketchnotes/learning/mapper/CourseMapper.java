package com.sketchnotes.learning.mapper;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.entity.Course;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = LessonMapper.class)
public interface CourseMapper {
    Course toEntity(CourseDTO dto);
    CourseDTO toDTO(Course entity);

    List<Course> toEntityList(List<CourseDTO> dtoList);
    List<CourseDTO> toDTOList(List<Course> entityList);
}

