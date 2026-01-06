package com.example.expensetracker.service;

import com.example.expensetracker.dto.tag.TagCreateRequest;
import com.example.expensetracker.dto.tag.TagUpdateRequest;
import com.example.expensetracker.dto.tag.TagResponse;
import com.example.expensetracker.model.Tag;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.TagRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TagService {

    private final CurrentUserService currentUserService;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public TagService(CurrentUserService currentUserService,
                      UserRepository userRepository,
                      TagRepository tagRepository) {
        this.currentUserService = currentUserService;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public TagResponse create(TagCreateRequest req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("name is required");
        }

        Long myId = currentUserService.getId();
        User owner = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + myId));

        if (tagRepository.existsByOwnerAndNameIgnoreCase(owner, req.getName().trim())) {
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

    public List<TagResponse> listMine(Boolean activeOnly) {
        Long myId = currentUserService.getId();
        User owner = userRepository.findById(myId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + myId));

        return tagRepository.findByOwner(owner).stream()
                .filter(t -> activeOnly == null || !activeOnly || Boolean.TRUE.equals(t.getActive()))
                .map(this::toResponse)
                .toList();
    }

    public TagResponse update(Long tagId, TagUpdateRequest req) {
        Long myId = currentUserService.getId();

        Tag tag = tagRepository.findByIdAndOwnerId(tagId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));

        if (req.getName() != null) {
            String newName = req.getName().trim();
            if (newName.isBlank()) throw new IllegalArgumentException("name cannot be blank");

            if (!newName.equalsIgnoreCase(tag.getName())
                    && tagRepository.existsByOwnerAndNameIgnoreCase(tag.getOwner(), newName)) {
                throw new IllegalArgumentException("Tag with that name already exists for this user");
            }
            tag.setName(newName);
        }

        if (req.getActive() != null) {
            tag.setActive(req.getActive());
        }

        Tag saved = tagRepository.save(tag);
        return toResponse(saved);
    }

    private TagResponse toResponse(Tag t) {
        return new TagResponse(
                t.getId(),
                t.getName(),
                t.getActive()
        );
    }
}
