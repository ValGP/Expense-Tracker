// CategoryRepository.java
package com.example.expensetracker.repository;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByOwner(User owner);
}
