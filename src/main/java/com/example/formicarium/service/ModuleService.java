//package com.example.formicarium.service;
//
//import com.example.formicarium.entity.*;
//import com.example.formicarium.entity.Module;
//import com.example.formicarium.repository.KanbanItemRepository;
//import com.example.formicarium.repository.ModuleRepository;
//import com.example.formicarium.repository.ProjectRepository;
//import com.example.formicarium.repository.ToDoItemRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.List;
//
//@Service
//public class ModuleService {
//
//    private final ModuleRepository moduleRepository;
//    private final ToDoItemRepository toDoItemRepository;
//    private final ProjectRepository projectRepository;
//    private final KanbanItemRepository kanbanItemRepository;
//
//   public ModuleService(ModuleRepository moduleRepository,
//                          ToDoItemRepository toDoItemRepository,
//                          ProjectRepository projectRepository,
//                          KanbanItemRepository kanbanItemRepository) { // NEW
//        this.moduleRepository = moduleRepository;
//        this.toDoItemRepository = toDoItemRepository;
//        this.projectRepository = projectRepository;
//        this.kanbanItemRepository = kanbanItemRepository; // NEW
//    }
//
//    @Transactional
//    public Module createModule(Long projectId, String name, String description, String moduleType, User user) {
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
//
//        // Validate moduleType
//        if (!List.of("TODO_LIST", "KANBAN", "OTHER_TYPE").contains(moduleType)) {
//            throw new IllegalArgumentException("Invalid module type: " + moduleType);
//        }
//
//
//        // Check for duplicate module name
//        boolean exists = project.getModules().stream()
//                .anyMatch(module -> module.getName().equalsIgnoreCase(name));
//
//        if (exists) {
//            throw new IllegalStateException("A module with this name already exists in the project.");
//        }
//
//        Module module = Module.builder()
//                .name(name)
//                .description(description)
//                .moduleType(moduleType) // Assign moduleType
//                .project(project)
//                .build();
//
//        return moduleRepository.save(module);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ToDoItem> getTasksByModule(Long moduleId) {
//        return toDoItemRepository.findByModuleId(moduleId);
//    }
//
//
//    @Transactional
//    public ToDoItem addTaskToToDoList(Long moduleId, String taskName) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
//
//        ToDoItem task = ToDoItem.builder()
//                .name(taskName)
//                .module(module)
//                .isCompleted(false)
//                .build();
//
//        return toDoItemRepository.save(task);
//    }
//
//    @Transactional
//    public ToDoItem markTaskAsCompleted(Long taskId) {
//        ToDoItem task = toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
//        task.setCompleted(true); // Use correct setter
//        return toDoItemRepository.save(task);
//    }
//    @Transactional
//    public void deleteTask(Long taskId) {
//        toDoItemRepository.deleteById(taskId);
//    }
//
//    @Transactional(readOnly = true)
//    public ToDoItem getToDoItem(Long taskId) {
//        return toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
//    }
//
//
//    @Transactional(readOnly = true)
//    public Module getModuleById(Long moduleId, Authentication authentication) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
//
//        // Verify access to the project
//        Project project = module.getProject();
//        User currentUser = (User) authentication.getPrincipal();
//        boolean hasAccess = project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
//
//        if (!hasAccess) {
//            throw new SecurityException("You do not have access to this module.");
//        }
//
//        // Load tasks for TODO_LIST modules
//        if ("TODO_LIST".equals(module.getModuleType())) {
//            List<ToDoItem> tasks = getTasksByModule(moduleId);
//            module.setTasks(tasks); // Ensure tasks are set dynamically
//        }
//
//        if ("KANBAN".equals(module.getModuleType())) {
//            List<KanbanItem> items = getKanbanItemsByModule(moduleId);
//            module.setKanbanItems(items);
//        }
//
//        return module;
//    }
//
//    @Transactional
//    public void deleteModule(Long moduleId, Authentication authentication) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
//
//        Project project = module.getProject();
//        User currentUser = (User) authentication.getPrincipal();
//
//        boolean hasPermission = project.getMemberships().stream()
//                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()) && m.getRole().equals("OWNER"));
//
//        if (!hasPermission) {
//            throw new SecurityException("You do not have permission to delete this module.");
//        }
//
//        moduleRepository.delete(module);
//    }
//
//
//    // 1) Retrieve all KanbanItems for a module
//    @Transactional(readOnly = true)
//    public List<KanbanItem> getKanbanItemsByModule(Long moduleId) {
//        return kanbanItemRepository.findByModuleId(moduleId);
//    }
//
//    // 2) Add new Kanban item
//    @Transactional
//    public KanbanItem addKanbanItem(Long moduleId, String title, String column) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
//
//        // only if module.moduleType == "KANBAN" do we add
//        if (!"KANBAN".equals(module.getModuleType())) {
//            throw new IllegalStateException("Module is not a KANBAN type");
//        }
//
//        KanbanItem item = KanbanItem.builder()
//                .title(title)
//                .kanbanColumn(column) // "TODO" / "IN PROGRESS" / "DONE"
//                .module(module)
//                .build();
//
//        return kanbanItemRepository.save(item);
//    }
//
//    // 3) Move a Kanban item to another column
//    @Transactional
//    public KanbanItem moveKanbanItem(Long itemId, String newColumn) {
//        KanbanItem item = kanbanItemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found"));
//
//        item.setKanbanColumn(newColumn);
//        return kanbanItemRepository.save(item);
//    }
//
//    @Transactional(readOnly = true)
//    public KanbanItem getKanbanItemById(Long itemId) {
//        return kanbanItemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found with ID: " + itemId));
//    }
//
//    @Transactional
//    public void deleteKanbanItem(Long itemId) {
//        kanbanItemRepository.deleteById(itemId);
//    }
//
//
//}

//package com.example.formicarium.service;
//
//import com.example.formicarium.entity.*;
//import com.example.formicarium.entity.Module;
//import com.example.formicarium.repository.KanbanItemRepository;
//import com.example.formicarium.repository.ModuleRepository;
//import com.example.formicarium.repository.ProjectRepository;
//import com.example.formicarium.repository.ToDoItemRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import java.util.List;
//
//@Service
//public class ModuleService {
//
//    private final ModuleRepository moduleRepository;
//    private final ToDoItemRepository toDoItemRepository;
//    private final ProjectRepository projectRepository;
//    private final KanbanItemRepository kanbanItemRepository;
//
//    public ModuleService(ModuleRepository moduleRepository,
//                         ToDoItemRepository toDoItemRepository,
//                         ProjectRepository projectRepository,
//                         KanbanItemRepository kanbanItemRepository) {
//        this.moduleRepository = moduleRepository;
//        this.toDoItemRepository = toDoItemRepository;
//        this.projectRepository = projectRepository;
//        this.kanbanItemRepository = kanbanItemRepository;
//    }
//
//    @Transactional
//    public Module createModule(Long projectId, String name, String description, String moduleType, User user) {
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
//
//        // Validate moduleType
//        if (!List.of("TODO_LIST", "KANBAN", "OTHER_TYPE").contains(moduleType)) {
//            throw new IllegalArgumentException("Invalid module type: " + moduleType);
//        }
//
//        // Check for duplicate module name
//        boolean exists = project.getModules().stream()
//                .anyMatch(module -> module.getName().equalsIgnoreCase(name));
//
//        if (exists) {
//            throw new IllegalStateException("A module with this name already exists in the project.");
//        }
//
//        Module module = Module.builder()
//                .name(name)
//                .description(description)
//                .moduleType(moduleType) // Assign moduleType
//                .project(project)
//                .build();
//
//        return moduleRepository.save(module);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ToDoItem> getTasksByModule(Long moduleId) {
//        return toDoItemRepository.findByModuleId(moduleId);
//    }
//
//    @Transactional
//    public ToDoItem addTaskToToDoList(Long moduleId, String taskName, User user) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
//
//        ToDoItem task = ToDoItem.builder()
//                .name(taskName)
//                .module(module)
//                .isCompleted(false)
//                .user(user) // Associate with user
//                .build();
//
//        return toDoItemRepository.save(task);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ToDoItem> getTasksByModuleAndUser(Long moduleId, Long userId) {
//        return toDoItemRepository.findByModuleIdAndUserId(moduleId, userId);
//    }
//
//    @Transactional
//    public ToDoItem markTaskAsCompleted(Long taskId, User user) {
//        ToDoItem task = toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
//
//        // Ensure the task belongs to the user
//        if (!task.getUser().getId().equals(user.getId())) {
//            throw new SecurityException("You do not have permission to modify this task.");
//        }
//
//        task.setCompleted(true);
//        return toDoItemRepository.save(task);
//    }
//
//    @Transactional
//    public void deleteTask(Long taskId, User user) {
//        ToDoItem task = toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
//
//        // Ensure the task belongs to the user
//        if (!task.getUser().getId().equals(user.getId())) {
//            throw new SecurityException("You do not have permission to delete this task.");
//        }
//
//        toDoItemRepository.delete(task);
//    }
//
//
////    @Transactional
////    public ToDoItem addTaskToToDoList(Long moduleId, String taskName) {
////        Module module = moduleRepository.findById(moduleId)
////                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
////
////        ToDoItem task = ToDoItem.builder()
////                .name(taskName)
////                .module(module)
////                .isCompleted(false)
////                .build();
////
////        return toDoItemRepository.save(task);
////    }
//
//
////    @Transactional
////    public ToDoItem markTaskAsCompleted(Long taskId) {
////        ToDoItem task = toDoItemRepository.findById(taskId)
////                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
////        task.setCompleted(true); // Use correct setter
////        return toDoItemRepository.save(task);
////    }
////
////    @Transactional
////    public void deleteTask(Long taskId) {
////        toDoItemRepository.deleteById(taskId);
////    }
//
//
//
////    @Transactional(readOnly = true)
////    public ToDoItem getToDoItem(Long taskId) {
////        return toDoItemRepository.findById(taskId)
////                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));
////    }
//
////    @Transactional(readOnly = true)
////    public Module getModuleById(Long moduleId, Authentication authentication) {
////        Module module = moduleRepository.findById(moduleId)
////                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
////
////        // Verify access to the project
////        Project project = module.getProject();
////        User currentUser = (User) authentication.getPrincipal();
////        boolean hasAccess = project.getMemberships().stream()
////                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
////
////        if (!hasAccess) {
////            throw new SecurityException("You do not have access to this module.");
////        }
////
////        // Load tasks for TODO_LIST modules
////        if ("TODO_LIST".equals(module.getModuleType())) {
////            List<ToDoItem> tasks = getTasksByModule(moduleId);
////            module.setTasks(tasks); // Ensure tasks are set dynamically
////        }
////
////        if ("KANBAN".equals(module.getModuleType())) {
////            List<KanbanItem> items = getKanbanItemsByModule(moduleId);
////            module.setKanbanItems(items);
////        }
////
////        return module;
////    }
//
//    @Transactional(readOnly = true)
//    public Module getModuleById(Long moduleId, Authentication authentication) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
//
//        // Verify access to the project
//        Project project = module.getProject();
//        User currentUser = (User) authentication.getPrincipal();
//        boolean hasAccess = project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
//
//        if (!hasAccess) {
//            throw new SecurityException("You do not have access to this module.");
//        }
//
//        // Load tasks for TODO_LIST modules specific to the current user
//        if ("TODO_LIST".equals(module.getModuleType())) {
//            List<ToDoItem> tasks = getTasksByModuleAndUser(moduleId, currentUser.getId());
//            module.setTasks(tasks); // Ensure tasks are set dynamically
//        }
//
//        if ("KANBAN".equals(module.getModuleType())) {
//            List<KanbanItem> items = getKanbanItemsByModule(moduleId);
//            module.setKanbanItems(items);
//        }
//
//        return module;
//    }
//
//    @Transactional
//    public void deleteModule(Long moduleId, Authentication authentication) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
//
//        Project project = module.getProject();
//        User currentUser = (User) authentication.getPrincipal();
//
//        boolean hasPermission = project.getMemberships().stream()
//                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()) && m.getRole().equals("OWNER"));
//
//        if (!hasPermission) {
//            throw new SecurityException("You do not have permission to delete this module.");
//        }
//
//        moduleRepository.delete(module);
//    }
//
//    // 1) Retrieve all KanbanItems for a module
//    @Transactional(readOnly = true)
//    public List<KanbanItem> getKanbanItemsByModule(Long moduleId) {
//        return kanbanItemRepository.findByModuleId(moduleId);
//    }
//
//    // 2) Add new Kanban item
//    @Transactional
//    public KanbanItem addKanbanItem(Long moduleId, String title, String column) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
//
//        // only if module.moduleType == "KANBAN" do we add
//        if (!"KANBAN".equals(module.getModuleType())) {
//            throw new IllegalStateException("Module is not a KANBAN type");
//        }
//
//        KanbanItem item = KanbanItem.builder()
//                .title(title)
//                .kanbanColumn(column) // "TODO" / "IN_PROGRESS" / "DONE"
//                .module(module)
//                .build();
//
//        return kanbanItemRepository.save(item);
//    }
//
//    // 3) Move a Kanban item to another column
//    @Transactional
//    public KanbanItem moveKanbanItem(Long itemId, String newColumn) {
//        KanbanItem item = kanbanItemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found"));
//
//        item.setKanbanColumn(newColumn);
//        return kanbanItemRepository.save(item);
//    }
//
//    @Transactional(readOnly = true)
//    public KanbanItem getKanbanItemById(Long itemId) {
//        return kanbanItemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found with ID: " + itemId));
//    }
//
//    @Transactional
//    public void deleteKanbanItem(Long itemId) {
//        kanbanItemRepository.deleteById(itemId);
//    }
//}

//package com.example.formicarium.service;
//
//import com.example.formicarium.entity.*;
//import com.example.formicarium.entity.Module;
//import com.example.formicarium.repository.KanbanItemRepository;
//import com.example.formicarium.repository.ModuleRepository;
//import com.example.formicarium.repository.ProjectRepository;
//import com.example.formicarium.repository.ToDoItemRepository;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class ModuleService {
//
//    private final ModuleRepository moduleRepository;
//    private final ToDoItemRepository toDoItemRepository;
//    private final ProjectRepository projectRepository;
//    private final KanbanItemRepository kanbanItemRepository;
//
//    public ModuleService(ModuleRepository moduleRepository,
//                         ToDoItemRepository toDoItemRepository,
//                         ProjectRepository projectRepository,
//                         KanbanItemRepository kanbanItemRepository) {
//        this.moduleRepository = moduleRepository;
//        this.toDoItemRepository = toDoItemRepository;
//        this.projectRepository = projectRepository;
//        this.kanbanItemRepository = kanbanItemRepository;
//    }
//
//    @Transactional
//    public Module createModule(Long projectId, String name, String description, String moduleType, User user) {
//        Project project = projectRepository.findById(projectId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));
//
//        // Validate moduleType
//        if (!List.of("TODO_LIST", "KANBAN", "OTHER_TYPE").contains(moduleType)) {
//            throw new IllegalArgumentException("Invalid module type: " + moduleType);
//        }
//
//        // Check for duplicate module name
//        boolean exists = project.getModules().stream()
//                .anyMatch(module -> module.getName().equalsIgnoreCase(name));
//
//        if (exists) {
//            throw new IllegalStateException("A module with this name already exists in the project.");
//        }
//
//        Module module = Module.builder()
//                .name(name)
//                .description(description)
//                .moduleType(moduleType)
//                .project(project)
//                .build();
//
//        return moduleRepository.save(module);
//    }
//
//    @Transactional
//    public ToDoItem addTaskToToDoList(Long moduleId, String taskName, User user) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
//
//        // Ensure the module type is TODO_LIST
//        if (!"TODO_LIST".equals(module.getModuleType())) {
//            throw new IllegalStateException("Module is not a TODO_LIST type");
//        }
//
//        ToDoItem task = ToDoItem.builder()
//                .name(taskName)
//                .module(module)
//                .isCompleted(false)
//                .user(user)
//                .build();
//
//        return toDoItemRepository.save(task);
//    }
//
//    @Transactional(readOnly = true)
//    public List<ToDoItem> getTasksByModuleAndUser(Long moduleId, Long userId) {
//        return toDoItemRepository.findByModuleIdAndUserId(moduleId, userId);
//    }
//
//    @Transactional
//    public ToDoItem markTaskAsCompleted(Long taskId, User user) {
//        ToDoItem task = toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
//
//        // Ensure the task belongs to the current user
//        if (!task.getUser().getId().equals(user.getId())) {
//            throw new SecurityException("You do not have permission to modify this task.");
//        }
//
//        task.setCompleted(true);
//        task.setStatus("DONE"); // Optionally update the status
//        return toDoItemRepository.save(task);
//    }
//
//    @Transactional
//    public void deleteTask(Long taskId, User user) {
//        ToDoItem task = toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
//
//        // Ensure the task belongs to the user
//        if (!task.getUser().getId().equals(user.getId())) {
//            throw new SecurityException("You do not have permission to delete this task.");
//        }
//
//        toDoItemRepository.delete(task);
//    }
//
//    @Transactional(readOnly = true)
//    public Module getModuleById(Long moduleId, Authentication authentication) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
//
//        // Verify access to the project
//        Project project = module.getProject();
//        User currentUser = (User) authentication.getPrincipal();
//        boolean hasAccess = project.getMemberships().stream()
//                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));
//
//        if (!hasAccess) {
//            throw new SecurityException("You do not have access to this module.");
//        }
//
//        // Load tasks for TODO_LIST modules specific to the current user
//        if ("TODO_LIST".equals(module.getModuleType())) {
//            List<ToDoItem> tasks = getTasksByModuleAndUser(moduleId, currentUser.getId());
//            module.setTasks(tasks);
//        }
//
//        // Load Kanban items
//        if ("KANBAN".equals(module.getModuleType())) {
//            List<KanbanItem> items = getKanbanItemsByModule(moduleId);
//            module.setKanbanItems(items);
//        }
//
//        return module;
//    }
//
//    @Transactional
//    public void deleteModule(Long moduleId, Authentication authentication) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));
//
//        Project project = module.getProject();
//        User currentUser = (User) authentication.getPrincipal();
//
//        boolean hasPermission = project.getMemberships().stream()
//                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()) && m.getRole().equals("OWNER"));
//
//        if (!hasPermission) {
//            throw new SecurityException("You do not have permission to delete this module.");
//        }
//
//        moduleRepository.delete(module);
//    }
//
//    // 1) Retrieve all KanbanItems for a module
//    @Transactional(readOnly = true)
//    public List<KanbanItem> getKanbanItemsByModule(Long moduleId) {
//        return kanbanItemRepository.findByModuleId(moduleId);
//    }
//
//    // 2) Add new Kanban item
//    @Transactional
//    public KanbanItem addKanbanItem(Long moduleId, String title, String column) {
//        Module module = moduleRepository.findById(moduleId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));
//
//        // Only add if module type is KANBAN
//        if (!"KANBAN".equals(module.getModuleType())) {
//            throw new IllegalStateException("Module is not a KANBAN type");
//        }
//
//        KanbanItem item = KanbanItem.builder()
//                .title(title)
//                .kanbanColumn(column) // "TODO" / "IN_PROGRESS" / "DONE"
//                .module(module)
//                .build();
//
//        return kanbanItemRepository.save(item);
//    }
//
//    // 3) Move a Kanban item to another column
//    @Transactional
//    public KanbanItem moveKanbanItem(Long itemId, String newColumn) {
//        KanbanItem item = kanbanItemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found"));
//
//        item.setKanbanColumn(newColumn);
//        return kanbanItemRepository.save(item);
//    }
//
//    @Transactional(readOnly = true)
//    public KanbanItem getKanbanItemById(Long itemId) {
//        return kanbanItemRepository.findById(itemId)
//                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found with ID: " + itemId));
//    }
//
//    @Transactional
//    public void deleteKanbanItem(Long itemId) {
//        kanbanItemRepository.deleteById(itemId);
//    }
//
//    @Transactional(readOnly = true)
//    public Long getTaskModuleId(Long taskId) {
//        ToDoItem task = toDoItemRepository.findById(taskId)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
//        return task.getModule().getId();
//    }
//}


package com.example.formicarium.service;

import com.example.formicarium.entity.*;
import com.example.formicarium.entity.Module;
import com.example.formicarium.repository.KanbanItemRepository;
import com.example.formicarium.repository.ModuleRepository;
import com.example.formicarium.repository.ProjectRepository;
import com.example.formicarium.repository.ToDoItemRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ToDoItemRepository toDoItemRepository;
    private final ProjectRepository projectRepository;
    private final KanbanItemRepository kanbanItemRepository;

    public ModuleService(ModuleRepository moduleRepository,
                         ToDoItemRepository toDoItemRepository,
                         ProjectRepository projectRepository,
                         KanbanItemRepository kanbanItemRepository) {
        this.moduleRepository = moduleRepository;
        this.toDoItemRepository = toDoItemRepository;
        this.projectRepository = projectRepository;
        this.kanbanItemRepository = kanbanItemRepository;
    }

    @Transactional
    public Module createModule(Long projectId, String name, String description, String moduleType, User user) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        // validare moduleType
        if (!List.of("TODO_LIST", "KANBAN", "OTHER_TYPE").contains(moduleType)) {
            throw new IllegalArgumentException("Invalid module type: " + moduleType);
        }

        // se asigura ca nu exista un modul cu acelasoi nume
        boolean exists = project.getModules().stream()
                .anyMatch(module -> module.getName().equalsIgnoreCase(name));

        if (exists) {
            throw new IllegalStateException("A module with this name already exists in the project.");
        }

        Module module = Module.builder()
                .name(name)
                .description(description)
                .moduleType(moduleType)
                .project(project)
                .build();

        return moduleRepository.save(module);
    }

    @Transactional
    public ToDoItem addTaskToToDoList(Long moduleId, String taskName, User user) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));

        // se asigura ca tipul modulului este TODO_LIST
        if (!"TODO_LIST".equals(module.getModuleType())) {
            throw new IllegalStateException("Module is not a TODO_LIST type");
        }

        ToDoItem task = ToDoItem.builder()
                .name(taskName)
                .module(module)
                .isCompleted(false)
                .user(user)
                .build();

        return toDoItemRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<ToDoItem> getTasksByModuleAndUser(Long moduleId, Long userId) {
        return toDoItemRepository.findByModuleIdAndUserId(moduleId, userId);
    }

    @Transactional
    public ToDoItem markTaskAsCompleted(Long taskId, User user) {
        ToDoItem task = toDoItemRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));

        // se asigura ca sarcina curenta apartine utilizatorului curent
        if (!task.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to modify this task.");
        }

        task.setCompleted(true);
        task.setStatus("DONE");
        return toDoItemRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long taskId, User user) {
        ToDoItem task = toDoItemRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));

        // se asigura ca sarcina curenta apartine utilizatorului curent
        if (!task.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You do not have permission to delete this task.");
        }

        toDoItemRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public Module getModuleById(Long moduleId, Authentication authentication) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));

        // verifica accesul la proiect
        Project project = module.getProject();
        User currentUser = (User) authentication.getPrincipal();
        boolean hasAccess = project.getMemberships().stream()
                .anyMatch(membership -> membership.getUser().getId().equals(currentUser.getId()));

        if (!hasAccess) {
            throw new SecurityException("You do not have access to this module.");
        }

        // Load tasks for TODO_LIST modules specific to the current user
        // se incarca toate sarcinile pentru modululul TODO_LIST pentru utilizatorul curent
        if ("TODO_LIST".equals(module.getModuleType())) {
            List<ToDoItem> tasks = getTasksByModuleAndUser(moduleId, currentUser.getId());
            module.setTasks(tasks);
        }

        // se incarca elementele Kanban
        if ("KANBAN".equals(module.getModuleType())) {
            List<KanbanItem> items = getKanbanItemsByModule(moduleId);
            module.setKanbanItems(items);
        }

        return module;
    }

    @Transactional
    public void deleteModule(Long moduleId, Authentication authentication) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found with ID: " + moduleId));

        Project project = module.getProject();
        User currentUser = (User) authentication.getPrincipal();

        boolean hasPermission = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(currentUser.getId()) && m.getRole().equals("OWNER"));

        if (!hasPermission) {
            throw new SecurityException("You do not have permission to delete this module.");
        }

        moduleRepository.delete(module);
    }

    // 1) Se obtin toate elementele Kanban pentru fiecare modul
    @Transactional(readOnly = true)
    public List<KanbanItem> getKanbanItemsByModule(Long moduleId) {
        return kanbanItemRepository.findByModuleId(moduleId);
    }

    // 2) se adauga un nou element Kanban
    @Transactional
    public KanbanItem addKanbanItem(Long moduleId, String title, String column) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid module ID"));

        // se adaug elementul doar daca este un modul KANBAN
        if (!"KANBAN".equals(module.getModuleType())) {
            throw new IllegalStateException("Module is not a KANBAN type");
        }

        KanbanItem item = KanbanItem.builder()
                .title(title)
                .kanbanColumn(column) // "TODO" / "IN_PROGRESS" / "DONE"
                .module(module)
                .build();

        return kanbanItemRepository.save(item);
    }

    // 3) se muta elementul intr-o alta coloana
    @Transactional
    public KanbanItem moveKanbanItem(Long itemId, String newColumn) {
        KanbanItem item = kanbanItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found"));

        item.setKanbanColumn(newColumn);
        return kanbanItemRepository.save(item);
    }

    @Transactional(readOnly = true)
    public KanbanItem getKanbanItemById(Long itemId) {
        return kanbanItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Kanban item not found with ID: " + itemId));
    }

    @Transactional
    public void deleteKanbanItem(Long itemId) {
        kanbanItemRepository.deleteById(itemId);
    }

    @Transactional(readOnly = true)
    public Long getTaskModuleId(Long taskId) {
        ToDoItem task = toDoItemRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid task ID"));
        return task.getModule().getId();
    }
}
