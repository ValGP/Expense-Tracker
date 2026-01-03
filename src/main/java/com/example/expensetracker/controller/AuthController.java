package com.example.expensetracker.controller;

import com.example.expensetracker.dto.auth.AuthResponse;
import com.example.expensetracker.dto.auth.LoginRequest;
import com.example.expensetracker.dto.auth.RegisterRequest;
import com.example.expensetracker.dto.auth.UserSummary;
import com.example.expensetracker.model.User;
import com.example.expensetracker.service.AuthService;
import com.example.expensetracker.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, toUserSummary(user));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, toUserSummary(user));
    }

    private UserSummary toUserSummary(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserSummary(
                user.getId(),
                user.getEmail(),
                user.getName(),
                roles
        );
    }
}
