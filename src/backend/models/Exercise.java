package backend.models;

import java.util.Map;
import java.util.HashMap;

public class Exercise {
    private String question;
    private String answer;
    private String hint;
    private String type; // CALCULATION, ANALYSIS, TRADING_SCENARIO
    private Map<String, Object> simulationData;

    public Exercise() {
        this.simulationData = new HashMap<>();
    }

    public Exercise(String question, String answer, String type) {
        this();
        this.question = question;
        this.answer = answer;
        this.type = type;
    }

    // Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getHint() { return hint; }
    public void setHint(String hint) { this.hint = hint; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getSimulationData() { return simulationData; }
    public void setSimulationData(Map<String, Object> simulationData) { this.simulationData = simulationData; }

    public void addSimulationData(String key, Object value) {
        if (this.simulationData == null) this.simulationData = new HashMap<>();
        this.simulationData.put(key, value);
    }

    public boolean validateAnswer(String userAnswer) {
        if (answer == null || userAnswer == null) return false;
        return answer.trim().equalsIgnoreCase(userAnswer.trim());
    }
}