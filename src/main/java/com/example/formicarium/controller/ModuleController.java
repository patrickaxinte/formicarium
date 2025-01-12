package com.example.formicarium.controller;

import com.example.formicarium.entity.KanbanItem;
import com.example.formicarium.entity.Module;
import com.example.formicarium.entity.User;
import com.example.formicarium.service.ModuleService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    // afiseaza formularul pentru crearea sau editarea unui modul
    @GetMapping("/form")
    public String showModuleForm(@RequestParam Long projectId,
                                 @RequestParam(required = false) Long moduleId,
                                 Model model,
                                 Authentication authentication) {

        Module module = (moduleId != null)
                ? moduleService.getModuleById(moduleId, authentication)
                : new Module();

        model.addAttribute("moduleTypes", List.of("TODO_LIST", "KANBAN", "OTHER_TYPE"));
        model.addAttribute("module", module);
        model.addAttribute("projectId", projectId);
        model.addAttribute("headerTitle", (moduleId != null) ? "Edit Module" : "Add Module");
        return "module-form";
    }

    // gestioneaza crearea unui modul nou
    @PostMapping("/create")
    public String createModule(@RequestParam Long projectId,
                               @RequestParam String name,
                               @RequestParam String description,
                               @RequestParam String moduleType,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = (User) authentication.getPrincipal();
            moduleService.createModule(projectId, name, description, moduleType, user);
            redirectAttributes.addFlashAttribute("successMessage", "Module created successfully.");
            return "redirect:/projects/" + projectId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/modules/form?projectId=" + projectId;
        }
    }

    // vizualizeaza detaliile unui modul
    @GetMapping("/{moduleId}")
    public String viewModuleDetails(@PathVariable Long moduleId, Model model, Authentication auth) {
        try {
            Module module = moduleService.getModuleById(moduleId, auth);
            model.addAttribute("module", module);
            return "module-details";
        } catch (IllegalArgumentException | SecurityException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/modules";
        }
    }

    // gestioneaza stergerea unui modul
    @PostMapping("/delete/{moduleId}")
    public String deleteModule(@PathVariable Long moduleId, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            moduleService.deleteModule(moduleId, authentication);
            redirectAttributes.addFlashAttribute("successMessage", "Module deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting module: " + e.getMessage());
        }
        Module module = moduleService.getModuleById(moduleId, authentication);
        return "redirect:/projects/" + module.getProject().getId();
    }

    // adauga o sarcina noua intr-un modul de tip to-do list
    @PostMapping("/{moduleId}/tasks/add")
    public String addTaskToToDoList(@PathVariable Long moduleId,
                                    @RequestParam String taskName,
                                    Authentication authentication,
                                    RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            moduleService.addTaskToToDoList(moduleId, taskName, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Task added successfully.");
            return "redirect:/modules/" + moduleId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/modules/" + moduleId;
        }
    }

    // marcheaza o sarcina ca fiind completata
    @PostMapping("/tasks/{taskId}/complete")
    public String markTaskAsCompleted(@PathVariable Long taskId,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            moduleService.markTaskAsCompleted(taskId, currentUser);
            Long moduleId = moduleService.getTaskModuleId(taskId);
            redirectAttributes.addFlashAttribute("successMessage", "Task marked as completed.");
            return "redirect:/modules/" + moduleId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid task ID: " + taskId);
            return "redirect:/modules";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/modules";
        }
    }

    // sterge o sarcina dintr-un modul de tip to-do list
    @PostMapping("/tasks/{taskId}/delete")
    public String deleteTask(@PathVariable Long taskId,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            Long moduleId = moduleService.getTaskModuleId(taskId);
            moduleService.deleteTask(taskId, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Task deleted successfully.");
            return "redirect:/modules/" + moduleId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid task ID: " + taskId);
            return "redirect:/modules";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/modules";
        }
    }

    // adauga un element nou in kanban
    @PostMapping("/kanban/{moduleId}/add")
    public String addKanbanItem(@PathVariable Long moduleId,
                                @RequestParam String title,
                                @RequestParam String column,
                                RedirectAttributes redirectAttributes) {
        try {
            moduleService.addKanbanItem(moduleId, title, column);
            redirectAttributes.addFlashAttribute("successMessage", "Kanban item added successfully.");
            return "redirect:/modules/" + moduleId;
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/modules/" + moduleId;
        }
    }

    // muta un element in kanban (simulare drag & drop)
    @PostMapping("/kanban/{id}/move")
    public String moveKanbanItem(@PathVariable Long id,
                                 @RequestParam String newColumn,
                                 RedirectAttributes redirectAttributes) {
        try {
            KanbanItem updated = moduleService.moveKanbanItem(id, newColumn);
            redirectAttributes.addFlashAttribute("successMessage", "Kanban item moved successfully.");
            return "redirect:/modules/" + updated.getModule().getId();
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/modules";
        }
    }

    // sterge un element din kanban
    @PostMapping("/kanban/{id}/delete")
    public String deleteKanbanItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            KanbanItem item = moduleService.getKanbanItemById(id);
            Long moduleId = item.getModule().getId();
            moduleService.deleteKanbanItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Kanban item deleted successfully.");
            return "redirect:/modules/" + moduleId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid Kanban item ID: " + id);
            return "redirect:/modules";
        }
    }
}
