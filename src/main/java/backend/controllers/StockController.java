package backend.controllers;

import backend.services.AlphaVantageService;
import com.google.gson.Gson;
import java.util.Map;

public class StockController {
    private final AlphaVantageService alphaVantageService;
    private final Gson gson = new Gson();

    public StockController(AlphaVantageService alphaVantageService) {
        this.alphaVantageService = alphaVantageService;
    }

    // Method for HttpServer to call
    public Map<String, Object> getStockQuote(String symbol) {
        try {
            return alphaVantageService.getStockQuote(symbol);
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch stock data: " + e.getMessage());
        }
    }

    // Method for HttpServer to call
    public Map<String, Object> searchStocks(String keywords) {
        try {
            return alphaVantageService.searchStocks(keywords);
        } catch (Exception e) {
            return Map.of("error", "Failed to search stocks: " + e.getMessage());
        }
    }
}