package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    // Inyección por constructor
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // GET /api/users  -> devuelve todos los usuarios
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
