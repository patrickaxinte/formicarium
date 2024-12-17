package com.example.formicarium.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "project_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMembership {

    @EmbeddedId
    private ProjectMembershipId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("projectId")
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    private String role; // Project-specific role: OWNER, COLLABORATOR, MEMBER

    private LocalDateTime dateJoined;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectMembership that = (ProjectMembership) o;

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
