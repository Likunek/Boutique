package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
@Entity
@Table(schema = "public", name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Range(min = 1, max = 5)
    @Column(name = "rating", nullable = false)
    private Integer rating;
    @Column(name = "text")
    private String text;
}
