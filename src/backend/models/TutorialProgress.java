package backend.models;

import java.util.Date;

public class TutorialProgress {
    private String userId;
    private String tutorialId;
    private boolean completed;
    private Date completedAt;
    private int score;

    public TutorialProgress(String userId, String tutorialId) {
        this.userId = userId;
        this.tutorialId = tutorialId;
        this.completed = false;
        this.score = 0;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTutorialId() { return tutorialId; }
    public void setTutorialId(String tutorialId) { this.tutorialId = tutorialId; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}