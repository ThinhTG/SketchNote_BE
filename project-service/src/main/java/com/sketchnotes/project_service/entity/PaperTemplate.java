package com.sketchnotes.project_service.entity;


import com.sketchnotes.project_service.enums.PaperSize;
import com.sketchnotes.project_service.enums.PaperType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "paper_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PaperTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paperTemplateId;
    private  String name;
    @Enumerated(EnumType.STRING)
    private PaperSize paperSize;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_paper_id")
    private CategoryPaper categoryPaper;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
