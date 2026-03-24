package com.expensify.backend.service;

import com.expensify.backend.dto.ExpenseRequest;
import com.expensify.backend.dto.ExpenseResponse;
import com.expensify.backend.entity.Expense;
import com.expensify.backend.exception.ResourceNotFoundException;
import com.expensify.backend.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<ExpenseResponse> getAllExpenses() {
        return expenseRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ExpenseResponse getExpenseById(Long id) {
        return toResponse(findExpense(id));
    }

    public ExpenseResponse createExpense(ExpenseRequest request) {
        Expense expense = new Expense();
        applyRequest(expense, request);
        return toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse updateExpense(Long id, ExpenseRequest request) {
        Expense existingExpense = findExpense(id);
        applyRequest(existingExpense, request);
        return toResponse(expenseRepository.save(existingExpense));
    }

    public void deleteExpense(Long id) {
        Expense expense = findExpense(id);
        expenseRepository.delete(expense);
    }

    private Expense findExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense with id " + id + " was not found."));
    }

    private void applyRequest(Expense expense, ExpenseRequest request) {
        expense.setTitle(request.title().trim());
        expense.setCategory(request.category().trim());
        expense.setAmount(request.amount());
        expense.setExpenseDate(request.expenseDate());
        expense.setNotes(normalizeNotes(request.notes()));
    }

    private String normalizeNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }
        return notes.trim();
    }

    private ExpenseResponse toResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getTitle(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getExpenseDate(),
                expense.getNotes(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }
}
