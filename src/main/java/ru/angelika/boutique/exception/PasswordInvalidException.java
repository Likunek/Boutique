package ru.angelika.boutique.exception;

/**
 * Исключение, выбрасываемое при попытке обновления данных аунтификации,
 * если указан неверный пароль.
 */
public class PasswordInvalidException extends RuntimeException {
    public PasswordInvalidException(String message) {
        super(message);
    }
}
