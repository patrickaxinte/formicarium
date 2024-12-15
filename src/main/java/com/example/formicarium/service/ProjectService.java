package com.example.formicarium.service;

import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.ProjectMembership;
import com.example.formicarium.entity.User;
import com.example.formicarium.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // proiectele utilizatorului conectat
    public List<Project> getActiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, true); // Proiectele active
    }

    public List<Project> getInactiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, false); // Proiectele inactive
    }

    public void activateProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMemberships(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // verific daca utilizatorul este OWNER al proiectului
        boolean isOwner = project.getMemberships().stream()
                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) && membership.getRole().equals("OWNER"));

        if (!isOwner) {
            throw new SecurityException("You do not have permission to activate this project.");
        }

        project.setActive(true);
        projectRepository.save(project);
    }

    public void deactivateProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMemberships(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // verific daca utilizatorul este OWNER al proiectului
        boolean isOwner = project.getMemberships().stream()
                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) && membership.getRole().equals("OWNER"));

        if (!isOwner) {
            throw new SecurityException("You do not have permission to deactivate this project.");
        }

        project.setActive(false);
        projectRepository.save(project);
    }

    // creare proiect
    public void createProject(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        project.setActive(true);
        project.setCreatedBy(user);
        project.setCreatedAt(LocalDateTime.now());

        ProjectMembership membership = ProjectMembership.builder()
                .project(project)
                .user(user)
                .role("OWNER")
                .dateJoined(LocalDateTime.now())
                .build();

        if (project.getMemberships() == null) {
            project.setMemberships(new ArrayList<>());
        }
        project.getMemberships().add(membership);

        projectRepository.save(project);
    }

    // obtinere proiect pentru editare
    public Project getProjectForEdit(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        // Verificăm permisiunea utilizând ID-ul
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }
        return project;
    }

    // salvare modificari in proiect
    public void updateProject(Long id, Project updatedProject, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // se verifica permisiunile utilizatorului
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }

        // actualizeaza doar campurile permise, pastrand `isActive` daca nu este explicit modificat
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setDeadline(updatedProject.getDeadline());
        project.setActive(updatedProject.isActive());

        projectRepository.save(project);
    }

    public void deleteProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMemberships(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // permite stergerea daca utilizatorul este creator sau membru al proiectului
        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId());
        boolean isMember = project.getMemberships().stream()
                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()));

        if (!isCreator && !isMember) {
            throw new SecurityException("You do not have permission to delete this project.");
        }

        projectRepository.delete(project);
    }
}
