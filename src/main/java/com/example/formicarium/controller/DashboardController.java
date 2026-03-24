package com.example.formicarium.controller;

import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.Task;
import com.example.formicarium.entity.User;
import com.example.formicarium.service.ProjectService;
import com.example.formicarium.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final TaskService taskService;
    private final ProjectService projectService;

    public DashboardController(TaskService taskService, ProjectService projectService) {
        this.taskService = taskService;
        this.projectService = projectService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        User currentUser = (User) authentication.getPrincipal();

        // Get tasks assigned to the current user
        List<Task> myTasks = taskService.getTasksAssignedToUser(currentUser.getId());
        
        // Get active projects
        List<Project> activeProjects = projectService.getActiveProjectsForUser(authentication);

        // Calculate stats
        long completedTasks = myTasks.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long pendingTasks = myTasks.size() - completedTasks;

        model.addAttribute("myTasks", myTasks); // Keep for the recent tasks list
        model.addAttribute("activeProjectsCount", activeProjects.size());
        model.addAttribute("completedTasksCount", completedTasks);
        model.addAttribute("pendingTasksCount", pendingTasks);
        
        model.addAttribute("headerTitle", "Dashboard");
        model.addAttribute("activePage", "dashboard");
        return "dashboard";
    }
}
