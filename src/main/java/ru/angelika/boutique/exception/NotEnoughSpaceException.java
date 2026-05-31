package ru.angelika.boutique.exception;

/**
 * Исключение, выбрасываемое при попытке разместить товар на складе,
 * если на складе недостаточно свободного места.
 */
public class NotEnoughSpaceException extends RuntimeException {

    public NotEnoughSpaceException(String message) {
        super(message);
    }
}