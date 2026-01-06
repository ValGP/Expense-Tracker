package com.example.expensetracker.service;

import com.example.expensetracker.dto.auth.UserSummary;
import com.example.expensetracker.dto.user.ChangePasswordRequest;
import com.example.expensetracker.dto.user.UpdateMeRequest;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.CurrentUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
public class UserMeService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserMeService(CurrentUserService currentUserService,
                         UserRepository userRepository,
                         PasswordEncoder passwordEncoder) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserSummary me() {
        User me = currentUserService.get();
        return toSummary(me);
    }

    public UserSummary updateMe(UpdateMeRequest req) {
        User me = currentUserService.get();

        if (req == null) throw new IllegalArgumentException("request body is required");

        if (req.name() != null) {
            String name = req.name().trim();
            if (name.isBlank()) throw new IllegalArgumentException("name cannot be blank");
            me.setName(name);
        }

        if (req.email() != null) {
            String email = req.email().trim().toLowerCase();
            if (email.isBlank()) throw new IllegalArgumentException("email cannot be blank");

            if (!email.equalsIgnoreCase(me.getEmail()) && userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email already in use");
            }

            me.setEmail(email);
        }

        User saved = userRepository.save(me);
        return toSummary(saved);
    }

    public void changePassword(ChangePasswordRequest req) {
        User me = currentUserService.get();

        if (req == null) throw new IllegalArgumentException("request body is required");

        if (!passwordEncoder.matches(req.currentPassword(), me.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        me.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(me);
    }

    private UserSummary toSummary(User u) {
        // Si todavía no estás usando roles en DB, devolvemos USER fijo.
        // Si ya los tenés en User, acá lo cambiamos.
        return new UserSummary(
                u.getId(),
                u.getEmail(),
                u.getName(),
                Set.of("USER")
        );
    }
}
