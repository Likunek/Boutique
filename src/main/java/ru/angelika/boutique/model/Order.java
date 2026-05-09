package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(schema = "public", name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    @Enumerated(EnumType.STRING)
    private Status status = Status.NEW;
    @Column(name = "date", nullable = false)
    private LocalDateTime date = LocalDateTime.now();
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "order_item",
            inverseJoinColumns = @JoinColumn(name = "item_id"))
    private List<Item> items;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_receipt_id")
    private PointReceipt pointReceipt;
}
