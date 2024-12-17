package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class TaskPageController {

    @GetMapping("/taskspage")
    public String showTasks(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing tasks: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "tasks");
        model.addAttribute("headerTitle", "Tasks");
        return "taskspage";
    }
}