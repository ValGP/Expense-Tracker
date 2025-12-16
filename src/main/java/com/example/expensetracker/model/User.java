package com.example.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "users") // nombre de tabla en la BD
@Data                 // getters, setters, toString, equals, hashCode (Lombok)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Nunca guardes la contraseña en texto plano
    @Column(nullable = false)
    private String passwordHash;

    // Relación con Currency (clase que vamos a crear después)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_currency_code")
    private Currency defaultCurrency;

    private LocalDateTime createdAt;

    private Boolean active;

    // ---- Lógica simple de dominio ---- //

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (active == null) {
            active = true;
        }
    }

    public void changePassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateProfile(String newName, String newEmail) {
        this.name = newName;
        this.email = newEmail;
    }

    public void changeDefaultCurrency(Currency newCurrency) {
        this.defaultCurrency = newCurrency;
    }
}

