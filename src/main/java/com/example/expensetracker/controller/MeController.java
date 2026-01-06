package com.example.expensetracker.controller;

import com.example.expensetracker.dto.auth.UserSummary;
import com.example.expensetracker.dto.user.ChangePasswordRequest;
import com.example.expensetracker.dto.user.UpdateMeRequest;
import com.example.expensetracker.service.UserMeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/me")
public class MeController {

    private final UserMeService userMeService;

    public MeController(UserMeService userMeService) {
        this.userMeService = userMeService;
    }

    @GetMapping
    public UserSummary me() {
        return userMeService.me();
    }

    @PatchMapping
    public UserSummary updateMe(@Valid @RequestBody UpdateMeRequest req) {
        return userMeService.updateMe(req);
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        userMeService.changePassword(req);
        return ResponseEntity.noContent().build();
    }
}
