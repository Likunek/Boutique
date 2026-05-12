package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "feedbacks")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "rating", nullable = false)
    private Integer rating;
    @Column(name = "text")
    private String text;
}
