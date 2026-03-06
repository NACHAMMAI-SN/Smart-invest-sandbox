package backend.models;

import java.time.LocalDateTime;

public class Portfolio {
    private int id;
    private String username;
    private String symbol;
    private String stockName;
    private int quantity;
    private double purchasePrice;
    private double currentPrice;
    private LocalDateTime purchaseDate;

    public Portfolio() {}

    public Portfolio(String username, String symbol, String stockName, int quantity,
                     double purchasePrice, double currentPrice) {
        this.username = username;
        this.symbol = symbol;
        this.stockName = stockName;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.currentPrice = currentPrice;
        this.purchaseDate = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(double purchasePrice) { this.purchasePrice = purchasePrice; }

    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    // Business logic methods
    public double getTotalValue() {
        return quantity * currentPrice;
    }

    public double getTotalCost() {
        return quantity * purchasePrice;
    }

    public double getTotalGainLoss() {
        return (currentPrice - purchasePrice) * quantity;
    }

    public double getGainLossPercentage() {
        return purchasePrice > 0 ? ((currentPrice - purchasePrice) / purchasePrice) * 100 : 0;
    }

    // Helper method for JSON serialization
    public String getPurchaseDateString() {
        return purchaseDate != null ? purchaseDate.toString() : LocalDateTime.now().toString();
    }

    // For better debugging and logging
    @Override
    public String toString() {
        return String.format(
                "Portfolio{symbol='%s', quantity=%d, purchasePrice=%.2f, currentPrice=%.2f, totalValue=%.2f}",
                symbol, quantity, purchasePrice, currentPrice, getTotalValue()
        );
    }

    // Helper method to check if this is a valid portfolio item
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
                symbol != null && !symbol.trim().isEmpty() &&
                quantity > 0 &&
                purchasePrice > 0 &&
                currentPrice > 0;
    }
}