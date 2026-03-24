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

    // get the list of active projects for a user
    public List<Project> getActiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, true); // Active projects
    }

    // get the list of inactive projects for a user
    public List<Project> getInactiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, false); // Inactive projects
    }

    // reactivate a project (only if the user has the OWNER role in that project)
    public void activateProject(Long id, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // check if the user has the OWNER role
        boolean isOwner = isOwner(project, authentication);

        if (!isOwner) {
            throw new SecurityException("You do not have permission to activate this project.");
        }

        project.setActive(true);
        projectRepository.save(project);
    }

    // deactivate a project (only if the user has the OWNER role in that project)
    public void deactivateProject(Long id, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // check if the user has the OWNER role
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

        // save the project to generate an ID
        Project savedProject = projectRepository.save(project);

        // create and assign ProjectMembership
        ProjectMembership membership = new ProjectMembership();
        ProjectMembershipId pmId = new ProjectMembershipId(savedProject.getId(), user.getId());
        membership.setId(pmId);
        membership.setProject(savedProject);
        membership.setUser(user);
        membership.setRole("OWNER");
        membership.setDateJoined(LocalDateTime.now());

        // add that membership to the project
        savedProject.getMemberships().add(membership);

        // save the project for membership persistence
        projectRepository.save(savedProject);
    }

    // get the project for editing (only if the user is the creator)
    public Project getProjectForEdit(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        // check if the user created the project
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }
        return project;
    }

    // delete a project (OWNER)
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

    // get project details
    @Transactional
    public Project getProjectDetails(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // check if the user is a member of that project
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!isMember) {
            throw new SecurityException("You do not have access to this project.");
        }

        return project;
    }

    // get recent messages
    @Transactional
    public List<Message> getRecentMessagesForProject(Long projectId) {
        return messageService.getRecentMessagesByProject(projectId);
    }

    // get recent tasks
    @Transactional
    public List<Task> getRecentTasksForProject(Long projectId) {
        return taskService.getRecentTasksByProject(projectId);
    }

    // check if the user is the owner or creator of a project
    public boolean isOwner(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return project.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getUser().getId().equals(user.getId()) &&
                                "OWNER".equalsIgnoreCase(membership.getRole()));
    }

    // check if the user has the COLLABORATOR role in a project
    public boolean isCollaborator(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return project.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getUser().getId().equals(user.getId()) &&
                                "COLLABORATOR".equalsIgnoreCase(membership.getRole()));
    }

    // check if a user has the necessary roles and permissions to add a user to a project (has OWNER or COLLABORATOR role)
    public void verifyCanAddMembers(Long projectId, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        boolean canAdd = isOwner(project, authentication) || isCollaborator(project, authentication);

        if (!canAdd) {
            throw new SecurityException("You do not have permission to add members to this project.");
        }
    }

    // add a new member to the project
    @Transactional
    public void addMemberToProject(Long projectId, String usernameOrEmail, String role, Authentication authentication) {
        // get the project
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // check if the current user has permission to add new members to the project
        verifyCanAddMembers(projectId, authentication);

        // find the searched user for addition
        User newUser = userService.findUserByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username/email: " + usernameOrEmail));

        // check if the respective user is already a member of that project
        boolean alreadyMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(newUser.getId()));
        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this project.");
        }

        // validate the role
        String assignedRole = role.toUpperCase();
        if (!assignedRole.equals("MEMBER") && !assignedRole.equals("COLLABORATOR")) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        if (assignedRole.equals("OWNER")) {
            throw new IllegalArgumentException("Cannot assign OWNER role to another user.");
        }

        // create the ProjectMembership entity for the new member
        ProjectMembership membership = new ProjectMembership();
        ProjectMembershipId pmId = new ProjectMembershipId(project.getId(), newUser.getId());
        membership.setId(pmId);
        membership.setProject(project);
        membership.setUser(newUser);
        membership.setRole(assignedRole);
        membership.setDateJoined(LocalDateTime.now());

        // add the new member and save the project
        project.getMemberships().add(membership);
        projectRepository.save(project);
    }

    // remove a member from a project
    @Transactional
    public void removeMemberFromProject(Long projectId, Long userId, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // check if the current user has the OWNER role
        boolean isOwner = isOwner(project, authentication);
        if (!isOwner) {
            throw new SecurityException("You do not have permission to remove members from this project.");
        }

        ProjectMembership membershipToRemove = project.getMemberships().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project."));

        // the user with the OWNER role cannot be removed
        if (membershipToRemove.getRole().equalsIgnoreCase("OWNER")) {
            throw new IllegalArgumentException("Cannot remove the Owner from the project.");
        }

        project.getMemberships().remove(membershipToRemove);
        projectRepository.save(project);
    }
}