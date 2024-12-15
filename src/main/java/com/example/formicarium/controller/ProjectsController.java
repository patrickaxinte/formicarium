package com.example.formicarium.controller;

import com.example.formicarium.entity.Project;
import com.example.formicarium.service.ProjectService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectsController {

    private final ProjectService projectService;

    public ProjectsController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Afișarea tuturor proiectelor active și inactive
    @GetMapping
    public String showProjects(Authentication authentication, Model model) {
        List<Project> activeProjects = projectService.getActiveProjectsForUser(authentication);
        List<Project> inactiveProjects = projectService.getInactiveProjectsForUser(authentication);

        model.addAttribute("activeProjects", activeProjects);
        model.addAttribute("inactiveProjects", inactiveProjects);
        model.addAttribute("pageContent", "projects");
        model.addAttribute("headerTitle", "Projects");
        return "base";
    }

    // Reactivarea unui proiect
    @PostMapping("/activate/{id}")
    public String activateProject(@PathVariable Long id, Authentication authentication, Model model) {
        projectService.activateProject(id, authentication);

        // Reîncarcă listele după activare
        List<Project> activeProjects = projectService.getActiveProjectsForUser(authentication);
        List<Project> inactiveProjects = projectService.getInactiveProjectsForUser(authentication);
        model.addAttribute("activeProjects", activeProjects);
        model.addAttribute("inactiveProjects", inactiveProjects);

        return "redirect:/projects";
    }

    // Dezactivarea unui proiect
    @PostMapping("/deactivate/{id}")
    public String deactivateProject(@PathVariable Long id, Authentication authentication) {
        projectService.deactivateProject(id, authentication);
        return "redirect:/projects";
    }

    // Formular universal pentru creare/editare proiect
    @GetMapping("/form")
    public String showProjectForm(@RequestParam(required = false) Long id, Model model, Authentication authentication) {
        Project project = (id != null)
                ? projectService.getProjectForEdit(id, authentication)
                : new Project(); // Proiect nou dacă `id` este null

        model.addAttribute("project", project);
        model.addAttribute("pageContent", "project-form");
        model.addAttribute("headerTitle", id != null ? "Edit Project" : "Add Project");
        return "base";
    }

    // Salvarea unui proiect (nou sau editat)
    @PostMapping("/form")
    public String saveProject(@RequestParam(required = false) Long id, @ModelAttribute Project project, Authentication authentication) {
        if (id == null) {
            projectService.createProject(project, authentication); // Creare
        } else {
            projectService.updateProject(id, project, authentication); // Editare
        }
        return "redirect:/projects";
    }

    // Ștergerea unui proiect
    @PostMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, Authentication authentication) {
        projectService.deleteProject(id, authentication);
        return "redirect:/projects";
    }
}
