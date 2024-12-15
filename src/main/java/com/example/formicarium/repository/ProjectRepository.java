package com.example.formicarium.repository;

import com.example.formicarium.entity.Project;
import com.example.formicarium.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p JOIN p.memberships pm WHERE pm.user = :user AND p.isActive = :isActive")
    List<Project> findProjectsByUserAndIsActive(@Param("user") User user, @Param("isActive") boolean isActive);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.memberships WHERE p.id = :id")
    Optional<Project> findByIdWithMemberships(@Param("id") Long id);
}
