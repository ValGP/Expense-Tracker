package com.example.expensetracker.repository;

import com.example.expensetracker.model.Tag;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByOwner(User owner);

    boolean existsByOwnerAndNameIgnoreCase(User owner, String name);

    // 🔒 traer tags SOLO del usuario
    List<Tag> findAllByIdInAndOwnerId(List<Long> ids, Long ownerId);

    Optional<Tag> findByIdAndOwnerId(Long id, Long ownerId);
}
