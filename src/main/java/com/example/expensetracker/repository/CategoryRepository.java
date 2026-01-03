package com.example.expensetracker.repository;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwner(User owner);

    boolean existsByOwnerAndNameIgnoreCase(User owner, String name);

    // 🔒 ownership
    Optional<Category> findByIdAndOwnerId(Long id, Long ownerId);
}
