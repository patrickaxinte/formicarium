package controller;

import entity.Project;
import entity.User;
import repository.ProjectRepository;
import repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    // Obține lista de proiecte ale utilizatorului autentificat
    @GetMapping
    public ResponseEntity<List<Project>> getUserProjects(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(user.getProjects()); // Presupunem că User are o listă de proiecte
    }

    // Adaugă un nou proiect și îl asociază utilizatorului autentificat
    @PostMapping
    public ResponseEntity<String> addProjectToUser(Authentication authentication, @RequestBody Project projectRequest) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Crează un nou proiect și asociază-l utilizatorului
        Project newProject = new Project();
        newProject.setName(projectRequest.getName());
        newProject.setDescription(projectRequest.getDescription());
        newProject.getUsers().add(user); // Asociază utilizatorul cu proiectul
        projectRepository.save(newProject); // Salvează proiectul

        return ResponseEntity.ok("Project added successfully");
    }

    // Șterge un proiect asociat utilizatorului autentificat
    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(Authentication authentication, @PathVariable Long projectId) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verifică dacă proiectul există și dacă utilizatorul are acces la el
        Optional<Project> project = projectRepository.findById(projectId);
        if (project.isEmpty() || !project.get().getUsers().contains(user)) {
            return ResponseEntity.status(403).body("You do not have access to this project");
        }

        // Șterge proiectul
        projectRepository.delete(project.get());
        return ResponseEntity.ok("Project deleted successfully");
    }

    // Actualizează informațiile unui proiect
    @PutMapping("/{projectId}")
    public ResponseEntity<String> updateProject(Authentication authentication, @PathVariable Long projectId,
                                                @RequestBody Project projectDetails) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verifică dacă proiectul există și dacă utilizatorul are acces la el
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUsers().contains(user)) {
            return ResponseEntity.status(403).body("You do not have access to this project");
        }

        // Actualizează detaliile proiectului
        project.setName(projectDetails.getName());
        project.setDescription(projectDetails.getDescription());
        projectRepository.save(project);

        return ResponseEntity.ok("Project updated successfully");
    }

    // Asociază un utilizator existent la un proiect (adăugare colaborator)
    @PostMapping("/{projectId}/addUser/{userId}")
    public ResponseEntity<String> addUserToProject(Authentication authentication,
                                                   @PathVariable Long projectId, @PathVariable Long userId) {
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        if (!project.getUsers().contains(currentUser)) {
            return ResponseEntity.status(403).body("You do not have access to this project");
        }

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User to add not found"));

        // Asociază utilizatorul la proiect
        project.getUsers().add(userToAdd);
        projectRepository.save(project);

        return ResponseEntity.ok("User added to project successfully");
    }
}
