package com.sketchnotes.blog_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "contents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Content {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

   private String sectionTitle;
    @Column(columnDefinition = "TEXT")
   private String content;
   private String contentUrl;
   private Integer index;
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
    private LocalDateTime deletedAt;
}
