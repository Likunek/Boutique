package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Authentication;

/**
 * Репозиторий для работы с сущностью {@link Authentication}.
 */
public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {

    /**
     * Находит запись аутентификации по номеру телефона.
     * @param number номер телефона (логин)
     */
    Authentication findByNumber(String number);
}