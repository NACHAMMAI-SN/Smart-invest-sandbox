import React, { useState, useEffect } from 'react';
import '../styles/News.css';

const News = ({ user, onLogout, onNavigate }) => {
    const [marketStatus, setMarketStatus] = useState('');
    const [currentTime, setCurrentTime] = useState(new Date());
    const [newsItems, setNewsItems] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [activeFilter, setActiveFilter] = useState('all');

    // Calculate real-time market status and update time
    useEffect(() => {
        const updateMarketStatus = () => {
            const now = new Date();
            setCurrentTime(now);

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

    // Fetch real news data from NewsAPI
    const fetchRealNews = async () => {
        setLoading(true);
        setError(null);

        try {
<<<<<<< HEAD
            const API_KEY = import.meta.env.VITE_NEWS_API_KEY; // Your actual API key
=======
            const API_KEY = '2235a4b502b249da9e24aaa71a7a230f'; // Your actual API key
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
            const query = getQueryByFilter(activeFilter);

            const response = await fetch(
                `https://newsapi.org/v2/everything?q=${query}&sortBy=publishedAt&language=en&pageSize=15&apiKey=${API_KEY}`
            );

            if (!response.ok) {
                throw new Error(`Failed to fetch news: ${response.status}`);
            }

            const data = await response.json();

            if (data.articles && data.articles.length > 0) {
                const formattedNews = data.articles.map((article, index) => ({
                    id: index + 1,
                    title: article.title,
                    author: article.author || article.source?.name || 'Unknown Author',
                    date: new Date(article.publishedAt),
                    content: article.description || 'No description available',
                    source: article.source.name,
                    url: article.url,
                    category: getCategoryFromContent(article.title + ' ' + (article.description || '')),
                    image: article.urlToImage,
                    isRecent: isRecentArticle(new Date(article.publishedAt))
                }));
                setNewsItems(formattedNews);
            } else {
                setNewsItems(getFallbackNews());
            }
        } catch (err) {
            console.error('Error fetching news:', err);
            setError('Failed to load live news. Showing sample market data.');
            setNewsItems(getFallbackNews());
        } finally {
            setLoading(false);
        }
    };

    const getQueryByFilter = (filter) => {
        const baseQuery = 'stock market OR stocks OR investing OR trading OR finance';
        switch (filter) {
            case 'technology':
                return 'technology stocks OR tech companies OR AI investing';
            case 'banking':
                return 'banking OR financial institutions OR JPMorgan OR Goldman Sachs';
            case 'economy':
                return 'Federal Reserve OR interest rates OR inflation OR economy';
            case 'commodities':
                return 'oil prices OR commodities OR gold OR crude oil';
            case 'crypto':
                return 'cryptocurrency OR bitcoin OR ethereum OR blockchain';
            case 'forecast':
                return 'stock forecast OR market prediction OR AI trading OR machine learning';
            default:
                return baseQuery;
        }
    };

    const isRecentArticle = (articleDate) => {
        const oneHourAgo = new Date(currentTime.getTime() - 60 * 60 * 1000);
        return articleDate > oneHourAgo;
    };

    // Fallback news data in case API fails
    const getFallbackNews = () => {
        const now = new Date();
        return [
            {
                id: 1,
                title: "Stock Markets Show Mixed Results in Early Trading",
                author: "Market Watch",
                date: new Date(now.getTime() - 30 * 60 * 1000),
                content: "Major indices are showing mixed performance in today's trading session. Technology stocks are leading gains while energy sectors face pressure.",
                source: "MarketWatch",
                url: "https://www.marketwatch.com",
                category: "Market News",
                image: null,
                isRecent: true
            },
            {
                id: 2,
                title: "Federal Reserve Meeting Minutes Released",
                author: "Federal Reserve",
                date: new Date(now.getTime() - 2 * 60 * 60 * 1000),
                content: "The Federal Reserve has released its latest meeting minutes, indicating ongoing concerns about inflation and potential future rate decisions.",
                source: "Federal Reserve",
                url: "https://www.federalreserve.gov",
                category: "Economy",
                image: null,
                isRecent: true
            },
            {
                id: 3,
                title: "Tech Giants Report Strong Quarterly Earnings",
                author: "Tech Financial News",
                date: new Date(now.getTime() - 3 * 60 * 60 * 1000),
                content: "Major technology companies have reported better-than-expected quarterly earnings, driven by strong performance in cloud computing and AI services.",
                source: "Bloomberg Technology",
                url: "https://www.bloomberg.com/technology",
                category: "Technology",
                image: null,
                isRecent: false
            },
            {
                id: 4,
                title: "AI-Powered Trading Algorithms Show Promising Results",
                author: "AI Finance Review",
                date: new Date(now.getTime() - 45 * 60 * 1000),
                content: "Machine learning models are increasingly being used for stock price prediction, with some hedge funds reporting significant improvements in trading accuracy.",
                source: "Financial AI Journal",
                url: "#",
                category: "Forecast",
                image: null,
                isRecent: true
            },
            {
                id: 5,
                title: "LSTM Neural Networks Revolutionize Market Forecasting",
                author: "Tech Analytics",
                date: new Date(now.getTime() - 90 * 60 * 1000),
                content: "Advanced deep learning techniques are providing more accurate stock market predictions, helping traders make better investment decisions.",
                source: "AI Trading Digest",
                url: "#",
                category: "Forecast",
                image: null,
                isRecent: false
            }
        ];
    };

    // Helper function to categorize news based on content
    const getCategoryFromContent = (content) => {
        const lowerContent = content.toLowerCase();
        if (lowerContent.includes('tech') || lowerContent.includes('ai') || lowerContent.includes('apple') || lowerContent.includes('microsoft') || lowerContent.includes('google')) {
            return 'Technology';
        } else if (lowerContent.includes('oil') || lowerContent.includes('crude') || lowerContent.includes('commodity') || lowerContent.includes('gold')) {
            return 'Commodities';
        } else if (lowerContent.includes('fed') || lowerContent.includes('interest rate') || lowerContent.includes('inflation') || lowerContent.includes('economy')) {
            return 'Economy';
        } else if (lowerContent.includes('bank') || lowerContent.includes('jpmorgan') || lowerContent.includes('goldman') || lowerContent.includes('financial')) {
            return 'Banking';
        } else if (lowerContent.includes('crypto') || lowerContent.includes('bitcoin') || lowerContent.includes('ethereum') || lowerContent.includes('blockchain')) {
            return 'Cryptocurrency';
        } else if (lowerContent.includes('forecast') || lowerContent.includes('prediction') || lowerContent.includes('ai') || lowerContent.includes('machine learning') || lowerContent.includes('neural network')) {
            return 'Forecast';
        } else {
            return 'Market News';
        }
    };

    // Fetch news on component mount and when filter changes
    useEffect(() => {
        fetchRealNews();
    }, [activeFilter]);

    // Auto-refresh news every 10 minutes
    useEffect(() => {
        const newsInterval = setInterval(fetchRealNews, 10 * 60 * 1000);
        return () => clearInterval(newsInterval);
    }, [activeFilter]);

    const handleReadMore = (url) => {
        if (url && url !== '#') {
            window.open(url, '_blank', 'noopener,noreferrer');
        }
    };

    const formatRelativeTime = (date) => {
        const now = currentTime;
        const diffInMs = now - date;
        const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
        const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));

        if (diffInMinutes < 1) return 'Just now';
        if (diffInMinutes < 60) return `${diffInMinutes}m ago`;
        if (diffInHours < 24) return `${diffInHours}h ago`;
        return `${Math.floor(diffInHours / 24)}d ago`;
    };

    const getNewsStats = () => {
        const oneHourAgo = new Date(currentTime.getTime() - 60 * 60 * 1000);
        const recentNews = newsItems.filter(news => news.date > oneHourAgo);
        const sources = new Set(newsItems.map(item => item.source));

        return {
            recent: recentNews.length,
            total: newsItems.length,
            sources: sources.size
        };
    };

    const newsStats = getNewsStats();

    const filteredNews = newsItems.filter(news => {
        if (activeFilter === 'all') return true;
        return news.category.toLowerCase() === activeFilter.toLowerCase();
    });

    if (loading) {
        return (
            <div className="dashboard-container">
                <div className="sidebar">
                    {/* Same sidebar as below */}
                </div>
                <div className="main-content">
                    <div className="loading-news">
                        <div className="loading-spinner"></div>
                        <p>Fetching latest market news...</p>
                    </div>
                </div>
            </div>
        );
    }

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
                        <div className="user-stat-item">
                            <span className="user-stat-label">AI News</span>
                            <span className="ai-features-text">Enabled</span>
                        </div>
                    </div>

                    <div className="sidebar-action-buttons">
                        <button onClick={() => onNavigate('dashboard')} className="sidebar-action-button">
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
                        <button onClick={() => onNavigate('news')} className="sidebar-action-button primary">
                             NEWS
                        </button>
                        <button onClick={() => onNavigate('transactions')} className="sidebar-action-button">
                             TRANSACTIONS
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
                        <button onClick={() => onNavigate('news')} className="sidebar-action-button primary">
                            📰 NEWS
                        </button>
                        <button onClick={() => onNavigate('transactions')} className="sidebar-action-button">
                            📋 TRANSACTIONS
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
                <div className="news-header">
<<<<<<< HEAD
                    <h1> Live Market News</h1>
=======
                    <h1>📰 Live Market News</h1>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                    <p>Real-time financial news and market updates • {currentTime.toLocaleString()}</p>
                    <div className="news-stats">
                        <div className="stat-item">
                            <span className="stat-number">{newsStats.recent}</span>
                            <span className="stat-label">Last Hour</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-number">{newsStats.total}</span>
                            <span className="stat-label">Total Stories</span>
                        </div>
                        <div className="stat-item">
                            <span className="stat-number">{newsStats.sources}</span>
                            <span className="stat-label">News Sources</span>
                        </div>
                    </div>
                </div>

                {error && (
                    <div className="news-error">
<<<<<<< HEAD
                        <span> {error}</span>
=======
                        <span>⚠️ {error}</span>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                    </div>
                )}

                <div className="news-filters">
                    <button
                        className={`filter-btn ${activeFilter === 'all' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('all')}
                    >
                        All News
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'technology' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('technology')}
                    >
                        Technology
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'economy' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('economy')}
                    >
                        Economy
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'banking' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('banking')}
                    >
                        Banking
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'commodities' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('commodities')}
                    >
                        Commodities
                    </button>
                    <button
                        className={`filter-btn ${activeFilter === 'forecast' ? 'active' : ''}`}
                        onClick={() => setActiveFilter('forecast')}
                    >
                        AI Forecast
                    </button>
                </div>

                <div className="news-container">
                    {filteredNews.length > 0 ? (
                        filteredNews.map((news) => (
                            <div key={news.id} className="news-card">
                                <div className="news-header-badge">
                                    <span className={`category-badge ${news.category.toLowerCase()}`}>
                                        {news.category}
                                    </span>
<<<<<<< HEAD
                                    {news.isRecent && <span className="recent-badge"> Recent</span>}
=======
                                    {news.isRecent && <span className="recent-badge">🆕 Recent</span>}
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                </div>
                                <div className="news-content">
                                    <h3>{news.title}</h3>
                                    <div className="news-meta">
                                        <span className="author">{news.author}</span>
                                        <span className="source">• {news.source}</span>
                                        <span className="date">• {formatRelativeTime(news.date)}</span>
                                    </div>
                                    <p className="news-description">{news.content}</p>
                                    <div className="news-actions">
                                        <button
                                            className="read-more"
                                            onClick={() => handleReadMore(news.url)}
                                            title={`Read full story on ${news.source}`}
                                            disabled={!news.url || news.url === '#'}
                                        >
                                            {news.url && news.url !== '#' ?
<<<<<<< HEAD
                                                ` Read Full Story on ${news.source}` :
                                                ' Source Not Available'
=======
                                                `📖 Read Full Story on ${news.source}` :
                                                '🔒 Source Not Available'
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                            }
                                        </button>
                                        {news.category === 'Forecast' && (
                                            <button
                                                className="forecast-cta"
                                                onClick={() => onNavigate('forecast')}
                                                title="Try our AI Forecasting tool"
                                            >
<<<<<<< HEAD
                                                 Try AI Forecast
=======
                                                🔮 Try AI Forecast
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                                            </button>
                                        )}
                                    </div>
                                </div>
                            </div>
                        ))
                    ) : (
                        <div className="no-news">
<<<<<<< HEAD
                            <div className="no-news-icon"></div>
=======
                            <div className="no-news-icon">📰</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                            <h3>No news available for this category</h3>
                            <p>Try selecting a different filter or check back later</p>
                        </div>
                    )}
                </div>

                <div className="news-footer">
<<<<<<< HEAD
                    <p> Auto-refreshes every 10 minutes •  Powered by NewsAPI •  AI Forecasting Available</p>
=======
                    <p>🔄 Auto-refreshes every 10 minutes • 📊 Powered by NewsAPI • 🔮 AI Forecasting Available</p>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                    <p className="last-updated">Last updated: {currentTime.toLocaleString()}</p>
                </div>
            </div>
        </div>
    );
};

export default News;