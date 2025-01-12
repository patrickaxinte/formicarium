package com.example.formicarium.controller;

import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.Task;
import com.example.formicarium.entity.User;
import com.example.formicarium.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // afiseaza pagina de sarcini cu toate sarcinile si sarcinile mele
    @GetMapping
    public String showTasks(
            @RequestParam(required = false) Long selectedProjectId,
            Authentication authentication,
            Model model) {

        User currentUser = (User) authentication.getPrincipal();

        // obtine proiectele in care utilizatorul poate crea sarcini
        List<Project> creatableProjects = taskService.findProjectsByUserAndRoles(currentUser, List.of("OWNER", "COLLABORATOR"));

        // daca un proiect este selectat, verifica si incarca-l
        Project selectedProject = null;
        if (selectedProjectId != null) {
            selectedProject = taskService.verifyUserAccessToProject(selectedProjectId, currentUser)
                    .orElse(null);
        }

        // obtine sarcinile atribuite utilizatorului
        List<Task> myTasks = taskService.getTasksAssignedToUser(currentUser.getId());

        // obtine toate sarcinile pentru utilizator (proiecte in care este implicat)
        List<Task> allTasks = taskService.getAllTasksForUser(currentUser.getId());

        model.addAttribute("selectedProject", selectedProject);
        model.addAttribute("creatableProjects", creatableProjects);
        model.addAttribute("myTasks", myTasks);
        model.addAttribute("allTasks", allTasks);
        model.addAttribute("headerTitle", "Tasks");
        model.addAttribute("activePage", "tasks");
        return "taskspage";
    }

    // selecteaza un proiect pentru a crea o noua sarcina
    @PostMapping("/selectProject")
    public String selectProject(@RequestParam Long projectId) {
        return "redirect:/tasks?selectedProjectId=" + projectId;
    }

    // creeaza o noua sarcina
    @PostMapping("/create")
    public String createTask(@RequestParam Long projectId,
                             @RequestParam String name,
                             @RequestParam String description,
                             @RequestParam(required = false) String dueDate, // format 'YYYY-MM-DD'
                             @RequestParam Long assigneeId,
                             Authentication authentication) {
        try {
            taskService.createTask(projectId, name, description, dueDate, assigneeId, authentication);
        } catch (Exception e) {
            return "redirect:/tasks?error=" + e.getMessage();
        }
        return "redirect:/tasks?success=Task+created+successfully";
    }

    // actualizeaza o sarcina
    @PostMapping("/update/{taskId}")
    public String updateTask(@PathVariable Long taskId,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(required = false) String dueDate,
                             Authentication authentication) {
        try {
            taskService.updateTask(taskId, status, title, description, dueDate, authentication);
        } catch (Exception e) {
            return "redirect:/tasks?error=" + e.getMessage();
        }
        return "redirect:/tasks?success=Task+updated+successfully";
    }

    // sterge o sarcina
    @PostMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId, Authentication authentication) {
        try {
            Task task = taskService.getTaskById(taskId, authentication);
            Long projectId = task.getProject().getId();
            taskService.deleteTask(taskId, authentication);
            return "redirect:/projects/" + projectId + "?success=Task+deleted+successfully";
        } catch (Exception e) {
            return "redirect:/tasks?error=" + e.getMessage();
        }
    }

    // vizualizeaza detalii sarcina
    @GetMapping("/{id}")
    public String viewTaskDetails(@PathVariable Long id, Authentication authentication, Model model) {
        try {
            Task task = taskService.getTaskById(id, authentication);
            User currentUser = (User) authentication.getPrincipal();
            boolean isCreator = task.getCreatedBy().getId().equals(currentUser.getId());

            model.addAttribute("task", task);
            model.addAttribute("isCreator", isCreator);
            model.addAttribute("headerTitle", "Task Details");
            model.addAttribute("activePage", "tasks");
            return "task-details";
        } catch (Exception e) {
            return "redirect:/tasks?error=" + e.getMessage();
        }
    }
}
