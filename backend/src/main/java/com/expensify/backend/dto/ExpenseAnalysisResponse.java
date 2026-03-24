package com.expensify.backend.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExpenseAnalysisResponse(
        BigDecimal totalExpense,
        int totalEntries,
        Map<String, BigDecimal> categoryBreakdown,
        String aiInsights,
        String model
) {
}
