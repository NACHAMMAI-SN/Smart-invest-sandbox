import React, { useState, useEffect } from 'react';
import Chatbot from './Chatbot';
import '../styles/Dashboard.css';

const Dashboard = ({ user, onLogout, onNavigate, transactions = [], portfolio = [], onUpdateTransactions, onUpdatePortfolio }) => {
    const [stockData, setStockData] = useState(null);
    const [searchTerm, setSearchTerm] = useState('JPM');
    const [loading, setLoading] = useState(false);
    const [marketStatus, setMarketStatus] = useState('');
    const [showChatbot, setShowChatbot] = useState(false);

    const fetchStockData = async (symbol = 'JPM') => {
        setLoading(true);
        try {
            const response = await fetch(`http://localhost:8080/api/stocks/${symbol}`);
            const data = await response.json();
            setStockData(data);
        } catch (error) {
            console.error('Error fetching stock data:', error);
        } finally {
            setLoading(false);
        }
    };

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
        const interval = setInterval(updateMarketStatus, 60000); // Update every minute
        return () => clearInterval(interval);
    }, []);

    useEffect(() => {
        fetchStockData();
    }, []);

    const handleSearch = (e) => {
        e.preventDefault();
        if (searchTerm.trim()) {
            fetchStockData(searchTerm);
        }
    };

    // Load transactions and portfolio data for chatbot
    useEffect(() => {
        const loadUserData = () => {
            try {
                // Load transactions from localStorage
                const savedTransactions = localStorage.getItem('userTransactions');
                if (savedTransactions) {
                    const transactionsData = JSON.parse(savedTransactions);
                    if (onUpdateTransactions) {
                        onUpdateTransactions(transactionsData);
                    }
                }

                // Load portfolio from localStorage
                const savedPortfolio = localStorage.getItem('userPortfolio');
                if (savedPortfolio) {
                    const portfolioData = JSON.parse(savedPortfolio);
                    if (onUpdatePortfolio) {
                        onUpdatePortfolio(portfolioData);
                    }
                }
            } catch (error) {
                console.error('Error loading user data:', error);
            }
        };

        loadUserData();
    }, [onUpdateTransactions, onUpdatePortfolio]);

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
                        <div className="user-detail-item">
                            <span className="user-detail-label">Total Trades:</span>
                            <span className="user-detail-value">{transactions?.length || 0}</span>
                        </div>
                        <div className="user-detail-item">
                            <span className="user-detail-label">Portfolio Holdings:</span>
                            <span className="user-detail-value">{portfolio?.length || 0}</span>
                        </div>
                    </div>

                    <div className="virtual-balance-card">
                        <div className="balance-label">VIRTUAL BALANCE</div>
                        <div className="balance-amount">
                            ${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '98,809.76'}
                        </div>
                        <div className="account-type-badge">Simulation Account</div>
                    </div>

                    {/* User Stats Section - FIXED VISIBILITY */}
                    <div className="user-stats-section">
                        <div className="stats-header">ACCOUNT DETAILS</div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">RISK LEVEL</span>
                            <span className="risk-level-text">{user?.experience || 'Beginner'}</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">PLATFORM</span>
                            <span className="platform-text">Simulation</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">EXPERIENCE</span>
                            <span className="experience-text">{user?.experience || 'Beginner'}</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">AI FEATURES</span>
                            <span className="ai-features-text">Enabled</span>
                        </div>
                    </div>

                    <div className="sidebar-action-buttons">
                        <button onClick={() => onNavigate('dashboard')} className="sidebar-action-button primary">
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
                        <button onClick={() => setShowChatbot(true)} className="sidebar-action-button chatbot-btn">
                            🤖 AI ASSISTANT
                        </button>
                    </div>

                    <div className="sign-out-section">
                        <button onClick={onLogout} className="sign-out-button">
                            🚪 Sign Out
                        </button>
                    </div>
                </div>
            </div>

            {/* Main Content */}
            <div className="main-content">
                {/* Welcome Section */}
                <div className="welcome-section">
                    <h1>Welcome to Smart Invest</h1>
                    <h2>Hello, {user?.username || 'User'}!</h2>

                    <div className="user-info-grid">
                        <div className="info-item">
                            <strong>Email:</strong>
                            <span>{user?.email || 'N/A'}</span>
                        </div>
                        <div className="info-item">
                            <strong>Virtual Balance:</strong>
                            <span>${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '98,809.76'}</span>
                        </div>
                        <div className="info-item">
                            <strong>Account Type:</strong>
                            <span>Simulation Account</span>
                        </div>
                        <div className="info-item">
                            <strong>Market Status:</strong>
                            <span>{marketStatus}</span>
                        </div>
                        <div className="info-item">
                            <strong>Total Trades:</strong>
                            <span>{transactions?.length || 0}</span>
                        </div>
                        <div className="info-item">
                            <strong>Portfolio Holdings:</strong>
                            <span>{portfolio?.length || 0}</span>
                        </div>
                    </div>
                </div>

                {/* Quick Actions */}
                <div className="quick-actions-main">
                    <div className="action-buttons-main">
                        <button onClick={() => onNavigate('portfolio')} className="action-btn-main portfolio">
                            <span className="action-icon">📊</span>
                            <span className="action-text">VIEW PORTFOLIO</span>
                        </button>
                        <button onClick={() => onNavigate('trade')} className="action-btn-main trade">
                            <span className="action-icon">💹</span>
                            <span className="action-text">START TRADING</span>
                        </button>
                        <button onClick={() => onNavigate('forecast')} className="action-btn-main forecast">
                            <span className="action-icon">🔮</span>
                            <span className="action-text">AI FORECAST</span>
                        </button>
                        <button onClick={() => onNavigate('news')} className="action-btn-main news">
                            <span className="action-icon">📰</span>
                            <span className="action-text">MARKET NEWS</span>
                        </button>
                        <button onClick={() => onNavigate('transactions')} className="action-btn-main transactions">
                            <span className="action-icon">📋</span>
                            <span className="action-text">TRANSACTIONS</span>
                        </button>
                        <button onClick={() => setShowChatbot(true)} className="action-btn-main chatbot">
                            <span className="action-icon">🤖</span>
                            <span className="action-text">AI ASSISTANT</span>
                        </button>
                    </div>
                </div>

                {/* AI Assistant Quick Stats */}
                <div className="ai-assistant-section">
                    <div className="ai-header">
                        <h3>🤖 AI Assistant Ready to Help</h3>
                        <p>Ask me about your portfolio, stock prices, or investment advice</p>
                    </div>
                    <div className="ai-quick-stats">
                        <div className="ai-stat-card">
                            <div className="ai-stat-icon">💰</div>
                            <div className="ai-stat-content">
                                <h4>Account Balance</h4>
                                <p>${user?.balance?.toLocaleString() || '98,809.76'}</p>
                            </div>
                        </div>
                        <div className="ai-stat-card">
                            <div className="ai-stat-icon">📊</div>
                            <div className="ai-stat-content">
                                <h4>Portfolio Holdings</h4>
                                <p>{portfolio?.length || 0} stocks</p>
                            </div>
                        </div>
                        <div className="ai-stat-card">
                            <div className="ai-stat-icon">📋</div>
                            <div className="ai-stat-content">
                                <h4>Total Trades</h4>
                                <p>{transactions?.length || 0} transactions</p>
                            </div>
                        </div>
                        <div className="ai-stat-card">
                            <div className="ai-stat-icon">🔮</div>
                            <div className="ai-stat-content">
                                <h4>AI Forecast</h4>
                                <p>Ready to analyze</p>
                            </div>
                        </div>
                    </div>
                    <button
                        onClick={() => setShowChatbot(true)}
                        className="btn-ai-assistant"
                    >
                        🤖 Open AI Assistant
                    </button>
                </div>

                {/* Search Section */}
                <div className="search-section">
                    <h3>Stock Search</h3>
                    <form onSubmit={handleSearch} className="search-form">
                        <input
                            type="text"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value.toUpperCase())}
                            placeholder="Enter stock symbol (e.g., JPM, AAPL, GOOGL)"
                        />
                        <button type="submit">SEARCH</button>
                    </form>
                </div>

                {loading ? (
                    <div className="loading">
                        <div className="loading-spinner"></div>
                        Loading stock data...
                    </div>
                ) : stockData ? (
                    <div className="stock-details">
                        <div className="stock-header">
                            <h2>{stockData.symbol}</h2>
                            <p>{stockData.name} | {stockData.exchange}</p>
                            <div className="stock-price">
                                <span className="price">${stockData.price}</span>
                                <span className={`change ${stockData.change >= 0 ? 'positive' : 'negative'}`}>
                                    {stockData.change >= 0 ? '+' : ''}{stockData.change} ({stockData.changePercent})
                                </span>
                            </div>
                        </div>

                        <div className="stock-info">
                            <table>
                                <thead>
                                <tr>
                                    <th>Symbol</th>
                                    <th>Name</th>
                                    <th>Exchange</th>
                                    <th>Country</th>
                                    <th>Currency</th>
                                    <th>MIC</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td><strong>{stockData.symbol}</strong></td>
                                    <td>{stockData.name}</td>
                                    <td>{stockData.exchange}</td>
                                    <td>{stockData.country}</td>
                                    <td>{stockData.currency}</td>
                                    <td>{stockData.mic}</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>

                        <div className="stock-actions">
                            <button
                                onClick={() => onNavigate('trade')}
                                className="btn-primary"
                            >
                                Trade {stockData.symbol}
                            </button>
                            <button
                                onClick={() => onNavigate('forecast')}
                                className="btn-primary"
                            >
                                Forecast {stockData.symbol}
                            </button>
                            <button
                                onClick={() => setShowChatbot(true)}
                                className="btn-ai"
                            >
                                🤖 Ask AI About {stockData.symbol}
                            </button>
                        </div>
                    </div>
                ) : (
                    <div className="error">
                        <p>No stock data available</p>
                        <p>Try searching for a valid stock symbol like JPM, AAPL, or GOOGL</p>
                    </div>
                )}
            </div>

            {/* Chatbot Component */}
            {showChatbot && (
                <div className="chatbot-overlay">
                    <div className="chatbot-wrapper">
                        <Chatbot
                            onClose={() => setShowChatbot(false)}
                            user={user}
                            transactions={transactions}
                            portfolio={portfolio}
                            onNavigate={onNavigate}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default Dashboard;