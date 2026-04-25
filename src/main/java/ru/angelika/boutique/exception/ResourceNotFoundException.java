package ru.angelika.boutique.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class<?> resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id));
    }

    public ResourceNotFoundException(Class<?> resourceType, String name) {
        super(String.format("%s not found with name: %s", resourceType, name));
    }
}
