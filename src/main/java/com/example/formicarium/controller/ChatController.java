package com.example.formicarium.controller;

import com.example.formicarium.entity.Project;
import com.example.formicarium.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class ChatController {

    private final ProjectService projectService;

    public ChatController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/chat")
    public String showChatHub(Authentication authentication, Model model) {
        // Get all active projects the user is a member of
        List<Project> projects = projectService.getActiveProjectsForUser(authentication);

        model.addAttribute("projects", projects);
        model.addAttribute("headerTitle", "Group Chats");
        model.addAttribute("activePage", "chat");
        return "chat-hub";
    }
}
