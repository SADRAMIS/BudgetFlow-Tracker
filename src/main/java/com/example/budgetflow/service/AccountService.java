package com.example.budgetflow.service;

import com.example.budgetflow.entity.Account;
import com.example.budgetflow.entity.User;
import com.example.budgetflow.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

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
        account.setAccountNumber(accountNumber);
        account.setName(name);
        account.setType(type);
        account.setCurrency(currency);
        account.setStatus("active");
        account.setBalance(BigDecimal.ZERO); // начальный остаток
        return accountRepository.save(account);
    }

    // Получить все счета пользователя
    public List<Account> getAccountsByUser(Long userId){
        return accountRepository.findByUserId(userId);
    }

    // Получить счет по id
    public Account getAccountById(Long id){
        return accountRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("Счет не найден с ID: " + id));
    }

    public Account updateAccount(Long accountId,String name,String type,String currency,String status,BigDecimal balance){
        Account account = getAccountById(accountId);
        if (name != null) account.setName(name);
        if (type != null) account.setType(type);
        if (currency != null) account.setCurrency(currency);
        if (status != null) account.setStatus(status);
        if (balance != null) account.setBalance(balance);

        return accountRepository.save(account);
    }

}
