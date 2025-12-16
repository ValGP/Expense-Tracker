package com.example.expensetracker.service;

import com.example.expensetracker.dto.category.CategoryCreateRequest;
import com.example.expensetracker.dto.category.CategoryResponse;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public CategoryService(UserRepository userRepository,
                           CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryCreateRequest req) {

        if (req.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        User owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getOwnerId()));

        // evitar duplicados por usuario + nombre
        if (categoryRepository.existsByOwnerAndNameIgnoreCase(owner, req.getName())) {
            throw new IllegalArgumentException("Category with that name already exists for this user");
        }

        Category category = Category.builder()
                .owner(owner)
                .name(req.getName().trim())
                .description(req.getDescription())
                .colorHex(req.getColorHex())
                .active(true)
                .build();

        Category saved = categoryRepository.save(category);

        return toResponse(saved);
    }

    public List<CategoryResponse> listByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

        return categoryRepository.findByOwner(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getColorHex(),
                c.getActive()
        );
    }
}
