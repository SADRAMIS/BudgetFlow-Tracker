package com.example.budgetflow.service;

import com.example.budgetflow.entity.Category;
import com.example.budgetflow.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category createCategory(String name, String type, String icon, String color){
        log.info("Создание новой категории: {}", name);

        if(categoryRepository.existsByName(name)){
            throw new IllegalArgumentException("Категория с таким именем уже существует");
        }

        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(color);

        return categoryRepository.save(category);
    }

    public List<Category> getAllCategories(){
        return categoryRepository.findAll();
    }

    public Optional<Category> getCategoriesByType(String type){
        return categoryRepository.findByType(type);
    }

    public Category getCategoryById(Long id){
        return categoryRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("Категория не найдена с ID: " + id));
    }

    public Category updatedCategory(Long id,String name,String type,String icon, String color){
        log.info("Обновление категории с ID: {}", id);

        Category category = getCategoryById(id);
        category.setName(name);
        category.setType(type);
        category.setIcon(icon);
        category.setColor(color);

        return categoryRepository.save(category);
    }


}
