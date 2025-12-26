package com.example.budgetflow.service;

import com.example.budgetflow.entity.*;
import com.example.budgetflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioAnalyticsService {

    private final AccountService accountService;
    private final AssetService assetService;
    private final TradeRepository tradeRepository;
    private final AccrualRepository accrualRepository;
    private final PortfolioSnapshotRepository snapshotRepository;
    private final MarketDataRepository marketDataRepository;

    public PortfolioAnalytics calculateAnalytics(Long userId) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        if (accounts.isEmpty()) {
            return new PortfolioAnalytics(0.0, 0.0, 0.0, 0.0, Map.of(), Map.of(), List.of());
        }

        double totalValue = 0.0;
        double totalCost = 0.0;
        Map<String, Double> byType = new HashMap<>();
        Map<String, Double> byCurrency = new HashMap<>();
        List<AssetAnalytics> assetDetails = new ArrayList<>();

        for (Account account : accounts) {
            List<Asset> assets = assetService.getAssetsByAccount(account.getId());
            
            for (Asset asset : assets) {
                double quantity = asset.getQuantity() != null ? asset.getQuantity() : 0.0;
                if (quantity <= 0) continue;

                // Получаем текущую цену или используем среднюю цену покупки
                double currentPrice = getCurrentPrice(asset);
                double assetValue = quantity * currentPrice;
                double assetCost = calculateAverageCost(asset);

                totalValue += assetValue;
                totalCost += assetCost;

                byType.merge(asset.getType(), assetValue, Double::sum);
                byCurrency.merge(asset.getCurrency(), assetValue, Double::sum);

                double returnPct = assetCost > 0 ? ((assetValue - assetCost) / assetCost) * 100 : 0.0;
                assetDetails.add(new AssetAnalytics(
                        asset.getId(), asset.getName(), asset.getTicker(),
                        asset.getType(), quantity, currentPrice, assetValue, assetCost, returnPct
                ));
            }
        }

        double totalReturn = totalCost > 0 ? ((totalValue - totalCost) / totalCost) * 100 : 0.0;
        double sharpeRatio = calculateSharpeRatio(userId);

        return new PortfolioAnalytics(
                totalValue, totalCost, totalReturn, sharpeRatio,
                byType, byCurrency, assetDetails
        );
    }

    public DividendCalendar getDividendCalendar(Long userId, int monthsAhead) {
        List<Account> accounts = accountService.getAccountsByUser(userId);
        List<DividendEvent> events = new ArrayList<>();
        LocalDate endDate = LocalDate.now().plusMonths(monthsAhead);

        for (Account account : accounts) {
            List<Asset> assets = assetService.getAssetsByAccount(account.getId());
            for (Asset asset : assets) {
                List<Accrual> accruals = accrualRepository.findByAssetId(asset.getId());
                for (Accrual accrual : accruals) {
                    if (accrual.getDate().isAfter(LocalDate.now()) && 
                        accrual.getDate().isBefore(endDate)) {
                        events.add(new DividendEvent(
                                accrual.getDate(), asset.getTicker(), asset.getName(),
                                accrual.getAmount(), accrual.getType()
                        ));
                    }
                }
            }
        }

        events.sort(Comparator.comparing(DividendEvent::date));
        return new DividendCalendar(events);
    }

    public RebalanceRecommendation calculateRebalance(Long userId, Map<String, Double> targetAllocation) {
        PortfolioAnalytics analytics = calculateAnalytics(userId);
        Map<String, Double> current = analytics.byType();
        Map<String, Double> recommendations = new HashMap<>();
        double totalValue = analytics.totalValue();

        for (Map.Entry<String, Double> target : targetAllocation.entrySet()) {
            double currentValue = current.getOrDefault(target.getKey(), 0.0);
            double targetValue = totalValue * target.getValue() / 100.0;
            double difference = targetValue - currentValue;
            recommendations.put(target.getKey(), difference);
        }

        return new RebalanceRecommendation(totalValue, current, targetAllocation, recommendations);
    }

    @Transactional
    public void createSnapshot(Long userId) {
        PortfolioAnalytics analytics = calculateAnalytics(userId);
        
        PortfolioSnapshot snapshot = new PortfolioSnapshot();
        snapshot.setUser(userService.getUserById(userId));
        snapshot.setDate(LocalDate.now());
        snapshot.setTotalValue(BigDecimal.valueOf(analytics.totalValue()).setScale(2, RoundingMode.HALF_UP));
        snapshot.setTotalCost(BigDecimal.valueOf(analytics.totalCost()).setScale(2, RoundingMode.HALF_UP));
        snapshot.setTotalReturn(BigDecimal.valueOf(analytics.totalReturn()).setScale(4, RoundingMode.HALF_UP));
        snapshot.setSharpeRatio(BigDecimal.valueOf(analytics.sharpeRatio()).setScale(4, RoundingMode.HALF_UP));
        snapshot.setCurrency("RUB");
        
        snapshotRepository.save(snapshot);
    }

    private double getCurrentPrice(Asset asset) {
        return marketDataRepository.findFirstByTickerOrderByDateDesc(asset.getTicker())
                .map(md -> md.getPrice().doubleValue())
                .orElse(calculateAverageCost(asset));
    }

    private double calculateAverageCost(Asset asset) {
        List<Trade> buys = tradeRepository.findByAssetIdAndType(asset.getId(), "BUY");
        if (buys.isEmpty()) return 0.0;

        double totalCost = 0.0;
        double totalQuantity = 0.0;

        for (Trade trade : buys) {
            double qty = trade.getQuantity() != null ? trade.getQuantity() : 0.0;
            double price = trade.getPrice() != null ? trade.getPrice() : 0.0;
            double fee = trade.getFee() != null ? trade.getFee() : 0.0;
            
            totalCost += (qty * price + fee);
            totalQuantity += qty;
        }

        return totalQuantity > 0 ? totalCost / totalQuantity : 0.0;
    }

    private double calculateSharpeRatio(Long userId) {
        List<PortfolioSnapshot> snapshots = snapshotRepository.findByUserIdAndDateAfter(
                userId, LocalDate.now().minusMonths(12));
        
        if (snapshots.size() < 2) return 0.0;

        List<Double> returns = snapshots.stream()
                .map(s -> s.getTotalReturn().doubleValue())
                .collect(Collectors.toList());

        double avgReturn = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream()
                .mapToDouble(r -> Math.pow(r - avgReturn, 2))
                .average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        return stdDev > 0 ? avgReturn / stdDev : 0.0;
    }

    private final UserService userService;

    public record PortfolioAnalytics(
            double totalValue, double totalCost, double totalReturn, double sharpeRatio,
            Map<String, Double> byType, Map<String, Double> byCurrency,
            List<AssetAnalytics> assets
    ) {}

    public record AssetAnalytics(
            Long id, String name, String ticker, String type,
            double quantity, double currentPrice, double value, double cost, double returnPct
    ) {}

    public record DividendCalendar(List<DividendEvent> events) {}

    public record DividendEvent(
            LocalDate date, String ticker, String name, Double amount, String type
    ) {}

    public record RebalanceRecommendation(
            double totalValue, Map<String, Double> current, 
            Map<String, Double> target, Map<String, Double> recommendations
    ) {}
}

