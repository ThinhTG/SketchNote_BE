package com.sketchnotes.learning.repository;

import com.sketchnotes.learning.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.lessons")
    List<Course> findAllWithLessons();
    
    @Query("SELECT DISTINCT c FROM Course c LEFT JOIN FETCH c.lessons WHERE c.courseId = :id")
    java.util.Optional<Course> findByIdWithLessons(Long id);
}
