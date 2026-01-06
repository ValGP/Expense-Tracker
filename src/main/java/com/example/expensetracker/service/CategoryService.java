package com.example.expensetracker.service;

import com.example.expensetracker.dto.category.CategoryCreateRequest;
import com.example.expensetracker.dto.category.CategoryUpdateRequest;
import com.example.expensetracker.dto.category.CategoryResponse;
import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public CategoryService(CurrentUserService currentUserService,
                           UserRepository userRepository,
                           CategoryRepository categoryRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse create(CategoryCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        Long myId = currentUserService.getId();
        User owner = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + myId));

        if (categoryRepository.existsByOwnerAndNameIgnoreCase(owner, req.getName().trim())) {
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

    public List<CategoryResponse> listMine(Boolean activeOnly) {
        Long myId = currentUserService.getId();
        User owner = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + myId));

        return categoryRepository.findByOwner(owner).stream()
                .filter(c -> activeOnly == null || !activeOnly || Boolean.TRUE.equals(c.getActive()))
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse update(Long categoryId, CategoryUpdateRequest req) {
        Long myId = currentUserService.getId();

        Category category = categoryRepository.findByIdAndOwnerId(categoryId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));

        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (newName.isBlank()) throw new IllegalArgumentException("name cannot be blank");

            if (!newName.equalsIgnoreCase(category.getName())
                    && categoryRepository.existsByOwnerAndNameIgnoreCase(category.getOwner(), newName)) {
                throw new IllegalArgumentException("Category with that name already exists for this user");
            }
            category.setName(newName);
        }

        if (req.getDescription() != null) {
            String desc = req.getDescription().trim();
            category.setDescription(desc.isBlank() ? null : desc);
        }

        if (req.getColorHex() != null) {
            String color = req.getColorHex().trim();
            if (!color.matches("^#[0-9A-Fa-f]{6}$")) {
                throw new IllegalArgumentException("colorHex must be like #RRGGBB");
            }
            category.setColorHex(color.toUpperCase());
        }

        if (req.getActive() != null) {
            category.setActive(req.getActive());
        }

        Category saved = categoryRepository.save(category);
        return toResponse(saved);
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
