package backend.controllers;

import backend.services.TutorialService;
import backend.models.TutorialSection;
import backend.models.Quiz;
import backend.models.Exercise;
import com.google.gson.Gson;

import java.util.*;
import static spark.Spark.*;

public class TutorialController {
    private TutorialService tutorialService;
    private Gson gson;

    public TutorialController() {
        this.tutorialService = new TutorialService();
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

        // Get all tutorials
        get("/api/tutorials", (req, res) -> {
            try {
                List<TutorialSection> tutorials = tutorialService.getAllTutorials();
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", tutorials);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch tutorials\"}";
            }
        });

        // Get tutorials by level
        get("/api/tutorials/level/:level", (req, res) -> {
            try {
                String level = req.params(":level");
                List<TutorialSection> tutorials = tutorialService.getTutorialsByLevel(level);
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", tutorials);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch tutorials by level\"}";
            }
        });

        // Get tutorials by category
        get("/api/tutorials/category/:category", (req, res) -> {
            try {
                String category = req.params(":category");
                List<TutorialSection> tutorials = tutorialService.getTutorialsByCategory(category);
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", tutorials);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch tutorials by category\"}";
            }
        });

        // Search tutorials
        get("/api/tutorials/search", (req, res) -> {
            try {
                String query = req.queryParams("q");
                List<TutorialSection> tutorials = tutorialService.searchTutorials(query);
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", tutorials);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to search tutorials\"}";
            }
        });
        post("/api/tutorials/:id/validate", (req, res) -> {
            try {
                String tutorialId = req.params(":id");
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String userAnswer = (String) body.get("answer");
                String username = (String) body.get("username");

                if (userAnswer == null || userAnswer.trim().isEmpty()) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Answer is required\"}";
                }

                boolean isCorrect = tutorialService.validateExercise(tutorialId, userAnswer);

                // Update progress if answer is correct
                if (isCorrect && username != null) {
                    tutorialService.updateUserProgress(username, tutorialId, true);
                }

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("correct", isCorrect);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to validate exercise\"}";
            }
        });
        // Get specific tutorial
        get("/api/tutorials/:id", (req, res) -> {
            try {
                String tutorialId = req.params(":id");
                TutorialSection tutorial = tutorialService.getTutorial(tutorialId);
                if (tutorial != null) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("success", true);
                    responseMap.put("data", tutorial);
                    return gson.toJson(responseMap);
                } else {
                    res.status(404);
                    return "{\"success\":false,\"error\":\"Tutorial not found\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch tutorial\"}";
            }
        });

        // Get tutorial quiz
        get("/api/tutorials/:id/quiz", (req, res) -> {
            try {
                String tutorialId = req.params(":id");
                Quiz quiz = tutorialService.getQuiz(tutorialId);
                if (quiz != null) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("success", true);
                    responseMap.put("data", quiz);
                    return gson.toJson(responseMap);
                } else {
                    res.status(404);
                    return "{\"success\":false,\"error\":\"Quiz not found for this tutorial\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch quiz\"}";
            }
        });

        // Get tutorial exercise
        get("/api/tutorials/:id/exercise", (req, res) -> {
            try {
                String tutorialId = req.params(":id");
                Exercise exercise = tutorialService.getExercise(tutorialId);
                if (exercise != null) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("success", true);
                    responseMap.put("data", exercise);
                    return gson.toJson(responseMap);
                } else {
                    res.status(404);
                    return "{\"success\":false,\"error\":\"Exercise not found for this tutorial\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch exercise\"}";
            }
        });

        // Validate exercise


        // Submit quiz
        post("/api/tutorials/:id/quiz", (req, res) -> {
            try {
                String tutorialId = req.params(":id");
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String username = (String) body.get("username");
                String answersJson = gson.toJson(body.get("answers"));

                if (username == null || body.get("answers") == null) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Username and answers are required\"}";
                }

                Map<String, Object> result = tutorialService.submitQuiz(tutorialId, username, answersJson);
                if (result != null) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("success", true);
                    responseMap.put("data", result);
                    return gson.toJson(responseMap);
                } else {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Failed to submit quiz\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to submit quiz\"}";
            }
        });

        // Get user progress - FIXED: Use query parameters instead of path parameters
        get("/api/tutorials/progress", (req, res) -> {
            try {
                String username = req.queryParams("username");
                System.out.println("Fetching progress for user: " + username);

                if (username == null || username.trim().isEmpty()) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Username parameter is required\"}";
                }

                Map<String, Boolean> progress = tutorialService.getUserProgress(username);
                Map<String, Object> stats = tutorialService.getUserStatistics(username);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", progress);
                responseMap.put("statistics", stats);

                System.out.println("Progress data for " + username + ": " + progress.size() + " tutorials");
                return gson.toJson(responseMap);
            } catch (Exception e) {
                System.err.println("Error in getUserProgress: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch user progress: " + e.getMessage() + "\"}";
            }
        });

        // Get detailed tutorial progress - FIXED: Use query parameters
        get("/api/tutorials/progress/tutorial", (req, res) -> {
            try {
                String username = req.queryParams("username");
                String tutorialId = req.queryParams("tutorialId");

                System.out.println("Fetching progress for user: " + username + ", tutorial: " + tutorialId);

                if (username == null || tutorialId == null) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Username and tutorialId parameters are required\"}";
                }

                Map<String, Object> progress = tutorialService.getTutorialProgress(username, tutorialId);
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", progress);

                System.out.println("Tutorial progress: " + progress);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                System.err.println("Error in getTutorialProgress: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch tutorial progress: " + e.getMessage() + "\"}";
            }
        });

        // Update user progress
        post("/api/tutorials/progress", (req, res) -> {
            try {
                Map<String, Object> body = gson.fromJson(req.body(), Map.class);
                String username = (String) body.get("username");
                String tutorialId = (String) body.get("tutorialId");
                Boolean completed = (Boolean) body.get("completed");

                if (username == null || tutorialId == null || completed == null) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Username, tutorialId, and completed are required\"}";
                }

                boolean success = tutorialService.updateUserProgress(username, tutorialId, completed);
                if (success) {
                    return "{\"success\":true,\"message\":\"Progress updated successfully\"}";
                } else {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Failed to update progress\"}";
                }
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to update progress\"}";
            }
        });

        // Mark tutorial as complete - CORRECTED VERSION
// Mark tutorial as complete - DEBUG VERSION
        post("/api/tutorials/progress/complete", (req, res) -> {
            try {
                String bodyStr = req.body();
                System.out.println("=== MARK COMPLETE DEBUG ===");
                System.out.println("Raw request body: " + bodyStr);
                System.out.println("Content-Type: " + req.contentType());
                System.out.println("Request headers: " + req.headers());

                if (bodyStr == null || bodyStr.isEmpty()) {
                    System.out.println("❌ Empty request body");
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Empty request body\"}";
                }

                Map<String, Object> body = gson.fromJson(bodyStr, Map.class);
                System.out.println("Parsed body: " + body);

                String username = (String) body.get("username");
                String tutorialId = (String) body.get("tutorialId");

                System.out.println("Username: '" + username + "'");
                System.out.println("Tutorial ID: '" + tutorialId + "'");
                System.out.println("Username is null: " + (username == null));
                System.out.println("Username is empty: " + (username != null && username.trim().isEmpty()));
                System.out.println("TutorialId is null: " + (tutorialId == null));
                System.out.println("TutorialId is empty: " + (tutorialId != null && tutorialId.trim().isEmpty()));

                if (username == null || username.trim().isEmpty() || tutorialId == null || tutorialId.trim().isEmpty()) {
                    System.out.println("❌ Missing required fields");
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Missing required fields: username='" + username + "', tutorialId='" + tutorialId + "'\"}";
                }

                System.out.println("✅ Calling updateUserProgress...");
                boolean success = tutorialService.updateUserProgress(username, tutorialId, true);
                System.out.println("Service result: " + success);

                if (success) {
                    System.out.println("✅ Tutorial marked as complete successfully");
                    return "{\"success\":true,\"message\":\"Tutorial marked as complete\"}";
                } else {
                    System.out.println("❌ Service returned false");
                    return "{\"success\":false,\"error\":\"Failed to mark tutorial complete in database\"}";
                }
            } catch (Exception e) {
                System.err.println("❌ Error in markTutorialComplete: " + e.getMessage());
                e.printStackTrace();
                res.status(500);
                return "{\"success\":false,\"error\":\"Internal server error: " + e.getMessage() + "\"}";
            }
        });


        // Get recommended tutorials - FIXED: Use query parameters
        get("/api/tutorials/recommended", (req, res) -> {
            try {
                String username = req.queryParams("username");
                if (username == null || username.trim().isEmpty()) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Username parameter is required\"}";
                }

                List<TutorialSection> recommendations = tutorialService.getRecommendedTutorials(username);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", recommendations);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch recommended tutorials\"}";
            }
        });

        // Get user statistics - FIXED: Use query parameters
        get("/api/tutorials/stats", (req, res) -> {
            try {
                String username = req.queryParams("username");
                if (username == null || username.trim().isEmpty()) {
                    res.status(400);
                    return "{\"success\":false,\"error\":\"Username parameter is required\"}";
                }

                Map<String, Object> stats = tutorialService.getUserStatistics(username);

                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("success", true);
                responseMap.put("data", stats);
                return gson.toJson(responseMap);
            } catch (Exception e) {
                res.status(500);
                return "{\"success\":false,\"error\":\"Failed to fetch user statistics\"}";
            }
        });

        // Health check
        get("/api/tutorials/health", (req, res) -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "OK");
            health.put("service", "Stock Market Tutorial API");
            health.put("timestamp", System.currentTimeMillis());
            health.put("totalTutorials", tutorialService.getAllTutorials().size());
            return gson.toJson(health);
        });
    }
}