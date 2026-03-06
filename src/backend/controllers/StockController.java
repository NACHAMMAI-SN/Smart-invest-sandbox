package backend.controllers;

import backend.services.AlphaVantageService;
import java.util.Map;

public class StockController {
    private AlphaVantageService alphaVantageService;

    public StockController() {
        this.alphaVantageService = new AlphaVantageService();
    }

    public Map<String, Object> getStockData(String symbol) {
        try {
            return alphaVantageService.getStockQuote(symbol);
        } catch (Exception e) {
            return Map.of("error", "Failed to fetch stock data: " + e.getMessage());
        }
    }

    public Map<String, Object> searchStocks(String keywords) {
        try {
            return alphaVantageService.searchStocks(keywords);
        } catch (Exception e) {
            return Map.of("error", "Failed to search stocks: " + e.getMessage());
        }
    }
}