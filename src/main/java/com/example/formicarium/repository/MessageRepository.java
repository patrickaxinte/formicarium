package com.example.formicarium.repository;

import com.example.formicarium.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // obtine ultimele 5 mesaje pentru un proiect
    List<Message> findTop5ByProjectIdOrderBySentAtDesc(Long projectId);

    // obtine toate mesajele pentru un proiect
    List<Message> findByProjectIdOrderBySentAtAsc(Long projectId);
}
