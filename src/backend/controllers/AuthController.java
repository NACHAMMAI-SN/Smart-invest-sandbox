package backend.controllers;

import backend.services.AuthService;
import backend.models.User;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;
import static spark.Spark.*;

public class AuthController {
    private AuthService authService;
    private Gson gson;

    public AuthController() {
        this.authService = new AuthService();
        this.gson = new Gson();
        setupRoutes();
    }

    private void setupRoutes() {
        // CORS headers
        options("/*", (request, response) -> {
            response.header("Access-Control-Allow-Headers", request.headers("Access-Control-Request-Headers"));
            response.header("Access-Control-Allow-Methods", request.headers("Access-Control-Request-Method"));
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Request-Method", "GET,POST,PUT,DELETE,OPTIONS");
            response.header("Access-Control-Allow-Headers", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
            response.type("application/json");
        });

        // Register user
        post("/api/register", (req, res) -> {
            try {
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String username = (String) body.get("username");
                String password = (String) body.get("password");
                String email = (String) body.get("email");

                if (username == null || password == null || email == null) {
                    res.status(400);
                    return "{\"success\":false,\"message\":\"Missing required fields\"}";
                }

                User newUser = authService.registerUser(username, password, email);
                if (newUser != null) {
                    // Remove password from response
                    Map<String, Object> userResponse = new HashMap<>();
                    userResponse.put("username", newUser.getUsername());
                    userResponse.put("email", newUser.getEmail());
                    userResponse.put("balance", newUser.getBalance());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Registration successful");
                    response.put("data", userResponse);
                    return gson.toJson(response);
                } else {
                    res.status(400);
                    return "{\"success\":false,\"message\":\"Registration failed - user may already exist\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"message\":\"Internal server error\"}";
            }
        });

        // Login user
        post("/api/login", (req, res) -> {
            try {
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String username = (String) body.get("username");
                String password = (String) body.get("password");

                if (username == null || password == null) {
                    res.status(400);
                    return "{\"success\":false,\"message\":\"Missing username or password\"}";
                }

                User user = authService.loginUser(username, password);
                if (user != null) {
                    // Remove password from response
                    Map<String, Object> userResponse = new HashMap<>();
                    userResponse.put("username", user.getUsername());
                    userResponse.put("email", user.getEmail());
                    userResponse.put("balance", user.getBalance());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "Login successful");
                    response.put("data", userResponse);
                    return gson.toJson(response);
                } else {
                    res.status(401);
                    return "{\"success\":false,\"message\":\"Invalid credentials\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"message\":\"Internal server error\"}";
            }
        });

        // Get user info
        get("/api/user", (req, res) -> {
            try {
                String username = req.queryParams("username");
                if (username == null) {
                    res.status(400);
                    return "{\"success\":false,\"message\":\"Username parameter required\"}";
                }

                User user = authService.getUser(username);
                if (user != null) {
                    // Remove password from response
                    Map<String, Object> userResponse = new HashMap<>();
                    userResponse.put("username", user.getUsername());
                    userResponse.put("email", user.getEmail());
                    userResponse.put("balance", user.getBalance());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
//                    response.put("message\", \"User found");
                    response.put("User found",true);
                    response.put("data", userResponse);
                    return gson.toJson(response);
                } else {
                    res.status(404);
                    return "{\"success\":false,\"message\":\"User not found\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"message\":\"Internal server error\"}";
            }
        });

        // Update user balance
        post("/api/user/balance", (req, res) -> {
            try {
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String username = (String) body.get("username");
                Double newBalance = (Double) body.get("balance");

                if (username == null || newBalance == null) {
                    res.status(400);
                    return "{\"success\":false,\"message\":\"Username and balance are required\"}";
                }

                boolean success = authService.updateUserBalance(username, newBalance);
                if (success) {
                    return "{\"success\":true,\"message\":\"Balance updated successfully\"}";
                } else {
                    res.status(400);
                    return "{\"success\":false,\"message\":\"Failed to update balance\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"message\":\"Internal server error\"}";
            }
        });

        // Health check
        get("/api/auth/health", (req, res) -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("service", "Authentication API");
            health.put("timestamp", System.currentTimeMillis());
            return gson.toJson(health);
        });
    }
}