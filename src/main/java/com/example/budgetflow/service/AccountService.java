package com.example.budgetflow.service;

import com.example.budgetflow.entity.Account;
import com.example.budgetflow.entity.User;
import com.example.budgetflow.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;

    public Account createAccount(Long userId,String accountNumber,String name,String type,String currency){
        if(accountRepository.findByAccountNumber(accountNumber) != null){
            throw new IllegalArgumentException("Счет с таким номером уже существует");
        }
        User user = userService.getUserById(userId);
        Account account = new Account();
        account.setUser(user);
        account.set
    }

}
