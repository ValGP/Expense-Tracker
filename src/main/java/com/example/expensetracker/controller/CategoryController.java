package com.example.expensetracker.controller;

import com.example.expensetracker.dto.category.CategoryCreateRequest;
import com.example.expensetracker.dto.category.CategoryUpdateRequest;
import com.example.expensetracker.dto.category.CategoryResponse;
import com.example.expensetracker.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // POST /api/categories
    @PostMapping
    public CategoryResponse create(@RequestBody CategoryCreateRequest request) {
        return categoryService.create(request);
    }

    // GET /api/categories?ownerId=1
    @GetMapping
    public List<CategoryResponse> listByOwner(
            @RequestParam Long ownerId,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return categoryService.listByOwner(ownerId, activeOnly);
    }

    // PATCH /api/categories/{id}
    @PatchMapping("/{id}")
    public CategoryResponse update(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        return categoryService.update(id, request);
    }
}
