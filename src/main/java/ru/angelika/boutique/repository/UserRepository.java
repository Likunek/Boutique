package ru.angelika.boutique.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.angelika.boutique.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByName(String name);

    User findByNumber(String number);

    User findByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.items i " +
            "LEFT JOIN FETCH i.feedbacks f " +
            "LEFT JOIN FETCH f.user " +
            "WHERE u.id = :id")
    User findByIdWithItemsAndFeedbacks(@Param("id") Long id);
}