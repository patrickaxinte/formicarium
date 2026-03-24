package com.example.formicarium.controller;

import com.example.formicarium.entity.Message;
import com.example.formicarium.service.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import java.time.format.DateTimeFormatter;
import java.security.Principal;

@Controller
public class ChatWebSocketController {

    private final MessageService messageService;

    public ChatWebSocketController(MessageService messageService) {
        this.messageService = messageService;
    }

    @MessageMapping("/chat/{projectId}")
    @SendTo("/topic/chat/{projectId}")
    public ChatMessageResponse sendChatMessage(@DestinationVariable Long projectId, ChatMessageRequest request, Principal principal) {
        System.out.println("====== WS MESSAGE RECV ======");
        System.out.println("ProjectId: " + projectId);
        System.out.println("Message: " + (request == null ? "null" : request.getContent()));
        System.out.println("Principal: " + (principal == null ? "null" : principal.getName()));

        Authentication authentication = (Authentication) principal;
        
        Message message = new Message();
        if (request != null) {
            message.setContent(request.getContent());
        }

        try {
            Message savedMessage = messageService.createMessage(projectId, message, authentication);
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
            String formattedTime = savedMessage.getSentAt().format(formatter);
            return new ChatMessageResponse(
                savedMessage.getId(), 
                savedMessage.getContent(), 
                savedMessage.getSender().getUsername(), 
                formattedTime,
                authentication.getName()
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static class ChatMessageRequest {
        private String content;
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    public static class ChatMessageResponse {
        public Long id;
        public String content;
        public String senderUsername;
        public String sentAt;
        public String currentUsername;

        public ChatMessageResponse(Long id, String content, String senderUsername, String sentAt, String currentUsername) {
            this.id = id;
            this.content = content;
            this.senderUsername = senderUsername;
            this.sentAt = sentAt;
            this.currentUsername = currentUsername;
        }
    }
}
