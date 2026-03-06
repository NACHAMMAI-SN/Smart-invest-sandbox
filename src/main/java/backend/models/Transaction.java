package backend.models;

import java.time.LocalDateTime;

public class Transaction {
    private int id;
    private String username;
    private String type; // "BUY" or "SELL"
    private String symbol;
    private String stockName;
    private int quantity;
    private double price;
    private double totalAmount;
    private String orderType; // "MARKET", "LIMIT"
    private String duration; // "IOC", "FOK"
    private LocalDateTime transactionDate;

    public Transaction() {}

    public Transaction(String username, String type, String symbol, String stockName,
                       int quantity, double price, String orderType, String duration) {
        this.username = username;
        this.type = type;
        this.symbol = symbol;
        this.stockName = stockName;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = quantity * price;
        this.orderType = orderType;
        this.duration = duration;
        this.transactionDate = LocalDateTime.now();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}