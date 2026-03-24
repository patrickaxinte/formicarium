package com.example.formicarium.controller;

import com.example.formicarium.entity.User;
import com.example.formicarium.repository.ProjectRepository;
import com.example.formicarium.repository.TaskRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public ProfileController(ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        User currentUser = (User) authentication.getPrincipal();

        // Count projects the user is a member of
        long projectCount = projectRepository.findProjectsByUserAndIsActive(currentUser, true).size();

        // Count tasks assigned to the user
        long taskCount = taskRepository.findByAssignedToIdWithCreatedBy(currentUser.getId()).size();

        model.addAttribute("user", currentUser);
        model.addAttribute("projectCount", projectCount);
        model.addAttribute("taskCount", taskCount);
        model.addAttribute("headerTitle", "Profile");
        model.addAttribute("activePage", "profile");
        return "profile";
    }
}
