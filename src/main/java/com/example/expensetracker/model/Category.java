package com.example.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private String name;
    private String description;
    private String colorHex;

    private Boolean active;

    public void rename(String newName) {
        this.name = newName;
    }

    public void changeDescription(String newDescription) {
        this.description = newDescription;
    }

    public void changeColor(String newColorHex) {
        this.colorHex = newColorHex;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}
