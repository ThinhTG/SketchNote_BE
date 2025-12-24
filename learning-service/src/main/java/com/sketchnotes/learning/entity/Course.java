package com.sketchnotes.learning.entity;

import com.sketchnotes.learning.dto.enums.SketchNoteCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "lessons")  // Tránh circular reference khi debug
@Table(name = "course")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long courseId;

    private String title; // tiêu đề lớn

    private  String subtitle;  // tiêu đề nhỏ

    private double price;   // giá tiền

    @Column(name = "student_count")
    private int studentCount; // số người đã enroll vào khóa học

    private String description;  // mô tả ngắn về khóa học

    private SketchNoteCategory category; // khóa học về chủ đề, lĩnh vực gì ( vẽ cái gì? )

    private String imageUrl; // ảnh banner cho khóa học

    private int totalDuration; // tổng thời gian hoàn thành khóa học

    @Column(name = "avg_rating")
    private Double avgRating; // điểm đánh giá trung bình của khóa học (1-5 sao)

    @Column(name = "rating_count")
    private Integer ratingCount = 0; // số lượng đánh giá

    private LocalDateTime createdAt = LocalDateTime.now();  // thời điểm khóa học được tạo

    private LocalDateTime updatedAt = LocalDateTime.now();  // thời điểm khóa học được cập nhật

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
    private List<Lesson> lessons = new ArrayList<>();

    public Course(Long courseId) {
        this.courseId = courseId;
    }

    public void updateTotalDuration() {
        this.totalDuration = this.lessons.stream()
                .mapToInt(Lesson::getDuration)
                .sum();
    }


}
