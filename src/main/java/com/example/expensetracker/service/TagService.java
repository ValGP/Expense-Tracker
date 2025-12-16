package com.example.expensetracker.service;

import com.example.expensetracker.dto.tag.TagCreateRequest;
import com.example.expensetracker.dto.tag.TagResponse;
import com.example.expensetracker.model.Tag;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.TagRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public TagService(UserRepository userRepository, TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public TagResponse create(TagCreateRequest req) {

        if (req.getOwnerId() == null) {
            throw new IllegalArgumentException("ownerId is required");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        User owner = userRepository.findById(req.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + req.getOwnerId()));

        // evitar duplicados por usuario + nombre
        if (tagRepository.existsByOwnerAndNameIgnoreCase(owner, req.getName())) {
            throw new IllegalArgumentException("Tag with that name already exists for this user");
        }

        Tag tag = Tag.builder()
                .owner(owner)
                .name(req.getName().trim())
                .active(true)
                .build();

        Tag saved = tagRepository.save(tag);

        return toResponse(saved);
    }

    public List<TagResponse> listByOwner(Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + ownerId));

        return tagRepository.findByOwner(owner)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TagResponse toResponse(Tag t) {
        return new TagResponse(
                t.getId(),
                t.getName(),
                t.getActive()
        );
    }
}
