package backend.models;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class TutorialSection {
    private String id;
    private String title;
    private String description;
    private String level; // BEGINNER, INTERMEDIATE, ADVANCED
    private String category;
    private int estimatedMinutes;
    private double completionRate;

    private String videoUrl;
    private String content;
    private List<String> infographics;
    private List<String> keyPoints;
    private Map<String, String> glossary;

    private Quiz quiz;
    private Exercise exercise;
    private String simulatorData;
    private List<CaseStudy> caseStudies;

    private boolean hasVideo;
    private boolean hasSimulator;
    private boolean hasQuiz;
    private boolean hasExercise;
    private List<String> prerequisites;
    private List<String> nextTutorials;
    private String certificationId;

    // Constructors
    public TutorialSection() {
        this.infographics = new ArrayList<>();
        this.keyPoints = new ArrayList<>();
        this.glossary = new HashMap<>();
        this.caseStudies = new ArrayList<>();
        this.prerequisites = new ArrayList<>();
        this.nextTutorials = new ArrayList<>();
    }

    public TutorialSection(String id, String title, String description, String level, String category) {
        this();
        this.id = id;
        this.title = title;
        this.description = description;
        this.level = level;
        this.category = category;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public double getCompletionRate() { return completionRate; }
    public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<String> getInfographics() { return infographics; }
    public void setInfographics(List<String> infographics) { this.infographics = infographics; }

    public List<String> getKeyPoints() { return keyPoints; }
    public void setKeyPoints(List<String> keyPoints) { this.keyPoints = keyPoints; }

    public Map<String, String> getGlossary() { return glossary; }
    public void setGlossary(Map<String, String> glossary) { this.glossary = glossary; }

    public Quiz getQuiz() { return quiz; }
    public void setQuiz(Quiz quiz) { this.quiz = quiz; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public String getSimulatorData() { return simulatorData; }
    public void setSimulatorData(String simulatorData) { this.simulatorData = simulatorData; }

    public List<CaseStudy> getCaseStudies() { return caseStudies; }
    public void setCaseStudies(List<CaseStudy> caseStudies) { this.caseStudies = caseStudies; }

    public boolean isHasVideo() { return hasVideo; }
    public void setHasVideo(boolean hasVideo) { this.hasVideo = hasVideo; }

    public boolean isHasExercise(){return hasExercise;}
    public void setHasExercise(boolean hasExercise) { this.hasExercise = hasExercise; }
    public boolean isHasSimulator() { return hasSimulator; }
    public void setHasSimulator(boolean hasSimulator) { this.hasSimulator = hasSimulator; }

    public boolean isHasQuiz() { return hasQuiz; }
    public void setHasQuiz(boolean hasQuiz) { this.hasQuiz = hasQuiz; }

    public List<String> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(List<String> prerequisites) { this.prerequisites = prerequisites; }

    public List<String> getNextTutorials() { return nextTutorials; }
    public void setNextTutorials(List<String> nextTutorials) { this.nextTutorials = nextTutorials; }

    public String getCertificationId() { return certificationId; }
    public void setCertificationId(String certificationId) { this.certificationId = certificationId; }

    // Helper methods
    public void addKeyPoint(String keyPoint) {
        if (this.keyPoints == null) this.keyPoints = new ArrayList<>();
        this.keyPoints.add(keyPoint);
    }

    public void addInfographic(String infographicUrl) {
        if (this.infographics == null) this.infographics = new ArrayList<>();
        this.infographics.add(infographicUrl);
    }

    public void addGlossaryTerm(String term, String definition) {
        if (this.glossary == null) this.glossary = new HashMap<>();
        this.glossary.put(term, definition);
    }

    public void addPrerequisite(String tutorialId) {
        if (this.prerequisites == null) this.prerequisites = new ArrayList<>();
        this.prerequisites.add(tutorialId);
    }

    public void addNextTutorial(String tutorialId) {
        if (this.nextTutorials == null) this.nextTutorials = new ArrayList<>();
        this.nextTutorials.add(tutorialId);
    }

    public void addCaseStudy(CaseStudy caseStudy) {
        if (this.caseStudies == null) this.caseStudies = new ArrayList<>();
        this.caseStudies.add(caseStudy);
    }
}