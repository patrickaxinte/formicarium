package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class ProjectsController {

    @GetMapping("/projects")
    public String showProjects(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing projects: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "projects");
        model.addAttribute("headerTitle", "Projects");
        return "base";
    }
}
