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
    private Integer square = 1;
    @Column(name = "verify")
    private Boolean verify = false;
    @Column(name = "seller", nullable = false)
    private String seller;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "item_card_id")
    private ItemCard itemCard;
}
