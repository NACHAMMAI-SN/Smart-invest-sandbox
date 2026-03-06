package backend.services;

import backend.database.DatabaseHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for computing portfolio analytics using SQL-based aggregates.
 */
public class PortfolioAnalyticsService {
    private final DatabaseHandler dbHandler;

    public PortfolioAnalyticsService(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    /**
     * Compute high-level portfolio analytics for the given user.
     *
     * @param username logical user identifier (username)
     * @return map containing analytics metrics
     */
    public Map<String, Object> getAnalytics(String username) {
        double totalInvestment = dbHandler.getTotalPortfolioInvestment(username);
        double portfolioValue = dbHandler.getTotalPortfolioValue(username);
        double profitLoss = portfolioValue - totalInvestment;
        int holdings = dbHandler.getHoldingsCount(username);
        String topStock = dbHandler.getTopPerformingStockSymbol(username);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("success", true);
        analytics.put("totalInvestment", round2(totalInvestment));
        analytics.put("portfolioValue", round2(portfolioValue));
        analytics.put("profitLoss", round2(profitLoss));
        analytics.put("holdings", holdings);
        analytics.put("topStock", topStock != null ? topStock : "");

        if (holdings == 0) {
            analytics.put("message", "No stocks in portfolio yet");
        }

        return analytics;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

