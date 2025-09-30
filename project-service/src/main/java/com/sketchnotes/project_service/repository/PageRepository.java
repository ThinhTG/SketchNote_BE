package com.sketchnotes.project_service.repository;

import com.sketchnotes.project_service.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageRepository extends JpaRepository<Page, Long> {
    List<Page> findByProject_ProjectId(Long projectId);
}