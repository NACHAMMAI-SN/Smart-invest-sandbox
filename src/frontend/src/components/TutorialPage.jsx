import React, { useState, useEffect } from 'react';
import { tutorialAPI } from '../services/api';
import '../styles/TutorialPage.css';

const TutorialPage = ({ onBackToHome, user }) => {
    const [activeSection, setActiveSection] = useState('stock-fundamentals');
    const [tutorials, setTutorials] = useState({});
    const [userProgress, setUserProgress] = useState({
        data: {},
        statistics: {
            completedTutorials: 0,
            currentLevel: 'Beginner',
            badges: [],
            certifications: [],
            totalTimeSpent: 0,
            averageScore: 0
        }
    });
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('content');
    const [quizAnswers, setQuizAnswers] = useState({});
    const [quizResults, setQuizResults] = useState(null);
    const [exerciseAnswer, setExerciseAnswer] = useState('');
    const [exerciseResult, setExerciseResult] = useState(null);
    const [tutorialProgress, setTutorialProgress] = useState({});
    const [recommendedTutorials, setRecommendedTutorials] = useState([]);
    const [showCompletion, setShowCompletion] = useState(false);

    useEffect(() => {
        loadTutorials();
        if (user && user.username) {
            loadUserProgress();
            loadRecommendedTutorials();
        }
    }, [user]);

    useEffect(() => {
        if (user && user.username && activeSection) {
            loadTutorialProgress();
        }
    }, [user, activeSection]);

    useEffect(() => {
        if (tutorialProgress.completed || tutorialProgress.quizPassed) {
            setShowCompletion(true);
            const timer = setTimeout(() => setShowCompletion(false), 3000);
            return () => clearTimeout(timer);
        }
    }, [tutorialProgress.completed, tutorialProgress.quizPassed]);

    const loadTutorials = async () => {
        try {
            const tutorialList = await tutorialAPI.getAllTutorials();
            if (Array.isArray(tutorialList)) {
                const tutorialMap = {};
                tutorialList.forEach(tutorial => {
                    tutorialMap[tutorial.id] = tutorial;
                });
                setTutorials(tutorialMap);
            } else {
                console.error('Tutorial list is not an array:', tutorialList);
                setTutorials({});
            }
        } catch (error) {
            console.error('Failed to load tutorials:', error);
            setTutorials({});
        } finally {
            setLoading(false);
        }
    };

    const loadUserProgress = async () => {
        try {
            const progress = await tutorialAPI.getUserProgress(user.username);
            const safeProgress = {
                data: progress.data || {},
                statistics: {
                    completedTutorials: progress.statistics?.completedTutorials || 0,
                    currentLevel: progress.statistics?.currentLevel || 'Beginner',
                    badges: progress.statistics?.badges || [],
                    certifications: progress.statistics?.certifications || [],
                    totalTimeSpent: progress.statistics?.totalTimeSpent || 0,
                    averageScore: progress.statistics?.averageScore || 0
                }
            };
            setUserProgress(safeProgress);
        } catch (error) {
            console.error('Failed to load user progress:', error);
            setUserProgress({
                data: {},
                statistics: {
                    completedTutorials: 0,
                    currentLevel: 'Beginner',
                    badges: [],
                    certifications: [],
                    totalTimeSpent: 0,
                    averageScore: 0
                }
            });
        }
    };

    const loadTutorialProgress = async () => {
        try {
            const progress = await tutorialAPI.getTutorialProgress(user.username, activeSection);
            setTutorialProgress(progress || {});
        } catch (error) {
            console.error('Failed to load tutorial progress:', error);
            setTutorialProgress({});
        }
    };

    const loadRecommendedTutorials = async () => {
        try {
            const recommendations = await tutorialAPI.getRecommendedTutorials(user.username);
            setRecommendedTutorials(recommendations || []);
        } catch (error) {
            console.error('Failed to load recommended tutorials:', error);
            setRecommendedTutorials([]);
        }
    };

    const handleQuizAnswer = (questionIndex, answerIndex) => {
        setQuizAnswers(prev => ({
            ...prev,
            [questionIndex]: answerIndex
        }));
    };

    const submitQuiz = async () => {
        try {
            const results = await tutorialAPI.submitQuiz(activeSection, quizAnswers, user.username);
            setQuizResults(results);

            // Refresh progress data immediately after quiz submission
            await Promise.all([
                loadUserProgress(),
                loadTutorialProgress(),
                loadRecommendedTutorials()
            ]);

            // If quiz was passed OR score is high enough, update progress
            if (results.passed || results.score >= 80) {
                setTutorialProgress(prev => ({
                    ...prev,
                    completed: true,
                    quizPassed: results.passed,
                    quizScore: results.score
                }));

                // Also update user progress state
                setUserProgress(prev => ({
                    ...prev,
                    data: {
                        ...prev.data,
                        [activeSection]: true
                    }
                }));

                // Show completion message for high scores
                if (results.score >= 90) {
                    alert(`🎉 Excellent! You scored ${results.score}% and completed the tutorial!`);
                } else if (results.passed) {
                    alert(`✅ Great job! You passed with ${results.score}% and completed the tutorial!`);
                }
            }
        } catch (error) {
            console.error('Failed to submit quiz:', error);
        }
    };

    const handleExerciseSubmit = async () => {
        if (!exerciseAnswer.trim()) {
            alert('Please enter an answer!');
            return;
        }

        try {
            const isCorrect = await tutorialAPI.validateExercise(activeSection, exerciseAnswer, user.username);
            setExerciseResult(isCorrect);
            if (isCorrect) {
                alert('Correct! Well done!');

                // Refresh progress data after successful exercise
                await Promise.all([
                    loadUserProgress(),
                    loadTutorialProgress(),
                    loadRecommendedTutorials()
                ]);

                // Update local state
                setTutorialProgress(prev => ({
                    ...prev,
                    completed: true
                }));

            } else {
                alert('Not quite right. Try again or use the hint!');
            }
        } catch (error) {
            console.error('Error validating answer:', error);
            alert('Error validating answer. Please try again.');
        }
    };

    const markAsComplete = async () => {
        try {
            console.log('🔍 markAsComplete called:');
            console.log('User:', user);
            console.log('Username:', user?.username);
            console.log('Active Section:', activeSection);

            if (!user || !user.username) {
                alert('User not logged in!');
                return;
            }

            if (!activeSection) {
                alert('No tutorial selected!');
                return;
            }

            const result = await tutorialAPI.markTutorialComplete(user.username, activeSection);
            console.log('API Result:', result);

            if (result.success) {
                alert('Tutorial marked as complete!');

                // Force refresh all progress data
                await Promise.all([
                    loadUserProgress(),
                    loadTutorialProgress(),
                    loadRecommendedTutorials()
                ]);

                // Force a re-render by updating state
                setTutorialProgress(prev => ({
                    ...prev,
                    completed: true
                }));

            } else {
                alert(`Failed to mark as complete: ${result.error || 'Unknown error'}`);
            }
        } catch (error) {
            console.error('Failed to mark as complete:', error);
            alert('Failed to mark tutorial as complete. Please try again.');
        }
    };

    const getLevelColor = (level) => {
        switch (level) {
            case 'BEGINNER': return '#28a745';
            case 'INTERMEDIATE': return '#ffc107';
            case 'ADVANCED': return '#dc3545';
            default: return '#6c757d';
        }
    };

    const getCompletionStatus = (tutorialId) => {
        return userProgress?.data?.[tutorialId] ? 'completed' : '';
    };

    const getProgressPercentage = () => {
        const total = tutorials ? Object.keys(tutorials).length : 0;

        // Use both possible sources for completed count
        const completedFromStatistics = userProgress?.statistics?.completedTutorials || 0;
        const completedFromData = userProgress?.data ? Object.values(userProgress.data).filter(Boolean).length : 0;

        // Use whichever is available
        const completed = completedFromStatistics > 0 ? completedFromStatistics : completedFromData;

        console.log('📊 Progress Calculation:', {
            totalTutorials: total,
            completedFromStatistics: completedFromStatistics,
            completedFromData: completedFromData,
            finalCompleted: completed,
            percentage: total > 0 ? (completed / total) * 100 : 0
        });

        return total > 0 ? (completed / total) * 100 : 0;
    };

    if (loading) {
        return <div className="tutorial-page loading">Loading tutorials...</div>;
    }

    const currentTutorial = tutorials[activeSection];

    if (!currentTutorial) {
        return (
            <div className="tutorial-page">
                <header className="tutorial-header">
                    <div className="header-content">
                        <button onClick={onBackToHome} className="back-btn">← Back to Home</button>
                        <h1>Smart Invest Tutorials</h1>
                    </div>
                </header>
                <div className="tutorial-container">
                    <div className="tutorial-content">
                        <p>No tutorial selected or tutorial not found.</p>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="tutorial-page">
            {/* Completion Celebration */}
            {showCompletion && (
                <div className="completion-celebration">
                    <div className="celebration-content">
                        <h3>🎉 Tutorial Completed! 🎉</h3>
                        <p>Great job completing "{currentTutorial.title}"!</p>
                        {tutorialProgress.quizScore > 0 && (
                            <p>Quiz Score: <strong>{tutorialProgress.quizScore}%</strong></p>
                        )}
                    </div>
                </div>
            )}

            <header className="tutorial-header">
                <div className="header-content">
                    <button onClick={onBackToHome} className="back-btn">← Back to Home</button>
                    <div className="header-text">
                        <h1>Smart Invest Tutorials</h1>
                        <p>Master stock market investing with interactive courses</p>
                        {user && (
                            <div className="user-stats">
                                <span className="welcome">Welcome, {user.username}!</span>
                                <div className="progress-stats">
                                    <span>Completed: {userProgress?.statistics?.completedTutorials || 0}</span>
                                    <span>Level: {userProgress?.statistics?.currentLevel || 'Beginner'}</span>
                                    <span>Badges: {userProgress?.statistics?.badges?.length || 0}</span>
                                    <span>Balance: ${user?.balance?.toLocaleString() || 0}</span>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </header>

            <div className="tutorial-container">
                <nav className="tutorial-nav">
                    <div className="level-section">
                        <h3>Beginner Level</h3>
                        {Object.values(tutorials)
                            .filter(t => t.level === 'BEGINNER')
                            .map(tutorial => (
                                <button
                                    key={tutorial.id}
                                    className={`nav-item ${activeSection === tutorial.id ? 'active' : ''} ${getCompletionStatus(tutorial.id)}`}
                                    onClick={() => {
                                        setActiveSection(tutorial.id);
                                        setActiveTab('content');
                                        setQuizResults(null);
                                        setExerciseAnswer('');
                                        setExerciseResult(null);
                                    }}
                                >
                                    <span className="tutorial-title">{tutorial.title}</span>
                                    <span className="level-badge beginner">Beginner</span>
                                    <span className="time-estimate">{tutorial.estimatedMinutes}min</span>
                                </button>
                            ))
                        }
                    </div>

                    <div className="level-section">
                        <h3>Intermediate Level</h3>
                        {Object.values(tutorials)
                            .filter(t => t.level === 'INTERMEDIATE')
                            .map(tutorial => (
                                <button
                                    key={tutorial.id}
                                    className={`nav-item ${activeSection === tutorial.id ? 'active' : ''} ${getCompletionStatus(tutorial.id)}`}
                                    onClick={() => {
                                        setActiveSection(tutorial.id);
                                        setActiveTab('content');
                                        setQuizResults(null);
                                        setExerciseAnswer('');
                                        setExerciseResult(null);
                                    }}
                                >
                                    <span className="tutorial-title">{tutorial.title}</span>
                                    <span className="level-badge intermediate">Intermediate</span>
                                    <span className="time-estimate">{tutorial.estimatedMinutes}min</span>
                                </button>
                            ))
                        }
                    </div>

                    <div className="level-section">
                        <h3>Advanced Level</h3>
                        {Object.values(tutorials)
                            .filter(t => t.level === 'ADVANCED')
                            .map(tutorial => (
                                <button
                                    key={tutorial.id}
                                    className={`nav-item ${activeSection === tutorial.id ? 'active' : ''} ${getCompletionStatus(tutorial.id)}`}
                                    onClick={() => {
                                        setActiveSection(tutorial.id);
                                        setActiveTab('content');
                                        setQuizResults(null);
                                        setExerciseAnswer('');
                                        setExerciseResult(null);
                                    }}
                                >
                                    <span className="tutorial-title">{tutorial.title}</span>
                                    <span className="level-badge advanced">Advanced</span>
                                    <span className="time-estimate">{tutorial.estimatedMinutes}min</span>
                                </button>
                            ))
                        }
                    </div>

                    {/* Recommended Tutorials Section */}
                    {recommendedTutorials && recommendedTutorials.length > 0 && (
                        <div className="level-section recommended">
                            <h3>Recommended for You</h3>
                            {recommendedTutorials.map(tutorial => (
                                <button
                                    key={tutorial.id}
                                    className={`nav-item recommended ${activeSection === tutorial.id ? 'active' : ''}`}
                                    onClick={() => {
                                        setActiveSection(tutorial.id);
                                        setActiveTab('content');
                                        setQuizResults(null);
                                        setExerciseAnswer('');
                                        setExerciseResult(null);
                                    }}
                                >
                                    <span className="tutorial-title">{tutorial.title}</span>
                                    <span className="level-badge" style={{backgroundColor: getLevelColor(tutorial.level)}}>
                                        {tutorial.level}
                                    </span>
                                </button>
                            ))}
                        </div>
                    )}
                </nav>

                <div className="tutorial-content">
                    {currentTutorial && (
                        <>
                            <div className="tutorial-header-content">
                                <div className="tutorial-meta">
                                    <span className="level-tag" style={{backgroundColor: getLevelColor(currentTutorial.level)}}>
                                        {currentTutorial.level}
                                    </span>
                                    <span className="category">{currentTutorial.category}</span>
                                    <span className="time-required">{currentTutorial.estimatedMinutes} minutes</span>
                                </div>
                                <h2>{currentTutorial.title}</h2>
                                <p className="tutorial-description">{currentTutorial.description}</p>

                                {/* Progress for current tutorial */}
                                {tutorialProgress && (
                                    <div className="current-progress">
                                        <span style={{color: "#666666"}}>Your Progress: </span>
                                        {tutorialProgress.completed || tutorialProgress.quizPassed ? (
                                            <span style={{color: '#28a745', fontWeight: 'bold'}}>✅ Completed</span>
                                        ) : tutorialProgress.timeSpent > 0 ? (
                                            <span style={{color: '#ffc107', fontWeight: 'bold'}}>🔄 In Progress</span>
                                        ) : (
                                            <span style={{color: '#6c757d', fontWeight: 'bold'}}>⏳ Not Started</span>
                                        )}
                                        {tutorialProgress.quizScore > 0 && (
                                            <span style={{marginLeft: '15px'}}>
                                                Quiz Score: <strong>{tutorialProgress.quizScore}%</strong>
                                                {tutorialProgress.quizPassed && ' ✅'}
                                            </span>
                                        )}
                                    </div>
                                )}
                            </div>

                            <div className="content-tabs">
                                <button className={`tab ${activeTab === 'content' ? 'active' : ''}`} onClick={() => setActiveTab('content')}>
                                    📚 Learning Content
                                </button>
                                {currentTutorial.hasVideo && (
                                    <button className={`tab ${activeTab === 'video' ? 'active' : ''}`} onClick={() => setActiveTab('video')}>
                                        🎥 Video Lesson
                                    </button>
                                )}
                                {currentTutorial.exercise && (
                                    <button className={`tab ${activeTab === 'exercise' ? 'active' : ''}`} onClick={() => setActiveTab('exercise')}>
                                        💡 Practice Exercise
                                    </button>
                                )}
                                {currentTutorial.hasQuiz && (
                                    <button className={`tab ${activeTab === 'quiz' ? 'active' : ''}`} onClick={() => setActiveTab('quiz')}>
                                        📝 Knowledge Check
                                    </button>
                                )}
                                {currentTutorial.caseStudies && currentTutorial.caseStudies.length > 0 && (
                                    <button className={`tab ${activeTab === 'cases' ? 'active' : ''}`} onClick={() => setActiveTab('cases')}>
                                        📊 Case Studies
                                    </button>
                                )}
                                {currentTutorial.glossary && Object.keys(currentTutorial.glossary).length > 0 && (
                                    <button className={`tab ${activeTab === 'glossary' ? 'active' : ''}`} onClick={() => setActiveTab('glossary')}>
                                        📖 Glossary
                                    </button>
                                )}
                            </div>

                            <div className="tab-content">
                                {activeTab === 'content' && (
                                    <div className="learning-content">
                                        <div dangerouslySetInnerHTML={{ __html: currentTutorial.content }} />

                                        {currentTutorial.keyPoints && currentTutorial.keyPoints.length > 0 && (
                                            <div className="key-points">
                                                <h4>Key Takeaways</h4>
                                                <ul>
                                                    {currentTutorial.keyPoints.map((point, index) => (
                                                        <li key={index}>{point}</li>
                                                    ))}
                                                </ul>
                                            </div>
                                        )}

                                        {currentTutorial.infographics && currentTutorial.infographics.length > 0 && (
                                            <div className="infographics">
                                                <h4>Visual Guides</h4>
                                                <div className="infographic-grid">
                                                    {currentTutorial.infographics.map((img, index) => (
                                                        <div key={index} className="infographic-item">
                                                            <img src={img} alt={`Infographic ${index + 1}`} />
                                                        </div>
                                                    ))}
                                                </div>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {activeTab === 'video' && currentTutorial.hasVideo && (
                                    <div className="video-content">
                                        <h3>Video Lesson</h3>
                                        <div className="video-container">
                                            {currentTutorial.videoUrl ? (
                                                <iframe
                                                    width="100%"
                                                    height="400"
                                                    src={currentTutorial.videoUrl}
                                                    title={currentTutorial.title}
                                                    frameBorder="0"
                                                    allowFullScreen
                                                ></iframe>
                                            ) : (
                                                <div className="video-placeholder">
                                                    <p>Video content coming soon!</p>
                                                    <p>In the meantime, check out the learning content and exercises.</p>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}

                                {activeTab === 'exercise' && currentTutorial.exercise && (
                                    <div className="exercise-content">
                                        <h3>Practice Exercise</h3>
                                        <div className="exercise-card">
                                            <p>{currentTutorial.exercise.question}</p>

                                            <div className="exercise-controls">
                                                <input
                                                    type="text"
                                                    placeholder="Enter your answer..."
                                                    className="answer-input"
                                                    value={exerciseAnswer}
                                                    onChange={(e) => setExerciseAnswer(e.target.value)}
                                                    onKeyPress={(e) => {
                                                        if (e.key === 'Enter') handleExerciseSubmit();
                                                    }}
                                                />
                                                <button
                                                    className="submit-btn"
                                                    onClick={handleExerciseSubmit}
                                                >
                                                    Check Answer
                                                </button>
                                            </div>

                                            {currentTutorial.exercise.hint && (
                                                <div className="hint-section">
                                                    <button
                                                        className="hint-btn"
                                                        onClick={() => {
                                                            const hintContent = document.querySelector('.hint-content');
                                                            if (hintContent) {
                                                                hintContent.style.display =
                                                                    hintContent.style.display === 'none' ? 'block' : 'none';
                                                            }
                                                        }}
                                                    >
                                                        💡 Show Hint
                                                    </button>
                                                    <div className="hint-content" style={{display: 'none'}}>
                                                        {currentTutorial.exercise.hint}
                                                    </div>
                                                </div>
                                            )}

                                            {exerciseResult !== null && (
                                                <div className={`exercise-result ${exerciseResult ? 'correct' : 'incorrect'}`}>
                                                    {exerciseResult ? '✅ Correct! Well done!' : '❌ Try again!'}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}

                                {activeTab === 'quiz' && currentTutorial.hasQuiz && (
                                    <div className="quiz-content">
                                        {!quizResults ? (
                                            <>
                                                <div className="quiz-header">
                                                    <h3>Knowledge Check</h3>
                                                    <div className="quiz-info">
                                                        <span>Time Limit: {currentTutorial.quiz?.timeLimit || 30} minutes</span>
                                                        <span>Passing Score: {currentTutorial.quiz?.passingScore || 70}%</span>
                                                        {tutorialProgress.quizScore > 0 && (
                                                            <span>Previous Score: {tutorialProgress.quizScore}%</span>
                                                        )}
                                                    </div>
                                                </div>

                                                <div className="questions-container">
                                                    {currentTutorial.quiz?.questions?.map((question, qIndex) => (
                                                        <div key={qIndex} className="question-card">
                                                            <h4>Question {qIndex + 1}</h4>
                                                            <p>{question.questionText}</p>
                                                            <div className="options-grid">
                                                                {question.options.map((option, oIndex) => (
                                                                    <label key={oIndex} className="option-label">
                                                                        <input
                                                                            type="radio"
                                                                            name={`question-${qIndex}`}
                                                                            value={oIndex}
                                                                            checked={quizAnswers[qIndex] === oIndex}
                                                                            onChange={() => handleQuizAnswer(qIndex, oIndex)}
                                                                        />
                                                                        <span>{option}</span>
                                                                    </label>
                                                                ))}
                                                            </div>
                                                        </div>
                                                    ))}
                                                </div>

                                                <button
                                                    className="submit-quiz-btn"
                                                    onClick={submitQuiz}
                                                    disabled={Object.keys(quizAnswers).length < (currentTutorial.quiz?.questions?.length || 0)}
                                                >
                                                    Submit Quiz
                                                </button>
                                            </>
                                        ) : (
                                            <div className="quiz-results">
                                                <h3>Quiz Results</h3>
                                                <div className={`result-card ${quizResults.passed ? 'passed' : 'failed'}`}>
                                                    <h4>{quizResults.passed ? '🎉 Congratulations!' : '📚 Keep Learning!'}</h4>
                                                    <div className="score-display">
                                                        Your Score: <span className="score">{quizResults.score}%</span>
                                                    </div>
                                                    <p>
                                                        {quizResults.correctAnswers} out of {quizResults.totalQuestions} correct
                                                    </p>
                                                    {quizResults.passed && (
                                                        <div className="success-badge">
                                                            🏆 Quiz Completed Successfully!
                                                        </div>
                                                    )}
                                                </div>

                                                <div className="detailed-results">
                                                    <h4>Review Your Answers</h4>
                                                    {quizResults.detailedResults?.map((result, index) => (
                                                        <div key={index} className={`answer-review ${result.correct ? 'correct' : 'incorrect'}`}>
                                                            <p><strong>Question {index + 1}:</strong> {result.correct ? '✅ Correct' : '❌ Incorrect'}</p>
                                                            {!result.correct && result.explanation && (
                                                                <p className="explanation">{result.explanation}</p>
                                                            )}
                                                        </div>
                                                    ))}
                                                </div>

                                                <button className="retake-btn" onClick={() => {
                                                    setQuizResults(null);
                                                    setQuizAnswers({});
                                                }}>
                                                    Try Again
                                                </button>
                                            </div>
                                        )}
                                    </div>
                                )}

                                {activeTab === 'cases' && currentTutorial.caseStudies && (
                                    <div className="case-studies-content">
                                        <h3>Real-World Case Studies</h3>
                                        {currentTutorial.caseStudies.map((caseStudy, index) => (
                                            <div key={index} className="case-study-card">
                                                <h4>{caseStudy.title}</h4>
                                                <div className="case-meta">
                                                    <span className="company">{caseStudy.company}</span>
                                                    <span className="timeframe">{caseStudy.timeframe}</span>
                                                </div>
                                                <p>{caseStudy.description}</p>

                                                {caseStudy.learningObjectives && caseStudy.learningObjectives.length > 0 && (
                                                    <div className="learning-objectives">
                                                        <h5>Learning Objectives:</h5>
                                                        <ul>
                                                            {caseStudy.learningObjectives.map((obj, objIndex) => (
                                                                <li key={objIndex}>{obj}</li>
                                                            ))}
                                                        </ul>
                                                    </div>
                                                )}

                                                {caseStudy.data && Object.keys(caseStudy.data).length > 0 && (
                                                    <div className="case-data">
                                                        <h5>Key Data:</h5>
                                                        <pre>{JSON.stringify(caseStudy.data, null, 2)}</pre>
                                                    </div>
                                                )}

                                                {caseStudy.analysis && (
                                                    <div className="case-analysis">
                                                        <h5>Analysis:</h5>
                                                        <p>{caseStudy.analysis}</p>
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}

                                {activeTab === 'glossary' && currentTutorial.glossary && (
                                    <div className="glossary-content">
                                        <h3>Investment Glossary</h3>
                                        <div className="glossary-grid">
                                            {Object.entries(currentTutorial.glossary).map(([term, definition]) => (
                                                <div key={term} className="glossary-item">
                                                    <dt className="glossary-term">{term}</dt>
                                                    <dd className="glossary-definition">{definition}</dd>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="progress-section">
                                <h4>Your Overall Progress</h4>
                                <div className="progress-bar">
                                    <div
                                        className="progress-fill"
                                        style={{
                                            width: `${getProgressPercentage()}%`
                                        }}
                                    ></div>
                                </div>
                                <p>{userProgress?.statistics?.completedTutorials || 0} of {tutorials ? Object.keys(tutorials).length : 0} tutorials completed</p>

                                {userProgress?.statistics?.badges && userProgress.statistics.badges.length > 0 && (
                                    <div className="badges-section">
                                        <h5>Your Badges:</h5>
                                        <div className="badges-grid">
                                            {userProgress.statistics.badges.map((badge, index) => (
                                                <span key={index} className="badge">
                                                    🏅 {badge}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {userProgress?.statistics?.certifications && userProgress.statistics.certifications.length > 0 && (
                                    <div className="certifications-section">
                                        <h5>Your Certifications:</h5>
                                        <div className="certifications-grid">
                                            {userProgress.statistics.certifications.map((cert, index) => (
                                                <span key={index} className="certification">
                                                    📜 {cert}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>

                            <div className="tutorial-footer">
                                <div className="progress-actions">
                                    <button className="complete-btn" onClick={markAsComplete}>
                                        ✅ Mark Complete
                                    </button>
                                </div>

                                {currentTutorial.nextTutorials && currentTutorial.nextTutorials.length > 0 && (
                                    <div className="next-steps">
                                        <h4>Continue Learning</h4>
                                        <div className="next-tutorials">
                                            {currentTutorial.nextTutorials.map(nextId => (
                                                tutorials[nextId] && (
                                                    <button
                                                        key={nextId}
                                                        className="next-tutorial-btn"
                                                        onClick={() => {
                                                            setActiveSection(nextId);
                                                            setActiveTab('content');
                                                            setQuizResults(null);
                                                            setExerciseAnswer('');
                                                            setExerciseResult(null);
                                                        }}
                                                    >
                                                        {tutorials[nextId].title}
                                                    </button>
                                                )
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default TutorialPage;