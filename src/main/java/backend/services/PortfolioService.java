package backend.services;

import backend.database.DatabaseHandler;
import backend.models.Portfolio;
import backend.models.Transaction;
import backend.models.User;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class PortfolioService {
    private final DatabaseHandler dbHandler;
    private final AlphaVantageService alphaVantageService;

    public PortfolioService(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
        this.alphaVantageService = new AlphaVantageService();
    }

    public Map<String, Object> buyStock(String username, String symbol, String stockName, int quantity, String orderType, String duration) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current stock price
            Map<String, Object> stockData = alphaVantageService.getStockQuote(symbol);
            double currentPrice = Double.parseDouble((String) stockData.get("price"));

            // Get user balance
            User user = dbHandler.getUserByUsername(username);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found");
                return response;
            }

            double totalCost = currentPrice * quantity;

            if (user.getBalance() < totalCost) {
                response.put("success", false);
                response.put("message", "Insufficient balance");
                return response;
            }

            // Update user balance
            double newBalance = user.getBalance() - totalCost;
            boolean balanceUpdated = dbHandler.updateUserBalance(username, newBalance);

            if (!balanceUpdated) {
                response.put("success", false);
                response.put("message", "Failed to update balance");
                return response;
            }

            // Check if stock already exists in portfolio
            List<Portfolio> portfolio = dbHandler.getPortfolioByUsername(username);
            Portfolio existingStock = portfolio.stream()
                    .filter(p -> p.getSymbol().equals(symbol))
                    .findFirst()
                    .orElse(null);

            if (existingStock != null) {
                // Update existing stock
                int newQuantity = existingStock.getQuantity() + quantity;
                double avgPrice = ((existingStock.getPurchasePrice() * existingStock.getQuantity()) + (currentPrice * quantity)) / newQuantity;

                existingStock.setQuantity(newQuantity);
                existingStock.setPurchasePrice(avgPrice);
                existingStock.setCurrentPrice(currentPrice);

                // Remove old entry and add updated one
                dbHandler.removeFromPortfolio(username, symbol);
                dbHandler.addToPortfolio(existingStock);
            } else {
                // Add new stock to portfolio
                Portfolio newPortfolio = new Portfolio(username, symbol, stockName, quantity, currentPrice, currentPrice);
                dbHandler.addToPortfolio(newPortfolio);
            }

            // Record transaction
            Transaction transaction = new Transaction(username, "BUY", symbol, stockName, quantity, currentPrice, orderType, duration);
            dbHandler.addTransaction(transaction);

            response.put("success", true);
            response.put("message", "Stock purchased successfully");
            response.put("totalCost", totalCost);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error buying stock: " + e.getMessage());
            return response;
        }
    }

    public Map<String, Object> sellStock(String username, String symbol, String stockName, int quantity, String orderType, String duration) {
        Map<String, Object> response = new HashMap<>();
        try {
            // Get current stock price
            Map<String, Object> stockData = alphaVantageService.getStockQuote(symbol);
            double currentPrice = Double.parseDouble((String) stockData.get("price"));

            // Check if user has enough stocks
            List<Portfolio> portfolio = dbHandler.getPortfolioByUsername(username);
            Portfolio existingStock = portfolio.stream()
                    .filter(p -> p.getSymbol().equals(symbol))
                    .findFirst()
                    .orElse(null);

            if (existingStock == null || existingStock.getQuantity() < quantity) {
                response.put("success", false);
                response.put("message", "Insufficient stocks");
                return response;
            }

            // Calculate total sale amount
            double totalSale = currentPrice * quantity;

            // Update user balance
            User user = dbHandler.getUserByUsername(username);
            double newBalance = user.getBalance() + totalSale;
            boolean balanceUpdated = dbHandler.updateUserBalance(username, newBalance);

            if (!balanceUpdated) {
                response.put("success", false);
                response.put("message", "Failed to update balance");
                return response;
            }

            // Update portfolio
            int newQuantity = existingStock.getQuantity() - quantity;
            if (newQuantity == 0) {
                // Remove from portfolio if quantity becomes zero
                dbHandler.removeFromPortfolio(username, symbol);
            } else {
                // Update quantity
                existingStock.setQuantity(newQuantity);
                existingStock.setCurrentPrice(currentPrice);
                dbHandler.removeFromPortfolio(username, symbol);
                dbHandler.addToPortfolio(existingStock);
            }

            // Record transaction
            Transaction transaction = new Transaction(username, "SELL", symbol, stockName, quantity, currentPrice, orderType, duration);
            dbHandler.addTransaction(transaction);

            response.put("success", true);
            response.put("message", "Stock sold successfully");
            response.put("totalSale", totalSale);
            return response;

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error selling stock: " + e.getMessage());
            return response;
        }
    }

    public List<Portfolio> getPortfolio(String username) {
        List<Portfolio> portfolio = dbHandler.getPortfolioByUsername(username);

        // Update current prices for all portfolio items
        for (Portfolio item : portfolio) {
            try {
                Map<String, Object> stockData = alphaVantageService.getStockQuote(item.getSymbol());
                double currentPrice = Double.parseDouble((String) stockData.get("price"));
                item.setCurrentPrice(currentPrice);
            } catch (Exception e) {
                System.out.println("Error updating price for " + item.getSymbol() + ": " + e.getMessage());
            }
        }

        return portfolio;
    }

    public List<Transaction> getTransactions(String username) {
        return dbHandler.getTransactionsByUsername(username);
    }

    public List<Transaction> getTransactionsByType(String username, String type) {
        return dbHandler.getTransactionsByType(username, type);
    }

    public double getPortfolioValue(String username) {
        List<Portfolio> portfolio = getPortfolio(username);
        return portfolio.stream()
                .mapToDouble(p -> p.getCurrentPrice() * p.getQuantity())
                .sum();
    }

    public double getTotalGainLoss(String username) {
        List<Portfolio> portfolio = getPortfolio(username);
        return portfolio.stream()
                .mapToDouble(p -> (p.getCurrentPrice() - p.getPurchasePrice()) * p.getQuantity())
                .sum();
    }

    public Map<String, Object> getPortfolioStats(String username) {
        List<Portfolio> portfolio = getPortfolio(username);
        double totalValue = getPortfolioValue(username);
        double totalGainLoss = getTotalGainLoss(username);
        double totalInvestment = dbHandler.getTotalInvestment(username);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalValue", totalValue);
        stats.put("totalGainLoss", totalGainLoss);
        stats.put("totalInvestment", totalInvestment);
        stats.put("holdingsCount", portfolio.size());
        stats.put("annualReturn", totalInvestment > 0 ? (totalGainLoss / totalInvestment) * 100 : 0);

        return stats;
    }
}