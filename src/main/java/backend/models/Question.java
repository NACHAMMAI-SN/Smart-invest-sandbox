package backend.models;

import java.util.List;
import java.util.ArrayList;

public class Question {
    private String questionText;
    private List<String> options;
    private int correctAnswerIndex;
    private String explanation;
    private String questionType; // MULTIPLE_CHOICE, TRUE_FALSE, etc.

    public Question() {
        this.options = new ArrayList<>();
    }

    public Question(String questionText, List<String> options, int correctAnswerIndex) {
        this();
        this.questionText = questionText;
        this.options = options;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // Getters and Setters
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public List<String> getOptions() { if (this.options == null) {
        this.options = new ArrayList<>();
    }
        return this.options; }
    public void setOptions(List<String> options) {
        if (options == null) {
        this.options = new ArrayList<>();
    } else {
        this.options = options;
    } }

    public int getCorrectAnswerIndex() { return correctAnswerIndex; }
    public void setCorrectAnswerIndex(int correctAnswerIndex) { this.correctAnswerIndex = correctAnswerIndex; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public void addOption(String option) {
        if (this.options == null) this.options = new ArrayList<>();
        this.options.add(option);
    }

    public boolean isCorrectAnswer(int selectedIndex) {
        return selectedIndex == correctAnswerIndex;
    }
}