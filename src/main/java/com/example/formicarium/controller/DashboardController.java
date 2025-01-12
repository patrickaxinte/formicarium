package com.example.formicarium.controller;

import com.example.formicarium.entity.Task;
import com.example.formicarium.entity.User;
import com.example.formicarium.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final TaskService taskService;

    public DashboardController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Authentication authentication, Model model) {
        if (authentication == null) {
            System.out.println("User not authenticated");
            return "redirect:/login"; // Sau altă redirecționare
        } else {
            System.out.println("Authenticated user: " + authentication.getName());
        }

        // Obținem user-ul curent
        User currentUser = (User) authentication.getPrincipal();

        // obține lista sarcinilor atribuite user-ului
        List<Task> myTasks = taskService.getTasksAssignedToUser(currentUser.getId());

        // Adaugă lista de task-uri în model, pentru afișarea în dashboard.html
        model.addAttribute("myTasks", myTasks);

        return "dashboard";
    }
}
