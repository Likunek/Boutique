package ru.angelika.boutique.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Физический товар (экземпляр) на складе или в поставке.
 * Содержит информацию о себестоимости, весе, площади, а также привязку к продавцу и карточке товара.
 */
@Data
@Entity
@Table(schema = "public", name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false, unique = true)
    private Long id;

    /** Название товара */
    @Column(name = "name", nullable = false)
    private String name;

    /** Стоимость, которую ставит продавец. */
    @Column(name = "costPrice", nullable = false)
    private Double costPrice;

    /** Вес товара с упаковкой в кг (для расчета места в машине при поставки со склада на пвз). */
    @Column(name = "weight")
    private Double weight;

    /** Площадь упаковки в м^2 (для расчёта места на складе). */
    @Column(name = "square")
    private Double square = 0.01;

    /** Прошёл ли товар проверку (модерацию). */
    @Column(name = "verify")
    private Boolean verify = false;

    /** Продавец, выставивший товар. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private Seller seller;

    /** Карточка товара, которую видит покупатель. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "item_card_id")
    private ItemCard itemCard;

    /** Связь с таблицей наличие вещей на складах (товар, склад, кол-во товара). */
    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ItemsAtStorage> itemsAtStorages = new ArrayList<>();
}