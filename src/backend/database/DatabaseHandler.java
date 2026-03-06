package backend.database;

import backend.models.User;
import backend.models.Portfolio;
import backend.models.Transaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:smartstock.db";
    private Connection connection;

    public DatabaseHandler() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
            initializePortfolioTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ensures the users table exists
    public void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT UNIQUE NOT NULL, "
                + "passwordHash TEXT NOT NULL, "
                + "email TEXT UNIQUE NOT NULL, "
                + "balance REAL DEFAULT 100000.0"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            System.out.println("Users table ready.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Initialize portfolio and transactions tables
    public void initializePortfolioTable() {
        String createPortfolioTable = "CREATE TABLE IF NOT EXISTS portfolio ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "symbol TEXT NOT NULL, "
                + "stock_name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL, "
                + "purchase_price REAL NOT NULL, "
                + "current_price REAL NOT NULL, "
                + "purchase_date TEXT NOT NULL, "
                + "FOREIGN KEY (username) REFERENCES users(username)"
                + ");";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL, "
                + "type TEXT NOT NULL, "
                + "symbol TEXT NOT NULL, "
                + "stock_name TEXT NOT NULL, "
                + "quantity INTEGER NOT NULL, "
                + "price REAL NOT NULL, "
                + "total_amount REAL NOT NULL, "
                + "order_type TEXT NOT NULL, "
                + "duration TEXT NOT NULL, "
                + "transaction_date TEXT NOT NULL, "
                + "FOREIGN KEY (username) REFERENCES users(username)"
                + ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPortfolioTable);
            stmt.execute(createTransactionsTable);
            System.out.println("Portfolio and Transactions tables ready.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Close the connection
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    // Add a user
    public boolean addUser(User user) {
        String sql = "INSERT INTO users(username, passwordHash, email, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());
            pstmt.setDouble(4, user.getBalance());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding user: " + e.getMessage());
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
                String uname = rs.getString("username");
                String passHash = rs.getString("passwordHash");
                String mail = rs.getString("email");
                double balance = rs.getDouble("balance");

                return new User(uname, passHash, mail, balance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update balance
    public boolean updateUserBalance(String username, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, username);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
        return false;
    }

    // PORTFOLIO METHODS

    // Add stock to portfolio
    public boolean addToPortfolio(Portfolio portfolio) {
        String sql = "INSERT INTO portfolio(username, symbol, stock_name, quantity, purchase_price, current_price, purchase_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, portfolio.getUsername());
            pstmt.setString(2, portfolio.getSymbol());
            pstmt.setString(3, portfolio.getStockName());
            pstmt.setInt(4, portfolio.getQuantity());
            pstmt.setDouble(5, portfolio.getPurchasePrice());
            pstmt.setDouble(6, portfolio.getCurrentPrice());
            pstmt.setString(7, portfolio.getPurchaseDate().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding to portfolio: " + e.getMessage());
            return false;
        }
    }

    // Get portfolio by username
    public List<Portfolio> getPortfolioByUsername(String username) {
        List<Portfolio> portfolio = new ArrayList<>();
        String sql = "SELECT * FROM portfolio WHERE username = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Portfolio item = new Portfolio();
                item.setId(rs.getInt("id"));
                item.setUsername(rs.getString("username"));
                item.setSymbol(rs.getString("symbol"));
                item.setStockName(rs.getString("stock_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPurchasePrice(rs.getDouble("purchase_price"));
                item.setCurrentPrice(rs.getDouble("current_price"));
                // Convert string back to LocalDateTime
                String dateStr = rs.getString("purchase_date");
                item.setPurchaseDate(LocalDateTime.parse(dateStr));
                portfolio.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return portfolio;
    }

    // Update portfolio quantity
    public boolean updatePortfolioQuantity(String username, String symbol, int newQuantity) {
        String sql = "UPDATE portfolio SET quantity = ? WHERE username = ? AND symbol = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setString(2, username);
            pstmt.setString(3, symbol);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
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
                Portfolio item = new Portfolio();
                item.setId(rs.getInt("id"));
                item.setUsername(rs.getString("username"));
                item.setSymbol(rs.getString("symbol"));
                item.setStockName(rs.getString("stock_name"));
                item.setQuantity(rs.getInt("quantity"));
                item.setPurchasePrice(rs.getDouble("purchase_price"));
                item.setCurrentPrice(rs.getDouble("current_price"));
                String dateStr = rs.getString("purchase_date");
                item.setPurchaseDate(LocalDateTime.parse(dateStr));
                return item;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Update portfolio current price
    public boolean updatePortfolioPrice(String username, String symbol, double currentPrice) {
        String sql = "UPDATE portfolio SET current_price = ? WHERE username = ? AND symbol = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, currentPrice);
            pstmt.setString(2, username);
            pstmt.setString(3, symbol);
            int rowsUpdated = pstmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // TRANSACTION METHODS

    // Add transaction
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
            pstmt.setString(10, transaction.getTransactionDate().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Error adding transaction: " + e.getMessage());
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
                // Convert string back to LocalDateTime
                String dateStr = rs.getString("transaction_date");
                transaction.setTransactionDate(LocalDateTime.parse(dateStr));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                transaction.setTransactionDate(LocalDateTime.parse(dateStr));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    // Get total investment (sum of all BUY transactions)
    public double getTotalInvestment(String username) {
        String sql = "SELECT SUM(total_amount) as total FROM transactions WHERE username = ? AND type = 'BUY'";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Get total cash flow (sum of SELL - sum of BUY)
    public double getTotalCashFlow(String username) {
        String buySql = "SELECT COALESCE(SUM(total_amount), 0) as buy_total FROM transactions WHERE username = ? AND type = 'BUY'";
        String sellSql = "SELECT COALESCE(SUM(total_amount), 0) as sell_total FROM transactions WHERE username = ? AND type = 'SELL'";

        try {
            // Get total buy amount
            double buyTotal = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(buySql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    buyTotal = rs.getDouble("buy_total");
                }
            }

            // Get total sell amount
            double sellTotal = 0;
            try (PreparedStatement pstmt = connection.prepareStatement(sellSql)) {
                pstmt.setString(1, username);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    sellTotal = rs.getDouble("sell_total");
                }
            }

            return sellTotal - buyTotal;

        } catch (SQLException e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}