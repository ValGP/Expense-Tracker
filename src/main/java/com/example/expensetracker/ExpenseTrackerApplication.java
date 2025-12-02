package com.example.expensetracker;

import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class ExpenseTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpenseTrackerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User demo = User.builder()
                        .name("Demo User")
                        .email("demo@example.com")
                        .passwordHash("hashed-password")
                        .createdAt(LocalDateTime.now())
                        .active(true)
                        .build();

                userRepository.save(demo);
            }
        };
    }
}
