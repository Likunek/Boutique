package ru.angelika.boutique.exception;

/**
 * Исключение, выбрасываемое при попытке получить ресурс по идентификатору или другому ключу,
 * если такой ресурс не найден в базе данных.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Создаёт исключение с произвольным текстом.
     *
     * @param message описание ошибки
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение для случая, когда ресурс не найден по числовому ID.
     *
     * @param resourceType класс сущности
     * @param id           идентификатор, по которому выполнялся поиск
     */
    public ResourceNotFoundException(Class<?> resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id));
    }

    /**
     * Создаёт исключение для случая, когда ресурс не найден по строковому значению (имя, номер, email).
     *
     * @param resourceType класс сущности
     * @param name         строковый ключ (например, номер телефона)
     */
    public ResourceNotFoundException(Class<?> resourceType, String name) {
        super(String.format("%s not found with data: %s", resourceType, name));
    }
}