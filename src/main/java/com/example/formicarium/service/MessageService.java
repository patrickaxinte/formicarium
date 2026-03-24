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

    // get recent messages for a project
    @Transactional(readOnly = true)
    public List<Message> getRecentMessagesByProject(Long projectId) {
        return messageRepository.findTop5ByProjectIdOrderBySentAtDesc(projectId);
    }

    // get all messages for a project
    @Transactional(readOnly = true)
    public List<Message> getAllMessagesByProject(Long projectId) {
        return messageRepository.findByProjectIdOrderBySentAtAsc(projectId);
    }

    // create a new message
    @Transactional
    public Message createMessage(Long projectId, Message message, Authentication authentication) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + projectId));

        // check if the user is a member of the project
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

    // get a message by ID
    @Transactional(readOnly = true)
    public Message getMessageById(Long messageId, Authentication authentication) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        Project project = message.getProject();

        // check if the user is a member of the project
        User user = (User) authentication.getPrincipal();
        boolean isMember = project.getMemberships().stream()
                .anyMatch(m -> m.getUser().getId().equals(user.getId()));

        if (!isMember) {
            throw new SecurityException("You do not have access to this message.");
        }

        return message;
    }

    // delete a message
    @Transactional
    public void deleteMessage(Long messageId, Authentication authentication) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found with ID: " + messageId));

        Project project = message.getProject();

        // check if the user is a member of the project or the sender of the message
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