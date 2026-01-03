package com.example.expensetracker.service;

import com.example.expensetracker.api.EmailAlreadyExistsException;
import com.example.expensetracker.api.InvalidCredentialsException;
import com.example.expensetracker.api.UserInactiveException;
import com.example.expensetracker.dto.auth.LoginRequest;
import com.example.expensetracker.dto.auth.RegisterRequest;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserOnboardingService onboardingService;


    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserOnboardingService onboardingService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.onboardingService = onboardingService;
    }

    public User register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .email(email)
                .name(req.getName().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .active(true)
                .build();

        user = userRepository.save(user);

        // 👇 crea cuentas/categorías/moneda default
        onboardingService.seedDefaultsFor(user);

        // si onboarding setea defaultCurrency, volvemos a guardar (por las dudas)
        return userRepository.save(user);
    }

    public User login(LoginRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new UserInactiveException();
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
