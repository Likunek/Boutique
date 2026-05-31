package ru.angelika.boutique.model;

/**
 * Статусы для заказов и поставок.
 */
public enum Status {

    /** Новый заказ. */
    NEW,

    /** В пути. */
    WAY,

    /** Доставлен (ещё не получен покупателем). */
    DELIVERED,

    /** Получен покупателем (заказ закрыт). */
    RECEIVED
}