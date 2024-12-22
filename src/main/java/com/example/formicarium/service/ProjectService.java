package com.example.formicarium.service;

import com.example.formicarium.entity.*;
import com.example.formicarium.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MessageService messageService;
    private final TaskService taskService;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, MessageService messageService,
                          TaskService taskService, UserService userService) {
        this.projectRepository = projectRepository;
        this.messageService = messageService;
        this.taskService = taskService;
        this.userService = userService;
    }

    // obtinem lista proiectelor active pentru un utilizator
    public List<Project> getActiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, true); // Active projects
    }

    // obtinem lista proiectelor inactive pentru un utilizator
    public List<Project> getInactiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, false); // Inactive projects
    }

    // reactivarea unui proiect (numai daca utilizatorul are rolul OWNER in acel proiect)
    public void activateProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // se verifica daca utilizatorul are rolul OWNER
        boolean isOwner = isOwner(project, authentication);

        if (!isOwner) {
            throw new SecurityException("You do not have permission to activate this project.");
        }

        project.setActive(true);
        projectRepository.save(project);
    }

    // dezactivarea unui proiect (numai daca utilizatorul are rolul OWNER in acel proiect)
    public void deactivateProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // se verifica daca utilizatorul are rolul OWNER
        boolean isOwner = isOwner(project, authentication);

        if (!isOwner) {
            throw new SecurityException("You do not have permission to deactivate this project.");
        }

        project.setActive(false);
        projectRepository.save(project);
    }

    @Transactional
    public void createProject(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        project.setActive(true);
        project.setCreatedBy(user);
        project.setCreatedAt(LocalDateTime.now());

        // se salveaza proiectul pentru a genera un ID
        Project savedProject = projectRepository.save(project);

        // se creaza si se atribuie ProjectMembership
        ProjectMembership membership = new ProjectMembership();
        ProjectMembershipId pmId = new ProjectMembershipId(savedProject.getId(), user.getId());
        membership.setId(pmId);
        membership.setProject(savedProject);
        membership.setUser(user);
        membership.setRole("OWNER");
        membership.setDateJoined(LocalDateTime.now());

        // se adauga acel membership la proiect
        savedProject.getMemberships().add(membership);

        // se salveaza proiectul pentru persistenta membership-ului
        projectRepository.save(savedProject);
    }

    // se obtine proiectul pentru editare (numai daca utilizatorul are rolul OWNER)
    public Project getProjectForEdit(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        // se verifica daca utilizatorul a creat proiectul
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }
        return project;
    }

    // actuelizarea unui proiect (OWNER)
    public void updateProject(Long id, Project updatedProject, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // Check if user is the creator
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }

        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setDeadline(updatedProject.getDeadline());
        project.setActive(updatedProject.isActive());

        projectRepository.save(project);
    }

    // stergerea unui proiect (OWNER)
    public void deleteProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId());
        boolean isMember = project.getMemberships().stream()
                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()));

        if (!isCreator && !isMember) {
            throw new SecurityException("You do not have permission to delete this project.");
        }

        projectRepository.delete(project);
    }

    // se obtin detaliile unui proiect
    @Transactional
    public Project getProjectDetails(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // se verifica daca utilizatorul este membru in acel proiect
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!isMember) {
            throw new SecurityException("You do not have access to this project.");
        }

        return project;
    }

    // se obtin mesajele recente
    @Transactional
    public List<Message> getRecentMessagesForProject(Long projectId) {
        return messageService.getRecentMessagesByProject(projectId);
    }

    // se obtin sarcinile recente
    @Transactional
    public List<Task> getRecentTasksForProject(Long projectId) {
        return taskService.getRecentTasksByProject(projectId);
    }

    // verificarea daca utilizatorul detinatorul sau creatorul unui proiect
    public boolean isOwner(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return project.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getUser().getId().equals(user.getId()) &&
                                "OWNER".equalsIgnoreCase(membership.getRole()));
    }

    // se verifica daca utilizatorul are rolul COLLABORATOR intr-un proiect
    public boolean isCollaborator(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return project.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getUser().getId().equals(user.getId()) &&
                                "COLLABORATOR".equalsIgnoreCase(membership.getRole()));
    }

    // verificare daca un utilizator are rolurile si permisiunile necesare pentru a aduaga un utilizator intr-un proeict (are rolul OWNER sau COLLABORATOR)
    public void verifyCanAddMembers(Long projectId, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        boolean canAdd = isOwner(project, authentication) || isCollaborator(project, authentication);

        if (!canAdd) {
            throw new SecurityException("You do not have permission to add members to this project.");
        }
    }

    // adaugarea unui membru nou in proiect
    @Transactional
    public void addMemberToProject(Long projectId, String usernameOrEmail, String role, Authentication authentication) {
        // obtinerea proiectului
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // verificare daca utilizatorul curent are permisiunea de a adauga membri noi proiect
        verifyCanAddMembers(projectId, authentication);

        // gasirea utilizatorului cautat pentru adaugare
        User newUser = userService.findUserByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username/email: " + usernameOrEmail));

        // verificare daca utilizatorul respectiv este deja membru in acel proiect
        boolean alreadyMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(newUser.getId()));
        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this project.");
        }

        // validarea rolului
        String assignedRole = role.toUpperCase();
        if (!assignedRole.equals("MEMBER") && !assignedRole.equals("COLLABORATOR")) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        if (assignedRole.equals("OWNER")) {
            throw new IllegalArgumentException("Cannot assign OWNER role to another user.");
        }

        // crearea entitatii ProjectMembership pentru membrul nou
        ProjectMembership membership = new ProjectMembership();
        ProjectMembershipId pmId = new ProjectMembershipId(project.getId(), newUser.getId());
        membership.setId(pmId);
        membership.setProject(project);
        membership.setUser(newUser);
        membership.setRole(assignedRole);
        membership.setDateJoined(LocalDateTime.now());

        // adaugarea membrului nou si salvarea proiectului
        project.getMemberships().add(membership);
        projectRepository.save(project);
    }

    // eliminarea unui membru dintr-un proiect
    @Transactional
    public void removeMemberFromProject(Long projectId, Long userId, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // se verifica daca utilizatorul curent are rolul OWNER
        boolean isOwner = isOwner(project, authentication);
        if (!isOwner) {
            throw new SecurityException("You do not have permission to remove members from this project.");
        }

        ProjectMembership membershipToRemove = project.getMemberships().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project."));

        // utilizatorul cu rolul OWNER nu se poate elimina
        if (membershipToRemove.getRole().equalsIgnoreCase("OWNER")) {
            throw new IllegalArgumentException("Cannot remove the Owner from the project.");
        }

        project.getMemberships().remove(membershipToRemove);
        projectRepository.save(project);
    }
}