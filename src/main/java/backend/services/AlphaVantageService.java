package backend.services;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;

public class AlphaVantageService {
    private final String API_KEY = System.getenv("ALPHA_VANTAGE_API_KEY"); // Get from https://www.alphavantage.co/support/#api-key
    private final String BASE_URL = "https://www.alphavantage.co/query";

    public Map<String, Object> getStockQuote(String symbol) {
        try {
            String urlString = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    BASE_URL, symbol, API_KEY);

            String response = makeApiCall(urlString);
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject globalQuote = jsonResponse.getJSONObject("Global Quote");

            return Map.of(
                    "symbol", globalQuote.getString("01. symbol"),
                    "name", getCompanyName(symbol),
                    "exchange", "NYSE",
                    "country", "United States",
                    "currency", "USD",
                    "mic", "XNYS",
                    "price", globalQuote.getString("05. price"),
                    "change", globalQuote.getString("09. change"),
                    "changePercent", globalQuote.getString("10. change percent")
            );
        } catch (Exception e) {
            throw new RuntimeException("Error fetching stock data: " + e.getMessage());
        }
    }

    public Map<String, Object> searchStocks(String keywords) {
        try {
            String urlString = String.format("%s?function=SYMBOL_SEARCH&keywords=%s&apikey=%s",
                    BASE_URL, keywords, API_KEY);

            String response = makeApiCall(urlString);
            JSONObject jsonResponse = new JSONObject(response);

            return jsonResponse.toMap();
        } catch (Exception e) {
            throw new RuntimeException("Error searching stocks: " + e.getMessage());
        }
    }

    private String makeApiCall(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HTTP Response Code: " + responseCode);
            }

            Scanner scanner = new Scanner(url.openStream());
            StringBuilder response = new StringBuilder();
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return response.toString();
        } catch (Exception e) {
            throw new RuntimeException("API call failed: " + e.getMessage());
        }
    }

    private String getCompanyName(String symbol) {
        Map<String, String> companyNames = Map.of(
                "JPM", "JP Morgan Chase & Co",
                "AAPL", "Apple Inc",
                "GOOGL", "Alphabet Inc",
                "MSFT", "Microsoft Corporation",
                "TSLA", "Tesla Inc"
        );
        return companyNames.getOrDefault(symbol, symbol + " Company");
    }
}