package com.example.budgetflow.controller;

import com.example.budgetflow.entity.Transaction;
import com.example.budgetflow.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/user/{userId}")
    public List<Transaction> getUserTransactions(@PathVariable Long userId){
        return transactionService.getUserTransaction(userId);
    }

    @PostMapping
    public ResponseEntity<Transaction> create(@RequestBody Transaction transaction){
        Transaction created = transactionService.createTransaction(
                transaction.getUser().getId(),transaction.getCategory().getId(),transaction.getAmount(),
                transaction.getType(),transaction.getDescription(),transaction.getDate()
        );
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public Transaction update(@PathVariable Long id,@RequestBody Transaction transaction){
        return transactionService.updateTransaction(
                id,transaction.getCategory().getId(),transaction.getAmount(),transaction.getType(),transaction.getDescription(),transaction.getDate()
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        transactionService.deleteTransaction(id);
    }
}
