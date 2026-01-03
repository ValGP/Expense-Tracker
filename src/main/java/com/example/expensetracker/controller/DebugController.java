package com.example.expensetracker.controller;

import com.example.expensetracker.security.CurrentUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final CurrentUserService currentUserService;

    public DebugController(CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        var u = currentUserService.get();
        return Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "roles", u.getRoles()
        );
    }
}
