package com.example.budgetflow.repository;

import com.example.budgetflow.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {

    Optional<Category> findByName(String name);
    // Поиск всех категорий по типу (INCOME или EXPENSE)
    Optional<Category> findByType(String type);

    boolean existsByName(String name);

}
