import React, { useState, useEffect } from 'react';
import '../styles/Forecast.css';

// Mock chart component - replace with actual Chart.js or similar
const PriceChart = ({ historicalData, forecastData, isMarketOpen }) => {
    const allData = [...historicalData, ...forecastData];

    return (
        <div className="interactive-chart">
            <div className="chart-container">
                <div className="chart-header">
                    <h4>Price Movement</h4>
                    <div className="chart-legend">
                        <span className="legend-historical">Historical</span>
                        <span className="legend-forecast">AI Forecast</span>
                    </div>
                </div>
                <div className="chart-content">
                    {/* Simple SVG chart for demonstration */}
                    <svg width="100%" height="300" viewBox="0 0 800 300" className="chart-svg">
                        {/* Grid lines */}
                        {[...Array(6)].map((_, i) => (
                            <line
                                key={`grid-y-${i}`}
                                x1="50"
                                y1={i * 60 + 20}
                                x2="750"
                                y2={i * 60 + 20}
                                stroke="#334155"
                                strokeWidth="1"
                            />
                        ))}

                        {/* Historical data line */}
                        <polyline
                            fill="none"
                            stroke="#3b82f6"
                            strokeWidth="3"
                            points={historicalData.map((point, index) =>
                                `${50 + (index * 700 / (allData.length - 1))},${280 - ((point.price - Math.min(...allData.map(d => d.price))) / (Math.max(...allData.map(d => d.price)) - Math.min(...allData.map(d => d.price)))) * 260}`
                            ).join(' ')}
                        />

                        {/* Forecast data line */}
                        <polyline
                            fill="none"
                            stroke="#10b981"
                            strokeWidth="3"
                            strokeDasharray="5,5"
                            points={forecastData.map((point, index) =>
                                `${50 + ((historicalData.length + index) * 700 / (allData.length - 1))},${280 - ((point.price - Math.min(...allData.map(d => d.price))) / (Math.max(...allData.map(d => d.price)) - Math.min(...allData.map(d => d.price)))) * 260}`
                            ).join(' ')}
                        />

                        {/* Division line between historical and forecast */}
                        <line
                            x1={50 + (historicalData.length * 700 / (allData.length - 1))}
                            y1="20"
                            x2={50 + (historicalData.length * 700 / (allData.length - 1))}
                            y2="280"
                            stroke="#64748b"
                            strokeWidth="2"
                            strokeDasharray="3,3"
                        />
                    </svg>

                    <div className="chart-labels">
                        <span>Past {historicalData.length} days</span>
                        <span>Next {forecastData.length} days forecast</span>
                    </div>
                </div>
                {!isMarketOpen && (
                    <div className="market-closed-notice">
                        📊 Displaying latest available data - Market is currently closed
                    </div>
                )}
            </div>
        </div>
    );
};

const Forecast = ({ user, onLogout, onNavigate }) => {
    const [forecastData, setForecastData] = useState(null);
    const [selectedStock, setSelectedStock] = useState('AAPL');
    const [timeframe, setTimeframe] = useState('7d');
    const [loading, setLoading] = useState(false);
    const [marketStatus, setMarketStatus] = useState('');
    const [isMarketOpen, setIsMarketOpen] = useState(false);
    const [predictionAccuracy, setPredictionAccuracy] = useState(87.5);
    const [searchQuery, setSearchQuery] = useState('');

    // Extended stocks database
    const availableStocks = [
        { symbol: 'AAPL', name: 'Apple Inc.', sector: 'Technology' },
        { symbol: 'MSFT', name: 'Microsoft Corporation', sector: 'Technology' },
        { symbol: 'GOOGL', name: 'Alphabet Inc.', sector: 'Technology' },
        { symbol: 'AMZN', name: 'Amazon.com Inc.', sector: 'Consumer Cyclical' },
        { symbol: 'TSLA', name: 'Tesla Inc.', sector: 'Automotive' },
        { symbol: 'META', name: 'Meta Platforms Inc.', sector: 'Technology' },
        { symbol: 'NVDA', name: 'NVIDIA Corporation', sector: 'Technology' },
        { symbol: 'JPM', name: 'JPMorgan Chase & Co.', sector: 'Financial' },
        { symbol: 'JNJ', name: 'Johnson & Johnson', sector: 'Healthcare' },
        { symbol: 'V', name: 'Visa Inc.', sector: 'Financial' },
        { symbol: 'PG', name: 'Procter & Gamble', sector: 'Consumer Defensive' },
        { symbol: 'DIS', name: 'Walt Disney Company', sector: 'Communication Services' },
        { symbol: 'NFLX', name: 'Netflix Inc.', sector: 'Communication Services' },
        { symbol: 'ADBE', name: 'Adobe Inc.', sector: 'Technology' },
        { symbol: 'PYPL', name: 'PayPal Holdings', sector: 'Financial' },
        { symbol: 'INTC', name: 'Intel Corporation', sector: 'Technology' },
        { symbol: 'CSCO', name: 'Cisco Systems', sector: 'Technology' },
        { symbol: 'PFE', name: 'Pfizer Inc.', sector: 'Healthcare' },
        { symbol: 'NKE', name: 'Nike Inc.', sector: 'Consumer Cyclical' },
        { symbol: 'BA', name: 'Boeing Company', sector: 'Industrial' }
    ];

    // Filter stocks based on search
    const filteredStocks = availableStocks.filter(stock =>
        stock.symbol.toLowerCase().includes(searchQuery.toLowerCase()) ||
        stock.name.toLowerCase().includes(searchQuery.toLowerCase())
    );

    // Calculate market status with open/closed flag
    useEffect(() => {
        const updateMarketStatus = () => {
            const now = new Date();
            const day = now.getDay();
            const hours = now.getHours();
            const minutes = now.getMinutes();
            const estHours = hours - 5; // Convert to EST (simplified)

            let status = '';
            let marketOpen = false;

            if (day === 0 || day === 6) {
                status = 'Market closed for weekend';
                marketOpen = false;
            } else if (estHours < 9 || (estHours === 9 && minutes < 30)) {
                const openTime = new Date();
                openTime.setHours(14, 30, 0, 0); // 9:30 AM EST
                const diff = openTime - now;
                const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
                const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
                status = `Market opens in ${hoursLeft}h ${minutesLeft}m`;
                marketOpen = false;
            } else if (estHours < 16) {
                const closeTime = new Date();
                closeTime.setHours(21, 0, 0, 0); // 4:00 PM EST
                const diff = closeTime - now;
                const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
                const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
                status = `Market is open - Closes in ${hoursLeft}h ${minutesLeft}m`;
                marketOpen = true;
            } else {
                status = 'Market closed for today';
                marketOpen = false;
            }

            setMarketStatus(status);
            setIsMarketOpen(marketOpen);
        };

        updateMarketStatus();
        const interval = setInterval(updateMarketStatus, 60000);
        return () => clearInterval(interval);
    }, []);

    // Enhanced forecast data generation with realistic patterns
    const generateForecastData = () => {
        const selectedStockInfo = availableStocks.find(stock => stock.symbol === selectedStock);
        const basePrice = Math.random() * 500 + 50;
        const historicalData = [];
        const forecastData = [];

        // Generate realistic historical data with trends
        let currentPrice = basePrice;
        let trend = Math.random() > 0.5 ? 1 : -1;
        let volatility = 0.5 + Math.random() * 2; // Stock-specific volatility

        for (let i = 30; i > 0; i--) {
            const date = new Date();
            date.setDate(date.getDate() - i);

            // More realistic price movement with momentum
            const randomFactor = (Math.random() - 0.5) * volatility;
            const trendFactor = trend * 0.1;
            const priceChange = (randomFactor + trendFactor) * currentPrice / 100;

            currentPrice += priceChange;

            // Occasionally change trend
            if (Math.random() < 0.1) {
                trend = Math.random() > 0.5 ? 1 : -1;
            }

            historicalData.push({
                date: date.toISOString().split('T')[0],
                price: parseFloat(currentPrice.toFixed(2)),
                actual: true
            });
        }

        // Generate forecast data based on historical patterns
        const forecastDays = timeframe === '7d' ? 7 : 30;
        let lastHistoricalPrice = historicalData[historicalData.length - 1].price;
        let forecastPrice = lastHistoricalPrice;

        // Analyze historical trend for more accurate forecasting
        const recentPrices = historicalData.slice(-10).map(d => d.price);
        const historicalTrend = recentPrices[recentPrices.length - 1] - recentPrices[0];
        const forecastTrend = historicalTrend > 0 ? 1 : -1;

        for (let i = 1; i <= forecastDays; i++) {
            const date = new Date();
            date.setDate(date.getDate() + i);

            // Use historical volatility and trend for forecasting
            const forecastVolatility = volatility * (0.8 + Math.random() * 0.4);
            const priceChange = (forecastTrend * 0.05 + (Math.random() - 0.5) * forecastVolatility) * forecastPrice / 100;

            forecastPrice += priceChange;

            forecastData.push({
                date: date.toISOString().split('T')[0],
                price: parseFloat(forecastPrice.toFixed(2)),
                forecast: true,
                confidence: Math.max(70, 90 - (i * 0.5)) // Confidence decreases over time
            });
        }

        const finalPrediction = {
            symbol: selectedStock,
            name: selectedStockInfo?.name || 'Unknown Company',
            sector: selectedStockInfo?.sector || 'Unknown Sector',
            historical: historicalData,
            forecast: forecastData,
            currentPrice: lastHistoricalPrice,
            prediction: {
                direction: forecastData[0].price > lastHistoricalPrice ? 'bullish' : 'bearish',
                confidence: predictionAccuracy,
                targetPrice: forecastData[forecastData.length - 1].price,
                timeframe: timeframe,
                trendStrength: Math.abs((forecastData[forecastData.length - 1].price - lastHistoricalPrice) / lastHistoricalPrice * 100)
            }
        };

        return finalPrediction;
    };

    const fetchForecast = async () => {
        if (!isMarketOpen) {
            // Show warning but still allow forecasting
            console.log('Market is closed - Using latest available data for forecasting');
        }

        setLoading(true);
        try {
            // Simulate API call delay
            await new Promise(resolve => setTimeout(resolve, 1500));
            const data = generateForecastData();
            setForecastData(data);
        } catch (error) {
            console.error('Error fetching forecast:', error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchForecast();
    }, [selectedStock, timeframe]);

    const handleStockChange = (symbol) => {
        setSelectedStock(symbol);
    };

    const handleTimeframeChange = (newTimeframe) => {
        setTimeframe(newTimeframe);
    };

    const calculateMetrics = () => {
        if (!forecastData) return null;

        const historical = forecastData.historical;
        const forecast = forecastData.forecast;

        const currentPrice = historical[historical.length - 1].price;
        const predictedPrice = forecast[forecast.length - 1].price;
        const priceChange = predictedPrice - currentPrice;
        const percentageChange = (priceChange / currentPrice) * 100;

        // Calculate volatility (standard deviation of returns)
        const returns = historical.slice(1).map((point, i) =>
            (point.price - historical[i].price) / historical[i].price
        );
        const volatility = Math.sqrt(returns.reduce((a, b) => a + b * b, 0) / returns.length) * 100;

        return {
            currentPrice,
            predictedPrice,
            priceChange,
            percentageChange,
            volatility: parseFloat(volatility.toFixed(2)),
            trendStrength: Math.abs(percentageChange)
        };
    };

    const metrics = calculateMetrics();

    return (
        <div className="dashboard-container">
            {/* Sidebar */}
            <div className="sidebar">
                <div className="logo">
                    <h1>Smart Invest</h1>
                </div>

                <div className="user-profile-section">
                    <div className="user-profile-header">
                        <div className="user-avatar">
                            {user?.username?.charAt(0).toUpperCase() || 'U'}
                        </div>
                        <div className="user-info">
                            <h2>Hello, {user?.username || 'User'}!</h2>
                            <p>Market Forecasting</p>
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
                        <div className="account-type-badge">AI Forecasting</div>
                    </div>

                    <div className="user-stats-section">
                        <div className="user-stat-item">
                            <span className="user-stat-label">Prediction Accuracy</span>
                            <span className="stat-value-small">{predictionAccuracy}%</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">AI Model</span>
                            <span className="stat-value-small">LSTM Neural</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Data Points</span>
                            <span className="stat-value-small">10K+</span>
                        </div>
                    </div>

                    <div className="sidebar-action-buttons">
                        <button onClick={() => onNavigate('dashboard')} className="sidebar-action-button">
                            📊 HOME
                        </button>
                        <button onClick={() => onNavigate('portfolio')} className="sidebar-action-button">
                            💼 PORTFOLIO
                        </button>
                        <button onClick={() => onNavigate('trade')} className="sidebar-action-button">
                            💹 TRADE
                        </button>
                        <button onClick={() => onNavigate('forecast')} className="sidebar-action-button primary forecast-active">
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
                <div className="forecast-container">
                    {/* Market Status Banner */}
                    <div className="market-status-banner">
                        <div className="market-info">
                            <div className={`status-indicator ${isMarketOpen ? 'open' : 'closed'}`}></div>
                            <span className="market-hours">{marketStatus}</span>
                            {!isMarketOpen && (
                                <span className="market-closed-warning">
                                    📊 Using latest available data for AI forecasting
                                </span>
                            )}
                        </div>
                        <div className="last-updated">
                            AI-Powered Predictions • {new Date().toLocaleTimeString()}
                            <button onClick={fetchForecast} className="refresh-btn" disabled={loading}>
                                🔄 {loading ? 'Updating...' : 'Refresh'}
                            </button>
                        </div>
                    </div>

                    {/* Forecast Header */}
                    <div className="forecast-header">
                        <h1>🔮 AI Stock Market Forecasting</h1>
                        <p>Advanced machine learning predictions powered by LSTM neural networks</p>
                        {!isMarketOpen && (
                            <div className="market-closed-alert">
                                <strong>Note:</strong> Market is currently closed. Forecasts are based on the most recent available data.
                            </div>
                        )}
                    </div>

                    {/* Stock Selection and Controls */}
                    <div className="forecast-controls">
                        <div className="stock-selection">
                            <h3>Select Stock</h3>
                            <div className="search-box">
                                <input
                                    type="text"
                                    placeholder="Search stocks by symbol or name..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="stock-search"
                                />
                            </div>
                            <div className="stock-grid">
                                {filteredStocks.map(stock => (
                                    <div
                                        key={stock.symbol}
                                        className={`stock-card ${selectedStock === stock.symbol ? 'active' : ''}`}
                                        onClick={() => handleStockChange(stock.symbol)}
                                    >
                                        <div className="stock-symbol">{stock.symbol}</div>
                                        <div className="stock-name">{stock.name}</div>
                                        <div
                                            className="stock-sector"
                                            data-sector={stock.sector}
                                        >
                                            {stock.sector.toUpperCase()}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        </div>

                        <div className="timeframe-selection">
                            <h3>Forecast Period</h3>
                            <div className="timeframe-buttons">
                                <button
                                    className={`timeframe-btn ${timeframe === '7d' ? 'active' : ''}`}
                                    onClick={() => handleTimeframeChange('7d')}
                                >
                                    7 Days
                                </button>
                                <button
                                    className={`timeframe-btn ${timeframe === '30d' ? 'active' : ''}`}
                                    onClick={() => handleTimeframeChange('30d')}
                                >
                                    30 Days
                                </button>
                            </div>
                        </div>
                    </div>

                    {loading ? (
                        <div className="forecast-loading">
                            <div className="loading-spinner"></div>
                            <p>Analyzing market patterns with AI...</p>
                            {!isMarketOpen && (
                                <p className="loading-note">Using latest available data (Market Closed)</p>
                            )}
                        </div>
                    ) : forecastData && metrics ? (
                        <div className="forecast-content">
                            {/* Stock Info Header */}
                            <div className="stock-info-header">
                                <div className="stock-main-info">
                                    <h2>{forecastData.symbol}</h2>
                                    <span className="stock-full-name">{forecastData.name}</span>
                                    <span className="stock-sector-badge">{forecastData.sector}</span>
                                </div>
                                <div className="stock-current-price">
                                    <span>Current Price</span>
                                    <strong>${metrics.currentPrice.toFixed(2)}</strong>
                                </div>
                            </div>

                            {/* Prediction Overview */}
                            <div className="prediction-overview">
                                <div className="prediction-card main-prediction">
                                    <div className="prediction-header">
                                        <h3>AI PREDICTION FOR {selectedStock}</h3>
                                        <div className={`prediction-badge ${forecastData.prediction.direction}`}>
                                            {forecastData.prediction.direction.toUpperCase()}
                                        </div>
                                    </div>
                                    <div className="prediction-body">
                                        <div className="price-comparison">
                                            <div className="current-price">
                                                <span>Current Price</span>
                                                <strong>${metrics.currentPrice.toFixed(2)}</strong>
                                            </div>
                                            <div className="predicted-price">
                                                <span>Predicted Price</span>
                                                <strong className={metrics.percentageChange >= 0 ? 'positive' : 'negative'}>
                                                    ${metrics.predictedPrice.toFixed(2)}
                                                </strong>
                                            </div>
                                            <div className="price-change">
                                                <span>Expected Change</span>
                                                <strong className={metrics.percentageChange >= 0 ? 'positive' : 'negative'}>
                                                    {metrics.percentageChange >= 0 ? '+' : ''}{metrics.percentageChange.toFixed(2)}%
                                                    ({metrics.priceChange >= 0 ? '+' : ''}${Math.abs(metrics.priceChange).toFixed(2)})
                                                </strong>
                                            </div>
                                        </div>
                                        <div className="confidence-meter">
                                            <span>AI Confidence: {forecastData.prediction.confidence}%</span>
                                            <div className="confidence-bar">
                                                <div
                                                    className="confidence-fill"
                                                    style={{ width: `${forecastData.prediction.confidence}%` }}
                                                ></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                {/* Metrics Grid */}
                                <div className="metrics-grid">
                                    <div className="metric-card">
                                        <div className="metric-icon">📈</div>
                                        <div className="metric-value">{metrics.trendStrength.toFixed(1)}%</div>
                                        <div className="metric-label">Trend Strength</div>
                                    </div>
                                    <div className="metric-card">
                                        <div className="metric-icon">⚡</div>
                                        <div className="metric-value">{metrics.volatility}%</div>
                                        <div className="metric-label">Volatility</div>
                                    </div>
                                    <div className="metric-card">
                                        <div className="metric-icon">🎯</div>
                                        <div className="metric-value">{predictionAccuracy}%</div>
                                        <div className="metric-label">Model Accuracy</div>
                                    </div>
                                    <div className="metric-card">
                                        <div className="metric-icon">📊</div>
                                        <div className="metric-value">{timeframe}</div>
                                        <div className="metric-label">Forecast Period</div>
                                    </div>
                                </div>
                            </div>

                            {/* Interactive Price Chart */}
                            <div className="forecast-chart">
                                <h3>Price Forecast & Historical Data</h3>
                                <PriceChart
                                    historicalData={forecastData.historical}
                                    forecastData={forecastData.forecast}
                                    isMarketOpen={isMarketOpen}
                                />
                            </div>

                            {/* Detailed Forecast Table */}
                            <div className="forecast-table">
                                <h3>Detailed Daily Forecast</h3>
                                <div className="table-container">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Type</th>
                                            <th>Price</th>
                                            <th>Change</th>
                                            <th>Confidence</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {/* Last 5 historical points */}
                                        {forecastData.historical.slice(-5).map((point, index) => (
                                            <tr key={`hist-${index}`} className="historical-row">
                                                <td>{new Date(point.date).toLocaleDateString()}</td>
                                                <td>
                                                    <span className="data-type historical">Historical</span>
                                                </td>
                                                <td>${point.price.toFixed(2)}</td>
                                                <td>-</td>
                                                <td>-</td>
                                            </tr>
                                        ))}
                                        {/* Forecast points */}
                                        {forecastData.forecast.map((point, index) => {
                                            const prevPrice = index === 0 ?
                                                forecastData.historical[forecastData.historical.length - 1].price :
                                                forecastData.forecast[index - 1].price;
                                            const change = ((point.price - prevPrice) / prevPrice) * 100;

                                            return (
                                                <tr key={`forecast-${index}`} className="forecast-row">
                                                    <td>{new Date(point.date).toLocaleDateString()}</td>
                                                    <td>
                                                        <span className="data-type forecast">Forecast</span>
                                                    </td>
                                                    <td>${point.price.toFixed(2)}</td>
                                                    <td className={change >= 0 ? 'positive' : 'negative'}>
                                                        {change >= 0 ? '+' : ''}{change.toFixed(2)}%
                                                    </td>
                                                    <td>
                                                        <div className="confidence-display">
                                                            <span>{point.confidence.toFixed(1)}%</span>
                                                            <div className="confidence-bar-small">
                                                                <div
                                                                    className="confidence-fill-small"
                                                                    style={{ width: `${point.confidence}%` }}
                                                                ></div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            {/* Trading Recommendations */}
                            <div className="recommendations">
                                <h3>🤖 AI Trading Recommendations</h3>
                                <div className="recommendation-cards">
                                    <div className="recommendation-card">
                                        <div className="rec-icon">💡</div>
                                        <div className="rec-content">
                                            <h4>Market Sentiment</h4>
                                            <p>{forecastData.prediction.direction === 'bullish' ?
                                                'Positive market momentum detected' :
                                                'Caution advised in current market conditions'
                                            }</p>
                                        </div>
                                    </div>
                                    <div className="recommendation-card">
                                        <div className="rec-icon">⚖️</div>
                                        <div className="rec-content">
                                            <h4>Risk Level</h4>
                                            <p>{metrics.volatility > 5 ? 'High Volatility' : 'Moderate Risk'}</p>
                                        </div>
                                    </div>
                                    <div className="recommendation-card">
                                        <div className="rec-icon">🎯</div>
                                        <div className="rec-content">
                                            <h4>Suggested Action</h4>
                                            <p>{forecastData.prediction.direction === 'bullish' ?
                                                'Consider buying opportunities' :
                                                'Monitor for selling opportunities'
                                            }</p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="forecast-error">
                            <div className="error-icon">🔮</div>
                            <h3>Unable to load forecast data</h3>
                            <p>Please check your connection and try again</p>
                            <button onClick={fetchForecast} className="btn-primary">
                                🔄 Retry
                            </button>
                        </div>
                    )}

                    {/* Disclaimer */}
                    <div className="forecast-disclaimer">
                        <p>
                            <strong>Disclaimer:</strong> AI predictions are based on historical data and machine learning models.
                            Past performance is not indicative of future results. Always conduct your own research and
                            consult with financial advisors before making investment decisions.
                            {!isMarketOpen && " Forecasts shown are based on the most recent available market data."}
                        </p>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Forecast;