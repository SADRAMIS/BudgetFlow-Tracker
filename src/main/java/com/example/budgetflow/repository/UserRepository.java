package com.example.budgetflow.repository;

import com.example.budgetflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    // Поиск пользователя по email
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    // Проверка существования пользователя по email
    boolean existsByEmail(String email);
}
