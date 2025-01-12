package com.example.formicarium.controller;

import com.example.formicarium.entity.Message;
import com.example.formicarium.service.MessageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/chat")
public class MessagesController {

    private final MessageService messageService;

    public MessagesController(MessageService messageService) {
        this.messageService = messageService;
    }

    // afiseaza chat-ul complet pentru un proiect
    @GetMapping("/{projectId}")
    public String showChat(@PathVariable Long projectId, Authentication authentication, Model model) {
        List<Message> messages = messageService.getAllMessagesByProject(projectId);
        model.addAttribute("messages", messages);
        model.addAttribute("projectId", projectId);
        model.addAttribute("headerTitle", "Chat for Project ID: " + projectId);
        model.addAttribute("pageContent", "chat");
        return "base";
    }

    // creaza un nou mesaj
    @PostMapping("/{projectId}")
    public String sendMessage(@PathVariable Long projectId, @RequestParam String content, Authentication authentication) {
        Message message = Message.builder()
                .content(content)
                .build();
        messageService.createMessage(projectId, message, authentication);
        return "redirect:/chat/" + projectId;
    }

    // sterge un mesaj
    @PostMapping("/delete/{messageId}")
    public String deleteMessage(@PathVariable Long messageId, Authentication authentication) {
        Message message = messageService.getMessageById(messageId, authentication);
        Long projectId = message.getProject().getId();
        messageService.deleteMessage(messageId, authentication);
        return "redirect:/chat/" + projectId;
    }
}
