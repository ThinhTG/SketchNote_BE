package com.sketchnotes.learning.mapper;

import com.sketchnotes.learning.dto.LessonDTO;
import com.sketchnotes.learning.entity.Lesson;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    Lesson toEntity(LessonDTO dto);
    LessonDTO toDTO(Lesson entity);

    List<Lesson> toEntityList(List<LessonDTO> dtoList);
    List<LessonDTO> toDTOList(List<Lesson> entityList);
}

