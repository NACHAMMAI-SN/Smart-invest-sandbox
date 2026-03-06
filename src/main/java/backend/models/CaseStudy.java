package backend.models;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class CaseStudy {
    private String title;
    private String description;
    private String company;
    private String timeframe;
    private List<String> learningObjectives;
    private Map<String, Object> data;
    private String analysis;

    public CaseStudy() {
        this.learningObjectives = new ArrayList<>();
        this.data = new HashMap<>();
    }

    public CaseStudy(String title, String description, String company) {
        this();
        this.title = title;
        this.description = description;
        this.company = company;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getTimeframe() { return timeframe; }
    public void setTimeframe(String timeframe) { this.timeframe = timeframe; }

    public List<String> getLearningObjectives() { return learningObjectives; }
    public void setLearningObjectives(List<String> learningObjectives) { this.learningObjectives = learningObjectives; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }

    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }

    public void addLearningObjective(String objective) {
        if (this.learningObjectives == null) this.learningObjectives = new ArrayList<>();
        this.learningObjectives.add(objective);
    }

    public void addData(String key, Object value) {
        if (this.data == null) this.data = new HashMap<>();
        this.data.put(key, value);
    }
}