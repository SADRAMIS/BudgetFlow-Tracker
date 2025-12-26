package com.example.budgetflow.controller;

import com.example.budgetflow.service.PortfolioAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final PortfolioAnalyticsService analyticsService;

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<PortfolioAnalyticsService.PortfolioAnalytics> getPortfolioAnalytics(
            @PathVariable Long userId) {
        return ResponseEntity.ok(analyticsService.calculateAnalytics(userId));
    }

    @GetMapping("/dividends/{userId}")
    public ResponseEntity<PortfolioAnalyticsService.DividendCalendar> getDividendCalendar(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "12") int monthsAhead) {
        return ResponseEntity.ok(analyticsService.getDividendCalendar(userId, monthsAhead));
    }

    @PostMapping("/rebalance/{userId}")
    public ResponseEntity<PortfolioAnalyticsService.RebalanceRecommendation> calculateRebalance(
            @PathVariable Long userId,
            @RequestBody Map<String, Double> targetAllocation) {
        return ResponseEntity.ok(analyticsService.calculateRebalance(userId, targetAllocation));
    }

    @PostMapping("/snapshot/{userId}")
    public ResponseEntity<String> createSnapshot(@PathVariable Long userId) {
        analyticsService.createSnapshot(userId);
        return ResponseEntity.ok("Снимок портфеля создан");
    }
}

