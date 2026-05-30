package ru.angelika.boutique.exception;

public class ResourceExistsException extends RuntimeException {

    public ResourceExistsException(Class<?> resource, String data) {
        super(String.format("%s with data %s already exists", resource, data));
    }
}
