package ru.angelika.boutique.exception;

/**
 * Исключение, выбрасываемое при попытке создать или обновить ресурс,
 * который уже существует в базе данных (нарушение уникальности).
 */
public class ResourceExistsException extends RuntimeException {

    /**
     * Создаёт исключение с форматированным сообщением,
     * содержащим тип ресурса и идентификационные данные (имя, номер и т.п.).
     *
     * @param resource класс сущности, которая уже существует
     * @param data     строковое представление уникального значения (имя, номер, email)
     */
    public ResourceExistsException(Class<?> resource, String data) {
        super(String.format("%s with data %s already exists", resource, data));
    }
}