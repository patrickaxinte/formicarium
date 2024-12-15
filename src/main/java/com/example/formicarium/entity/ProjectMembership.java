package com.example.formicarium.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // legatura catre proiect
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // legatura catre User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // rolul utilizatorului in proiect
    @Column(nullable = false)
    private String role;

    private LocalDateTime dateJoined;
}
