package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(schema = "public", name = "carts")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Integer id;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cart_item_card",
            inverseJoinColumns = @JoinColumn(name = "item_card_id"))
    private List<ItemCard> itemCards;
    @Column(name = "total_price")
    private Double totalPrice = 0.0;
}
