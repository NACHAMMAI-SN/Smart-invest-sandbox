package backend.models;

import java.util.Date;

public class UserProgress {
    private Long id;
    private String userId;
    private String tutorialId;
    private boolean completed;
    private Date startedAt;
    private Date completedAt;
    private int score;
    private int timeSpent; // in minutes
    private int quizScore;
    private boolean quizPassed;
    private Date createdAt;
    private Date updatedAt;

    public UserProgress() {}

    public UserProgress(String userId, String tutorialId) {
        this.userId = userId;
        this.tutorialId = tutorialId;
        this.startedAt = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTutorialId() { return tutorialId; }
    public void setTutorialId(String tutorialId) { this.tutorialId = tutorialId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Date getStartedAt() { return startedAt; }
    public void setStartedAt(Date startedAt) { this.startedAt = startedAt; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTimeSpent() { return timeSpent; }
    public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }

    public int getQuizScore() { return quizScore; }
    public void setQuizScore(int quizScore) { this.quizScore = quizScore; }

    public boolean isQuizPassed() { return quizPassed; }
    public void setQuizPassed(boolean quizPassed) { this.quizPassed = quizPassed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Date updatedAt) { this.updatedAt = updatedAt; }
}