package backend.database;

import backend.models.User;
import backend.models.Portfolio;
import backend.models.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:smartstock.db";
    private Connection connection;
    private static final Logger logger = Logger.getLogger(DatabaseHandler.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public DatabaseHandler() {
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            // Enable foreign keys for SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
            initializeDatabase();
            initializePortfolioTable();
            logger.info("Database initialized successfully");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Failed to initialize database connection", e);
        }
    }

    // Ensures the users table exists
    public void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "passwordHash TEXT NOT NULL, "
                + "email TEXT UNIQUE NOT NULL, "
                + "balance REAL DEFAULT 100000.0, "
                + "created_at TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            logger.info("Users table ready.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating users table", e);
        }
    }

    // Initialize portfolio and transactions tables
    public void initializePortfolioTable() {
        String createPortfolioTable = "CREATE TABLE IF NOT EXISTS portfolio ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "symbol TEXT NOT NULL, "
                + "stock_name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL CHECK (quantity >= 0), "
                + "purchase_price REAL NOT NULL CHECK (purchase_price >= 0), "
                + "current_price REAL NOT NULL CHECK (current_price >= 0), "
                + "purchase_date TEXT NOT NULL, "
                + "last_updated TEXT DEFAULT CURRENT_TIMESTAMP, "
                + "UNIQUE(username, symbol), "
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE"
                + ");";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "type TEXT NOT NULL CHECK (type IN ('BUY', 'SELL')), "
                + "symbol TEXT NOT NULL, "
                + "stock_name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL CHECK (quantity > 0), "
                + "price REAL NOT NULL CHECK (price >= 0), "
                + "total_amount REAL NOT NULL, "
                + "order_type TEXT NOT NULL, "
                + "duration TEXT NOT NULL, "
                + "transaction_date TEXT NOT NULL, "
                + "FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPortfolioTable);
            stmt.execute(createTransactionsTable);
            logger.info("Portfolio and Transactions tables ready.");
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error creating portfolio/transactions tables", e);
        }
    }

    // Close the connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.info("Database connection closed.");
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error closing database connection", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // Check if connection is valid
    public boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Database connection validation failed", e);
            return false;
        }
    }

    // Add a user with transaction support
    public boolean addUser(User user) {
        String sql = "INSERT INTO users(username, passwordHash, email, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());
            pstmt.setDouble(4, user.getBalance());
            pstmt.executeUpdate();
            logger.info("User added successfully: " + user.getUsername());
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error adding user: " + user.getUsername(), e);
            return false;
        }
    }

    // Fetch a user by username
    public User getUserByUsername(String username) {
        String sql = "SELECT username, passwordHash, email, balance FROM users WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("passwordHash"),
                        rs.getString("email"),
                        rs.getDouble("balance")
                );
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching user: " + username, e);
        }
        return null;
    }

    // Update balance with transaction support
    public boolean updateUserBalance(String username, double newBalance) {
        if (newBalance < 0) {
            logger.warning("Attempt to set negative balance for user: " + username);
            return false;
        }

        String sql = "UPDATE users SET balance = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, username);
            int rowsUpdated = pstmt.executeUpdate();
            boolean success = rowsUpdated > 0;
            if (success) {
                logger.info("Balance updated for user: " + username + " to " + newBalance);
            }
            return success;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error updating balance for user: " + username, e);
            return false;
        }
    }

    // Validate login
    public boolean validateUser(String username, String passwordHash) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND passwordHash = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error validating user: " + username, e);
        }
        return false;
    }

    // PORTFOLIO METHODS

    // Add stock to portfolio with validation
    public boolean addToPortfolio(Portfolio portfolio) {
        if (!portfolio.isValid()) {
            logger.warning("Invalid portfolio data for user: " + portfolio.getUsername());
            return false;
        }

        String sql = "INSERT INTO portfolio(username, symbol, stock_name, quantity, purchase_price, current_price, purchase_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, portfolio.getUsername());
            pstmt.setString(2, portfolio.getSymbol());
            pstmt.setString(3, portfolio.getStockName());
            pstmt.setInt(4, portfolio.getQuantity());
            pstmt.setDouble(5, portfolio.getPurchasePrice());
            pstmt.setDouble(6, portfolio.getCurrentPrice());
            pstmt.setString(7, portfolio.getPurchaseDate().format(DATE_FORMATTER));
            pstmt.executeUpdate();
            logger.info("Portfolio item added for user: " + portfolio.getUsername() + " - " + portfolio.getSymbol());
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error adding to portfolio for user: " + portfolio.getUsername(), e);
            return false;
        }
    }

    // Get portfolio by username
    public List<Portfolio> getPortfolioByUsername(String username) {
        List<Portfolio> portfolio = new ArrayList<>();
        String sql = "SELECT * FROM portfolio WHERE username = ? ORDER BY symbol";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Portfolio item = createPortfolioFromResultSet(rs);
                if (item != null) {
                    portfolio.add(item);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching portfolio for user: " + username, e);
        }
        return portfolio;
    }

    // Update portfolio quantity with validation
    public boolean updatePortfolioQuantity(String username, String symbol, int newQuantity) {
        if (newQuantity < 0) {
            logger.warning("Attempt to set negative quantity for user: " + username + ", symbol: " + symbol);
            return false;
        }

        String sql = "UPDATE portfolio SET quantity = ?, last_updated = CURRENT_TIMESTAMP WHERE username = ? AND symbol = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, username);
            pstmt.setString(3, symbol);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error updating portfolio quantity for user: " + username + ", symbol: " + symbol, e);
            return false;
        }
    }

    // Remove from portfolio
    public boolean removeFromPortfolio(String username, String symbol) {
        String sql = "DELETE FROM portfolio WHERE username = ? AND symbol = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, symbol);
            int rowsDeleted = pstmt.executeUpdate();
            boolean success = rowsDeleted > 0;
            if (success) {
                logger.info("Portfolio item removed for user: " + username + " - " + symbol);
            }
            return success;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error removing from portfolio for user: " + username + ", symbol: " + symbol, e);
            return false;
        }
    }

    // Check if stock exists in portfolio
    public Portfolio getPortfolioItem(String username, String symbol) {
        String sql = "SELECT * FROM portfolio WHERE username = ? AND symbol = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, symbol);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createPortfolioFromResultSet(rs);
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching portfolio item for user: " + username + ", symbol: " + symbol, e);
        }
        return null;
    }

    // Update portfolio current price
    public boolean updatePortfolioPrice(String username, String symbol, double currentPrice) {
        if (currentPrice < 0) {
            logger.warning("Attempt to set negative price for user: " + username + ", symbol: " + symbol);
            return false;
        }

        String sql = "UPDATE portfolio SET current_price = ?, last_updated = CURRENT_TIMESTAMP WHERE username = ? AND symbol = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, currentPrice);
            pstmt.setString(2, username);
            pstmt.setString(3, symbol);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error updating portfolio price for user: " + username + ", symbol: " + symbol, e);
            return false;
        }
    }

    // TRANSACTION METHODS

    // Add transaction with validation
    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions(username, type, symbol, stock_name, quantity, price, total_amount, order_type, duration, transaction_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getUsername());
            pstmt.setString(2, transaction.getType());
            pstmt.setString(3, transaction.getSymbol());
            pstmt.setString(4, transaction.getStockName());
            pstmt.setInt(5, transaction.getQuantity());
            pstmt.setDouble(6, transaction.getPrice());
            pstmt.setDouble(7, transaction.getTotalAmount());
            pstmt.setString(8, transaction.getOrderType());
            pstmt.setString(9, transaction.getDuration());
            pstmt.setString(10, transaction.getTransactionDate().format(DATE_FORMATTER));
            pstmt.executeUpdate();
            logger.info("Transaction recorded for user: " + transaction.getUsername() + " - " + transaction.getType() + " " + transaction.getSymbol());
            return true;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error adding transaction for user: " + transaction.getUsername(), e);
            return false;
        }
    }

    // Get transactions by username
    public List<Transaction> getTransactionsByUsername(String username) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE username = ? ORDER BY transaction_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = createTransactionFromResultSet(rs);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching transactions for user: " + username, e);
        }
        return transactions;
    }

    // Get transactions by type
    public List<Transaction> getTransactionsByType(String username, String type) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE username = ? AND type = ? ORDER BY transaction_date DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, type);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = createTransactionFromResultSet(rs);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching transactions for user: " + username + ", type: " + type, e);
        }
        return transactions;
    }

    // Get total investment based on portfolio cost (quantity * purchase_price)
    public double getTotalInvestment(String username) {
        String sql = "SELECT COALESCE(SUM(quantity * purchase_price), 0) as total_investment FROM portfolio WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total_investment");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error calculating total investment for user: " + username, e);
        }
        return 0.0;
    }

    // Get total cash flow (sum of SELL - sum of BUY)
    public double getTotalCashFlow(String username) {
        String buySql = "SELECT COALESCE(SUM(total_amount), 0) as buy_total FROM transactions WHERE username = ? AND type = 'BUY'";
        String sellSql = "SELECT COALESCE(SUM(total_amount), 0) as sell_total FROM transactions WHERE username = ? AND type = 'SELL'";

        try {
            double buyTotal = executeSumQuery(buySql, username);
            double sellTotal = executeSumQuery(sellSql, username);
            return sellTotal - buyTotal;
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error calculating cash flow for user: " + username, e);
            return 0.0;
        }
    }

    // Helper method for sum queries
    private double executeSumQuery(String sql, String username) throws SQLException {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // Helper method to create Portfolio from ResultSet
    private Portfolio createPortfolioFromResultSet(ResultSet rs) throws SQLException {
        try {
            Portfolio item = new Portfolio();
            item.setId(rs.getInt("id"));
            item.setUsername(rs.getString("username"));
            item.setSymbol(rs.getString("symbol"));
            item.setStockName(rs.getString("stock_name"));
            item.setQuantity(rs.getInt("quantity"));
            item.setPurchasePrice(rs.getDouble("purchase_price"));
            item.setCurrentPrice(rs.getDouble("current_price"));

            String dateStr = rs.getString("purchase_date");
            item.setPurchaseDate(LocalDateTime.parse(dateStr, DATE_FORMATTER));

            return item;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating portfolio from result set", e);
            return null;
        }
    }

    // Helper method to create Transaction from ResultSet
    private Transaction createTransactionFromResultSet(ResultSet rs) throws SQLException {
        try {
            Transaction transaction = new Transaction();
            transaction.setId(rs.getInt("id"));
            transaction.setUsername(rs.getString("username"));
            transaction.setType(rs.getString("type"));
            transaction.setSymbol(rs.getString("symbol"));
            transaction.setStockName(rs.getString("stock_name"));
            transaction.setQuantity(rs.getInt("quantity"));
            transaction.setPrice(rs.getDouble("price"));
            transaction.setTotalAmount(rs.getDouble("total_amount"));
            transaction.setOrderType(rs.getString("order_type"));
            transaction.setDuration(rs.getString("duration"));

            String dateStr = rs.getString("transaction_date");
            transaction.setTransactionDate(LocalDateTime.parse(dateStr, DATE_FORMATTER));

            return transaction;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error creating transaction from result set", e);
            return null;
        }
    }

    // Additional utility method: Get user's total portfolio value
    public double getTotalPortfolioValue(String username) {
        String sql = "SELECT COALESCE(SUM(quantity * current_price), 0) as total_value FROM portfolio WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_value");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error calculating total portfolio value for user: " + username, e);
        }
        return 0.0;
    }

    // === Analytics helpers ===

    /**
     * Returns the total invested amount based on portfolio purchase prices.
     * This complements {@link #getTotalInvestment(String)} which uses transactions.
     */
    public double getTotalPortfolioInvestment(String username) {
        String sql = "SELECT COALESCE(SUM(quantity * purchase_price), 0) as total_investment " +
                "FROM portfolio WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_investment");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error calculating total portfolio investment for user: " + username, e);
        }
        return 0.0;
    }

    /**
     * Returns the number of holdings (rows) for the user.
     */
    public int getHoldingsCount(String username) {
        String sql = "SELECT COUNT(*) AS holdings FROM portfolio WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("holdings");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error calculating holdings count for user: " + username, e);
        }
        return 0;
    }

    /**
     * Returns the symbol of the best performing stock based on price difference.
     */
    public String getTopPerformingStockSymbol(String username) {
        String sql = "SELECT symbol, (current_price - purchase_price) AS profit " +
                "FROM portfolio WHERE username = ? " +
                "ORDER BY profit DESC LIMIT 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("symbol");
            }
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error fetching top performing stock for user: " + username, e);
        }
        return null;
    }
}