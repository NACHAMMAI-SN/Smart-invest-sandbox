package backend.services;

import backend.models.TutorialSection;
import backend.models.Quiz;
import backend.models.Question;
import backend.models.Exercise;
import backend.models.CaseStudy;
import backend.database.TutorialDatabaseHandler;
import backend.models.data.QuizData;

import java.util.*;
import java.util.stream.Collectors;

public class TutorialService {
    private Map<String, TutorialSection> tutorials = new HashMap<>();
    private TutorialDatabaseHandler dbHandler;

    public TutorialService() {
        this.dbHandler = new TutorialDatabaseHandler();
        initializeStockMarketTutorials();
    }

    // Basic tutorial operations
    public List<TutorialSection> getAllTutorials() {
        return new ArrayList<>(tutorials.values());
//        List<TutorialSection> allTutorials = new ArrayList<>(tutorials.values());
//
//        for (TutorialSection tutorial : allTutorials) {
//            if (tutorial.isHasQuiz() && tutorial.getQuiz() == null) {
//                System.out.println("⚠️  Tutorial " + tutorial.getId() + " has hasQuiz=true but no quiz object!");
//                // You might want to auto-generate a default quiz here
//            }
//        }

//        return allTutorials;
    }

    public TutorialSection getTutorial(String tutorialId) {
        return tutorials.get(tutorialId);
    }

    // Search and filter methods
    public List<TutorialSection> searchTutorials(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllTutorials();
        }

        String lowerSearchTerm = searchTerm.toLowerCase();
        return tutorials.values().stream()
                .filter(tutorial ->
                        tutorial.getTitle().toLowerCase().contains(lowerSearchTerm) ||
                                tutorial.getDescription().toLowerCase().contains(lowerSearchTerm) ||
                                tutorial.getContent().toLowerCase().contains(lowerSearchTerm) ||
                                (tutorial.getKeyPoints() != null && tutorial.getKeyPoints().stream()
                                        .anyMatch(point -> point.toLowerCase().contains(lowerSearchTerm)))
                )
                .collect(Collectors.toList());
    }

    public List<TutorialSection> getTutorialsByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return getAllTutorials();
        }

        String lowerCategory = category.toLowerCase();
        return tutorials.values().stream()
                .filter(tutorial -> tutorial.getCategory() != null &&
                        tutorial.getCategory().toLowerCase().contains(lowerCategory))
                .collect(Collectors.toList());
    }

    public List<TutorialSection> getTutorialsByLevel(String level) {
        if (level == null || level.trim().isEmpty()) {
            return getAllTutorials();
        }

        String upperLevel = level.toUpperCase();
        return tutorials.values().stream()
                .filter(tutorial -> tutorial.getLevel() != null &&
                        tutorial.getLevel().toUpperCase().equals(upperLevel))
                .collect(Collectors.toList());
    }

    // Exercise validation
    public boolean validateExercise(String tutorialId, String userAnswer) {
        TutorialSection tutorial = getTutorial(tutorialId);
        if (tutorial == null || tutorial.getExercise() == null) {
            return false;
        }

        Exercise exercise = tutorial.getExercise();
        return exercise.validateAnswer(userAnswer);
    }

    // Quiz operations
    public Quiz getQuiz(String tutorialId) {
        TutorialSection tutorial = getTutorial(tutorialId);
        return tutorial != null ? tutorial.getQuiz() : null;
    }

    public Map<String, Object> submitQuiz(String tutorialId, String username, String answersJson) {
        TutorialSection tutorial = getTutorial(tutorialId);
        if (tutorial == null || tutorial.getQuiz() == null) {
            return null;
        }

        Quiz quiz = tutorial.getQuiz();
        List<Question> questions = quiz.getQuestions();

        // Parse answers
        Map<Integer, Integer> userAnswers = parseAnswers(answersJson);

        int correctAnswers = 0;
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Integer userAnswer = userAnswers.get(i);
            if (userAnswer != null && question.isCorrectAnswer(userAnswer)) {
                correctAnswers++;
            }
        }

        double score = (double) correctAnswers / questions.size() * 100;
        boolean passed = score >= quiz.getPassingScore();
        int intScore = (int) score;

        // ✅ SAVE QUIZ RESULT TO DATABASE
        boolean saveSuccess = dbHandler.updateQuizResult(username, tutorialId, intScore, passed);

        System.out.println("=== QUIZ RESULT SAVED TO DATABASE ===");
        System.out.println("User: " + username);
        System.out.println("Tutorial: " + tutorialId);
        System.out.println("Score: " + intScore + "%");
        System.out.println("Passed: " + passed);
        System.out.println("Save successful: " + saveSuccess);
        System.out.println("=====================================");

        // Award badges based on performance
        if (passed) {
            if (intScore >= 90) {
                dbHandler.addUserBadge(username, "Quiz Master");
            }
            if (intScore == 100) {
                dbHandler.addUserBadge(username, "Perfect Score");
            }

            // Update progress if passed
            updateUserProgress(username, tutorialId, true);

            // Check for certification eligibility
            checkCertificationEligibility(username, tutorialId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("score", intScore);
        result.put("passed", passed);
        result.put("correctAnswers", correctAnswers);
        result.put("totalQuestions", questions.size());
        result.put("passingScore", quiz.getPassingScore());

        // Add detailed results for review
        List<Map<String, Object>> detailedResults = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Map<String, Object> questionResult = new HashMap<>();
            questionResult.put("correct", userAnswers.get(i) != null && question.isCorrectAnswer(userAnswers.get(i)));
            questionResult.put("explanation", question.getExplanation());
            questionResult.put("userAnswer", userAnswers.get(i));
            questionResult.put("correctAnswer", question.getCorrectAnswerIndex());
            detailedResults.add(questionResult);
        }
        result.put("detailedResults", detailedResults);

        return result;
    }

    // Exercise operations
    public Exercise getExercise(String tutorialId) {
        TutorialSection tutorial = getTutorial(tutorialId);
        return tutorial != null ? tutorial.getExercise() : null;
    }

    // User progress tracking - FIXED VERSION
    // FIXME
    public boolean updateUserProgress(String username, String tutorialId, boolean completed) {
        System.out.println("=== UPDATE USER PROGRESS DEBUG ===");
        System.out.println("Username: " + username);
        System.out.println("Tutorial ID: '" + tutorialId + "'");
        System.out.println("Available tutorials: " + tutorials.keySet());

        // Basic validation
        if (username == null || username.trim().isEmpty() || tutorialId == null || tutorialId.trim().isEmpty()) {
            System.out.println("❌ Missing username or tutorialId");
            return false;
        }

        // Check if tutorial exists (more flexible validation)
        boolean tutorialExists = tutorials.containsKey(tutorialId);
        System.out.println("Tutorial exists in map: " + tutorialExists);

        if (!tutorialExists) {
            // Try case-insensitive search
            tutorialExists = tutorials.keySet().stream()
                    .anyMatch(key -> key.equalsIgnoreCase(tutorialId));
            System.out.println("Tutorial exists (case-insensitive): " + tutorialExists);

            if (!tutorialExists) {
                System.out.println("❌ Tutorial not found: " + tutorialId);
                // For now, let's proceed anyway to test the database flow
                System.out.println("⚠️  Proceeding with database update despite tutorial not found in map");
            }
        }

        // Use database to mark tutorial complete
        System.out.println("Calling database handler...");
        boolean success = dbHandler.markTutorialComplete(username, tutorialId);

        System.out.println("Database update successful: " + success);

        if (success && completed) {
            // Award completion badge
            dbHandler.addUserBadge(username, "Course Completer");

            // Check for level completion badges
            checkLevelCompletion(username);
        }

        System.out.println("=== PROGRESS UPDATE COMPLETE ===");
        System.out.println("Final result: " + success);
        System.out.println("===================================");

        return success;
    }

    public Map<String, Boolean> getUserProgress(String username) {
        // Get progress from database
        List<backend.models.UserProgress> progressList = dbHandler.getAllUserProgress(username);
        Map<String, Boolean> progressMap = new HashMap<>();

        for (backend.models.UserProgress progress : progressList) {
            progressMap.put(progress.getTutorialId(), progress.isCompleted());
        }

        return progressMap;
    }

    // NEW METHOD: Get quiz score for a specific user and tutorial
    public Integer getQuizScore(String username, String tutorialId) {
        backend.models.UserProgress progress = dbHandler.getUserProgress(username, tutorialId);
        return progress != null ? progress.getQuizScore() : null;
    }

    // NEW METHOD: Get detailed tutorial progress including quiz scores
    public Map<String, Object> getTutorialProgress(String username, String tutorialId) {
        backend.models.UserProgress progress = dbHandler.getUserProgress(username, tutorialId);
        Map<String, Object> progressInfo = new HashMap<>();

        if (progress != null) {
            progressInfo.put("completed", progress.isCompleted());
            progressInfo.put("score", progress.getScore());
            progressInfo.put("quizScore", progress.getQuizScore());
            progressInfo.put("quizPassed", progress.isQuizPassed());
            progressInfo.put("timeSpent", progress.getTimeSpent());
        } else {
            progressInfo.put("completed", false);
            progressInfo.put("score", 0);
            progressInfo.put("quizScore", 0);
            progressInfo.put("quizPassed", false);
            progressInfo.put("timeSpent", 0);
        }

        return progressInfo;
    }


    private Map<Integer, Integer> parseAnswers(String answersJson) {
        Map<Integer, Integer> answers = new HashMap<>();
        try {
            System.out.println("=== PARSING ANSWERS ===");
            System.out.println("Input answersJson: " + answersJson);

            String unescapedJson = answersJson;
            if (answersJson.contains("\\\"")) {
                unescapedJson = answersJson.replace("\\\"", "\"");
                System.out.println("After unescaping: " + unescapedJson);
            }

            if (unescapedJson.startsWith("{") && unescapedJson.endsWith("}")) {
                String content = unescapedJson.substring(1, unescapedJson.length() - 1).trim();
                System.out.println("Content without braces: " + content);

                if (content.isEmpty()) {
                    return answers;
                }

                String[] pairs = content.split(",");
                System.out.println("Found " + pairs.length + " pairs");

                for (String pair : pairs) {
                    System.out.println("Processing pair: '" + pair + "'");

                    int colonIndex = pair.indexOf(':');
                    if (colonIndex > 0) {
                        String keyStr = pair.substring(0, colonIndex).trim();
                        String valueStr = pair.substring(colonIndex + 1).trim();

                        keyStr = keyStr.replace("\"", "");
                        valueStr = valueStr.replace("\"", "");

                        System.out.println("Key: '" + keyStr + "', Value: '" + valueStr + "'");

                        try {
                            int questionIndex = Integer.parseInt(keyStr);
                            int answerIndex = Integer.parseInt(valueStr);
                            answers.put(questionIndex, answerIndex);
                            System.out.println("Added: Q" + questionIndex + " = " + answerIndex);
                        } catch (NumberFormatException e) {
                            System.err.println("Number format error in pair: " + pair);
                        }
                    } else {
                        System.err.println("No colon found in pair: " + pair);
                    }
                }
            } else {
                System.err.println("Answers JSON doesn't start/end with braces: " + unescapedJson);
            }

            System.out.println("Final answers map: " + answers);
            System.out.println("Total answers: " + answers.size());

        } catch (Exception e) {
            System.err.println("Error parsing answers: " + e.getMessage());
            e.printStackTrace();
        }
        return answers;
    }

    private void checkLevelCompletion(String username) {
        int beginnerCompleted = (int) tutorials.values().stream()
                .filter(t -> "BEGINNER".equals(t.getLevel()))
                .filter(t -> {
                    Map<String, Boolean> progress = getUserProgress(username);
                    return progress.getOrDefault(t.getId(), false);
                })
                .count();

        int intermediateCompleted = (int) tutorials.values().stream()
                .filter(t -> "INTERMEDIATE".equals(t.getLevel()))
                .filter(t -> {
                    Map<String, Boolean> progress = getUserProgress(username);
                    return progress.getOrDefault(t.getId(), false);
                })
                .count();

        int advancedCompleted = (int) tutorials.values().stream()
                .filter(t -> "ADVANCED".equals(t.getLevel()))
                .filter(t -> {
                    Map<String, Boolean> progress = getUserProgress(username);
                    return progress.getOrDefault(t.getId(), false);
                })
                .count();

        // Award level completion badges
        if (beginnerCompleted >= 3) {
            dbHandler.addUserBadge(username, "Beginner Trader");
        }
        if (intermediateCompleted >= 3) {
            dbHandler.addUserBadge(username, "Intermediate Investor");
        }
        if (advancedCompleted >= 2) {
            dbHandler.addUserBadge(username, "Advanced Analyst");
        }
    }

    private void checkCertificationEligibility(String username, String tutorialId) {
        // Check if user qualifies for any certifications
        Map<String, Boolean> progress = getUserProgress(username);
        long completedCount = progress.values().stream().filter(Boolean::booleanValue).count();

        if (completedCount >= 5) {
            dbHandler.addUserCertification(username, "stock_market_fundamentals", 85);
            dbHandler.addUserBadge(username, "Certified Investor");
        }
    }

    // Stock Market Tutorials Initialization
    private void initializeStockMarketTutorials() {
        // BEGINNER LEVEL TUTORIALS

        // Tutorial 1: Stock Market Fundamentals
        TutorialSection fundamentals = new TutorialSection(
                "stock-fundamentals",  // This should match what frontend sends
                "Stock Market Fundamentals",
                "Learn the basic concepts of stock market investing and how markets work",
                "BEGINNER",
                "FOUNDATION"
        );
        fundamentals.setContent("<h3>Welcome to Stock Market Investing</h3>" +
                "<p>The stock market is where investors buy and sell shares of publicly traded companies. " +
                "Understanding how it works is the first step toward building wealth through investing.</p>" +
                "<h4>Key Concepts:</h4>" +
                "<ul>" +
                "<li><strong>Stocks:</strong> Represent ownership in a company</li>" +
                "<li><strong>Bonds:</strong> Debt instruments issued by companies or governments</li>" +
                "<li><strong>Market Index:</strong> Measures the performance of a group of stocks</li>" +
                "<li><strong>Bull Market:</strong> Period of rising stock prices</li>" +
                "<li><strong>Bear Market:</strong> Period of falling stock prices</li>" +
                "</ul>");
        fundamentals.setEstimatedMinutes(45);
        fundamentals.setVideoUrl("https://www.youtube.com/embed/GcZW24SkbHM?si=s2Av7IIVAffAuRyG");
        fundamentals.setHasVideo(true);
        fundamentals.setQuiz(QuizData.getStockFundamentalsQuiz());
        fundamentals.setHasQuiz(true);
        fundamentals.setCompletionRate(85.5);

        // Add exercise for fundamentals
        Exercise fundamentalsExercise = new Exercise();
        fundamentalsExercise.setQuestion("Calculate the market capitalization: A company has 1 million shares outstanding trading at $50 per share.");
        fundamentalsExercise.setAnswer("50000000");
        fundamentalsExercise.setHint("Market Cap = Shares Outstanding × Price Per Share");
        fundamentalsExercise.setType("CALCULATION");
        fundamentals.setExercise(fundamentalsExercise);
        // Add key points
        fundamentals.addKeyPoint("Stocks represent ownership in companies");
        fundamentals.addKeyPoint("Market capitalization measures company size");
        fundamentals.addKeyPoint("Bull markets rise, bear markets fall");
        fundamentals.addKeyPoint("Diversification reduces risk");

        // Add glossary
        fundamentals.addGlossaryTerm("Stock", "A type of security that signifies ownership in a corporation");
        fundamentals.addGlossaryTerm("Dividend", "A portion of a company's earnings paid to shareholders");
        fundamentals.addGlossaryTerm("IPO", "Initial Public Offering - when a company first sells shares to the public");

        tutorials.put(fundamentals.getId(), fundamentals);

        // Tutorial 2: Reading Stock Charts
        TutorialSection chartReading = new TutorialSection(
                "chart-reading",
                "Reading Stock Charts & Technical Analysis",
                "Learn how to interpret stock charts and identify trading patterns",
                "BEGINNER",
                "TECHNICAL_ANALYSIS"
        );
        chartReading.setContent("<h3>Understanding Stock Charts</h3>" +
                "<p>Stock charts provide visual representations of price movements over time. " +
                "Learning to read them is essential for technical analysis.</p>" +
                "<h4>Chart Types:</h4>" +
                "<ul>" +
                "<li><strong>Line Charts:</strong> Simple price movement over time</li>" +
                "<li><strong>Bar Charts:</strong> Show open, high, low, and close prices</li>" +
                "<li><strong>Candlestick Charts:</strong> Visual representation of price action</li>" +
                "</ul>");
        chartReading.setEstimatedMinutes(60);
        chartReading.setVideoUrl("https://www.youtube.com/embed/J-ntsk7Dsd0?si=0Nor5ytnxK3NHr8e");
        chartReading.setHasVideo(true);
        chartReading.setQuiz(QuizData.getChartReadingQuiz());
        chartReading.setHasQuiz(true);

        Exercise chartExercise = new Exercise();
        chartExercise.setQuestion("Identify the pattern: A stock consistently bounces off support at $50 and resistance at $60.");
        chartExercise.setAnswer("Trading Range");
        chartExercise.setHint("This pattern shows price moving between established support and resistance levels");
        chartExercise.setType("ANALYSIS");
        chartReading.setExercise(chartExercise);
        chartReading.setHasExercise(true);


        tutorials.put(chartReading.getId(), chartReading);

        // Tutorial 3: Risk Management
        TutorialSection riskManagement = new TutorialSection(
                "risk-management",
                "Investment Risk Management",
                "Learn strategies to protect your capital and manage investment risks",
                "BEGINNER",
                "RISK_MANAGEMENT"
        );
        riskManagement.setContent(
                "<h3>Managing Investment Risk</h3>" +
                        "<p>Proper risk management is crucial for long-term investing success. It helps investors " +
                        "protect their capital while aiming for consistent returns, and ensures that unexpected market events " +
                        "do not severely impact your portfolio.</p>" +

                        "<h4>Key Principles of Risk Management</h4>" +
                        "<ul>" +
                        "<li><strong>Diversification:</strong> Spread investments across different asset classes, sectors, and geographies to reduce portfolio risk.</li>" +
                        "<li><strong>Asset Allocation:</strong> Allocate your capital according to risk tolerance and investment goals, balancing stocks, bonds, and other assets.</li>" +
                        "<li><strong>Risk-Return Trade-Off:</strong> Higher potential returns come with higher risk. Understand how much risk you are willing to take for the returns you desire.</li>" +
                        "<li><strong>Stop-Loss Strategies:</strong> Implement stop-loss orders to automatically sell an asset if it falls below a predetermined price, limiting potential losses.</li>" +
                        "<li><strong>Regular Monitoring:</strong> Continuously review your portfolio and market conditions to adjust strategies proactively.</li>" +
                        "</ul>" +

                        "<h4>Types of Investment Risks</h4>" +
                        "<ul>" +
                        "<li><strong>Market Risk:</strong> Risk of losses due to overall market movements.</li>" +
                        "<li><strong>Credit Risk:</strong> Risk that a bond issuer or counterparty may default.</li>" +
                        "<li><strong>Liquidity Risk:</strong> Risk of being unable to quickly sell an investment without significant loss.</li>" +
                        "<li><strong>Inflation Risk:</strong> Risk that returns may not keep pace with inflation.</li>" +
                        "</ul>" +

                        "<h4>Practical Risk Management Tips</h4>" +
                        "<ul>" +
                        "<li>Set clear investment goals and define your risk tolerance before investing.</li>" +
                        "<li>Use diversification and asset allocation to spread and control risk.</li>" +
                        "<li>Regularly rebalance your portfolio to maintain the desired risk level.</li>" +
                        "<li>Stay informed about market trends and economic indicators.</li>" +
                        "<li>Consider using hedging instruments like options or bonds for additional protection.</li>" +
                        "</ul>"

        );

        riskManagement.setEstimatedMinutes(50);
        riskManagement.setVideoUrl("https://www.youtube.com/embed/IP-E75FGFkU?si=_CHcx0TfVt0IIy1s");
        riskManagement.setHasVideo(true);
        riskManagement.setQuiz(QuizData.getInvestmentRiskManagementQuiz());
        riskManagement.setHasQuiz(true);

        tutorials.put(riskManagement.getId(), riskManagement);

        // INTERMEDIATE LEVEL TUTORIALS

        // Tutorial 4: Fundamental Analysis
        TutorialSection fundamentalAnalysis = new TutorialSection(
                "fundamental-analysis",
                "Fundamental Analysis of Companies",
                "Learn how to evaluate companies using financial statements and ratios",
                "INTERMEDIATE",
                "FUNDAMENTAL_ANALYSIS"
        );
        fundamentalAnalysis.setContent(
                "<h3>Analyzing Company Fundamentals</h3>" +
                        "<p>Fundamental analysis involves examining a company's financial health " +
                        "to determine its intrinsic value and long-term growth potential. Investors " +
                        "study both quantitative and qualitative factors to make informed decisions.</p>" +

                        "<h4>Key Components of Fundamental Analysis</h4>" +
                        "<ul>" +
                        "<li><strong>Financial Statements:</strong> Analyze the Income Statement, Balance Sheet, and Cash Flow Statement to evaluate profitability, liquidity, and solvency.</li>" +
                        "<li><strong>Financial Ratios:</strong> Use ratios like Price-to-Earnings (P/E), Return on Equity (ROE), Debt-to-Equity, and Current Ratio to compare performance against industry benchmarks.</li>" +
                        "<li><strong>Company Management:</strong> Assess the quality and experience of the company's leadership team and their strategic decisions.</li>" +
                        "<li><strong>Industry and Market Trends:</strong> Examine the competitive environment, regulatory factors, and market demand that affect the company's growth.</li>" +
                        "<li><strong>Growth Potential:</strong> Evaluate opportunities for expansion, product development, and innovation.</li>" +
                        "</ul>" +

                        "<h4>Benefits of Fundamental Analysis</h4>" +
                        "<ul>" +
                        "<li>Helps determine whether a stock is undervalued or overvalued.</li>" +
                        "<li>Provides insights for long-term investment decisions.</li>" +
                        "<li>Reduces the risk of making speculative investments based on short-term market fluctuations.</li>" +
                        "</ul>"
        );

        fundamentalAnalysis.setEstimatedMinutes(75);
        fundamentalAnalysis.setPrerequisites(Arrays.asList("stock-fundamentals"));
        fundamentalAnalysis.setQuiz(QuizData.getFundamentalAnalysisOfCompaniesQuiz());
        fundamentalAnalysis.setHasQuiz(true);
        fundamentalAnalysis.setVideoUrl("https://www.youtube.com/embed/3BOE1A8HXeE?si=0zdTcXpCfLw6hcCr");
        fundamentalAnalysis.setHasVideo(true);
        // Add case study
        CaseStudy appleCase = new CaseStudy(
                "Apple Inc. Financial Analysis",
                "Analyzing Apple's financial performance and valuation",
                "Apple Inc."
        );
        appleCase.setTimeframe("Q4 2023");
        appleCase.addLearningObjective("Calculate key financial ratios");
        appleCase.addLearningObjective("Evaluate company valuation");
        appleCase.addData("Revenue", "89.5B");
        appleCase.addData("Net Income", "22.9B");
        appleCase.addData("P/E Ratio", "28.5");
        appleCase.setAnalysis("Apple demonstrates strong profitability with consistent revenue growth...");
        fundamentalAnalysis.addCaseStudy(appleCase);

        tutorials.put(fundamentalAnalysis.getId(), fundamentalAnalysis);

        // Tutorial 5: Options Trading Basics
        TutorialSection optionsTrading = new TutorialSection(
                "options-trading",
                "Introduction to Options Trading",
                "Learn the basics of call and put options and how to use them",
                "INTERMEDIATE",
                "DERIVATIVES"
        );
        optionsTrading.setContent(
                "<h3>Options Trading Fundamentals</h3>" +
                        "<p>Options are financial derivatives that give traders the right, but not the obligation, " +
                        "to buy or sell an underlying asset at a predetermined price within a specified timeframe. " +
                        "They provide flexibility, leverage, and risk management opportunities in trading strategies.</p>" +

                        "<h4>Types of Options</h4>" +
                        "<ul>" +
                        "<li><strong>Call Options:</strong> Give the holder the right to buy an asset at a specific price.</li>" +
                        "<li><strong>Put Options:</strong> Give the holder the right to sell an asset at a specific price.</li>" +
                        "</ul>" +

                        "<h4>Key Components of Options</h4>" +
                        "<ul>" +
                        "<li><strong>Strike Price:</strong> The price at which the asset can be bought or sold.</li>" +
                        "<li><strong>Expiration Date:</strong> The date by which the option must be exercised.</li>" +
                        "<li><strong>Premium:</strong> The cost paid to purchase the option.</li>" +
                        "<li><strong>Underlying Asset:</strong> The security (stock, index, commodity, etc.) on which the option is based.</li>" +
                        "</ul>" +

                        "<h4>Benefits of Options Trading</h4>" +
                        "<ul>" +
                        "<li>Provides leverage to control larger positions with less capital.</li>" +
                        "<li>Helps hedge existing positions to reduce risk exposure.</li>" +
                        "<li>Allows traders to implement complex strategies for bullish, bearish, or neutral markets.</li>" +
                        "</ul>" +

                        "<h4>Risks of Options Trading</h4>" +
                        "<ul>" +
                        "<li>Options can expire worthless, leading to a total loss of the premium paid.</li>" +
                        "<li>Complex strategies can increase the risk of losses if not properly managed.</li>" +
                        "<li>Requires careful monitoring of time decay, volatility, and market movements.</li>" +
                        "</ul>"
        );

        optionsTrading.setEstimatedMinutes(90);
        optionsTrading.setPrerequisites(Arrays.asList("stock-fundamentals", "risk-management"));
        optionsTrading.setQuiz(QuizData.getIntroductionToOptionsTradingQuiz());
        optionsTrading.setHasQuiz(true);
        optionsTrading.setVideoUrl("https://www.youtube.com/embed/4HMm6mBvGKE?si=weBTcVwvhdOc4tlo");
        optionsTrading.setHasVideo(true);

        tutorials.put(optionsTrading.getId(), optionsTrading);

        // ADVANCED LEVEL TUTORIALS

        // Tutorial 6: Advanced Technical Analysis
        TutorialSection advancedTechnical = new TutorialSection(
                "advanced-technical",
                "Advanced Technical Analysis Strategies",
                "Master complex chart patterns and technical indicators",
                "ADVANCED",
                "TECHNICAL_ANALYSIS"
        );
        advancedTechnical.setContent(
                "<h3>Advanced Technical Analysis</h3>" +
                        "<p>Advanced technical analysis involves the use of sophisticated tools, indicators, and charting techniques " +
                        "to predict future price movements and identify profitable trading opportunities. It builds on basic technical analysis " +
                        "concepts like trendlines, support and resistance, and price patterns.</p>" +

                        "<h4>Key Advanced Techniques</h4>" +
                        "<ul>" +
                        "<li><strong>Fibonacci Retracement and Extensions:</strong> These tools help identify potential reversal and target levels " +
                        "based on key Fibonacci ratios (23.6%, 38.2%, 50%, 61.8%, etc.). Traders often use them to predict corrections or continuation patterns.</li>" +

                        "<li><strong>Elliott Wave Theory:</strong> This method analyzes price movements as a series of repeating waves " +
                        "that reflect investor psychology and market sentiment. Understanding the wave structure can help traders forecast long-term trends.</li>" +

                        "<li><strong>Ichimoku Cloud:</strong> A comprehensive indicator that shows support, resistance, trend direction, and momentum " +
                        "in one view. It helps traders make quick and informed decisions about potential entry and exit points.</li>" +

                        "<li><strong>Volume and Market Breadth Analysis:</strong> Evaluating volume trends and the number of advancing vs. declining stocks " +
                        "provides insight into market strength and helps confirm price movements.</li>" +

                        "<li><strong>Multi-Timeframe Analysis:</strong> Comparing signals from different timeframes (e.g., daily, weekly, monthly charts) " +
                        "can enhance accuracy by confirming long-term trends and short-term momentum alignment.</li>" +
                        "</ul>"

        );

        advancedTechnical.setEstimatedMinutes(120);
        advancedTechnical.setPrerequisites(Arrays.asList("chart-reading", "fundamental-analysis"));
        advancedTechnical.setQuiz(QuizData.getAdvancedTechnicalAnalysisStrategiesQuiz());
        advancedTechnical.setHasQuiz(true);
        advancedTechnical.setVideoUrl("https://www.youtube.com/embed/eynxyoKgpng?si=yp_V1nC_8sQ5GjEd");
        advancedTechnical.setHasVideo(true);
        advancedTechnical.setCertificationId("advanced_technical_analyst");
        advancedTechnical.addKeyPoint("Fibonacci Retracement Levels use ratios to identify potential support and resistance zones");
        advancedTechnical.addKeyPoint("Elliott Wave Theory posits that markets move in repetitive wave patterns");
        advancedTechnical.addKeyPoint("Ichimoku Cloud is a multi-faceted indicator that reveals dynamic support/resistance, trend direction, and momentum");
        tutorials.put(advancedTechnical.getId(), advancedTechnical);

        // Tutorial 7: Portfolio Management
        TutorialSection portfolioManagement = new TutorialSection(
                "portfolio-management",
                "Professional Portfolio Management",
                "Learn institutional portfolio management strategies and asset allocation",
                "ADVANCED",
                "PORTFOLIO_MANAGEMENT"
        );
        portfolioManagement.setContent(
                "<h3>Professional Portfolio Management</h3>" +
                        "<p>Portfolio management is the art and science of selecting and overseeing a group of investments " +
                        "that meet the long-term financial goals and risk tolerance of an investor. Professional portfolio managers " +
                        "use data-driven strategies, diversification, and continuous monitoring to optimize returns while minimizing risk.</p>" +

                        "<h4>Core Principles of Portfolio Management</h4>" +
                        "<ul>" +
                        "<li><strong>Asset Allocation:</strong> The process of distributing investments across asset classes " +
                        "such as equities, bonds, and alternative assets to achieve a balanced risk-return profile.</li>" +

                        "<li><strong>Diversification:</strong> Reducing exposure to any single investment or sector " +
                        "by holding a variety of assets that respond differently to market conditions.</li>" +

                        "<li><strong>Risk Assessment:</strong> Evaluating the investor’s risk tolerance, investment horizon, and market conditions " +
                        "to determine appropriate portfolio composition and strategy.</li>" +

                        "<li><strong>Performance Evaluation:</strong> Using benchmarks and risk-adjusted metrics such as the Sharpe Ratio, " +
                        "Alpha, and Beta to measure portfolio performance relative to the market.</li>" +

                        "<li><strong>Rebalancing:</strong> Periodically adjusting portfolio weights to maintain the desired asset allocation " +
                        "as market prices change, ensuring the portfolio stays aligned with investment goals.</li>" +
                        "</ul>"

        );

        portfolioManagement.setEstimatedMinutes(100);
        portfolioManagement.setPrerequisites(Arrays.asList("risk-management", "fundamental-analysis"));
        portfolioManagement.setQuiz(QuizData.getProfessionalPortfolioManagementQuiz());
        portfolioManagement.setHasQuiz(true);
        portfolioManagement.setVideoUrl("https://www.youtube.com/embed/wZsg4ldP6vA?si=LHFTCMYa5mDV6kOf");
        portfolioManagement.setHasVideo(true);
        portfolioManagement.setCertificationId("portfolio_manager");
        portfolioManagement.addKeyPoint("Asset Allocation: Distributes investments across asset classes to balance risk and return.");
        portfolioManagement.addKeyPoint("Portfolio Rebalancing: Adjusts holdings periodically to maintain target risk levels.");
        portfolioManagement.addKeyPoint("Sharpe Ratio: Measures risk-adjusted returns to evaluate portfolio efficiency.");
        portfolioManagement.addKeyPoint("Behavioral Finance: Mitigates investor biases for rational decision-making.");
        tutorials.put(portfolioManagement.getId(), portfolioManagement);

        // Set up tutorial relationships
        fundamentals.addNextTutorial("chart-reading");
        fundamentals.addNextTutorial("risk-management");
        chartReading.addNextTutorial("fundamental-analysis");
        riskManagement.addNextTutorial("options-trading");
        fundamentalAnalysis.addNextTutorial("advanced-technical");
        fundamentalAnalysis.addNextTutorial("portfolio-management");

        // Initialize sample progress in database
        try {
            dbHandler.markTutorialComplete("john_doe", "stock-fundamentals");
            dbHandler.markTutorialComplete("john_doe", "chart-reading");
            dbHandler.updateQuizResult("john_doe", "stock-fundamentals", 85, true);
            dbHandler.updateQuizResult("john_doe", "chart-reading", 78, true);

            dbHandler.markTutorialComplete("jane_smith", "stock-fundamentals");
            dbHandler.markTutorialComplete("jane_smith", "risk-management");
            dbHandler.markTutorialComplete("jane_smith", "fundamental-analysis");
            dbHandler.updateQuizResult("jane_smith", "stock-fundamentals", 92, true);
            dbHandler.updateQuizResult("jane_smith", "fundamental-analysis", 88, true);

            // Award some badges
            dbHandler.addUserBadge("john_doe", "Beginner Trader");
            dbHandler.addUserBadge("jane_smith", "Intermediate Investor");
            dbHandler.addUserCertification("jane_smith", "stock_market_fundamentals", 90);

            System.out.println("✅ Stock market tutorial data initialized in database");
        } catch (Exception e) {
            System.err.println("❌ Error initializing sample data: " + e.getMessage());
        }
    }

    // Additional utility methods
    public List<TutorialSection> getTutorialsByTag(String tag) {
        return getTutorialsByCategory(tag);
    }

    public int getCompletedTutorialsCount(String username) {
        return dbHandler.getCompletedTutorialCount(username);
    }

    public double getOverallProgress(String username) {
        int totalTutorials = tutorials.size();
        int completedCount = getCompletedTutorialsCount(username);
        return totalTutorials > 0 ? (double) completedCount / totalTutorials * 100 : 0.0;
    }

    // Get user statistics
    public Map<String, Object> getUserStatistics(String username) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("completedTutorials", dbHandler.getCompletedTutorialCount(username));
        stats.put("totalTimeSpent", dbHandler.getTotalTimeSpent(username));
        stats.put("averageScore", dbHandler.getAverageScore(username));
        stats.put("badges", dbHandler.getUserBadges(username));
        stats.put("certifications", dbHandler.getUserCertifications(username));

        // Determine level based on completed tutorials
        int completed = dbHandler.getCompletedTutorialCount(username);
        if (completed >= 5) stats.put("currentLevel", "Expert");
        else if (completed >= 3) stats.put("currentLevel", "Intermediate");
        else stats.put("currentLevel", "Beginner");

        return stats;
    }

    // Get recommended tutorials based on user progress
    public List<TutorialSection> getRecommendedTutorials(String username) {
        Map<String, Boolean> progress = getUserProgress(username);
        List<TutorialSection> recommendations = new ArrayList<>();

        // Find next tutorials based on completed ones
        for (TutorialSection tutorial : tutorials.values()) {
            if (!progress.getOrDefault(tutorial.getId(), false)) {
                // Check if prerequisites are met
                boolean prerequisitesMet = true;
                if (tutorial.getPrerequisites() != null) {
                    for (String prereq : tutorial.getPrerequisites()) {
                        if (!progress.getOrDefault(prereq, false)) {
                            prerequisitesMet = false;
                            break;
                        }
                    }
                }
                if (prerequisitesMet) {
                    recommendations.add(tutorial);
                }
            }
        }

        return recommendations;
    }

    // Return tutorial with user progress
    public Map<String, Object> getTutorialForUser(String username, String tutorialId) {
        TutorialSection tutorial = getTutorial(tutorialId);
        if (tutorial == null) {
            return null; // Tutorial not found
        }

        Map<String, Object> progress = getTutorialProgress(username, tutorialId);

        Map<String, Object> response = new HashMap<>();
        response.put("tutorial", tutorial);
        response.put("progress", progress);

        return response;
    }

}