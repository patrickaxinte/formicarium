package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String showProfile(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing profile: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "profile");
        model.addAttribute("headerTitle", "Profile");
        return "base";
    }
}


