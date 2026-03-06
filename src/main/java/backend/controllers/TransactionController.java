package backend.controllers;

import backend.services.PortfolioService;
import static spark.Spark.*;
import com.google.gson.Gson;

import java.util.Map;

public class TransactionController {
    private final Gson gson = new Gson();

    public TransactionController(PortfolioService portfolioService) {
        // Get user transactions
        get("/api/transactions/:username", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                return gson.toJson(portfolioService.getTransactions(username));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to fetch transactions: " + e.getMessage()));
            }
        });

        // Get transactions by type
        get("/api/transactions/:username/:type", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                String type = req.params(":type");
                return gson.toJson(portfolioService.getTransactionsByType(username, type));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to fetch transactions: " + e.getMessage()));
            }
        });
    }
}