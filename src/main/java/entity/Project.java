package entity;

import lombok.Data;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Data
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToMany(mappedBy = "projects", fetch = FetchType.LAZY)
    private List<User> users;

    // Eliminate tasks and notes if not needed
}
