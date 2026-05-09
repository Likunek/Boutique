package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(schema = "public", name = "supplies")
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "supply_item",
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items;
    @Column(name = "weight")
    private Double weight;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id")
    private Storage storage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PickupPoint point;
}
