package com.example.expensetracker.controller;

import com.example.expensetracker.dto.summary.SummaryResponse;
import com.example.expensetracker.service.SummaryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService summaryService;

    public SummaryController(SummaryService summaryService) {
        this.summaryService = summaryService;
    }

    // GET /api/summary?ownerId=1&from=2025-12-01&to=2025-12-31&top=5
    @GetMapping
    public SummaryResponse getSummary(
            @RequestParam Long ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer top
    ) {
        return summaryService.getSummary(ownerId, from, to, top);
    }
}
