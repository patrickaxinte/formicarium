package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard(org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            System.out.println("User not authenticated");
        } else {
            System.out.println("Authenticated user: " + authentication.getName());
        }
        return "dashboard";
    }
}
