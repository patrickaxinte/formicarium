package com.example.formicarium.service;

import com.example.formicarium.entity.Task;
import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.User;
import com.example.formicarium.repository.TaskRepository;
import com.example.formicarium.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
    }

    // Obține sarcinile recente pentru un proiect
    @Transactional(readOnly = true)
    public List<Task> getRecentTasksByProject(Long projectId) {
        return taskRepository.findTop5ByProjectIdOrderByCreatedAtDesc(projectId);
    }

    // Obține toate sarcinile pentru un proiect
    @Transactional(readOnly = true)
    public List<Task> getAllTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    // Crează o nouă sarcină
    @Transactional
    public Task createTask(Long projectId, Task task, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // Verifică dacă utilizatorul are permisiuni pentru a adăuga sarcini
        User user = (User) authentication.getPrincipal();
        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        (m.getRole().equals("OWNER") || m.getRole().equals("COLLABORATOR")));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to add tasks to this project.");
        }

        task.setProject(project);
        return taskRepository.save(task);
    }

    // Actualizează o sarcină existentă
    @Transactional
    public Task updateTask(Long taskId, Task updatedTask, Authentication authentication) {
        Task existingTask = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = existingTask.getProject();

        // Verifică dacă utilizatorul are permisiuni pentru a actualiza sarcinile
        User user = (User) authentication.getPrincipal();
        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        (m.getRole().equals("OWNER") || m.getRole().equals("COLLABORATOR")));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to update tasks in this project.");
        }

        existingTask.setName(updatedTask.getName());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setAssignedTo(updatedTask.getAssignedTo());

        return taskRepository.save(existingTask);
    }

    // Șterge o sarcină
    @Transactional
    public void deleteTask(Long taskId, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = task.getProject();

        // Verifică dacă utilizatorul are permisiuni pentru a șterge sarcinile
        User user = (User) authentication.getPrincipal();
        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        (m.getRole().equals("OWNER") || m.getRole().equals("COLLABORATOR")));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to delete tasks from this project.");
        }

        taskRepository.delete(task);
    }

    // Obține o sarcină după ID
    @Transactional(readOnly = true)
    public Task getTaskById(Long taskId, Authentication authentication) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        Project project = task.getProject();

        // Verifică dacă utilizatorul este membru al proiectului
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new SecurityException("You do not have access to this task.");
        }

        return task;
    }
}
