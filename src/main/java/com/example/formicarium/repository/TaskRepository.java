package com.example.formicarium.repository;

import com.example.formicarium.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // returneaza cele mai recente 5 task-uri pentru un proiect cu createdBy initializat
    @Query("SELECT t FROM Task t JOIN FETCH t.createdBy WHERE t.project.id = :projectId ORDER BY t.createdAt DESC")
    List<Task> findTop5ByProjectIdOrderByCreatedAtDescWithCreatedBy(@Param("projectId") Long projectId);

    // returneaza toate task-urile pentru un proiect cu createdBy initializat
    @Query("SELECT t FROM Task t JOIN FETCH t.createdBy WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithCreatedBy(@Param("projectId") Long projectId);

    // returneaza toate task-urile atribuite unui utilizator cu createdBy initializat
    @Query("SELECT t FROM Task t JOIN FETCH t.createdBy WHERE t.assignedTo.id = :userId")
    List<Task> findByAssignedToIdWithCreatedBy(@Param("userId") Long userId);

    // returneaza cele mai recente 5 task-uri create, atribuite unui utilizator, cu createdBy initializat
    @Query("SELECT t FROM Task t JOIN FETCH t.createdBy WHERE t.assignedTo.id = :userId ORDER BY t.createdAt DESC")
    List<Task> findTop5ByAssignedToIdOrderByCreatedAtDescWithCreatedBy(@Param("userId") Long userId);

    // returneaza toate task-urile pentru proiectele unde utilizatorul este membru cu createdBy initializat
    @Query("SELECT DISTINCT t FROM Task t JOIN FETCH t.createdBy JOIN t.project p JOIN p.memberships m WHERE m.user.id = :userId")
    List<Task> findAllByUserIdWithCreatedBy(@Param("userId") Long userId);

    // returneaza un task dupa id cu toate asociatiile necesare initializate
    @Query("SELECT t FROM Task t JOIN FETCH t.createdBy JOIN FETCH t.assignedTo JOIN FETCH t.project WHERE t.id = :taskId")
    Optional<Task> findByIdWithDetails(@Param("taskId") Long taskId);
}
