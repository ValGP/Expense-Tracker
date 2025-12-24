package com.example.expensetracker.controller;

import com.example.expensetracker.dto.tag.TagCreateRequest;
import com.example.expensetracker.dto.tag.TagUpdateRequest;
import com.example.expensetracker.dto.tag.TagResponse;
import com.example.expensetracker.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    // POST /api/tags
    @PostMapping
    public TagResponse create(@RequestBody TagCreateRequest request) {
        return tagService.create(request);
    }

    // GET /api/tags?ownerId=1
    @GetMapping
    public List<TagResponse> listByOwner(
            @RequestParam Long ownerId,
            @RequestParam(required = false) Boolean activeOnly
    ) {
        return tagService.listByOwner(ownerId, activeOnly);
    }

    @PatchMapping("/{id}")
    public TagResponse update(@PathVariable Long id, @RequestBody TagUpdateRequest request) {
        return tagService.update(id, request);
    }
}
