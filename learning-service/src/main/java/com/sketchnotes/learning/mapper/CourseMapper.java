package com.sketchnotes.learning.mapper;

import com.sketchnotes.learning.dto.CourseDTO;
import com.sketchnotes.learning.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = LessonMapper.class)
public interface CourseMapper {
    @Mapping(source = "studentCount", target = "studentCount")
    Course toEntity(CourseDTO dto);

    @Mapping(source = "studentCount", target = "studentCount")
    CourseDTO toDTO(Course entity);
    List<Course> toEntityList(List<CourseDTO> dtoList);
    List<CourseDTO> toDTOList(List<Course> entityList);
}

