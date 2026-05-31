package ru.angelika.boutique.exception;

/**
 * Исключение, выбрасываемое при ошибках в процессе автоматического формирования поставок (Supply).
 * Обычно связано с проблемами при обработке заказов.
 */
public class SupplyException extends RuntimeException {
    public SupplyException(String message) {
        super(message);
    }
}
