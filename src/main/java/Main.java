import backend.controllers.StockController;
import backend.controllers.ForecastController;
import backend.database.DatabaseHandler;
import backend.models.Exercise;
import backend.models.Portfolio;
import backend.models.Question;
import backend.models.Quiz;
import backend.models.Transaction;
import backend.models.TutorialSection;
import backend.models.User;
import backend.services.AlphaVantageService;
import backend.services.AuthService;
import backend.services.LSTMForecaster;
import backend.services.PortfolioAnalyticsService;
import backend.services.PortfolioService;
import backend.services.TutorialService;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class Main {
    private static StockController stockController;
    private static AuthService authService;
    private static PortfolioService portfolioService;
    private static PortfolioAnalyticsService portfolioAnalyticsService;
    private static TutorialService tutorialService;
    private static final Gson gson = new Gson();
    private static HttpServer server;

    public static void main(String[] args) {
        try {
            System.out.println(" Starting Smart-Invest Server...");

            // Initialize services
            System.out.println(" Initializing services...");
            DatabaseHandler dbHandler = new DatabaseHandler();
            authService = new AuthService(dbHandler);
            AlphaVantageService alphaVantageService = new AlphaVantageService();
            stockController = new StockController(alphaVantageService);
            portfolioService = new PortfolioService(dbHandler);
            portfolioAnalyticsService = new PortfolioAnalyticsService(dbHandler);
            tutorialService = new TutorialService();

            // Create HTTP server
            System.out.println(" Creating HTTP server on port 8080...");
            server = HttpServer.create(new InetSocketAddress(8080), 0);

            // ========== TRADING SYSTEM ROUTES ==========
            server.createContext("/api/stocks", new StockHandler());
            server.createContext("/api/auth", new AuthHandler());
            server.createContext("/api/portfolio", new PortfolioHandler());
            server.createContext("/api/portfolio/analytics", new PortfolioAnalyticsHandler());
            server.createContext("/api/transactions", new TransactionsHandler());
            server.createContext("/api/trade", new TradeHandler());

            // ========== TUTORIAL SYSTEM ROUTES ==========
            server.createContext("/api/tutorials", new TutorialsHandler());
            server.createContext("/api/tutorial", new TutorialHandler());
            server.createContext("/api/tutorials/search", new TutorialSearchHandler());
            server.createContext("/api/tutorials/by-level", new TutorialsByLevelHandler());
            server.createContext("/api/tutorials/by-category", new TutorialsByCategoryHandler());
            server.createContext("/api/tutorials/progress", new TutorialProgressHandler());
            server.createContext("/api/tutorial/quiz", new TutorialQuizHandler());
            server.createContext("/api/tutorial/exercise", new TutorialExerciseHandler());
            server.createContext("/api/tutorials/progress/complete", new TutorialProgressCompleteHandler());
            server.createContext("/api/health", new HealthHandler());

            // ========== FORECAST ENDPOINT ==========
            server.createContext("/api/forecast", new ForecastHandler());

            // Set executor to null (uses default)
            server.setExecutor(null);

            // Start the server
            server.start();

            System.out.println(" Smart-Invest Server started successfully on port 8080");
            System.out.println(" Server is running and waiting for requests...");

            // Keep the main thread alive
            System.out.println(" Press Ctrl+C to stop the server...");

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n Shutting down server...");
                if (server != null) {
                    server.stop(0);
                }
                System.out.println(" Server stopped.");
            }));

            // Keep the main thread alive
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println(" Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ===================== FORECAST HANDLER =====================
    static class ForecastHandler implements HttpHandler {
        private final ForecastController forecastController;
        private final Gson gson = new Gson();

        public ForecastHandler() {
            System.out.println(" Initializing ForecastHandler...");
            this.forecastController = new ForecastController();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received forecast request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);

            // Handle preflight OPTIONS request
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                System.out.println(" Handling OPTIONS preflight request");
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
                return;
            }

            // Only allow GET requests
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                System.out.println(" Method not allowed: " + exchange.getRequestMethod());
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                // Parse query parameters
                String query = exchange.getRequestURI().getQuery();
                String symbol = "AAPL"; // default
                int days = 7; // default

                if (query != null) {
                    System.out.println(" Query string: " + query);
                    Map<String, String> params = parseQueryParams(query);
                    symbol = params.getOrDefault("symbol", "AAPL");
                    String daysStr = params.get("days");
                    if (daysStr != null) {
                        try {
                            days = Integer.parseInt(daysStr);
                        } catch (NumberFormatException e) {
                            System.out.println("️ Invalid days parameter, using default: 7");
                            days = 7;
                        }
                    }
                }

                System.out.println(" Forecast request for symbol: " + symbol + ", days: " + days);

                // Get historical prices
                List<Double> prices = forecastController.getHistoricalPrices(symbol);
                System.out.println(" Got " + prices.size() + " historical prices");

                // Run forecast
                LSTMForecaster.ForecastResult result = forecastController.getForecastResult(symbol, days);
                System.out.println(" Forecast generated with confidence: " + result.confidence + "%");

                // Prepare response - only return last 30 days of historical data
                int historicalSize = prices.size();
                List<Double> recentHistorical = prices.subList(
                        Math.max(0, historicalSize - Math.min(30, historicalSize)),
                        historicalSize
                );

                List<Double> forecast = result.predictions.subList(
                        0,
                        Math.min(days, result.predictions.size())
                );

                Map<String, Object> response = new HashMap<>();
                response.put("symbol", symbol);
                response.put("historical", recentHistorical);
                response.put("forecast", forecast);
                response.put("confidence", result.confidence);
                response.put("direction", result.direction);
                response.put("modelType", "LSTM Neural Network");

                String jsonResponse = gson.toJson(response);
                System.out.println(" Forecast generated successfully for " + symbol);
                sendResponse(exchange, 200, jsonResponse);

            } catch (Exception e) {
                System.err.println(" Error in ForecastHandler: " + e.getMessage());
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
            } finally {
                exchange.close();
            }
        }

        private Map<String, String> parseQueryParams(String query) {
            Map<String, String> params = new HashMap<>();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        params.put(pair[0], pair[1]);
                    } else if (pair.length == 1) {
                        params.put(pair[0], "");
                    }
                }
            }
            return params;
        }
    }

    // ===================== STOCK HANDLER =====================
    static class StockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received stock request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                if (path.startsWith("/api/stocks/")) {
                    String symbol = path.substring("/api/stocks/".length());
                    if (symbol.isEmpty()) {
                        sendResponse(exchange, 400, "{\"error\": \"Stock symbol is required\"}");
                        return;
                    }

                    System.out.println(" Fetching stock data for: " + symbol);
                    Map<String, Object> stockData = stockController.getStockQuote(symbol);
                    String response = "{\"success\":true,\"data\":" + convertObjectToJson(stockData) + "}";
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                }
            } catch (Exception e) {
                System.err.println("Error in StockHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
            } finally {
                exchange.close();
            }
        }
    }

    // ===================== AUTH HANDLER =====================
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received auth request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String path = exchange.getRequestURI().getPath();

                if (path.equals("/api/auth/login")) {
                    handleLogin(exchange, requestBody);
                } else if (path.equals("/api/auth/register")) {
                    handleRegister(exchange, requestBody);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                }
            } catch (Exception e) {
                System.err.println("Error in AuthHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
            } finally {
                exchange.close();
            }
        }

        private void handleLogin(HttpExchange exchange, String requestBody) throws IOException {
            try {
                String username = extractValue(requestBody, "username");
                String password = extractValue(requestBody, "password");

                if (username == null || password == null) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Username and password are required\"}");
                    return;
                }

                System.out.println(" Login attempt for user: " + username);
                User user = authService.loginUser(username, password);
                if (user != null) {
                    String response = String.format(
                            "{\"success\":true,\"message\":\"Login successful\",\"data\":{\"username\":\"%s\",\"email\":\"%s\",\"balance\":%.2f}}",
                            escapeJson(user.getUsername()),
                            escapeJson(user.getEmail()),
                            user.getBalance()
                    );
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 401, "{\"success\":false,\"error\":\"Invalid credentials\"}");
                }
            } catch (Exception e) {
                throw new IOException("Login processing failed", e);
            }
        }

        private void handleRegister(HttpExchange exchange, String requestBody) throws IOException {
            try {
                String username = extractValue(requestBody, "username");
                String email = extractValue(requestBody, "email");
                String password = extractValue(requestBody, "password");

                if (username == null || email == null || password == null) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Username, email and password are required\"}");
                    return;
                }

                System.out.println(" Registration attempt for user: " + username);
                User newUser = authService.registerUser(username, password, email);
                if (newUser != null) {
                    String response = String.format(
                            "{\"success\":true,\"message\":\"Registration successful\",\"data\":{\"username\":\"%s\",\"email\":\"%s\",\"balance\":%.2f}}",
                            escapeJson(newUser.getUsername()),
                            escapeJson(newUser.getEmail()),
                            newUser.getBalance()
                    );
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Username already exists\"}");
                }
            } catch (Exception e) {
                throw new IOException("Registration processing failed", e);
            }
        }
    }

    // ===================== PORTFOLIO HANDLER =====================
    static class PortfolioHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received portfolio request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                String username = getParamValue(query, "username");

                if (username == null) {
                    sendResponse(exchange, 400, "{\"error\": \"Username parameter required\"}");
                    return;
                }

                System.out.println(" Fetching portfolio for user: " + username);
                List<Portfolio> portfolio = portfolioService.getPortfolio(username);
                String portfolioJson = convertPortfolioToJson(portfolio);
                double totalValue = portfolioService.getPortfolioValue(username);
                double totalGainLoss = portfolioService.getTotalGainLoss(username);

                String response = String.format(
                        "{\"success\":true,\"portfolio\":%s,\"totalValue\":%.2f,\"totalGainLoss\":%.2f}",
                        portfolioJson, totalValue, totalGainLoss
                );
                sendResponse(exchange, 200, response);

            } catch (Exception e) {
                System.err.println("Error in PortfolioHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
            } finally {
                exchange.close();
            }
        }
    }

    // ===================== PORTFOLIO ANALYTICS HANDLER =====================
    static class PortfolioAnalyticsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received portfolio analytics request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                // Prefer explicit username parameter, but accept userId as fallback
                String username = getParamValue(query, "username");
                if (username == null) {
                    username = getParamValue(query, "userId");
                }

                if (username == null) {
                    sendResponse(exchange, 400, "{\"error\": \"username parameter required\"}");
                    return;
                }

                System.out.println(" Computing portfolio analytics for user: " + username);
                Map<String, Object> analytics = portfolioAnalyticsService.getAnalytics(username);
                String responseJson = convertMapToJson(analytics);
                sendResponse(exchange, 200, responseJson);

            } catch (Exception e) {
                System.err.println("Error in PortfolioAnalyticsHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
            } finally {
                exchange.close();
            }
        }
    }

    // ===================== TRANSACTIONS HANDLER =====================
    static class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received transactions request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                String query = exchange.getRequestURI().getQuery();
                String username = getParamValue(query, "username");

                if (username == null) {
                    sendResponse(exchange, 400, "{\"error\": \"Username parameter required\"}");
                    return;
                }

                System.out.println(" Fetching transactions for user: " + username);
                List<Transaction> transactions = portfolioService.getTransactions(username);
                String transactionsJson = convertTransactionsToJson(transactions);

                String response = "{\"success\":true,\"transactions\":" + transactionsJson + "}";
                sendResponse(exchange, 200, response);

            } catch (Exception e) {
                System.err.println("Error in TransactionsHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
            } finally {
                exchange.close();
            }
        }
    }

    // ===================== TRADE HANDLER =====================
    static class TradeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(" Received trade request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());

            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String path = exchange.getRequestURI().getPath();

                if (path.equals("/api/trade/buy")) {
                    handleBuy(exchange, requestBody);
                } else if (path.equals("/api/trade/sell")) {
                    handleSell(exchange, requestBody);
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                }
            } catch (Exception e) {
                System.err.println("Error in TradeHandler: " + e.getMessage());
                sendResponse(exchange, 500, "{\"error\": \"Internal server error\"}");
            } finally {
                exchange.close();
            }
        }

        private void handleBuy(HttpExchange exchange, String requestBody) throws IOException {
            String username = extractValue(requestBody, "username");
            String symbol = extractValue(requestBody, "symbol");
            String stockName = extractValue(requestBody, "stockName");
            String quantityStr = extractValue(requestBody, "quantity");
            String orderType = extractValue(requestBody, "orderType");
            String duration = extractValue(requestBody, "duration");

            if (username == null || symbol == null || stockName == null || quantityStr == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing required fields\"}");
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                System.out.println(" Buy order: " + username + " wants to buy " + quantity + " shares of " + symbol);
                Map<String, Object> result = portfolioService.buyStock(username, symbol, stockName, quantity, orderType, duration);

                if (Boolean.TRUE.equals(result.get("success"))) {
                    User user = authService.getUser(username);
                    String response = String.format(
                            "{\"success\":true,\"message\":\"%s\",\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"balance\":%.2f}}",
                            result.get("message"),
                            escapeJson(user.getUsername()),
                            escapeJson(user.getEmail()),
                            user.getBalance()
                    );
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + result.get("message") + "\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Invalid quantity\"}");
            }
        }

        private void handleSell(HttpExchange exchange, String requestBody) throws IOException {
            String username = extractValue(requestBody, "username");
            String symbol = extractValue(requestBody, "symbol");
            String stockName = extractValue(requestBody, "stockName");
            String quantityStr = extractValue(requestBody, "quantity");
            String orderType = extractValue(requestBody, "orderType");
            String duration = extractValue(requestBody, "duration");

            if (username == null || symbol == null || stockName == null || quantityStr == null) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing required fields\"}");
                return;
            }

            try {
                int quantity = Integer.parseInt(quantityStr);
                System.out.println(" Sell order: " + username + " wants to sell " + quantity + " shares of " + symbol);
                Map<String, Object> result = portfolioService.sellStock(username, symbol, stockName, quantity, orderType, duration);

                if (Boolean.TRUE.equals(result.get("success"))) {
                    User user = authService.getUser(username);
                    String response = String.format(
                            "{\"success\":true,\"message\":\"%s\",\"user\":{\"username\":\"%s\",\"email\":\"%s\",\"balance\":%.2f}}",
                            result.get("message"),
                            escapeJson(user.getUsername()),
                            escapeJson(user.getEmail()),
                            user.getBalance()
                    );
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + result.get("message") + "\"}");
                }
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"Invalid quantity\"}");
            }
        }
    }


    // ===================== TUTORIAL HANDLERS =====================

    static class TutorialsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                List<TutorialSection> tutorials = tutorialService.getAllTutorials();
                String tutorialsJson = convertTutorialsToJson(tutorials);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + tutorialsJson + "}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to fetch tutorials\"}");
            }
        }
    }

    static class TutorialHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            try {
                if (path.endsWith("/validate")) {
                    String tutorialId = extractTutorialIdFromPath(path, "/validate");
                    handleValidateExercise(exchange, tutorialId);
                } else {
                    String tutorialId = extractTutorialIdFromPath(path, "");
                    handleGetTutorial(exchange, tutorialId);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Internal server error\"}");
            }
        }

        private String extractTutorialIdFromPath(String path, String suffix) {
            String basePath = "/api/tutorial/";
            if (path.startsWith(basePath)) {
                String remaining = path.substring(basePath.length());
                if (suffix.isEmpty()) {
                    return remaining;
                } else if (remaining.endsWith(suffix)) {
                    return remaining.substring(0, remaining.length() - suffix.length());
                }
            }
            return null;
        }

        private void handleGetTutorial(HttpExchange exchange, String tutorialId) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            if (tutorialId == null || tutorialId.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Tutorial ID is required\"}");
                return;
            }
            TutorialSection tutorial = tutorialService.getTutorial(tutorialId);
            if (tutorial != null) {
                String tutorialJson = convertTutorialToJson(tutorial);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + tutorialJson + "}");
            } else {
                sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Tutorial not found\"}");
            }
        }

        private void handleValidateExercise(HttpExchange exchange, String tutorialId) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            if (tutorialId == null || tutorialId.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Tutorial ID is required\"}");
                return;
            }
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String userAnswer = extractValue(requestBody, "answer");
                if (userAnswer == null || userAnswer.trim().isEmpty()) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Answer is required\"}");
                    return;
                }
                boolean isCorrect = tutorialService.validateExercise(tutorialId, userAnswer);
                String response = String.format("{\"success\":true,\"correct\":%b}", isCorrect);
                sendJsonResponse(exchange, 200, response);
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to validate exercise\"}");
            }
        }
    }

    static class TutorialSearchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                String searchTerm = getParameterFromQuery(query, "q");

                List<TutorialSection> tutorials;
                if (searchTerm != null) {
                    tutorials = tutorialService.searchTutorials(searchTerm);
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Search term parameter required\"}");
                    return;
                }

                String tutorialsJson = convertTutorialsToJson(tutorials);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + tutorialsJson + "}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to search tutorials\"}");
            }
        }
    }

    static class TutorialsByLevelHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                String level = getParameterFromQuery(query, "level");

                if (level == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Level parameter required\"}");
                    return;
                }

                List<TutorialSection> tutorials = tutorialService.getTutorialsByLevel(level);
                String tutorialsJson = convertTutorialsToJson(tutorials);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + tutorialsJson + "}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to fetch tutorials by level\"}");
            }
        }
    }

    static class TutorialsByCategoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            try {
                String query = exchange.getRequestURI().getQuery();
                String category = getParameterFromQuery(query, "category");

                if (category == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Category parameter required\"}");
                    return;
                }

                List<TutorialSection> tutorials = tutorialService.getTutorialsByCategory(category);
                String tutorialsJson = convertTutorialsToJson(tutorials);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + tutorialsJson + "}");
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to fetch tutorials by category\"}");
            }
        }
    }

    static class TutorialProgressHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                if ("POST".equals(exchange.getRequestMethod())) {
                    handleUpdateProgress(exchange);
                } else if ("GET".equals(exchange.getRequestMethod())) {
                    handleGetProgress(exchange);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to process progress request\"}");
            }
        }

        private void handleUpdateProgress(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String username = extractValue(requestBody, "username");
            String tutorialId = extractValue(requestBody, "tutorialId");
            String completedStr = extractValue(requestBody, "completed");

            if (username == null || tutorialId == null || completedStr == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing required fields\"}");
                return;
            }

            boolean completed = Boolean.parseBoolean(completedStr);
            boolean success = tutorialService.updateUserProgress(username, tutorialId, completed);

            if (success) {
                sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Progress updated\"}");
            } else {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"message\":\"Failed to update progress\"}");
            }
        }

        private void handleGetProgress(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String username = getParameterFromQuery(query, "username");

            if (username == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Username parameter required\"}");
                return;
            }

            Map<String, Boolean> progress = tutorialService.getUserProgress(username);
            Map<String, Object> stats = tutorialService.getUserStatistics(username);

            String progressJson = convertProgressToJson(progress);
            String statsJson = convertObjectToJson(stats);

            String response = String.format("{\"success\":true,\"data\":%s,\"statistics\":%s}",
                    progressJson, statsJson);
            sendJsonResponse(exchange, 200, response);
        }

        private String convertProgressToJson(Map<String, Boolean> progress) {
            if (progress == null || progress.isEmpty()) {
                return "{}";
            }

            StringBuilder json = new StringBuilder("{");
            int i = 0;
            for (Map.Entry<String, Boolean> entry : progress.entrySet()) {
                json.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
                if (i < progress.size() - 1) {
                    json.append(",");
                }
                i++;
            }
            json.append("}");
            return json.toString();
        }
    }

    static class TutorialProgressCompleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                String username = extractValue(requestBody, "username");
                String tutorialId = extractValue(requestBody, "tutorialId");

                if (username == null || tutorialId == null) {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing required fields\"}");
                    return;
                }

                boolean success = tutorialService.updateUserProgress(username, tutorialId, true);
                if (success) {
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"message\":\"Tutorial marked as complete\"}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Failed to mark tutorial complete\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Internal server error\"}");
            }
        }
    }

    static class TutorialQuizHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    handleGetQuiz(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleSubmitQuiz(exchange);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to process quiz request\"}");
            }
        }

        private void handleGetQuiz(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String tutorialId = getParameterFromQuery(query, "tutorialId");

            if (tutorialId == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Tutorial ID parameter required\"}");
                return;
            }

            Quiz quiz = tutorialService.getQuiz(tutorialId);
            if (quiz != null) {
                String quizJson = convertQuizToJson(quiz);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + quizJson + "}");
            } else {
                sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Quiz not found for this tutorial\"}");
            }
        }

        private void handleSubmitQuiz(HttpExchange exchange) throws IOException {
            try {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("=== QUIZ SUBMISSION DEBUG ===");
                System.out.println("Raw request body: " + requestBody);

                String tutorialId = extractJsonValue(requestBody, "tutorialId");
                String username = extractJsonValue(requestBody, "username");
                String answersJson = extractJsonValue(requestBody, "answers");

                System.out.println("Parsed - Tutorial ID: " + tutorialId);
                System.out.println("Parsed - Username: " + username);
                System.out.println("Parsed - Answers: " + answersJson);

                if (tutorialId == null || username == null || answersJson == null) {
                    System.out.println("Missing required fields");
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing required fields\"}");
                    return;
                }

                Map<String, Object> result = tutorialService.submitQuiz(tutorialId, username, answersJson);
                if (result != null) {
                    String resultJson = convertObjectToJson(result);
                    System.out.println("Quiz submission successful, sending response: " + resultJson);
                    sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + resultJson + "}");
                } else {
                    sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Failed to submit quiz\"}");
                }
            } catch (Exception e) {
                System.err.println("Error in handleSubmitQuiz: " + e.getMessage());
                e.printStackTrace();
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to submit quiz: " + e.getMessage() + "\"}");
            }
        }
    }

    static class TutorialExerciseHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                if ("GET".equals(exchange.getRequestMethod())) {
                    handleGetExercise(exchange);
                } else if ("POST".equals(exchange.getRequestMethod())) {
                    handleSubmitExercise(exchange);
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"success\":false,\"error\":\"Failed to process exercise request\"}");
            }
        }

        private void handleGetExercise(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String tutorialId = getParameterFromQuery(query, "tutorialId");

            if (tutorialId == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Tutorial ID parameter required\"}");
                return;
            }

            Exercise exercise = tutorialService.getExercise(tutorialId);
            if (exercise != null) {
                String exerciseJson = convertExerciseToJson(exercise);
                sendJsonResponse(exchange, 200, "{\"success\":true,\"data\":" + exerciseJson + "}");
            } else {
                sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Exercise not found for this tutorial\"}");
            }
        }

        private void handleSubmitExercise(HttpExchange exchange) throws IOException {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String tutorialId = extractValue(requestBody, "tutorialId");
            String userAnswer = extractValue(requestBody, "answer");

            if (tutorialId == null || userAnswer == null) {
                sendJsonResponse(exchange, 400, "{\"success\":false,\"error\":\"Missing required fields\"}");
                return;
            }

            boolean isCorrect = tutorialService.validateExercise(tutorialId, userAnswer);
            sendJsonResponse(exchange, 200, String.format("{\"success\":true,\"correct\":%b}", isCorrect));
        }
    }

    static class HealthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            sendJsonResponse(exchange, 200, "{\"status\":\"OK\",\"service\":\"Smart-Invest API\"}");
        }
    }

    // ===================== UTILITY METHODS =====================

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        sendResponse(exchange, statusCode, jsonResponse);
    }

    private static String getParamValue(String query, String paramName) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int equalsIndex = pair.indexOf("=");
            if (equalsIndex > 0) {
                String key = pair.substring(0, equalsIndex);
                String value = pair.substring(equalsIndex + 1);
                if (key.equals(paramName)) {
                    return value;
                }
            }
        }
        return null;
    }

    private static String getParameterFromQuery(String query, String paramName) {
        if (query == null) return null;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        return null;
    }

    private static String extractValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int startIndex = json.indexOf(searchKey);
            if (startIndex == -1) return null;
            startIndex += searchKey.length();
            int endIndex = json.indexOf("\"", startIndex);
            if (endIndex == -1) return null;
            return json.substring(startIndex, endIndex);
        } catch (Exception e) {
            return null;
        }
    }

    private static String extractJsonValue(String json, String key) {
        try {
            String searchKey = "\"" + key + "\":";
            int keyIndex = json.indexOf(searchKey);
            if (keyIndex == -1) {
                return null;
            }

            int valueStart = keyIndex + searchKey.length();
            while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
                valueStart++;
            }

            if (valueStart >= json.length()) {
                return null;
            }

            char firstChar = json.charAt(valueStart);
            if (firstChar == '"') {
                valueStart++;
                StringBuilder valueBuilder = new StringBuilder();
                int currentPos = valueStart;
                boolean escaped = false;

                while (currentPos < json.length()) {
                    char c = json.charAt(currentPos);

                    if (escaped) {
                        valueBuilder.append(c);
                        escaped = false;
                    } else if (c == '\\') {
                        escaped = true;
                    } else if (c == '"') {
                        return valueBuilder.toString();
                    } else {
                        valueBuilder.append(c);
                    }
                    currentPos++;
                }
                return null;
            } else {
                int valueEnd = valueStart;
                while (valueEnd < json.length()) {
                    char c = json.charAt(valueEnd);
                    if (c == ',' || c == '}' || Character.isWhitespace(c)) {
                        break;
                    }
                    valueEnd++;
                }
                return json.substring(valueStart, valueEnd);
            }
        } catch (Exception e) {
            return null;
        }
    }

    // JSON Conversion Methods (keep as they were)
    private static String convertPortfolioToJson(List<Portfolio> portfolio) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < portfolio.size(); i++) {
            Portfolio item = portfolio.get(i);
            json.append(String.format(
                    "{\"id\":%d,\"symbol\":\"%s\",\"stockName\":\"%s\",\"quantity\":%d,\"purchasePrice\":%.2f,\"currentPrice\":%.2f,\"totalValue\":%.2f,\"totalGainLoss\":%.2f,\"gainLossPercentage\":%.2f}",
                    item.getId(),
                    escapeJson(item.getSymbol()),
                    escapeJson(item.getStockName()),
                    item.getQuantity(),
                    item.getPurchasePrice(),
                    item.getCurrentPrice(),
                    item.getTotalValue(),
                    item.getTotalGainLoss(),
                    item.getGainLossPercentage()
            ));
            if (i < portfolio.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String convertTransactionsToJson(List<Transaction> transactions) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);
            json.append(String.format(
                    "{\"id\":%d,\"type\":\"%s\",\"symbol\":\"%s\",\"stockName\":\"%s\",\"quantity\":%d,\"price\":%.2f,\"totalAmount\":%.2f,\"orderType\":\"%s\",\"duration\":\"%s\",\"transactionDate\":\"%s\"}",
                    transaction.getId(),
                    escapeJson(transaction.getType()),
                    escapeJson(transaction.getSymbol()),
                    escapeJson(transaction.getStockName()),
                    transaction.getQuantity(),
                    transaction.getPrice(),
                    transaction.getTotalAmount(),
                    escapeJson(transaction.getOrderType()),
                    escapeJson(transaction.getDuration()),
                    escapeJson(transaction.getTransactionDate().toString())
            ));
            if (i < transactions.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String convertObjectToJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Map) {
            return convertMapToJson((Map<?, ?>) obj);
        } else if (obj instanceof String) {
            return "\"" + escapeJson(obj.toString()) + "\"";
        } else if (obj instanceof Number || obj instanceof Boolean) {
            return obj.toString();
        } else {
            return "\"" + escapeJson(obj.toString()) + "\"";
        }
    }

    private static String convertMapToJson(Map<?, ?> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            json.append("\"").append(escapeJson(entry.getKey().toString())).append("\":")
                    .append(convertObjectToJson(entry.getValue()));
            if (i < map.size() - 1) json.append(",");
            i++;
        }
        json.append("}");
        return json.toString();
    }

    private static String convertTutorialsToJson(List<TutorialSection> tutorials) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tutorials.size(); i++) {
            TutorialSection tutorial = tutorials.get(i);
            json.append(convertTutorialToJson(tutorial));
            if (i < tutorials.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String convertTutorialToJson(TutorialSection tutorial) {
        return String.format(
                "{\"id\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"level\":\"%s\",\"category\":\"%s\",\"estimatedMinutes\":%d,\"completionRate\":%.2f,\"videoUrl\":\"%s\",\"content\":\"%s\",\"infographics\":%s,\"keyPoints\":%s,\"glossary\":%s,\"quiz\":%s,\"exercise\":%s,\"hasVideo\":%b,\"hasSimulator\":%b,\"hasQuiz\":%b,\"prerequisites\":%s,\"nextTutorials\":%s,\"certificationId\":\"%s\"}",
                escapeJson(tutorial.getId()),
                escapeJson(tutorial.getTitle()),
                escapeJson(tutorial.getDescription()),
                escapeJson(tutorial.getLevel()),
                escapeJson(tutorial.getCategory()),
                tutorial.getEstimatedMinutes(),
                tutorial.getCompletionRate(),
                escapeJson(tutorial.getVideoUrl()),
                escapeJson(tutorial.getContent()),
                convertStringListToJson(tutorial.getInfographics()),
                convertStringListToJson(tutorial.getKeyPoints()),
                convertMapToJson(tutorial.getGlossary()),
                convertQuizToJson(tutorial.getQuiz()),
                convertExerciseToJson(tutorial.getExercise()),
                tutorial.isHasVideo(),
                tutorial.isHasSimulator(),
                tutorial.isHasQuiz(),
                convertStringListToJson(tutorial.getPrerequisites()),
                convertStringListToJson(tutorial.getNextTutorials()),
                escapeJson(tutorial.getCertificationId())
        );
    }

    private static String convertQuizToJson(Quiz quiz) {
        if (quiz == null) return "null";
        return String.format(
                "{\"id\":\"%s\",\"passingScore\":%d,\"timeLimit\":%d,\"allowRetakes\":%b,\"questions\":%s}",
                escapeJson(quiz.getId()),
                quiz.getPassingScore(),
                quiz.getTimeLimit(),
                quiz.isAllowRetakes(),
                convertQuestionsToJson(quiz.getQuestions())
        );
    }

    private static String convertQuestionsToJson(List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            json.append(convertQuestionToJson(question));
            if (i < questions.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String convertQuestionToJson(Question question) {
        if (question == null) return "null";
        return String.format(
                "{\"questionText\":\"%s\",\"options\":%s,\"correctAnswerIndex\":%d,\"explanation\":\"%s\",\"questionType\":\"%s\"}",
                escapeJson(question.getQuestionText()),
                convertStringListToJson(question.getOptions()),
                question.getCorrectAnswerIndex(),
                escapeJson(question.getExplanation()),
                escapeJson(question.getQuestionType())
        );
    }

    private static String convertExerciseToJson(Exercise exercise) {
        if (exercise == null) return "null";
        return String.format(
                "{\"question\":\"%s\",\"hint\":\"%s\",\"type\":\"%s\"}",
                escapeJson(exercise.getQuestion()),
                escapeJson(exercise.getHint()),
                escapeJson(exercise.getType())
        );
    }

    private static String convertStringListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            json.append("\"").append(escapeJson(list.get(i))).append("\"");
            if (i < list.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");
        return json.toString();
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("/", "\\/");
    }
}