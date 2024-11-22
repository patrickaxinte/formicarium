package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class ReportsController {

    @GetMapping("/reports")
    public String showReports(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing reports: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "reports");
        model.addAttribute("headerTitle", "Reports");
        return "base";
    }
}
