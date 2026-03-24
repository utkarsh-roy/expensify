package com.expensify.backend.controller;

import com.expensify.backend.dto.ExpenseAnalysisResponse;
import com.expensify.backend.service.ExpenseAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses/analysis")
public class ExpenseAnalysisController {

    private final ExpenseAnalysisService expenseAnalysisService;

    public ExpenseAnalysisController(ExpenseAnalysisService expenseAnalysisService) {
        this.expenseAnalysisService = expenseAnalysisService;
    }

    @GetMapping
    public ExpenseAnalysisResponse analyzeExpenses() {
        return expenseAnalysisService.analyzeExpenses();
    }
}
