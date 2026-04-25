package ru.angelika.boutique.exception;

public class ItemCardNotFoundException extends RuntimeException {
    public ItemCardNotFoundException(String message) {
        super(message);
    }
}

