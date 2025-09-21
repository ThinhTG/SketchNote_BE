package com.sketchnotes.learning.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long courseId;

    private String title; // tiêu đề lớn

    private  String subtitle;  // tiêu đề nhỏ

    private double price;   // giá tiền

    private int student_count; // số người đã enroll vào khóa học

    private String description;  // mô tả ngắn về khóa học

    private String category; // khóa học về chủ đề, lĩnh vực gì ( vẽ cái gì? )

    private LocalDateTime createdAt;  // thời điểm khóa học được tạo

    private LocalDateTime updatedAt;  // thời điểm khóa học được cập nhật

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    // khóa học do ai tạo ra created_by
    // ........

    public Course(long courseId) {
        this.courseId = courseId;
    }
}
