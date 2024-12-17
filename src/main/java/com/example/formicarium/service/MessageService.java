package com.example.formicarium.service;

import com.example.formicarium.entity.Message;
import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.User;
import com.example.formicarium.repository.MessageRepository;
import com.example.formicarium.repository.ProjectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final ProjectRepository projectRepository;

    public MessageService(MessageRepository messageRepository, ProjectRepository projectRepository) {
        this.messageRepository = messageRepository;
        this.projectRepository = projectRepository;
    }

    // obtine mesajele recente pentru un proiect
    @Transactional(readOnly = true)
    public List<Message> getRecentMessagesByProject(Long projectId) {
        return messageRepository.findTop5ByProjectIdOrderBySentAtDesc(projectId);
    }

    // obtine toate mesajele pentru un proiect
    @Transactional(readOnly = true)
    public List<Message> getAllMessagesByProject(Long projectId) {
        return messageRepository.findByProjectIdOrderBySentAtAsc(projectId);
    }

    // creaza un nou mesaj
    @Transactional
    public Message createMessage(Long projectId, Message message, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // verifica daca utilizatorul este membru al proiectului
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new SecurityException("You do not have permission to send messages to this project.");
        }

        message.setProject(project);
        message.setSender(user);
        return messageRepository.save(message);
    }

    // obtine un mesaj dupa ID
    @Transactional(readOnly = true)
    public Message getMessageById(Long messageId, Authentication authentication) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        Project project = message.getProject();

        // verifica daca utilizatorul este membru al proiectului
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new SecurityException("You do not have access to this message.");
        }

        return message;
    }

    // sterge un mesaj
    @Transactional
    public void deleteMessage(Long messageId, Authentication authentication) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        Project project = message.getProject();

        // verifica daca utilizatorul este membru al proiectului sau expeditorul mesajului
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));
        boolean isSender = message.getSender().getId().equals(user.getId());

        if (!isMember && !isSender) {
            throw new SecurityException("You do not have permission to delete this message.");
        }

        messageRepository.delete(message);
    }
}
