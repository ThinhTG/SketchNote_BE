package com.sketchnotes.project_service.entity;

import com.sketchnotes.project_service.enums.PaperType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "category_paper")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPaper {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryPaperId;

    @Enumerated(EnumType.STRING)
    private PaperType paperType;
    private String name;


    @OneToMany(mappedBy = "categoryPaper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PaperTemplate> paperTemplates;

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
