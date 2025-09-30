package com.sketchnotes.project_service.service.implement;

import com.sketchnotes.project_service.dtos.ProjectDTO;
import com.sketchnotes.project_service.dtos.mapper.ProjectMapper;
import com.sketchnotes.project_service.entity.Project;
import com.sketchnotes.project_service.repository.ProjectRepository;
import com.sketchnotes.project_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepository;

    @Override
    public ProjectDTO createProject(ProjectDTO dto) {
        Project project = ProjectMapper.toEntity(dto);
        Project saved = projectRepository.save(project);
        return ProjectMapper.toDTO(saved);
    }

    @Override
    public ProjectDTO getProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        return ProjectMapper.toDTO(project);
    }

    @Override
    public List<ProjectDTO> getProjectsByOwner(Long ownerId) {
        return projectRepository.findByOwnerId(ownerId).stream()
                .map(ProjectMapper::toDTO)
                .toList();
    }

    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        Project updated = projectRepository.save(project);
        return ProjectMapper.toDTO(updated);
    }

    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}

