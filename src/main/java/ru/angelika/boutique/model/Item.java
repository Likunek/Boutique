package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(schema = "public", name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "costPrice", nullable = false)
    private Double costPrice;
    @Column(name = "weight")
    private Double weight;
    @Column(name = "square")
    private Double square = 0.01;
    @Column(name = "verify")
    private Boolean verify = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "item_card_id")
    private ItemCard itemCard;
}
