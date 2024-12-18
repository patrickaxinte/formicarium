package com.example.formicarium.controller;

import com.example.formicarium.entity.Task;
import com.example.formicarium.entity.TaskStatus;
import com.example.formicarium.entity.Project;
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

    //se afiseaza endpoint-urile pentru pagina Task si sarcinile recente de pe paginil proiectelor
    @GetMapping("/page")
    public String showTasksPage(Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        // se obtin proiectele unde utilizatorul are rolul OWNER sau COLLABORATOR pentru crearea sarcinilor
        List<Project> creatableProjects = projectRepository.findProjectsByUserAndRoles(currentUser, List.of("OWNER", "COLLABORATOR"));

        // se obtin sarcinile atribuite utilizatorilor din fiecare proiect
        List<Task> assignedTasks = taskService.getTasksAssignedToUser(currentUser.getId());

        model.addAttribute("creatableProjects", creatableProjects);
        model.addAttribute("assignedTasks", assignedTasks);
        model.addAttribute("headerTitle", "Tasks");
        return "taskspage";
    }

    // crearea sarcinilor
    @PostMapping("/create")
    public String createTask(@RequestParam Long projectId,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam(required = false) String dueDate, // format acceptat 'YYYY-MM-DD'
                             @RequestParam Long assigneeId,
                             Authentication authentication,
                             Model model) {
        Task task = Task.builder()
                .name(name)
                .description(description)
                .build();

        if (dueDate != null && !dueDate.isEmpty()) {
            try {
                task.setDueDate(LocalDate.parse(dueDate));
            } catch (DateTimeParseException e) {
                // eroare pentru format de data gresit
                return "redirect:/tasks/page?error=Invalid+due+date+format";
            }
        }

        User assignee = new User();
        assignee.setId(assigneeId);
        task.setAssignedTo(assignee);

        // crearea unei sarcini
        try {
            taskService.createTask(projectId, task, authentication);
        } catch (Exception e) {
            return "redirect:/tasks/page?error=" + e.getMessage();
        }

        return "redirect:/tasks/page?success=Task+created+successfully";
    }

    // starea unei sarcini (de ex. "Task Completed)
    @PostMapping("/update/{taskId}")
    public String updateTaskStatus(@PathVariable Long taskId,
                                   @RequestParam String status,
                                   Authentication authentication,
                                   Model model) {
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
                //tratarea altor exceptii
                Task updatedTask = new Task();
                updatedTask.setStatus(newStatus);
                taskService.updateTask(taskId, updatedTask, authentication);
            }
        } catch (Exception e) {
            return "redirect:/tasks/page?error=" + e.getMessage();
        }

        return "redirect:/tasks/page?success=Task+updated+successfully";
    }

    //stergerea unei sarcini
    @PostMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId, Authentication authentication, Model model) {
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
