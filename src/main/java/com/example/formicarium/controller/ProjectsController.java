package com.example.formicarium.controller;

import com.example.formicarium.entity.Message;
import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.Task;
import com.example.formicarium.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectsController {

    private final ProjectService projectService;

    public ProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // display all active and inactive projects
    @GetMapping
    public String showProjects(Authentication authentication, Model model) {
        List<Project> activeProjects = projectService.getActiveProjectsForUser(authentication);
        List<Project> inactiveProjects = projectService.getInactiveProjectsForUser(authentication);

        model.addAttribute("activeProjects", activeProjects);
        model.addAttribute("inactiveProjects", inactiveProjects);
        model.addAttribute("headerTitle", "Projects");
        model.addAttribute("activePage", "projects");
        return "projects";
    }

    // display project details
    @GetMapping("/{id}")
    public String showProjectDetails(@PathVariable Long id, Authentication authentication, Model model) {
        Project project = projectService.getProjectDetails(id, authentication);
        List<Message> recentMessages = projectService.getRecentMessagesForProject(id);
        List<Task> recentTasks = projectService.getRecentTasksForProject(id);

        model.addAttribute("project", project);
        model.addAttribute("recentMessages", recentMessages);
        model.addAttribute("recentTasks", recentTasks);
        model.addAttribute("headerTitle", project.getName());
        model.addAttribute("isOwner", projectService.isOwner(project, authentication));
        model.addAttribute("isCollaborator", projectService.isCollaborator(project, authentication));
        model.addAttribute("activePage", "projects");

        return "project-details";
    }

    // form for adding a new member to a project
    @GetMapping("/{projectId}/members/add")
    public String showAddMemberForm(@PathVariable Long projectId, Authentication authentication, Model model) {
        // check permissions (OWNER or COLLABORATOR)
        projectService.verifyCanAddMembers(projectId, authentication);

        // add form attributes (usernameOrEmail, role)
        model.addAttribute("usernameOrEmail", "");
        model.addAttribute("selectedRole", "MEMBER"); // Default to MEMBER
        model.addAttribute("projectId", projectId);
        model.addAttribute("headerTitle", "Add New Member");

        return "project-add-member";
    }

    // add a new member via a form
    @PostMapping("/{projectId}/members/add")
    public String addNewMember(@PathVariable Long projectId,
                               @RequestParam("usernameOrEmail") String usernameOrEmail,
                               @RequestParam("role") String role,
                               Authentication authentication,
                               Model model) {
        try {
            projectService.addMemberToProject(projectId, usernameOrEmail, role, authentication);
            return "redirect:/projects/" + projectId;
        } catch (IllegalArgumentException | SecurityException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("usernameOrEmail", usernameOrEmail);
            model.addAttribute("selectedRole", role);
            model.addAttribute("projectId", projectId);
            model.addAttribute("headerTitle", "Add New Member");
            return "project-add-member";
        }
    }

    // reactivate a project
    @PostMapping("/activate/{id}")
    public String activateProject(@PathVariable Long id, Authentication authentication) {
        projectService.activateProject(id, authentication);
        return "redirect:/projects";
    }

    // deactivate a project
    @PostMapping("/deactivate/{id}")
    public String deactivateProject(@PathVariable Long id, Authentication authentication) {
        projectService.deactivateProject(id, authentication);
        return "redirect:/projects";
    }

    // universal form for creating/editing a project
    @GetMapping("/form")
    public String showProjectForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        Project project = (id != null)
                ? projectService.getProjectForEdit(id, authentication)
                : new Project();

        model.addAttribute("project", project);
        model.addAttribute("headerTitle", id != null ? "Edit Project" : "Add Project");
        return "project-form";
    }

    @PostMapping("/form")
    public String saveProject(@Valid @ModelAttribute("project") Project project,
                              BindingResult bindingResult,
                              Authentication authentication,
                              Model model) {
        if (bindingResult.hasErrors()) {
            return "project-form";
        }

        try {
            projectService.createProject(project, authentication);
            return "redirect:/projects";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "project-form";
        }
    }

    // delete a project
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, Authentication authentication) {
        projectService.deleteProject(id, authentication);
        return "redirect:/projects";
    }

    @GetMapping("/remove-member")
    public String removeMember(@RequestParam("projectId") Long projectId,
                               @RequestParam("userId") Long userId,
                               Authentication authentication) {
        projectService.removeMemberFromProject(projectId, userId, authentication);
        return "redirect:/projects/" + projectId;
    }

}