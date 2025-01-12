package com.example.formicarium.repository;

import com.example.formicarium.entity.ToDoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ToDoItemRepository extends JpaRepository<ToDoItem, Long> {
    List<ToDoItem> findByModuleIdAndUserId(Long moduleId, Long userId);
    List<ToDoItem> findByModuleId(Long moduleId);
}

