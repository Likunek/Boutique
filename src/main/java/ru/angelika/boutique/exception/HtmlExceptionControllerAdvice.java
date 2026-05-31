package ru.angelika.boutique.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Глобальный обработчик исключений для MVC-контроллеров (возвращает HTML-страницу error).
 * Перехватывает различные типы исключений и добавляет в модель атрибуты для отображения на странице ошибки.
 */
@ControllerAdvice
public class HtmlExceptionControllerAdvice {

    /**
     * Обрабатывает исключение {@link ResourceNotFoundException} (ресурс не найден).
     *
     * @param model модель для передачи атрибутов в представление
     * @param e     перехваченное исключение
     * @return имя представления "error"
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model, ResourceNotFoundException e) {
        model.addAttribute("errorCode", "404 NOT FOUND");
        model.addAttribute("errorMessage", "Resource not found");
        model.addAttribute("errorDetails", e.getMessage());
        return "error";
    }

    /**
     * Обрабатывает исключение {@link PasswordInvalidException} (неверный пароль).
     *
     * @param model модель
     * @param e     перехваченное исключение
     * @return "error"
     */
    @ExceptionHandler(PasswordInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handlePasswordInvalid(Model model, PasswordInvalidException e) {
        model.addAttribute("errorCode", "400 BAD REQUEST");
        model.addAttribute("errorMessage", "Invalid password");
        model.addAttribute("errorDetails", e.getMessage());
        return "error";
    }

    /**
     * Обрабатывает исключение {@link ResourceExistsException} (ресурс уже существует).
     *
     * @param model модель
     * @param e     перехваченное исключение
     * @return "error"
     */
    @ExceptionHandler(ResourceExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleResourceExists(Model model, ResourceExistsException e) {
        model.addAttribute("errorCode", "409 CONFLICT");
        model.addAttribute("errorMessage", "Resource already exists");
        model.addAttribute("errorDetails", e.getMessage());
        return "error";
    }

    /**
     * Обрабатывает исключение {@link SupplyException} (ошибка при формировании поставки).
     *
     * @param model модель
     * @param e     перехваченное исключение
     * @return "error"
     */
    @ExceptionHandler(SupplyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleSupplyError(Model model, SupplyException e) {
        model.addAttribute("errorCode", "500 INTERNAL SERVER ERROR");
        model.addAttribute("errorMessage", "Supply processing error");
        model.addAttribute("errorDetails", e.getMessage());
        return "error";
    }

    /**
     * Обрабатывает все остальные непредвиденные исключения.
     *
     * @param model модель
     * @param e     перехваченное исключение
     * @return "error"
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Model model, Exception e) {
        model.addAttribute("errorCode", "500 INTERNAL SERVER ERROR");
        model.addAttribute("errorMessage", "Unexpected error");
        model.addAttribute("errorDetails", "Please try again later.");
        return "error";
    }
}