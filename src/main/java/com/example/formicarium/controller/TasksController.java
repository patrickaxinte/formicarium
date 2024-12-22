package com.example.formicarium.controller;

import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.Task;
import com.example.formicarium.entity.TaskStatus;
import com.example.formicarium.entity.User;
import com.example.formicarium.service.TaskService;
import com.example.formicarium.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TasksController {

    private final TaskService taskService;
    private final ProjectRepository projectRepository;

    public TasksController(TaskService taskService, ProjectRepository projectRepository) {
        this.taskService = taskService;
        this.projectRepository = projectRepository;
    }

    // Display Tasks Page with Project Selection
    @GetMapping("/page")
    public String showTasksPage(Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        // Fetch creatable projects with roles OWNER and COLLABORATOR
        List<Project> creatableProjects = projectRepository.findProjectsByUserAndRoles(currentUser, List.of("OWNER", "COLLABORATOR"));
        // Removed problematic logging

        // Fetch assigned tasks
        List<Task> assignedTasks = taskService.getTasksAssignedToUser(currentUser.getId());

        model.addAttribute("creatableProjects", creatableProjects);
        model.addAttribute("assignedTasks", assignedTasks);
        model.addAttribute("headerTitle", "Create Task");
        return "taskspage"; // Ensure this matches your Thymeleaf template name
    }

    // Handle Project Selection and Display Task Creation Form
    @PostMapping("/selectProject")
    public String selectProject(@RequestParam Long projectId, Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        Project selectedProject = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // Verify the user has access to the selected project
        if (!taskService.isUserMemberOfProject(selectedProject, currentUser)) {
            return "redirect:/tasks/page?error=You+do+not+have+access+to+this+project.";
        }

        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("creatableProjects", projectRepository.findProjectsByUserAndRoles(currentUser, List.of("OWNER", "COLLABORATOR")));
        model.addAttribute("assignedTasks", taskService.getTasksAssignedToUser(currentUser.getId()));
        model.addAttribute("headerTitle", "Create Task");
        return "taskspage";
    }

    // Create Task
    @PostMapping("/create")
    public String createTask(@RequestParam Long projectId,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam(required = false) String dueDate, // format 'YYYY-MM-DD'
                             @RequestParam Long assigneeId,
                             Authentication authentication) {
        Task task = Task.builder()
                .name(name)
                .description(description)
                .build();

        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                task.setDueDate(LocalDate.parse(dueDate));
            } catch (DateTimeParseException e) {
                // Handle invalid date format
                return "redirect:/tasks/page?error=Invalid+due+date+format";
            }
        }

        // Assign the user
        User assignee = taskService.getUserById(assigneeId);
        if (assignee == null) {
            return "redirect:/tasks/page?error=Assignee+not+found";
        }
        task.setAssignedTo(assignee);

        // Create Task
        try {
            taskService.createTask(projectId, task, authentication);
        } catch (Exception e) {
            return "redirect:/tasks/page?error=" + e.getMessage();
        }

        return "redirect:/tasks/page?success=Task+created+successfully";
    }

    // Update Task Status
    @PostMapping("/update/{taskId}")
    public String updateTaskStatus(@PathVariable Long taskId,
                                   @RequestParam String status,
                                   Authentication authentication) {
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(status.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            newStatus = TaskStatus.TO_DO;
        }

        try {
            if (newStatus == TaskStatus.COMPLETED) {
                taskService.markTaskAsCompleted(taskId, authentication);
            } else {
                Task updatedTask = new Task();
                updatedTask.setStatus(newStatus);
                taskService.updateTask(taskId, updatedTask, authentication);
            }
        } catch (Exception e) {
            return "redirect:/tasks/page?error=" + e.getMessage();
        }

        return "redirect:/tasks/page?success=Task+updated+successfully";
    }

    // Delete Task
    @PostMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId, Authentication authentication) {
        try {
            Task task = taskService.getTaskById(taskId, authentication);
            Long projectId = task.getProject().getId();
            taskService.deleteTask(taskId, authentication);
            return "redirect:/projects/" + projectId + "?success=Task+deleted+successfully";
        } catch (Exception e) {
            return "redirect:/tasks/page?error=" + e.getMessage();
        }
    }
}


