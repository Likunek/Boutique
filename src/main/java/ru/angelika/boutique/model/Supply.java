package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(schema = "public", name = "supplies")
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "supply_item",
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items = new ArrayList<>();
    @Column(name = "weight")
    private Double weight;
    @Enumerated(EnumType.STRING)
    private Status status = Status.WAY;
    @Column(name = "date")
    private LocalDateTime date = LocalDateTime.now();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PickupPoint point;
}
