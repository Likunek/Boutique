package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.angelika.boutique.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);

    User findByNumber(String number);

    User findByEmail(String email);
}