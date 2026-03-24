package com.expensify.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String title,
        String category,
        BigDecimal amount,
        LocalDate expenseDate,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
