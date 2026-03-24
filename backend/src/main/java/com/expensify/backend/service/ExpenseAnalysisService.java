package com.expensify.backend.service;

import com.expensify.backend.dto.ExpenseAnalysisResponse;
import com.expensify.backend.dto.ExpenseResponse;
import com.expensify.backend.exception.OpenAiIntegrationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class ExpenseAnalysisService {

    private final ExpenseService expenseService;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final String openAiApiKey;
    private final String openAiModel;

    public ExpenseAnalysisService(
            ExpenseService expenseService,
            @Value("${openai.base-url}") String openAiBaseUrl,
            @Value("${openai.api-key:}") String openAiApiKey,
            @Value("${openai.model}") String openAiModel
    ) {
        this.expenseService = expenseService;
        this.objectMapper = new ObjectMapper();
        this.openAiApiKey = openAiApiKey;
        this.openAiModel = openAiModel;
        this.restClient = RestClient.builder()
                .baseUrl(openAiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ExpenseAnalysisResponse analyzeExpenses() {
        List<ExpenseResponse> expenses = expenseService.getAllExpenses();
        if (expenses.isEmpty()) {
            throw new OpenAiIntegrationException("Add at least one expense before requesting AI analysis.");
        }

        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new OpenAiIntegrationException("OPENAI_API_KEY is not configured on the backend.");
        }

        BigDecimal totalExpense = expenses.stream()
                .map(ExpenseResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> categoryBreakdown = buildCategoryBreakdown(expenses);
        String aiInsights = fetchInsights(expenses, totalExpense, categoryBreakdown);

        return new ExpenseAnalysisResponse(
                totalExpense,
                expenses.size(),
                categoryBreakdown,
                aiInsights,
                openAiModel
        );
    }

    private Map<String, BigDecimal> buildCategoryBreakdown(List<ExpenseResponse> expenses) {
        Map<String, BigDecimal> totals = new LinkedHashMap<>();

        for (ExpenseResponse expense : expenses) {
            totals.merge(expense.category(), expense.amount(), BigDecimal::add);
        }

        return totals;
    }

    private String fetchInsights(
            List<ExpenseResponse> expenses,
            BigDecimal totalExpense,
            Map<String, BigDecimal> categoryBreakdown
    ) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", openAiModel,
                    "instructions", """
                            You are a finance assistant inside an expense tracker.
                            Analyze the provided expense data and return:
                            1. A short spending summary.
                            2. The most important spending pattern.
                            3. Two actionable suggestions to improve spending.
                            Keep the response concise and practical.
                            """,
                    "input", """
                            Analyze this expense dataset and respond in plain English.
                            Expense data:
                            %s
                            """.formatted(buildAnalysisPrompt(expenses, totalExpense, categoryBreakdown))
            );

            String responseBody = restClient.post()
                    .uri("/responses")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            return extractOutputText(objectMapper.readTree(responseBody));
        } catch (JsonProcessingException exception) {
            throw new OpenAiIntegrationException("Could not prepare the expense data for AI analysis.", exception);
        } catch (RestClientException exception) {
            throw new OpenAiIntegrationException("OpenAI request failed. Check your API key and network access.", exception);
        }
    }

    private String buildAnalysisPrompt(
            List<ExpenseResponse> expenses,
            BigDecimal totalExpense,
            Map<String, BigDecimal> categoryBreakdown
    ) {
        StringJoiner expenseLines = new StringJoiner(System.lineSeparator());
        for (ExpenseResponse expense : expenses) {
            expenseLines.add("- %s | %s | %s | %s | %s".formatted(
                    expense.expenseDate(),
                    expense.title(),
                    expense.category(),
                    expense.amount(),
                    expense.notes() == null ? "No notes" : expense.notes()
            ));
        }

        StringJoiner categoryLines = new StringJoiner(System.lineSeparator());
        for (Map.Entry<String, BigDecimal> entry : categoryBreakdown.entrySet()) {
            categoryLines.add("- %s: %s".formatted(entry.getKey(), entry.getValue()));
        }

        return """
                Total spend: %s
                Total entries: %d

                Category totals:
                %s

                Expense entries:
                %s
                """.formatted(totalExpense, expenses.size(), categoryLines, expenseLines);
    }

    private String extractOutputText(JsonNode responseNode) {
        if (responseNode == null) {
            throw new OpenAiIntegrationException("OpenAI returned an empty response.");
        }

        JsonNode outputArray = responseNode.path("output");
        if (!outputArray.isArray()) {
            throw new OpenAiIntegrationException("OpenAI response did not contain output content.");
        }

        StringBuilder textBuilder = new StringBuilder();

        for (JsonNode outputItem : outputArray) {
            JsonNode contentArray = outputItem.path("content");
            if (!contentArray.isArray()) {
                continue;
            }

            for (JsonNode contentItem : contentArray) {
                if ("output_text".equals(contentItem.path("type").asText())) {
                    if (!textBuilder.isEmpty()) {
                        textBuilder.append(System.lineSeparator()).append(System.lineSeparator());
                    }
                    textBuilder.append(contentItem.path("text").asText());
                }
            }
        }

        if (textBuilder.isEmpty()) {
            throw new OpenAiIntegrationException("OpenAI returned a response, but no text insight was found.");
        }

        return textBuilder.toString();
    }
}
