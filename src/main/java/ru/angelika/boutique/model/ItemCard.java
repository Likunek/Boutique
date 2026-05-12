package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(schema = "public", name = "item_cards")
public class ItemCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "price", nullable = false)
    private Double price = 0.0;
    @Column(name = "seller", nullable = false)
    private String seller;
    @Column(name = "rating")
    private Double rating = 0.0;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "item_feedback",
            inverseJoinColumns = @JoinColumn(name = "feedback_id"))
    private List<Feedback> feedbacks = new ArrayList<>();
}