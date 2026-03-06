import React, { useState, useEffect } from 'react';
import '../styles/Home.css';
<<<<<<< HEAD
import { portfolioAPI } from '../services/api';

const Home = ({ user, onLogout, onNavigate, onNavigateToTutorials }) => {
    const [marketStatus, setMarketStatus] = useState('');
    const [analytics, setAnalytics] = useState({
        totalInvestment: 0,
        portfolioValue: 0,
        profitLoss: 0,
        holdings: 0,
        topStock: ''
    });
=======

const Home = ({ user, onLogout, onNavigate, onNavigateToTutorials }) => {
    const [marketStatus, setMarketStatus] = useState('');
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

    // Calculate real-time market status
    useEffect(() => {
        const updateMarketStatus = () => {
            const now = new Date();
            const day = now.getDay();
            const hours = now.getHours();
            const minutes = now.getMinutes();

            // Market hours: Mon-Fri, 9:30 AM - 4:00 PM EST
            if (day === 0 || day === 6) {
                setMarketStatus('The market is closed for the weekend.');
            } else if (hours < 9 || (hours === 9 && minutes < 30)) {
                const openTime = new Date();
                openTime.setHours(9, 30, 0, 0);
                const diff = openTime - now;
                const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
                const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
                setMarketStatus(`The market opens in ${hoursLeft}h ${minutesLeft}m`);
            } else if (hours < 16) {
                const closeTime = new Date();
                closeTime.setHours(16, 0, 0, 0);
                const diff = closeTime - now;
                const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
                const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
                setMarketStatus(`The market is open. It will close in ${hoursLeft}h ${minutesLeft}m`);
            } else {
                setMarketStatus('The market is closed for the day.');
            }
        };

        updateMarketStatus();
        const interval = setInterval(updateMarketStatus, 60000);
        return () => clearInterval(interval);
    }, []);

<<<<<<< HEAD
    // Load portfolio analytics for dashboard card
    useEffect(() => {
        const loadAnalytics = async () => {
            if (!user?.username) return;
            try {
                const data = await portfolioAPI.getPortfolioAnalytics(user.username);
                setAnalytics(data || {});
            } catch (e) {
                console.error('Failed to load portfolio analytics', e);
            }
        };
        loadAnalytics();
    }, [user]);

=======
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    return (
        <div className="dashboard-container">
            {/* Sidebar */}
            <div className="sidebar">
                <div className="logo">
                    <h1>Smart Invest</h1>
                </div>

                {/* User Profile Section */}
                <div className="user-profile-section">
                    <div className="user-profile-header">
                        <div className="user-avatar">
                            {user?.username?.charAt(0).toUpperCase() || 'U'}
                        </div>
                        <div className="user-info">
                            <h2>Hello, {user?.username || 'User'}!</h2>
                            <p>Welcome to Smart Invest</p>
                        </div>
                    </div>

                    <div className="user-details">
                        <div className="user-detail-item">
                            <span className="user-detail-label">Email:</span>
                            <span className="user-detail-value">{user?.email || 'N/A'}</span>
                        </div>
                        <div className="user-detail-item">
                            <span className="user-detail-label">Market Status:</span>
                            <span className="user-detail-value">{marketStatus}</span>
                        </div>
                    </div>

                    <div className="virtual-balance-card">
                        <div className="balance-label">Virtual Balance</div>
                        <div className="balance-amount">
                            ${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                        </div>
                        <div className="account-type-badge">Simulation Account</div>
                    </div>

                    <div className="user-stats-section">
                        <div className="user-stat-item">
                            <span className="user-stat-label">Risk Level</span>
                            <span className="risk-level-text">Beginner</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Platform</span>
                            <span className="platform-text">Simulation</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Experience</span>
                            <span className="experience-text">Beginner</span>
                        </div>
                    </div>

                    <div className="sidebar-action-buttons">
                        <button onClick={() => onNavigate('dashboard')} className="sidebar-action-button primary">
<<<<<<< HEAD
                             HOME
                        </button>
                        <button onClick={() => onNavigate('portfolio')} className="sidebar-action-button">
                             PORTFOLIO
                        </button>
                        <button onClick={() => onNavigate('trade')} className="sidebar-action-button">
                             TRADE
                        </button>
                        <button onClick={() => onNavigate('forecast')} className="sidebar-action-button">
                             FORECAST
                        </button>
                        <button onClick={() => onNavigate('news')} className="sidebar-action-button">
                             NEWS
                        </button>
                        <button onClick={() => onNavigate('transactions')} className="sidebar-action-button">
                             TRANSACTIONS
                        </button>
                        {/* NEW: Tutorials Navigation */}
                        <button onClick={onNavigateToTutorials} className="sidebar-action-button tutorial-btn">
                             TUTORIALS
=======
                            📊 HOME
                        </button>
                        <button onClick={() => onNavigate('portfolio')} className="sidebar-action-button">
                            💼 PORTFOLIO
                        </button>
                        <button onClick={() => onNavigate('trade')} className="sidebar-action-button">
                            💹 TRADE
                        </button>
                        <button onClick={() => onNavigate('forecast')} className="sidebar-action-button">
                            🔮 FORECAST
                        </button>
                        <button onClick={() => onNavigate('news')} className="sidebar-action-button">
                            📰 NEWS
                        </button>
                        <button onClick={() => onNavigate('transactions')} className="sidebar-action-button">
                            📋 TRANSACTIONS
                        </button>
                        {/* NEW: Tutorials Navigation */}
                        <button onClick={onNavigateToTutorials} className="sidebar-action-button tutorial-btn">
                            📚 TUTORIALS
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                        </button>
                    </div>

                    <div className="sign-out-section">
                        <button onClick={onLogout} className="sign-out-button">
<<<<<<< HEAD
                             Sign Out
=======
                            🚪 Sign Out
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="main-content">
                <div className="home-container">
                    <div className="home-card">
                        <div className="welcome-section">
<<<<<<< HEAD
                            <div className="success-icon"></div>
=======
                            <div className="success-icon">🎉</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                            <h1>Welcome to Smart Invest</h1>
                            <h2>Hello, {user?.username}!</h2>

                            <div className="user-info">
                                <div className="info-item">
                                    <strong>Email:</strong>
                                    <span className="info-value">{user?.email}</span>
                                </div>
                                <div className="info-item">
                                    <strong>Virtual Balance:</strong>
                                    <span className="info-value">${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</span>
                                </div>
                                <div className="info-item">
                                    <strong>Account Type:</strong>
                                    <span className="info-value">Simulation Account</span>
                                </div>
                                <div className="info-item">
                                    <strong>Market Status:</strong>
                                    <span className="info-value">{marketStatus}</span>
                                </div>
                            </div>

                            <div className="navigation-buttons">
                                <button onClick={() => onNavigate('portfolio')} className="btn-primary">
<<<<<<< HEAD
                                     View Portfolio
                                </button>
                                <button onClick={() => onNavigate('trade')} className="btn-primary">
                                     Start Trading
                                </button>
                                <button onClick={() => onNavigate('forecast')} className="btn-primary">
                                     Market Forecast
                                </button>
                                <button onClick={() => onNavigate('news')} className="btn-primary">
                                     Market News
                                </button>
                                <button onClick={() => onNavigate('transactions')} className="btn-primary">
                                     Transaction History
                                </button>
                                {/* NEW: Tutorials Button */}
                                <button onClick={onNavigateToTutorials} className="btn-primary tutorial-highlight">
                                     Learn & Practice
=======
                                    📊 View Portfolio
                                </button>
                                <button onClick={() => onNavigate('trade')} className="btn-primary">
                                    💹 Start Trading
                                </button>
                                <button onClick={() => onNavigate('forecast')} className="btn-primary">
                                    🔮 Market Forecast
                                </button>
                                <button onClick={() => onNavigate('news')} className="btn-secondary">
                                    📰 Market News
                                </button>
                                <button onClick={() => onNavigate('transactions')} className="btn-secondary">
                                    📋 Transaction History
                                </button>
                                {/* NEW: Tutorials Button */}
                                <button onClick={onNavigateToTutorials} className="btn-primary tutorial-highlight">
                                    📚 Learn & Practice
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                </button>
                            </div>

                            <div className="quick-stats">
                                <div className="stat-badge">
                                    <span className="stat-label">Risk Level</span>
                                    <span className="stat-value beginner">Beginner</span>
                                </div>
                                <div className="stat-badge">
                                    <span className="stat-label">Platform</span>
<<<<<<< HEAD
                                    <span className="stat-value beginner">Simulation</span>
                                </div>
                                <div className="stat-badge">
                                    <span className="stat-label">Experience</span>
                                    <span className="stat-value beginner">Risk-Free</span>
                                </div>
                                <div className="stat-badge">
                                    <span className="stat-label">AI Features</span>
                                    <span className="stat-value beginner">Enabled</span>
                                </div>
                            </div>

                            {/* Portfolio Analytics Card */}
                            <div className="analytics-card">
                                <h2>Portfolio Analytics</h2>
                                <div className="analytics-grid">
                                    <div className="analytics-item">
                                        <span className="analytics-label">Total Investment</span>
                                        <span className="analytics-value">
                                            ${analytics.totalInvestment?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                                        </span>
                                    </div>
                                    <div className="analytics-item">
                                        <span className="analytics-label">Portfolio Value</span>
                                        <span className="analytics-value">
                                            ${analytics.portfolioValue?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                                        </span>
                                    </div>
                                    <div className="analytics-item">
                                        <span className="analytics-label">Profit / Loss</span>
                                        <span className={`analytics-value ${analytics.profitLoss >= 0 ? 'positive' : 'negative'}`}>
                                            {analytics.profitLoss >= 0 ? '+' : '-'}${Math.abs(analytics.profitLoss || 0).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                        </span>
                                    </div>
                                    <div className="analytics-item">
                                        <span className="analytics-label">Holdings</span>
                                        <span className="analytics-value">
                                            {analytics.holdings || 0}
                                        </span>
                                    </div>
                                    <div className="analytics-item">
                                        <span className="analytics-label">Top Performing Stock</span>
                                        <span className="analytics-value">
                                            {analytics.topStock || 'N/A'}
                                        </span>
                                    </div>
=======
                                    <span className="stat-value">Simulation</span>
                                </div>
                                <div className="stat-badge">
                                    <span className="stat-label">Experience</span>
                                    <span className="stat-value">Risk-Free</span>
                                </div>
                                <div className="stat-badge">
                                    <span className="stat-label">AI Features</span>
                                    <span className="stat-value">Enabled</span>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                </div>
                            </div>

                            {/* Features Grid */}
                            <div className="features-grid">
                                <div className="feature-card">
<<<<<<< HEAD
                                    <div className="feature-icon"></div>
=======
                                    <div className="feature-icon">💹</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                    <h3>Live Trading</h3>
                                    <p>Execute real-time trades with virtual money in a simulated market environment</p>
                                    <button onClick={() => onNavigate('trade')} className="btn-feature">
                                        Start Trading
                                    </button>
                                </div>
                                <div className="feature-card">
<<<<<<< HEAD
                                    <div className="feature-icon"></div>
=======
                                    <div className="feature-icon">🔮</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                    <h3>AI Forecasting</h3>
                                    <p>Advanced machine learning predictions powered by LSTM neural networks</p>
                                    <button onClick={() => onNavigate('forecast')} className="btn-feature">
                                        View Forecasts
                                    </button>
                                </div>
                                <div className="feature-card">
<<<<<<< HEAD
                                    <div className="feature-icon"></div>
=======
                                    <div className="feature-icon">📊</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                    <h3>Portfolio Tracking</h3>
                                    <p>Monitor your investments with detailed analytics and performance metrics</p>
                                    <button onClick={() => onNavigate('portfolio')} className="btn-feature">
                                        View Portfolio
                                    </button>
                                </div>
                                <div className="feature-card">
<<<<<<< HEAD
                                    <div className="feature-icon"></div>
=======
                                    <div className="feature-icon">📚</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                    <h3>Interactive Tutorials</h3>
                                    <p>Learn stock market fundamentals with guided lessons, quizzes, and exercises</p>
                                    <button onClick={onNavigateToTutorials} className="btn-feature tutorial-feature">
                                        Start Learning
                                    </button>
                                </div>
                            </div>

                            {/* Learning Section */}
                            <div className="learning-section">
                                <div className="learning-header">
<<<<<<< HEAD
                                    <h2> Master Stock Market Investing</h2>
=======
                                    <h3>🎓 Master Stock Market Investing</h3>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                    <p>Build your knowledge with our comprehensive tutorial system</p>
                                </div>
                                <div className="learning-features">
                                    <div className="learning-feature">
<<<<<<< HEAD
                                        <span className="learning-icon"></span>
                                        <span>Step-by-step lessons</span>
                                    </div>
                                    <div className="learning-feature">
                                        <span className="learning-icon"></span>
                                        <span>Interactive exercises</span>
                                    </div>
                                    <div className="learning-feature">
                                        <span className="learning-icon"></span>
                                        <span>Knowledge quizzes</span>
                                    </div>
                                    <div className="learning-feature">
                                        <span className="learning-icon"></span>
=======
                                        <span className="learning-icon">📖</span>
                                        <span>Step-by-step lessons</span>
                                    </div>
                                    <div className="learning-feature">
                                        <span className="learning-icon">🧩</span>
                                        <span>Interactive exercises</span>
                                    </div>
                                    <div className="learning-feature">
                                        <span className="learning-icon">📝</span>
                                        <span>Knowledge quizzes</span>
                                    </div>
                                    <div className="learning-feature">
                                        <span className="learning-icon">📊</span>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                        <span>Real case studies</span>
                                    </div>
                                </div>
                                <button onClick={onNavigateToTutorials} className="btn-learning">
<<<<<<< HEAD
                                     Explore Tutorials
=======
                                    🚀 Explore Tutorials
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                </button>
                            </div>

                            <div className="action-buttons">
                                <button onClick={onLogout} className="btn-logout">
<<<<<<< HEAD
                                     Sign Out
=======
                                    🚪 Sign Out
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Home;