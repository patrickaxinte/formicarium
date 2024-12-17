package com.example.formicarium.controller;

import com.example.formicarium.entity.Task;
import com.example.formicarium.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TasksController {

    private final TaskService taskService;

    public TasksController(TaskService taskService) {
        this.taskService = taskService;
    }

    // afiseaza toate sarcinile unui proiect
    @GetMapping("/{projectId}")
    public String showAllTasks(@PathVariable Long projectId, Authentication authentication, Model model) {
        List<Task> tasks = taskService.getAllTasksByProject(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("projectId", projectId);
        model.addAttribute("headerTitle", "Tasks for Project ID: " + projectId);
        model.addAttribute("pageContent", "tasks");
        return "base";
    }

    // formular pentru crearea unei sarcini noi
    @GetMapping("/taskform")
    public String showTaskForm(@RequestParam Long projectId, @RequestParam(required = false) Long taskId, Model model) {
        Task task = (taskId != null) ? taskService.getTaskById(taskId, null) : new Task();
        model.addAttribute("task", task);
        model.addAttribute("projectId", projectId);
        model.addAttribute("headerTitle", (taskId != null) ? "Edit Task" : "Add Task");
        model.addAttribute("pageContent", "task-form");
        return "base";
    }

    // salvarea unei sarcini (noua sau editata)
    @PostMapping("/taskform")
    public String saveTask(@RequestParam Long projectId, @RequestParam(required = false) Long taskId,
                           @ModelAttribute Task task, Authentication authentication) {
        if (taskId == null) {
            taskService.createTask(projectId, task, authentication);
        } else {
            taskService.updateTask(taskId, task, authentication);
        }
        return "redirect:/projects/" + projectId;
    }

    // sterge o sarcina
    @PostMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long taskId, Authentication authentication) {
        Task task = taskService.getTaskById(taskId, authentication);
        Long projectId = task.getProject().getId();
        taskService.deleteTask(taskId, authentication);
        return "redirect:/projects/" + projectId;
    }
}
