package backend.controllers;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

public class NewsController {
    private final Gson gson = new Gson();
    private final String NEWS_API_KEY = System.getenv("NEWS_API_KEY");

    public NewsController() {
        // Get market news
        get("/api/news/market", (req, res) -> {
            res.type("application/json");
            try {
                String urlString = "https://newsapi.org/v2/everything?q=stock+market+OR+investing+OR+trading&sortBy=publishedAt&language=en&pageSize=10&apiKey=" + NEWS_API_KEY;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    return gson.toJson(Map.of("articles", new Object[0], "error", "API returned response code: " + responseCode));
                }

                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                return response.toString();
            } catch (Exception e) {
                return gson.toJson(Map.of("articles", new Object[0], "error", "Failed to fetch news: " + e.getMessage()));
            }
        });

        // Search news
        get("/api/news/search", (req, res) -> {
            res.type("application/json");
            try {
                String query = req.queryParams("query");
                if (query == null) query = "stock market";

                String urlString = "https://newsapi.org/v2/everything?q=" + query + "&sortBy=publishedAt&language=en&pageSize=15&apiKey=" + NEWS_API_KEY;

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                Scanner scanner = new Scanner(url.openStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                return response.toString();
            } catch (Exception e) {
                return gson.toJson(Map.of("articles", new Object[0], "error", "Failed to fetch news: " + e.getMessage()));
            }
        });
    }
}