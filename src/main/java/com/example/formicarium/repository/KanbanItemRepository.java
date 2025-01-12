package com.example.formicarium.repository;

import com.example.formicarium.entity.KanbanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface KanbanItemRepository extends JpaRepository<KanbanItem, Long> {
    List<KanbanItem> findByModuleId(Long moduleId);
}
