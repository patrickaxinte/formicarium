package com.example.formicarium.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class ChatController {

    @GetMapping("/chat")
    public String showChat(Authentication authentication, Model model) {
        if (authentication != null) {
            System.out.println("User accessing chat: " + authentication.getName());
        } else {
            System.out.println("User not authenticated");
        }

        model.addAttribute("pageContent", "chat");
        model.addAttribute("headerTitle", "Group Chats");
        return "base";
    }
}
