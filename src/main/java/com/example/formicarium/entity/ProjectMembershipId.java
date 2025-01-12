package com.example.formicarium.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMembershipId implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "user_id")
    private Long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProjectMembershipId that = (ProjectMembershipId) o;

        if (!projectId.equals(that.projectId)) return false;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        int result = projectId.hashCode();
        result = 31 * result + userId.hashCode();
        return result;
    }
}
