package com.example.formicarium.service;

import com.example.formicarium.entity.*;
import com.example.formicarium.repository.ProjectRepository;
import com.example.formicarium.repository.TaskRepository;
import com.example.formicarium.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // obtine sarcini atribuite unui utilizator din toate proiectele, cu createdBy initializat
    @Transactional(readOnly = true)
    public List<Task> getTasksAssignedToUser(Long userId) {
        return taskRepository.findByAssignedToIdWithCreatedBy(userId);
    }

    // obtine cele mai recente 5 sarcini atribuite unui utilizator, cu createdBy initializat
    @Transactional(readOnly = true)
    public List<Task> getRecentTasksByUser(Long userId) {
        return taskRepository.findTop5ByAssignedToIdOrderByCreatedAtDescWithCreatedBy(userId);
    }

    // obtine toate sarcinile pentru un utilizator (proiecte din care face parte), cu createdBy initializat
    @Transactional(readOnly = true)
    public List<Task> getAllTasksForUser(Long userId) {
        return taskRepository.findAllByUserIdWithCreatedBy(userId);
    }

    // obtine cele mai recente 5 sarcini dintr-un proiect specific, cu createdBy initializat
    @Transactional(readOnly = true)
    public List<Task> getRecentTasksByProject(Long projectId) {
        return taskRepository.findTop5ByProjectIdOrderByCreatedAtDescWithCreatedBy(projectId);
    }

    // obtine toate sarcinile pentru un proiect, cu createdBy initializat
    @Transactional(readOnly = true)
    public List<Task> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectIdWithCreatedBy(projectId);
    }

    // crearea unui task nou
    @Transactional
    public Task createTask(Long projectId, String name, String description, String dueDate,
                           Long assigneeId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // sa asigura ca utilizatorul curent este OWNER sau COLLABORATOR...
        if (!hasRole(project, currentUser, "OWNER", "COLLABORATOR")) {
            throw new SecurityException("You do not have permission to create tasks in this project.");
        }

        User assignee = null;
        if (assigneeId != null) {
            assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found with ID: " + assigneeId));
            if (!isUserMemberOfProject(project, assignee)) {
                throw new IllegalArgumentException("Assignee is not a member of the project.");
            }
        }

        Task task = Task.builder()
                .name(name)
                .description(description)
                .dueDate(parseDueDate(dueDate))
                .assignedTo(assignee)
                .project(project)
                .createdBy(currentUser)
                .status(TaskStatus.TO_DO)
                .build();

        // se salveaza sarcina
        Task savedTask = taskRepository.save(task);

        // daca exista o persoana atribuita, creeaza o notificare
        if (assignee != null) {
            Notification notif = Notification.builder()
                    .recipientId(assigneeId)
                    .message("You have been assigned a new task: " + name)
                    .taskId(savedTask.getId())
                    .projectName(project.getName())
                    .build();
            notificationService.createAndSendNotification(notif);
        }

        return savedTask;
    }

    // actualizeaza o sarcina
    @Transactional
    public Task updateTask(Long taskId, String status, String title, String description, String dueDate, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        // obtine sarcina cu toate asociatiile necesare
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());
        boolean isAssignee = task.getAssignedTo() != null && task.getAssignedTo().getId().equals(currentUser.getId());

        if (!isCreator && !isAssignee) {
            throw new SecurityException("You do not have permission to update this task.");
        }

        // verifica daca sarcina este COMPLETED
        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new IllegalStateException("Cannot update a completed task.");
        }

        // daca este creator, permite actualizarea tuturor campurilor
        if (isCreator) {
            if (status != null && !status.isEmpty()) {
                TaskStatus newStatus = parseStatus(status);
                task.setStatus(newStatus);
            }

            if (title != null && !title.isEmpty()) {
                task.setName(title);
            }

            if (description != null) {
                task.setDescription(description);
            }

            if (dueDate != null && !dueDate.isEmpty()) {
                task.setDueDate(parseDueDate(dueDate));
            }
        }

        // If assignee, allow only updating status
        if (isAssignee && !isCreator) {
            if (status != null && !status.isEmpty()) {
                TaskStatus newStatus = parseStatus(status);
                task.setStatus(newStatus);
            } else {
                throw new IllegalArgumentException("Status is required to update the task.");
            }
        }

        return taskRepository.save(task);
    }

    // sterge un task
    @Transactional
    public void deleteTask(Long taskId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = task.getProject();

        // verifica daca utilizatorul are rol OWNER sau COLLABORATOR in proiect
        if (!hasRole(project, currentUser, "OWNER", "COLLABORATOR")) {
            throw new SecurityException("You do not have permission to delete tasks in this project.");
        }

        taskRepository.delete(task);
    }

    // obtine o sarcina dupa ID, cu verificarea accesului si initializarea asociatiilor
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId, Authentication authentication) {
        Task task = taskRepository.findByIdWithDetails(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        User user = (User) authentication.getPrincipal();
        if (!isUserMemberOfProject(task.getProject(), user)) {
            throw new SecurityException("You do not have access to this task.");
        }

        return task;
    }

    // verifica accesul utilizatorului la un proiect si obtine proiectul cu asociatiile relevante
    @Transactional(readOnly = true)
    public Optional<Project> verifyUserAccessToProject(Long projectId, User user) {
        Project project = projectRepository.findByIdWithMembershipsMessagesAndModules(projectId)
                .orElse(null);
        if (project == null) return Optional.empty();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        return isMember ? Optional.of(project) : Optional.empty();
    }

    // obtine proiecte in functie de utilizator si roluri
    @Transactional(readOnly = true)
    public List<Project> findProjectsByUserAndRoles(User user, List<String> roles) {
        return projectRepository.findProjectsByUserAndRoles(user, roles);
    }

    // metode ajutatoare
    private boolean hasRole(Project project, User user, String... roles) {
        return project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        java.util.Arrays.stream(roles).anyMatch(role -> role.equalsIgnoreCase(m.getRole())));
    }

    @Transactional(readOnly = true)
    public boolean isUserMemberOfProject(Project project, User user) {
        return project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
    }

    private LocalDate parseDueDate(String dueDate) {
        if (dueDate == null || dueDate.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dueDate);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid due date format.");
        }
    }

    private TaskStatus parseStatus(String status) {
        try {
            return TaskStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid task status.");
        }
    }
}
