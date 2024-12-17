//package com.example.formicarium.service;
//
//import com.example.formicarium.entity.*;
//import com.example.formicarium.repository.ProjectRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.HashSet;
//import java.util.List;
//
//@Service
//public class ProjectService {
//
//    private final ProjectRepository projectRepository;
//    private final MessageService messageService;
//    private final TaskService taskService;
//    private final UserService userService;
//
//    public ProjectService(ProjectRepository projectRepository, MessageService messageService, TaskService taskService, UserService userService) {
//        this.projectRepository = projectRepository;
//        this.messageService = messageService;
//        this.taskService = taskService;
//        this.userService = userService;
//    }
//
//    // proiectele utilizatorului conectat
//    public List<Project> getActiveProjectsForUser(Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        return projectRepository.findProjectsByUserAndIsActive(user, true); // Proiectele active
//    }
//
//    public List<Project> getInactiveProjectsForUser(Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        return projectRepository.findProjectsByUserAndIsActive(user, false); // Proiectele inactive
//    }
//
//    public void activateProject(Long id, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
//
//        // verific daca utilizatorul este OWNER al proiectului
//        boolean isOwner = project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) && membership.getRole().equals("OWNER"));
//
//        if (!isOwner) {
//            throw new SecurityException("You do not have permission to activate this project.");
//        }
//
//        project.setActive(true);
//        projectRepository.save(project);
//    }
//
//    public void deactivateProject(Long id, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
//
//        // verific daca utilizatorul este OWNER al proiectului
//        boolean isOwner = project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) && membership.getRole().equals("OWNER"));
//
//        if (!isOwner) {
//            throw new SecurityException("You do not have permission to deactivate this project.");
//        }
//
//        project.setActive(false);
//        projectRepository.save(project);
//    }
//
//    public void createProject(Project project, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        project.setActive(true);
//        project.setCreatedBy(user);
//        project.setCreatedAt(LocalDateTime.now());
//
//        ProjectMembership membership = ProjectMembership.builder()
//                .project(project)
//                .user(user)
//                .role("OWNER")
//                .dateJoined(LocalDateTime.now())
//                .build();
//
//        if (project.getMemberships() == null) {
//            project.setMemberships(new HashSet<>());
//        }
//        project.getMemberships().add(membership);
//
//        projectRepository.save(project);
//    }
//
//    // obtinere proiect pentru editare
//    public Project getProjectForEdit(Long id, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
//
//        // Verificăm permisiunea utilizând ID-ul
//        if (!project.getCreatedBy().getId().equals(user.getId())) {
//            throw new SecurityException("You do not have permission to edit this project.");
//        }
//        return project;
//    }
//
//    // salvare modificari in proiect
//    public void updateProject(Long id, Project updatedProject, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
//
//        // se verifica permisiunile utilizatorului
//        if (!project.getCreatedBy().getId().equals(user.getId())) {
//            throw new SecurityException("You do not have permission to edit this project.");
//        }
//
//        // actualizeaza doar campurile permise, pastrand `isActive` daca nu este explicit modificat
//        project.setName(updatedProject.getName());
//        project.setDescription(updatedProject.getDescription());
//        project.setDeadline(updatedProject.getDeadline());
//        project.setActive(updatedProject.isActive());
//
//        projectRepository.save(project);
//    }
//
//    public void deleteProject(Long id, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
//
//        // permite stergerea daca utilizatorul este creator sau membru al proiectului
//        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId());
//        boolean isMember = project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()));
//
//        if (!isCreator && !isMember) {
//            throw new SecurityException("You do not have permission to delete this project.");
//        }
//
//        projectRepository.delete(project);
//    }
//
//    @Transactional
//    public Project getProjectDetails(Long id, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
//
//        // Check if the user is a member of the project
//        boolean isMember = project.getMemberships().stream()
//                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
//        if (!isMember) {
//            throw new SecurityException("You do not have access to this project.");
//        }
//
//        return project;
//    }
//
//    // Obține ultimele 5 mesaje pentru un proiect.
//    @Transactional
//    public List<Message> getRecentMessagesForProject(Long projectId) {
//        return messageService.getRecentMessagesByProject(projectId);
//    }
//
//    // Obține ultimele 5 sarcini pentru un proiect.
//    @Transactional
//    public List<Task> getRecentTasksForProject(Long projectId) {
//        return taskService.getRecentTasksByProject(projectId);
//    }
//
//    // Verifică dacă utilizatorul este OWNER al proiectului.
//    public boolean isOwner(Project project, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        return project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) && membership.getRole().equals("OWNER"));
//    }
//
////    @Transactional
////    public void verifyCanAddMembers(Long projectId, Authentication authentication) {
////        User user = (User) authentication.getPrincipal();
////        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
////                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
////
////        boolean canAdd = project.getMemberships().stream()
////                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
////                        (m.getRole().equals("OWNER") || m.getRole().equals("COLLABORATOR")));
////
////        if (!canAdd) {
////            throw new SecurityException("You do not have permission to add members to this project.");
////        }
////    }
////
////    @Transactional
////    public void addMemberToProject(Long projectId, String usernameOrEmail, String role, Authentication authentication) {
////        User currentUser = (User) authentication.getPrincipal();
////        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
////                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
////
////        // Check if current user can add members
////        verifyCanAddMembers(projectId, authentication);
////
////        // Find user to add
////        User newUser = userService.findUserByUsernameOrEmail(usernameOrEmail)
////                .orElseThrow(() -> new IllegalArgumentException("User not found with username/email: " + usernameOrEmail));
////
////        // Check if user is already a member
////        boolean alreadyMember = project.getMemberships().stream()
////                .anyMatch(m -> m.getUser().getId().equals(newUser.getId()));
////        if (alreadyMember) {
////            throw new IllegalArgumentException("User is already a member of this project.");
////        }
////
////        // Create membership
////        ProjectMembershipId pmId = new ProjectMembershipId(project.getId(), newUser.getId());
////        ProjectMembership membership = ProjectMembership.builder()
////                .id(pmId)
////                .project(project)
////                .user(newUser)
////                .role(role.toUpperCase()) // Ensure role is uppercase: OWNER, COLLABORATOR, MEMBER
////                .dateJoined(LocalDateTime.now())
////                .build();
////
////        project.getMemberships().add(membership);
////        projectRepository.save(project);
////    }
//
//    // Check if user is a Collaborator
//    public boolean isCollaborator(Project project, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        return project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) &&
//                        membership.getRole().equals("COLLABORATOR"));
//    }
//
//    // Check if user is an Owner
//    public boolean isOwner(Project project, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        return project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()) &&
//                        membership.getRole().equals("OWNER"));
//    }
//
//    // Verify if the user can add members (OWNER or COLLABORATOR)
//    public void verifyCanAddMembers(Long projectId, Authentication authentication) {
//        User user = (User) authentication.getPrincipal();
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
//
//        boolean canAdd = isOwner(project, authentication) || isCollaborator(project, authentication);
//
//        if (!canAdd) {
//            throw new SecurityException("You do not have permission to add members to this project.");
//        }
//    }
//
//    // Add a member to the project
//    @Transactional
//    public void addMemberToProject(Long projectId, String usernameOrEmail, String role, Authentication authentication) {
//        User currentUser = (User) authentication.getPrincipal();
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
//
//        // Check if current user can add members
//        verifyCanAddMembers(projectId, authentication);
//
//        // Find user to add
//        User newUser = userService.findUserByUsernameOrEmail(usernameOrEmail)
//                .orElseThrow(() -> new IllegalArgumentException("User not found with username/email: " + usernameOrEmail));
//
//        // Check if user is already a member
//        boolean alreadyMember = project.getMemberships().stream()
//                .anyMatch(m -> m.getUser().getId().equals(newUser.getId()));
//        if (alreadyMember) {
//            throw new IllegalArgumentException("User is already a member of this project.");
//        }
//
//        // Validate role
//        Role assignedRole;
//        try {
//            assignedRole = Role.valueOf(role.toUpperCase());
//        } catch (IllegalArgumentException e) {
//            throw new IllegalArgumentException("Invalid role: " + role);
//        }
//
//        if (assignedRole == Role.OWNER) {
//            throw new IllegalArgumentException("Cannot assign OWNER role to another user.");
//        }
//
//        // Create membership
//        ProjectMembershipId pmId = new ProjectMembershipId(project.getId(), newUser.getId());
//        ProjectMembership membership = ProjectMembership.builder()
//                .id(pmId)
//                .project(project)
//                .user(newUser)
//                .role(assignedRole.name())
//                .dateJoined(LocalDateTime.now())
//                .build();
//
//        project.getMemberships().add(membership);
//        projectRepository.save(project);
//    }
//
//}



package com.example.formicarium.service;

import com.example.formicarium.entity.*;
import com.example.formicarium.repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final MessageService messageService;
    private final TaskService taskService;
    private final UserService userService; // Inject UserService

    public ProjectService(ProjectRepository projectRepository, MessageService messageService,
                          TaskService taskService, UserService userService) {
        this.projectRepository = projectRepository;
        this.messageService = messageService;
        this.taskService = taskService;
        this.userService = userService;
    }

    // Retrieve active projects for the user
    public List<Project> getActiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, true); // Active projects
    }

    // Retrieve inactive projects for the user
    public List<Project> getInactiveProjectsForUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return projectRepository.findProjectsByUserAndIsActive(user, false); // Inactive projects
    }

    // Activate a project (only by Owner)
    public void activateProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // Verify if user is Owner
        boolean isOwner = isOwner(project, authentication);

        if (!isOwner) {
            throw new SecurityException("You do not have permission to activate this project.");
        }

        project.setActive(true);
        projectRepository.save(project);
    }

    // Deactivate a project (only by Owner)
    public void deactivateProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // Verify if user is Owner
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

        // Step 1: Save the Project to generate the ID
        Project savedProject = projectRepository.save(project);

        // Step 2: Create and associate ProjectMembership
        ProjectMembership membership = new ProjectMembership();
        ProjectMembershipId pmId = new ProjectMembershipId(savedProject.getId(), user.getId());
        membership.setId(pmId);
        membership.setProject(savedProject);
        membership.setUser(user);
        membership.setRole("OWNER");
        membership.setDateJoined(LocalDateTime.now());

        // Step 3: Add the membership to the Project
        savedProject.getMemberships().add(membership);

        // Step 4: Save the Project again to persist the membership
        projectRepository.save(savedProject);
    }

    // Get project for editing (only by Creator)
    public Project getProjectForEdit(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        // Check if user is the creator
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }
        return project;
    }

    // Update a project (only by Creator)
    public void updateProject(Long id, Project updatedProject, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // Check if user is the creator
        if (!project.getCreatedBy().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to edit this project.");
        }

        // Update fields
        project.setName(updatedProject.getName());
        project.setDescription(updatedProject.getDescription());
        project.setDeadline(updatedProject.getDeadline());
        project.setActive(updatedProject.isActive());

        projectRepository.save(project);
    }

    // Delete a project (only by Creator or if user is a member)
    public void deleteProject(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // Check if user is Creator or a member
        boolean isCreator = project.getCreatedBy() != null && project.getCreatedBy().getId().equals(user.getId());
        boolean isMember = project.getMemberships().stream()
                .anyMatch(membership -> membership.getUser().getId().equals(user.getId()));

        if (!isCreator && !isMember) {
            throw new SecurityException("You do not have permission to delete this project.");
        }

        projectRepository.delete(project);
    }

    // Get project details (only if user is a member)
    @Transactional
    public Project getProjectDetails(Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Project project = projectRepository.findByIdWithMembershipsAndMessages(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));

        // Check if the user is a member of the project
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        if (!isMember) {
            throw new SecurityException("You do not have access to this project.");
        }

        return project;
    }

    // Get recent messages
    @Transactional
    public List<Message> getRecentMessagesForProject(Long projectId) {
        return messageService.getRecentMessagesByProject(projectId);
    }

    // Get recent tasks
    @Transactional
    public List<Task> getRecentTasksForProject(Long projectId) {
        return taskService.getRecentTasksByProject(projectId);
    }

    // Check if user is Owner
    public boolean isOwner(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return project.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getUser().getId().equals(user.getId()) &&
                                "OWNER".equalsIgnoreCase(membership.getRole()));
    }

    // Check if user is Collaborator
    public boolean isCollaborator(Project project, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return project.getMemberships().stream()
                .anyMatch(membership ->
                        membership.getUser().getId().equals(user.getId()) &&
                                "COLLABORATOR".equalsIgnoreCase(membership.getRole()));
    }

    // Verify if the user can add members (OWNER or COLLABORATOR)
    public void verifyCanAddMembers(Long projectId, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        boolean canAdd = isOwner(project, authentication) || isCollaborator(project, authentication);

        if (!canAdd) {
            throw new SecurityException("You do not have permission to add members to this project.");
        }
    }

//    // Add a member to the project
//    @Transactional
//    public void addMemberToProject(Long projectId, String usernameOrEmail, String role, Authentication authentication) {
//        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));
//
//        // Check if current user can add members
//        verifyCanAddMembers(projectId, authentication);
//
//        // Find user to add
//        User newUser = userService.findUserByUsernameOrEmail(usernameOrEmail)
//                .orElseThrow(() -> new IllegalArgumentException("User not found with username/email: " + usernameOrEmail));
//
//        // Check if user is already a member
//        boolean alreadyMember = project.getMemberships().stream()
//                .anyMatch(m -> m.getUser().getId().equals(newUser.getId()));
//        if (alreadyMember) {
//            throw new IllegalArgumentException("User is already a member of this project.");
//        }
//
//        // Validate role
//        String assignedRole = role.toUpperCase();
//        if (!assignedRole.equals("MEMBER") && !assignedRole.equals("COLLABORATOR")) {
//            throw new IllegalArgumentException("Invalid role: " + role);
//        }
//
//        if (assignedRole.equals("OWNER")) {
//            throw new IllegalArgumentException("Cannot assign OWNER role to another user.");
//        }
//
//        // Create membership
//        ProjectMembershipId pmId = new ProjectMembershipId(project.getId(), newUser.getId());
//        ProjectMembership membership = ProjectMembership.builder()
//                .id(pmId)
//                .project(project)
//                .user(newUser)
//                .role(assignedRole)
//                .dateJoined(LocalDateTime.now())
//                .build();
//
//        project.getMemberships().add(membership);
//        projectRepository.save(project);
//    }

    // Add a member to the project
    @Transactional
    public void addMemberToProject(Long projectId, String usernameOrEmail, String role, Authentication authentication) {
        // Step 1: Retrieve the project
        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // Step 2: Verify that the current user can add members
        verifyCanAddMembers(projectId, authentication);

        // Step 3: Find the user to add
        User newUser = userService.findUserByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username/email: " + usernameOrEmail));

        // Step 4: Check if the user is already a member
        boolean alreadyMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(newUser.getId()));
        if (alreadyMember) {
            throw new IllegalArgumentException("User is already a member of this project.");
        }

        // Step 5: Validate the role
        String assignedRole = role.toUpperCase();
        if (!assignedRole.equals("MEMBER") && !assignedRole.equals("COLLABORATOR")) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        if (assignedRole.equals("OWNER")) {
            throw new IllegalArgumentException("Cannot assign OWNER role to another user.");
        }

        // Step 6: Create a new ProjectMembership entity
        ProjectMembership membership = new ProjectMembership();
        ProjectMembershipId pmId = new ProjectMembershipId(project.getId(), newUser.getId());
        membership.setId(pmId);
        membership.setProject(project);
        membership.setUser(newUser);
        membership.setRole(assignedRole);
        membership.setDateJoined(LocalDateTime.now());

        // Step 7: Add the new membership to the project and save the project
        project.getMemberships().add(membership);
        projectRepository.save(project); // Cascade will save the new membership
    }


    // Remove a member from the project
    @Transactional
    public void removeMemberFromProject(Long projectId, Long userId, Authentication authentication) {
        Project project = projectRepository.findByIdWithMembershipsAndMessages(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // Check if current user is Owner
        boolean isOwner = isOwner(project, authentication);
        if (!isOwner) {
            throw new SecurityException("You do not have permission to remove members from this project.");
        }

        // Find membership to remove
        ProjectMembership membershipToRemove = project.getMemberships().stream()
                .filter(m -> m.getUser().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project."));

        // Prevent removing the Owner
        if (membershipToRemove.getRole().equalsIgnoreCase("OWNER")) {
            throw new IllegalArgumentException("Cannot remove the Owner from the project.");
        }

        project.getMemberships().remove(membershipToRemove);
        projectRepository.save(project);
    }
}
