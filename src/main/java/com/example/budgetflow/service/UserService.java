package com.example.budgetflow.service;

import com.example.budgetflow.entity.User;
import com.example.budgetflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String username,String email,String password){
        log.info("Регистрация нового пользователя: {}", username);

        if(userRepository.existsByUsername(username)){
            throw new IllegalArgumentException("Пользователь с таким username уже существует");
        }

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));  // Шифрование пароля

        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUser(){
        return userRepository.findAll();
    }

    public User getUserById(Long id){
        return userRepository.findById(id).
                orElseThrow(()-> new IllegalArgumentException("Пользователь не найден с ID: " + id));
    }

    public void deleteUser(Long id){
        log.info("Удаление пользователя с ID: {}", id);
        userRepository.deleteById(id);
    }
}
