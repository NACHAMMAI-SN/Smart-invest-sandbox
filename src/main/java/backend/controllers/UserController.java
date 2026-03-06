package backend.controllers;

import backend.database.DatabaseHandler;
import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.Map;

public class UserController {
    private final Gson gson = new Gson();

    public UserController(DatabaseHandler dbHandler) {
        // Get user data
        get("/api/users/:username", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                return gson.toJson(dbHandler.getUserByUsername(username));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("error", "Failed to fetch user: " + e.getMessage()));
            }
        });

        // Update user balance
        put("/api/users/:username/balance", (req, res) -> {
            res.type("application/json");
            try {
                String username = req.params(":username");
                Map<String, Object> requestData = gson.fromJson(req.body(), Map.class);
                double newBalance = (Double) requestData.get("newBalance");

                boolean success = dbHandler.updateUserBalance(username, newBalance);
                return gson.toJson(Map.of("success", success));
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(Map.of("success", false, "error", e.getMessage()));
            }
        });
    }
}