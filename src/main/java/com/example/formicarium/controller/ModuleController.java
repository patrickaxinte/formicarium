package com.example.formicarium.controller;

import com.example.formicarium.entity.Module;
import com.example.formicarium.service.ModuleService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/modules")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    // formular pentru crearea unui modul nou
    @GetMapping("/form")
    public String showModuleForm(@RequestParam Long projectId, @RequestParam(required = false) Long moduleId, Model model, Authentication authentication) {
        Module module = (moduleId != null) ? moduleService.getModuleById(moduleId, authentication) : new Module();
        model.addAttribute("module", module);
        model.addAttribute("projectId", projectId);
        model.addAttribute("headerTitle", (moduleId != null) ? "Edit Module" : "Add Module");
        model.addAttribute("pageContent", "module-form");
        return "base";
    }

    // salvarea unui modul (nou sau editat)
    @PostMapping("/form")
    public String saveModule(@RequestParam Long projectId, @RequestParam(required = false) Long moduleId,
                             @ModelAttribute Module module, Authentication authentication) {
        if (moduleId == null) {
            moduleService.createModule(projectId, module, authentication);
        } else {
            moduleService.updateModule(moduleId, module, authentication);
        }
        return "redirect:/projects/" + projectId;
    }

    // sterge un modul
    @PostMapping("/delete/{moduleId}")
    public String deleteModule(@PathVariable Long moduleId, @RequestParam Long projectId, Authentication authentication) {
        moduleService.deleteModule(moduleId, authentication);
        return "redirect:/projects/" + projectId;
    }
}
