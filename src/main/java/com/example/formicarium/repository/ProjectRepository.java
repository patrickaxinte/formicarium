package com.example.formicarium.repository;

import com.example.formicarium.entity.User;
import com.example.formicarium.entity.Project;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.memberships pm WHERE pm.user = :user AND p.isActive = :isActive")
    List<Project> findProjectsByUserAndIsActive(@Param("user") User user, @Param("isActive") boolean isActive);

    @EntityGraph(attributePaths = {"memberships", "memberships.user", "messages"})
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdWithMembershipsAndMessages(@Param("id") Long id);
}
