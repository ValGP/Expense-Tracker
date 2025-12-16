package com.example.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "category_budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // dueño
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    // Ej: "2025-11" para noviembre 2025
    private String period;

    private BigDecimal limitAmount;
}
