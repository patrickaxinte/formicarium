package com.example.formicarium.service;

import com.example.formicarium.entity.Module;
import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.User;
import com.example.formicarium.repository.ModuleRepository;
import com.example.formicarium.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProjectRepository projectRepository;

    public ModuleService(ModuleRepository moduleRepository, ProjectRepository projectRepository) {
        this.moduleRepository = moduleRepository;
        this.projectRepository = projectRepository;
    }

    // Obține modulele pentru un proiect
    @Transactional(readOnly = true)
    public List<Module> getModulesByProjectId(Long projectId) {
        return moduleRepository.findByProjectId(projectId);
    }

    // Creează un nou modul
    @Transactional
    public Module createModule(Long projectId, Module module, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // Verifică dacă utilizatorul are permisiuni pentru a adăuga module
        User user = (User) authentication.getPrincipal();
        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        (m.getRole().equals("OWNER") || m.getRole().equals("MANAGER")));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to add modules to this project.");
        }

        module.setProject(project);
        return moduleRepository.save(module);
    }

    // Actualizează un modul existent
    @Transactional
    public Module updateModule(Long moduleId, Module updatedModule, Authentication authentication) {
        Module existingModule = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));

        Project project = existingModule.getProject();

        // Verifică dacă utilizatorul are permisiuni pentru a actualiza modulele
        User user = (User) authentication.getPrincipal();
        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        (m.getRole().equals("OWNER") || m.getRole().equals("MANAGER")));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to update modules in this project.");
        }

        existingModule.setName(updatedModule.getName());
        existingModule.setDescription(updatedModule.getDescription());

        return moduleRepository.save(existingModule);
    }

    // Șterge un modul
    @Transactional
    public void deleteModule(Long moduleId, Authentication authentication) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));

        Project project = module.getProject();

        // Verifică dacă utilizatorul are permisiuni pentru a șterge modulele
        User user = (User) authentication.getPrincipal();
        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()) &&
                        (m.getRole().equals("OWNER") || m.getRole().equals("MANAGER")));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to delete modules from this project.");
        }

        moduleRepository.delete(module);
    }

    // Obține un modul după ID
    @Transactional(readOnly = true)
    public Module getModuleById(Long moduleId, Authentication authentication) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));

        Project project = module.getProject();

        // Verifică dacă utilizatorul este membru al proiectului
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new SecurityException("You do not have access to this module.");
        }

        return module;
    }
}
