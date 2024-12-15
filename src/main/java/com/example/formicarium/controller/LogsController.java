package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class LogsController {

    @GetMapping("/logs")
    public String showLogs(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing logs: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "logs");
        model.addAttribute("headerTitle", "Logs");
        return "base";
    }
}
