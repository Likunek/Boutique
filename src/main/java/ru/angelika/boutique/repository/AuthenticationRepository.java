package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.Authentication;
public interface AuthenticationRepository extends JpaRepository<Authentication, Long> {
    Authentication findByNumber(String number);
}
