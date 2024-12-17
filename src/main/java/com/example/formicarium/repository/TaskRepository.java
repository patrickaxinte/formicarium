package com.example.formicarium.repository;

import com.example.formicarium.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // obtine cele mai recente 5 sarcini pentru un proiect
    List<Task> findTop5ByProjectIdOrderByCreatedAtDesc(Long projectId);

    // obtine toate sarcinile pentru un proiect
    List<Task> findByProjectId(Long projectId);
}

