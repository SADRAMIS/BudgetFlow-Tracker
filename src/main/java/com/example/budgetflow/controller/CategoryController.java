package com.example.budgetflow.controller;

import com.example.budgetflow.entity.Category;
import com.example.budgetflow.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<Category> getAll(){
        return categoryService.getAllCategories();
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category){
        Category created = categoryService.createCategory(category.getName(), category.getType(), category.getIcon(), category.getColor());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id,@RequestBody Category category){
        return categoryService.updatedCategory(id, category.getName(), category.getType(), category.getIcon(), category.getColor());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
