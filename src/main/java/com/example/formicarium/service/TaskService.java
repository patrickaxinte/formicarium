package com.example.formicarium.service;

import com.example.formicarium.entity.*;
import com.example.formicarium.repository.TaskRepository;
import com.example.formicarium.repository.ProjectRepository;
import com.example.formicarium.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    // returneaza sarcinile atribuite unui utilizator in toate proiectele
    @Transactional(readOnly = true)
    public List<Task> getTasksAssignedToUser(Long userId) {
        return taskRepository.findByAssignedToId(userId);
    }

    // returneaza sarcinile recente dintr-un proiect
    @Transactional(readOnly = true)
    public List<Task> getRecentTasksByProject(Long projectId) {
        return taskRepository.findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
    }

    // creeaza o sarcina noua
    @Transactional
    public Task createTask(Long projectId, Task task, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // verifica daca utilizatorul curent este OWNER sau COLLABORATOR in proiect
        if (!hasRole(project, currentUser, "OWNER", "COLLABORATOR")) {
            throw new SecurityException("You do not have permission to create tasks in this project.");
        }

        // verifica daca persoana desemnata este membru al proiectului
        if (task.getAssignedTo() != null) {
            User assignee = userRepository.findById(task.getAssignedTo().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found with ID: " + task.getAssignedTo().getId()));
            if (!isUserMemberOfProject(project, assignee)) {
                throw new IllegalArgumentException("Assignee is not a member of the project.");
            }
            task.setAssignedTo(assignee);
        }

        task.setProject(project);
        task.setCreatedBy(currentUser);
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TO_DO); // status implicit
        }

        return taskRepository.save(task);
    }

    // actualizeaza o sarcina existenta
    @Transactional
    public Task updateTask(Long taskId, Task updatedTask, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = existingTask.getProject();

        // verifica daca utilizatorul curent este OWNER sau COLLABORATOR in proiect
        if (!hasRole(project, currentUser, "OWNER", "COLLABORATOR")) {
            throw new SecurityException("You do not have permission to update tasks in this project.");
        }

        // verifica daca noua persoana desemnata este membru al proiectului
        if (updatedTask.getAssignedTo() != null) {
            User assignee = userRepository.findById(updatedTask.getAssignedTo().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found with ID: " + updatedTask.getAssignedTo().getId()));
            if (!isUserMemberOfProject(project, assignee)) {
                throw new IllegalArgumentException("Assignee is not a member of the project.");
            }
            existingTask.setAssignedTo(assignee);
        } else {
            existingTask.setAssignedTo(null);
        }

        // actualizeaza detaliile sarcinii
        existingTask.setName(updatedTask.getName());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());

        return taskRepository.save(existingTask);
    }

    // sterge o sarcina
    @Transactional
    public void deleteTask(Long taskId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = task.getProject();

        // verifica daca utilizatorul curent este OWNER sau COLLABORATOR in proiect
        if (!hasRole(project, currentUser, "OWNER", "COLLABORATOR")) {
            throw new SecurityException("You do not have permission to delete tasks in this project.");
        }

        taskRepository.delete(task);
    }

    // marcheaza o sarcina ca fiind finalizata (doar persoana desemnata poate face asta)
    @Transactional
    public Task markTaskAsCompleted(Long taskId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        // verifica daca utilizatorul curent este persoana desemnata
        if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
            throw new SecurityException("You do not have permission to update this task.");
        }

        task.setStatus(TaskStatus.COMPLETED);
        return taskRepository.save(task);
    }

    //returneaza o sarcina dupa ID cu verificari de autentificare
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = task.getProject();

        // verifica daca utilizatorul este membru al proiectului
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new SecurityException("You do not have access to this task.");
        }

        return task;
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

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

}


