package backend.controllers;

import backend.services.PortfolioService;
import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class PortfolioController {
    private final Gson gson = new Gson();

    public PortfolioController(PortfolioService portfolioService) {
        // Get portfolio
        get("/api/portfolio/:username", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                return gson.toJson(portfolioService.getPortfolio(username));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to fetch portfolio: " + e.getMessage()));
            }
        });

        // Get portfolio stats
        get("/api/portfolio/:username/stats", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                return gson.toJson(portfolioService.getPortfolioStats(username));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to fetch portfolio stats: " + e.getMessage()));
            }
        });

        // Get portfolio value
        get("/api/portfolio/:username/value", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                double value = portfolioService.getPortfolioValue(username);
                return gson.toJson(Map.of("value", value));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to fetch portfolio value: " + e.getMessage()));
            }
        });
    }
}