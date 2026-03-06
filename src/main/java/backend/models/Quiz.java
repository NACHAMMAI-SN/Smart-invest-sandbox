package backend.models;

import java.util.List;
import java.util.ArrayList;

public class Quiz {
    private String id;
    private List<Question> questions;
    private int passingScore;
    private int timeLimit; // in minutes
    private boolean allowRetakes;

    public Quiz() {
        this.questions = new ArrayList<>();
    }

    public Quiz(String id, int passingScore, int timeLimit) {
        this();
        this.id = id;
        this.passingScore = passingScore;
        this.timeLimit = timeLimit;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Question> getQuestions() {
        if (this.questions == null) {
            this.questions = new ArrayList<>();
        }
        return this.questions;

    }
    public void setQuestions(List<Question> questions) {
        if (questions == null) {
            this.questions = new ArrayList<>();
        } else {
            this.questions = questions;
        }
    }

    public int getPassingScore() { return passingScore; }
    public void setPassingScore(int passingScore) { this.passingScore = passingScore; }

    public int getTimeLimit() { return timeLimit; }
    public void setTimeLimit(int timeLimit) { this.timeLimit = timeLimit; }

    public boolean isAllowRetakes() { return allowRetakes; }
    public void setAllowRetakes(boolean allowRetakes) { this.allowRetakes = allowRetakes; }

    public void addQuestion(Question question) {
        if (this.questions == null) this.questions = new ArrayList<>();
        this.questions.add(question);
    }

    public int getTotalQuestions() {
        return questions != null ? questions.size() : 0;
    }
}