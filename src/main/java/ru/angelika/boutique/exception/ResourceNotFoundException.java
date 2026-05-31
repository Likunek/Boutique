package ru.angelika.boutique.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(Class<?> resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id));
    }

    public ResourceNotFoundException(Class<?> resourceType, String name) {
        super(String.format("%s not found with data: %s", resourceType, name));
    }
}
