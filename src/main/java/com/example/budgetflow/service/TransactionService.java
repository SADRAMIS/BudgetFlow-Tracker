package com.example.budgetflow.service;

import com.example.budgetflow.entity.Category;
import com.example.budgetflow.entity.Transaction;
import com.example.budgetflow.entity.User;
import com.example.budgetflow.repository.CategoryRepository;
import com.example.budgetflow.repository.TransactionRepository;
import com.example.budgetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public Transaction createTransaction(Long userId, Long categoryId,
                                         Double amount, String type,
                                         String description, LocalDate date){
        log.info("Создание новой транзакции для пользователя с ID: {}", userId);

        if(amount <= 0){
            throw  new IllegalArgumentException("Сумма должна быть положительной");
        }

        User user = userService.getUserId(userId);
        Category category = categoryService.getCategoryById(categoryId);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setCategory(category);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setDescription(description);
        transaction.setDate(date);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getUserTransaction(Long userId){
        return transactionRepository.findByUserId(userId);
    }
}
