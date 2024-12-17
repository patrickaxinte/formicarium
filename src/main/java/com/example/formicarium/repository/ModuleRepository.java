package com.example.formicarium.repository;

import com.example.formicarium.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    // obtine toate modulele pentru un proiect specific
    List<Module> findByProjectId(Long projectId);
}
