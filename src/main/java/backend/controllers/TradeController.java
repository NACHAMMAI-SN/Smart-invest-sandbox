package backend.controllers;

import backend.services.PortfolioService;
import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class TradeController {
    private final Gson gson = new Gson();

    public TradeController(PortfolioService portfolioService) {
        // Buy stock
        post("/api/trade/buy", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> requestData = gson.fromJson(req.body(), Map.class);

                String username = (String) requestData.get("username");
                String symbol = (String) requestData.get("symbol");
                String stockName = (String) requestData.get("stockName");
                int quantity = ((Double) requestData.get("quantity")).intValue();
                String orderType = (String) requestData.get("orderType");
                String duration = (String) requestData.get("duration");

                Map<String, Object> result = portfolioService.buyStock(
                        username, symbol, stockName, quantity, orderType, duration
                );

                return gson.toJson(result);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Error processing buy order: " + e.getMessage()
                ));
            }
        });

        // Sell stock
        post("/api/trade/sell", (req, res) -> {
            res.type("application/json");
            try {
                Map<String, Object> requestData = gson.fromJson(req.body(), Map.class);

                String username = (String) requestData.get("username");
                String symbol = (String) requestData.get("symbol");
                String stockName = (String) requestData.get("stockName");
                int quantity = ((Double) requestData.get("quantity")).intValue();
                String orderType = (String) requestData.get("orderType");
                String duration = (String) requestData.get("duration");

                Map<String, Object> result = portfolioService.sellStock(
                        username, symbol, stockName, quantity, orderType, duration
                );

                return gson.toJson(result);
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of(
                        "success", false,
                        "message", "Error processing sell order: " + e.getMessage()
                ));
            }
        });
    }
}