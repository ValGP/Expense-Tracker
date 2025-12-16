package com.example.expensetracker.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "currencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Currency {

    @Id
    @Column(length = 3)
    private String code;          // "ARS", "USD", "EUR"

    private String name;          // "Argentine Peso"
    private String symbol;        // "$", "US$", "€"
    private int decimalDigits;    // 2, etc.

    private BigDecimal exchangeRateToBase; // ej: tipo de cambio a una moneda base

    public void updateExchangeRateToBase(BigDecimal newRate) {
        this.exchangeRateToBase = newRate;
    }
}
