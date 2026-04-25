package ru.angelika.boutique.exception;

public class ItemExistsException extends RuntimeException {
    public ItemExistsException(String message) {
        super(message);
    }
}
