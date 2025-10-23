package backend;

import backend.controllers.StockController;
import backend.services.AuthService;
import backend.services.PortfolioService;
import backend.services.TutorialService;
import backend.database.DatabaseHandler;
import backend.models.User;
import backend.models.Portfolio;
import backend.models.Transaction;
import backend.models.TutorialSection;
import backend.models.Quiz;
import backend.models.Question;
import backend.models.Exercise;
import backend.models.CaseStudy;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;

public class Main {
    private static StockController stockController;
    private static AuthService authService;
    private static PortfolioService portfolioService;
    private static TutorialService tutorialService;

    public static void main(String[] args) {
        try {
            // Initialize services
            DatabaseHandler dbHandler = new DatabaseHandler();
            authService = new AuthService(dbHandler);
            stockController = new StockController();
            portfolioService = new PortfolioService(dbHandler);
            tutorialService = new TutorialService();

            // Create HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // ========== TRADING SYSTEM ROUTES ==========
            server.createContext("/api/stocks", new StockHandler());
            server.createContext("/api/auth", new AuthHandler());
            server.createContext("/api/portfolio", new PortfolioHandler());
            server.createContext("/api/transactions", new PortfolioHandler());
            server.createContext("/api/trade", new PortfolioHandler());

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

            server.setExecutor(null);
            server.start();

            System.out.println("🚀 Smart-Invest Server started on port 8080");
            System.out.println("=== TRADING SYSTEM ENDPOINTS ===");
            System.out.println("GET  /api/stocks/{symbol}");
            System.out.println("POST /api/auth/login");
            System.out.println("POST /api/auth/register");
            System.out.println("GET  /api/portfolio?username={username}");
            System.out.println("GET  /api/transactions?username={username}");
            System.out.println("POST /api/trade/buy");
            System.out.println("POST /api/trade/sell");

            System.out.println("=== TUTORIAL SYSTEM ENDPOINTS ===");
            System.out.println("GET  /api/tutorials");
            System.out.println("GET  /api/tutorial/{id}");
            System.out.println("GET  /api/tutorials/search?q={query}");
            System.out.println("GET  /api/tutorials/by-level?level={level}");
            System.out.println("GET  /api/tutorials/by-category?category={category}");
            System.out.println("GET  /api/tutorials/progress?username={username}");
            System.out.println("POST /api/tutorials/progress/complete");
            System.out.println("GET  /api/tutorial/quiz?tutorialId={id}");
            System.out.println("POST /api/tutorial/quiz");
            System.out.println("GET  /api/tutorial/exercise?tutorialId={id}");
            System.out.println("POST /api/tutorial/{id}/validate");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===================== STOCK HANDLER =====================
    static class StockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
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
                    Map<String, Object> stockData = stockController.getStockData(symbol);

                    JSONObject response = new JSONObject(stockData);
                    sendResponse(exchange, 200, response.toString());
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                }
            } catch (Exception e) {
                sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

    // ===================== AUTH HANDLER =====================
    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
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
                // Read request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                JSONObject json = new JSONObject(requestBody);

                String path = exchange.getRequestURI().getPath();
                JSONObject response = new JSONObject();

                if (path.equals("/api/auth/login")) {
                    String username = json.getString("username");
                    String password = json.getString("password");

                    User user = authService.loginUser(username, password);
                    if (user != null) {
                        response.put("success", true);
                        response.put("message", "Login successful");
                        response.put("data", new JSONObject()
                                .put("username", user.getUsername())
                                .put("email", user.getEmail())
                                .put("balance", user.getBalance()));
                    } else {
                        response.put("success", false);
                        response.put("message", "Invalid credentials");
                    }

                } else if (path.equals("/api/auth/register")) {
                    String username = json.getString("username");
                    String email = json.getString("email");
                    String password = json.getString("password");

                    User newUser = authService.registerUser(username, password, email);
                    if (newUser != null) {
                        response.put("success", true);
                        response.put("message", "Registration successful");
                        response.put("data", new JSONObject()
                                .put("username", newUser.getUsername())
                                .put("email", newUser.getEmail())
                                .put("balance", newUser.getBalance()));
                    } else {
                        response.put("success", false);
                        response.put("message", "Username already exists");
                    }

                } else {
                    sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                    return;
                }

                sendResponse(exchange, 200, response.toString());
            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
            }
        }
    }

    // ===================== PORTFOLIO HANDLER =====================
    static class PortfolioHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }

            try {
                String path = exchange.getRequestURI().getPath();
                String query = exchange.getRequestURI().getQuery();

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    if (path.equals("/api/portfolio")) {
                        // Get portfolio for user
                        String username = getParamValue(query, "username");
                        if (username == null) {
                            sendResponse(exchange, 400, "{\"error\": \"Username parameter required\"}");
                            return;
                        }

                        List<Portfolio> portfolio = portfolioService.getPortfolio(username);

                        JSONArray portfolioArray = new JSONArray();
                        for (Portfolio item : portfolio) {
                            JSONObject portfolioObj = new JSONObject();
                            portfolioObj.put("id", item.getId());
                            portfolioObj.put("symbol", item.getSymbol());
                            portfolioObj.put("stockName", item.getStockName());
                            portfolioObj.put("quantity", item.getQuantity());
                            portfolioObj.put("purchasePrice", item.getPurchasePrice());
                            portfolioObj.put("currentPrice", item.getCurrentPrice());
                            portfolioObj.put("totalValue", item.getTotalValue());
                            portfolioObj.put("totalGainLoss", item.getTotalGainLoss());
                            portfolioObj.put("gainLossPercentage", item.getGainLossPercentage());
                            portfolioArray.put(portfolioObj);
                        }

                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("portfolio", portfolioArray);
                        response.put("totalValue", portfolioService.getPortfolioValue(username));
                        response.put("totalGainLoss", portfolioService.getTotalGainLoss(username));

                        sendResponse(exchange, 200, response.toString());

                    } else if (path.equals("/api/transactions")) {
                        // Get transactions for user
                        String username = getParamValue(query, "username");
                        if (username == null) {
                            sendResponse(exchange, 400, "{\"error\": \"Username parameter required\"}");
                            return;
                        }

                        List<Transaction> transactions = portfolioService.getTransactions(username);

                        JSONArray transactionsArray = new JSONArray();
                        for (Transaction transaction : transactions) {
                            JSONObject transactionObj = new JSONObject();
                            transactionObj.put("id", transaction.getId());
                            transactionObj.put("type", transaction.getType());
                            transactionObj.put("symbol", transaction.getSymbol());
                            transactionObj.put("stockName", transaction.getStockName());
                            transactionObj.put("quantity", transaction.getQuantity());
                            transactionObj.put("price", transaction.getPrice());
                            transactionObj.put("totalAmount", transaction.getTotalAmount());
                            transactionObj.put("orderType", transaction.getOrderType());
                            transactionObj.put("duration", transaction.getDuration());
                            transactionObj.put("transactionDate", transaction.getTransactionDate().toString());
                            transactionsArray.put(transactionObj);
                        }

                        JSONObject response = new JSONObject();
                        response.put("success", true);
                        response.put("transactions", transactionsArray);

                        sendResponse(exchange, 200, response.toString());
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                    }

                } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String requestBody = new String(exchange.getRequestBody().readAllBytes());
                    JSONObject json = new JSONObject(requestBody);

                    if (path.equals("/api/trade/buy")) {
                        String username = json.getString("username");
                        String symbol = json.getString("symbol");
                        String stockName = json.getString("stockName");
                        int quantity = json.getInt("quantity");
                        String orderType = json.getString("orderType");
                        String duration = json.getString("duration");

                        boolean success = portfolioService.buyStock(username, symbol, stockName, quantity, orderType, duration);

                        JSONObject response = new JSONObject();
                        if (success) {
                            response.put("success", true);
                            response.put("message", "Stock purchased successfully");
                            // Return updated user data
                            User user = authService.getUser(username);
                            response.put("user", new JSONObject()
                                    .put("username", user.getUsername())
                                    .put("email", user.getEmail())
                                    .put("balance", user.getBalance()));
                        } else {
                            response.put("success", false);
                            response.put("message", "Failed to purchase stock - insufficient balance or invalid data");
                        }

                        sendResponse(exchange, 200, response.toString());

                    } else if (path.equals("/api/trade/sell")) {
                        String username = json.getString("username");
                        String symbol = json.getString("symbol");
                        String stockName = json.getString("stockName");
                        int quantity = json.getInt("quantity");
                        String orderType = json.getString("orderType");
                        String duration = json.getString("duration");

                        boolean success = portfolioService.sellStock(username, symbol, stockName, quantity, orderType, duration);

                        JSONObject response = new JSONObject();
                        if (success) {
                            response.put("success", true);
                            response.put("message", "Stock sold successfully");
                            // Return updated user data
                            User user = authService.getUser(username);
                            response.put("user", new JSONObject()
                                    .put("username", user.getUsername())
                                    .put("email", user.getEmail())
                                    .put("balance", user.getBalance()));
                        } else {
                            response.put("success", false);
                            response.put("message", "Failed to sell stock - insufficient shares or invalid data");
                        }

                        sendResponse(exchange, 200, response.toString());
                    } else {
                        sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
                    }
                } else {
                    sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
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
                String username = extractValue(requestBody, "username");

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
                sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Quiz.java not found for this tutorial\"}");
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
                    System.out.println("Quiz.java submission successful, sending response: " + resultJson);
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
                sendJsonResponse(exchange, 404, "{\"success\":false,\"error\":\"Exercise.java not found for this tutorial\"}");
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().set("Access-Control-Max-Age", "3600");
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int statusCode, String jsonResponse) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes());
        }
    }

    private static String getParamValue(String query, String paramName) {
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(paramName)) {
                    return keyValue[1];
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

    // JSON Conversion Methods
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

    private static String convertMapToJson(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            json.append("\"").append(escapeJson(entry.getKey())).append("\":\"")
                    .append(escapeJson(entry.getValue())).append("\"");
            if (i < map.size() - 1) {
                json.append(",");
            }
            i++;
        }
        json.append("}");
        return json.toString();
    }

    private static String convertObjectMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            json.append("\"").append(escapeJson(key)).append("\":");

            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Number) {
                json.append(value);
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof List) {
                json.append(convertListToJson((List<?>) value));
            } else if (value instanceof Map) {
                json.append(convertObjectMapToJson((Map<String, Object>) value));
            } else {
                json.append("\"").append(escapeJson(value != null ? value.toString() : "")).append("\"");
            }

            if (i < map.size() - 1) {
                json.append(",");
            }
            i++;
        }
        json.append("}");
        return json.toString();
    }

    private static String convertObjectToJson(Object obj) {
        if (obj == null) return "null";

        try {
            if (obj instanceof Map) {
                return convertObjectMapToJson((Map<String, Object>) obj);
            } else if (obj instanceof List) {
                return convertListToJson((List<?>) obj);
            } else if (obj instanceof String) {
                return "\"" + escapeJson(obj.toString()) + "\"";
            } else if (obj instanceof Number || obj instanceof Boolean) {
                return obj.toString();
            } else {
                return "\"" + escapeJson(obj.toString()) + "\"";
            }
        } catch (Exception e) {
            System.err.println("Error converting object to JSON: " + e.getMessage());
            e.printStackTrace();
            return "null";
        }
    }

    private static String convertListToJson(List<?> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Object item = list.get(i);
            json.append(convertObjectToJson(item));
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
                .replace("\t", "\\t");
    }
}