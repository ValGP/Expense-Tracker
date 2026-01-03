package com.example.expensetracker.security;

import com.example.expensetracker.api.ForbiddenException;
import com.example.expensetracker.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public User get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new ForbiddenException("Not authenticated");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        throw new ForbiddenException("Invalid authentication principal");
    }

    public Long getId() {
        return get().getId();
    }

    public String getEmail() {
        return get().getEmail();
    }
}
