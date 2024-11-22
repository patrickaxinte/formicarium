package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String showSettings(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing settings: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "settings");
        model.addAttribute("headerTitle", "Settings");
        return "base";
    }
}
