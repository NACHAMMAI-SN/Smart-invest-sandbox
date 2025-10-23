import React, { useState, useEffect } from 'react';
import '../styles/Portfolio.css';

const Portfolio = ({ user, onLogout, onNavigate }) => {
    const [portfolio, setPortfolio] = useState([]);
    const [loading, setLoading] = useState(true);
    const [totalValue, setTotalValue] = useState(0);
    const [totalGainLoss, setTotalGainLoss] = useState(0);
    const [marketStatus, setMarketStatus] = useState('');
    const [currentPrices, setCurrentPrices] = useState({});

    // Stock names mapping
    const stockNames = {
        'AAPL': 'Apple Inc.',
        'MSFT': 'Microsoft Corporation',
        'GOOGL': 'Alphabet Inc.',
        'AMZN': 'Amazon.com Inc.',
        'TSLA': 'Tesla Inc.',
        'META': 'Meta Platforms Inc.',
        'NVDA': 'NVIDIA Corporation',
        'AVGO': 'Broadcom Inc.',
        'ORCL': 'Oracle Corporation',
        'CRM': 'Salesforce Inc.',
        'ADBE': 'Adobe Inc.',
        'CSCO': 'Cisco Systems Inc.',
        'INTC': 'Intel Corporation',
        'IBM': 'International Business Machines',
        'QCOM': 'Qualcomm Inc.',
        'TXN': 'Texas Instruments',
        'AMD': 'Advanced Micro Devices',
        'NFLX': 'Netflix Inc.',
        'DIS': 'Walt Disney Company',
        'PEP': 'PepsiCo Inc.',
        'COST': 'Costco Wholesale',
        'TMO': 'Thermo Fisher Scientific',
        'ABNB': 'Airbnb Inc.',
        'UBER': 'Uber Technologies',
        'SNOW': 'Snowflake Inc.',
        'DDOG': 'Datadog Inc.',
        'MRNA': 'Moderna Inc.',
        'PYPL': 'PayPal Holdings',
        'SQ': 'Block Inc.',
        'SHOP': 'Shopify Inc.',
        'NET': 'Cloudflare Inc.',
        'CRWD': 'CrowdStrike Holdings',
        'ZS': 'Zscaler Inc.',
        'PANW': 'Palo Alto Networks',
        'FTNT': 'Fortinet Inc.',
        'OKTA': 'Okta Inc.',
        'TEAM': 'Atlassian Corporation',
        'DOCU': 'DocuSign Inc.',
        'ZM': 'Zoom Video Communications',
        'ROKU': 'Roku Inc.',
        'SPOT': 'Spotify Technology',
        'SNAP': 'Snap Inc.',
        'PINS': 'Pinterest Inc.',
        'TWLO': 'Twilio Inc.',
        'ASAN': 'Asana Inc.',
        'PLTR': 'Palantir Technologies',
        'AI': 'C3.ai Inc.',
        'PATH': 'UiPath Inc.',
        'ESTC': 'Elastic N.V.',
        'MDB': 'MongoDB Inc.',
        'DBX': 'Dropbox Inc.',
        'W': 'Wayfair Inc.',
        'ETSY': 'Etsy Inc.',
        'CHWY': 'Chewy Inc.'
    };

    // Real current stock prices (you can update these or fetch from API)
    const realCurrentPrices = {
        'AAPL': 182.52,
        'MSFT': 413.64,
        'GOOGL': 151.21,
        'AMZN': 178.22,
        'TSLA': 248.42,
        'META': 485.58,
        'NVDA': 118.11,
        'AVGO': 1325.45,
        'ORCL': 124.67,
        'CRM': 298.34,
        'ADBE': 567.89,
        'CSCO': 52.34,
        'INTC': 44.21,
        'IBM': 185.67,
        'QCOM': 167.89,
        'TXN': 175.43,
        'AMD': 176.54,
        'NFLX': 615.67,
        'DIS': 112.34,
        'PEP': 167.89,
        'COST': 723.45,
        'TMO': 556.78,
        'ABNB': 154.32,
        'UBER': 76.54,
        'SNOW': 165.43,
        'DDOG': 123.45,
        'MRNA': 112.34,
        'PYPL': 67.89,
        'SQ': 78.90,
        'SHOP': 145.67,
        'NET': 87.65,
        'CRWD': 298.76,
        'ZS': 234.56,
        'PANW': 345.67,
        'FTNT': 67.89,
        'OKTA': 87.65,
        'TEAM': 234.56,
        'DOCU': 56.78,
        'ZM': 67.89,
        'ROKU': 87.65,
        'SPOT': 298.76,
        'SNAP': 16.54,
        'PINS': 34.56,
        'TWLO': 67.89,
        'ASAN': 23.45,
        'PLTR': 21.34,
        'AI': 28.76,
        'PATH': 23.45,
        'ESTC': 67.89,
        'MDB': 398.76,
        'DBX': 23.45,
        'W': 56.78,
        'ETSY': 67.89,
        'CHWY': 18.76
    };

    useEffect(() => {
        loadPortfolioData();
        updateMarketStatus();
        const interval = setInterval(updateMarketStatus, 60000);
        return () => clearInterval(interval);
    }, [user]);

    const loadPortfolioData = () => {
        try {
            // Load portfolio and transactions from localStorage
            const savedPortfolio = localStorage.getItem('userPortfolio');
            const savedTransactions = localStorage.getItem('userTransactions');

            let portfolioData = {};
            if (savedPortfolio) {
                portfolioData = JSON.parse(savedPortfolio);
            }

            // Convert portfolio object to array with accurate calculations
            const portfolioArray = Object.keys(portfolioData).map(symbol => {
                const quantity = portfolioData[symbol];
                const currentPrice = realCurrentPrices[symbol] || 100;
                const totalValue = quantity * currentPrice;

                // Calculate accurate average cost from transaction history
                const avgPrice = calculateAccurateAveragePrice(symbol, savedTransactions);
                const totalCost = quantity * avgPrice;
                const totalGainLoss = totalValue - totalCost;
                const gainLossPercentage = avgPrice > 0 ? ((totalGainLoss / totalCost) * 100) : 0;

                return {
                    symbol,
                    stockName: stockNames[symbol] || `${symbol} Corporation`,
                    quantity,
                    currentPrice,
                    totalValue,
                    avgPrice,
                    totalCost,
                    totalGainLoss,
                    gainLossPercentage
                };
            });

            setPortfolio(portfolioArray);

            // Calculate accurate totals
            const newTotalValue = portfolioArray.reduce((sum, item) => sum + item.totalValue, 0);
            const newTotalGainLoss = portfolioArray.reduce((sum, item) => sum + item.totalGainLoss, 0);
            const totalInvestment = portfolioArray.reduce((sum, item) => sum + item.totalCost, 0);

            setTotalValue(newTotalValue);
            setTotalGainLoss(newTotalGainLoss);

        } catch (error) {
            console.error('Error loading portfolio:', error);
        } finally {
            setLoading(false);
        }
    };

    const calculateAccurateAveragePrice = (symbol, savedTransactions) => {
        if (!savedTransactions) return realCurrentPrices[symbol] || 100;

        try {
            const transactions = JSON.parse(savedTransactions);
            const stockTransactions = transactions.filter(t => t.symbol === symbol);

            if (stockTransactions.length === 0) return realCurrentPrices[symbol] || 100;

            let totalShares = 0;
            let totalCost = 0;

            // Process transactions in chronological order
            stockTransactions.sort((a, b) => new Date(a.timestamp) - new Date(b.timestamp));

            stockTransactions.forEach(transaction => {
                if (transaction.action === 'BUY') {
                    totalCost += transaction.quantity * transaction.price;
                    totalShares += transaction.quantity;
                } else if (transaction.action === 'SELL') {
                    // When selling, we reduce the average cost basis proportionally
                    const avgPrice = totalShares > 0 ? totalCost / totalShares : 0;
                    totalCost -= transaction.quantity * avgPrice;
                    totalShares -= transaction.quantity;
                }
            });

            return totalShares > 0 ? totalCost / totalShares : realCurrentPrices[symbol] || 100;
        } catch (error) {
            console.error('Error calculating average price:', error);
            return realCurrentPrices[symbol] || 100;
        }
    };

    const updateMarketStatus = () => {
        const now = new Date();
        const day = now.getDay();
        const hours = now.getHours();
        const minutes = now.getMinutes();

        if (day === 0 || day === 6) {
            setMarketStatus('Market closed for weekend');
        } else if (hours < 9 || (hours === 9 && minutes < 30)) {
            const openTime = new Date();
            openTime.setHours(9, 30, 0, 0);
            const diff = openTime - now;
            const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
            const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            setMarketStatus(`Market opens in ${hoursLeft}h ${minutesLeft}m`);
        } else if (hours < 16) {
            const closeTime = new Date();
            closeTime.setHours(16, 0, 0, 0);
            const diff = closeTime - now;
            const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
            const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            setMarketStatus(`Market is open - Closes in ${hoursLeft}h ${minutesLeft}m`);
        } else {
            setMarketStatus('Market closed for today');
        }
    };

    const calculateAnnualReturn = () => {
        const totalInvestment = portfolio.reduce((sum, item) => sum + item.totalCost, 0);
        if (totalInvestment === 0) return '0.00';
        return ((totalGainLoss / totalInvestment) * 100).toFixed(2);
    };

    const getBestPerformingStock = () => {
        if (portfolio.length === 0) return null;
        return portfolio.reduce((best, current) =>
            current.gainLossPercentage > best.gainLossPercentage ? current : best
        );
    };

    const getWorstPerformingStock = () => {
        if (portfolio.length === 0) return null;
        return portfolio.reduce((worst, current) =>
            current.gainLossPercentage < worst.gainLossPercentage ? current : worst
        );
    };

    const handleRefresh = () => {
        setLoading(true);
        setTimeout(() => {
            loadPortfolioData();
        }, 1000);
    };

    const handleQuickTrade = (symbol, action) => {
        onNavigate('trade', { symbol, action });
    };

    const handleForecastNavigation = (symbol = null) => {
        if (symbol) {
            onNavigate('forecast', { symbol });
        } else {
            onNavigate('forecast');
        }
    };

    if (loading) {
        return (
            <div className="loading-screen">
                <div className="loading-spinner"></div>
                <p>Loading portfolio...</p>
            </div>
        );
    }

    const bestStock = getBestPerformingStock();
    const worstStock = getWorstPerformingStock();
    const totalInvestment = portfolio.reduce((sum, item) => sum + item.totalCost, 0);
    const annualReturn = calculateAnnualReturn();

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
                        <div className="balance-label">Available Balance</div>
                        <div className="balance-amount">
                            ${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                        </div>
                        <div className="account-type-badge">Live Trading</div>
                    </div>

                    <div className="user-stats-section">
                        <div className="user-stat-item">
                            <span className="user-stat-label">Total Holdings</span>
                            <span className="stat-value-small">{portfolio.length}</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Portfolio Value</span>
                            <span className="stat-value-small">${totalValue.toFixed(2)}</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Total Return</span>
                            <span className={`stat-value-small ${totalGainLoss >= 0 ? 'positive' : 'negative'}`}>
                                {totalGainLoss >= 0 ? '+' : ''}${Math.abs(totalGainLoss).toFixed(2)}
                            </span>
                        </div>
                    </div>

                    <div className="sidebar-action-buttons">
                        <button onClick={() => onNavigate('dashboard')} className="sidebar-action-button">
                            📊 HOME
                        </button>
                        <button onClick={() => onNavigate('portfolio')} className="sidebar-action-button primary">
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
                <div className="portfolio-container">
                    {/* Market Status Banner */}
                    <div className="market-status-banner">
                        <div className="market-info">
                            <div className={`status-indicator ${marketStatus.includes('open') ? 'open' : 'closed'}`}></div>
                            <span className="market-hours">{marketStatus}</span>
                        </div>
                        <div className="last-updated">
                            Real-time prices • {new Date().toLocaleTimeString()}
                            <button onClick={handleRefresh} className="refresh-btn">
                                🔄 Refresh
                            </button>
                        </div>
                    </div>

                    {/* Portfolio Header */}
                    <div className="portfolio-header">
                        <h1>📊 Portfolio Overview</h1>
                        <p>Track your investments and performance with accurate calculations</p>
                        <div className="header-actions">
                            <button onClick={() => onNavigate('trade')} className="btn-primary">
                                ➕ Trade Stocks
                            </button>
                            <button onClick={() => handleForecastNavigation()} className="btn-forecast">
                                🔮 Get AI Forecast
                            </button>
                        </div>
                    </div>

                    {/* Portfolio Stats Grid */}
                    <div className="portfolio-stats-grid">
                        <div className="stats-side">
                            <div className="stat-card main-stat">
                                <h3>TOTAL PORTFOLIO VALUE</h3>
                                <div className="stat-value">${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
                                <div className={`stat-change ${totalGainLoss >= 0 ? 'positive' : 'negative'}`}>
                                    {totalGainLoss >= 0 ? '+' : ''}${Math.abs(totalGainLoss).toFixed(2)} ({annualReturn}%)
                                </div>
                            </div>

                            <div className="stat-card">
                                <h3>TOTAL RETURN</h3>
                                <div className="stat-value">{annualReturn}%</div>
                                <div className={`stat-change ${parseFloat(annualReturn) >= 0 ? 'positive' : 'negative'}`}>
                                    {parseFloat(annualReturn) >= 0 ? '+' : ''}{annualReturn}%
                                </div>
                            </div>

                            <div className="stat-card">
                                <h3>AVAILABLE CASH</h3>
                                <div className="stat-value">${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}</div>
                                <div className="stat-change neutral">
                                    Ready to invest
                                </div>
                            </div>

                            <div className="stat-card">
                                <h3>TOTAL INVESTMENT</h3>
                                <div className="stat-value">${totalInvestment.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}</div>
                                <div className="stat-change neutral">
                                    Principal amount
                                </div>
                            </div>
                        </div>

                        <div className="summary-side">
                            <div className="portfolio-summary">
                                <div className="summary-item">
                                    <span>TOTAL HOLDINGS</span>
                                    <strong>{portfolio.length} {portfolio.length === 1 ? 'stock' : 'stocks'}</strong>
                                </div>
                                <div className="summary-item">
                                    <span>TOTAL GAIN/LOSS</span>
                                    <strong className={totalGainLoss >= 0 ? 'positive' : 'negative'}>
                                        {totalGainLoss >= 0 ? '+' : '-'}${Math.abs(totalGainLoss).toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                                        {totalGainLoss >= 0 ? ' 📈' : ' 📉'}
                                    </strong>
                                </div>
                                <div className="summary-item">
                                    <span>PORTFOLIO DIVERSITY</span>
                                    <strong>{portfolio.length > 3 ? 'Excellent' : portfolio.length > 1 ? 'Good' : 'Low'}</strong>
                                </div>
                                <div className="summary-item">
                                    <span>AVERAGE RETURN</span>
                                    <strong className={parseFloat(annualReturn) >= 0 ? 'positive' : 'negative'}>
                                        {annualReturn}%
                                    </strong>
                                </div>
                            </div>

                            {/* Performance Highlights */}
                            {portfolio.length > 0 && (
                                <div className="performance-highlights">
                                    <h4>🎯 Performance Highlights</h4>
                                    {bestStock && bestStock.gainLossPercentage !== 0 && (
                                        <div className={`highlight-item ${bestStock.gainLossPercentage >= 0 ? 'positive' : 'negative'}`}>
                                            <span>BEST PERFORMER</span>
                                            <div className="highlight-content">
                                                <strong>{bestStock.symbol} {bestStock.gainLossPercentage >= 0 ? '+' : ''}{bestStock.gainLossPercentage.toFixed(2)}%</strong>
                                                <button
                                                    onClick={() => handleForecastNavigation(bestStock.symbol)}
                                                    className="btn-forecast-small"
                                                    title="Get AI forecast for this stock"
                                                >
                                                    🔮
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                    {worstStock && worstStock.gainLossPercentage !== 0 && (
                                        <div className={`highlight-item ${worstStock.gainLossPercentage >= 0 ? 'positive' : 'negative'}`}>
                                            <span>WORST PERFORMER</span>
                                            <div className="highlight-content">
                                                <strong>{worstStock.symbol} {worstStock.gainLossPercentage >= 0 ? '+' : ''}{worstStock.gainLossPercentage.toFixed(2)}%</strong>
                                                <button
                                                    onClick={() => handleForecastNavigation(worstStock.symbol)}
                                                    className="btn-forecast-small"
                                                    title="Get AI forecast for this stock"
                                                >
                                                    🔮
                                                </button>
                                            </div>
                                        </div>
                                    )}
                                    {portfolio.length > 1 && (
                                        <div className="highlight-item neutral">
                                            <span>DIVERSIFICATION</span>
                                            <strong>{portfolio.length} holdings</strong>
                                        </div>
                                    )}
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Portfolio Table */}
                    <div className="portfolio-table">
                        <div className="table-header">
                            <h3>📋 Your Stock Holdings</h3>
                            <div className="table-header-actions">
                                <button onClick={() => onNavigate('trade')} className="btn-primary">
                                    ➕ Trade Stocks
                                </button>
                                <button onClick={() => handleForecastNavigation()} className="btn-forecast">
                                    🔮 AI Forecast
                                </button>
                            </div>
                        </div>
                        {portfolio.length === 0 ? (
                            <div className="empty-portfolio">
                                <div className="empty-icon">📊</div>
                                <h3>Your portfolio is empty</h3>
                                <p>Start building your investment portfolio by making your first trade!</p>
                                <div className="empty-portfolio-actions">
                                    <button onClick={() => onNavigate('trade')} className="btn-primary">
                                        🚀 Start Trading
                                    </button>
                                    <button onClick={() => handleForecastNavigation()} className="btn-forecast">
                                        🔮 Explore Forecasts
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <table>
                                <thead>
                                <tr>
                                    <th>Symbol</th>
                                    <th>Company Name</th>
                                    <th>Current Price</th>
                                    <th>Quantity</th>
                                    <th>Avg Cost</th>
                                    <th>Total Value</th>
                                    <th>Gain/Loss</th>
                                    <th>Return %</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                {portfolio.map((item) => (
                                    <tr key={item.symbol}>
                                        <td>
                                            <strong className="stock-symbol">{item.symbol}</strong>
                                        </td>
                                        <td className="stock-name">{item.stockName}</td>
                                        <td className="stock-price">${item.currentPrice.toFixed(2)}</td>
                                        <td className="stock-quantity">{item.quantity}</td>
                                        <td className="stock-avg-cost">${item.avgPrice.toFixed(2)}</td>
                                        <td className="stock-value">${item.totalValue.toFixed(2)}</td>
                                        <td className={item.totalGainLoss >= 0 ? 'positive' : 'negative'}>
                                            {item.totalGainLoss >= 0 ? '+' : ''}${Math.abs(item.totalGainLoss).toFixed(2)}
                                        </td>
                                        <td className={item.gainLossPercentage >= 0 ? 'positive' : 'negative'}>
                                            {item.gainLossPercentage >= 0 ? '+' : ''}{item.gainLossPercentage.toFixed(2)}%
                                        </td>
                                        <td>
                                            <div className="action-buttons-small">
                                                <button
                                                    onClick={() => handleQuickTrade(item.symbol, 'BUY')}
                                                    className="btn-buy"
                                                    title={`Buy more ${item.symbol}`}
                                                >
                                                    Buy
                                                </button>
                                                <button
                                                    onClick={() => handleQuickTrade(item.symbol, 'SELL')}
                                                    className="btn-sell"
                                                    title={`Sell ${item.symbol}`}
                                                >
                                                    Sell
                                                </button>
                                                <button
                                                    onClick={() => handleForecastNavigation(item.symbol)}
                                                    className="btn-forecast-small"
                                                    title={`Get AI forecast for ${item.symbol}`}
                                                >
                                                    🔮
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        )}
                    </div>

                    {/* Quick Stats */}
                    {portfolio.length > 0 && (
                        <div className="quick-stats-section">
                            <h3>📈 Portfolio Snapshot</h3>
                            <div className="quick-stats-grid">
                                <div className="quick-stat">
                                    <span>Total Holdings</span>
                                    <strong>{portfolio.length}</strong>
                                </div>
                                <div className="quick-stat">
                                    <span>Average Return</span>
                                    <strong className={parseFloat(annualReturn) >= 0 ? 'positive' : 'negative'}>
                                        {annualReturn}%
                                    </strong>
                                </div>
                                <div className="quick-stat">
                                    <span>Best Performer</span>
                                    <strong>{bestStock?.symbol || 'N/A'}</strong>
                                </div>
                                <div className="quick-stat">
                                    <span>Total Investment</span>
                                    <strong>${totalInvestment.toFixed(2)}</strong>
                                </div>
                            </div>
                            <div className="quick-stats-actions">
                                <button onClick={() => handleForecastNavigation()} className="btn-forecast">
                                    🔮 Get Portfolio Forecast
                                </button>
                            </div>
                        </div>
                    )}

                    {/* Portfolio Tips */}
                    <div className="portfolio-tips">
                        <h3>💡 Portfolio Management Tips</h3>
                        <div className="tips-grid">
                            <div className="tip-card">
                                <span className="tip-icon">📊</span>
                                <h4>Track Your Performance</h4>
                                <p>Monitor your gain/loss percentages regularly to make informed investment decisions.</p>
                            </div>
                            <div className="tip-card">
                                <span className="tip-icon">🔮</span>
                                <h4>Use AI Forecasting</h4>
                                <p>Leverage AI predictions to anticipate market trends and optimize your portfolio strategy.</p>
                            </div>
                            <div className="tip-card">
                                <span className="tip-icon">💰</span>
                                <h4>Understand Your Costs</h4>
                                <p>Keep track of your average cost basis to accurately calculate your returns.</p>
                            </div>
                            <div className="tip-card">
                                <span className="tip-icon">🎯</span>
                                <h4>Set Realistic Goals</h4>
                                <p>Aim for consistent returns rather than chasing short-term gains.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Portfolio;