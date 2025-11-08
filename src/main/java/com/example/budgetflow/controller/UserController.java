package com.example.budgetflow.controller;

import com.example.budgetflow.entity.User;
import com.example.budgetflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user){
        User created = userService.registerUser(user.getUsername(),user.getEmail(),user.getPassword());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id){
        return userService.getUserById(id);
    }

}
