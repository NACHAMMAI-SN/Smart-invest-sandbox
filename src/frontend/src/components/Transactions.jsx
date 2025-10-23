import React, { useState, useEffect } from 'react';
import '../styles/Transactions.css';

const Transactions = ({ user, onLogout, onNavigate }) => {
    const [transactions, setTransactions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filter, setFilter] = useState('ALL');
    const [searchTerm, setSearchTerm] = useState('');
    const [marketStatus, setMarketStatus] = useState('');

    useEffect(() => {
        loadTransactions();
        updateMarketStatus();
        const interval = setInterval(updateMarketStatus, 60000);
        return () => clearInterval(interval);
    }, [user]);

    const loadTransactions = () => {
        try {
            // Load transactions from localStorage
            const savedTransactions = localStorage.getItem('userTransactions');

            if (savedTransactions) {
                const transactionsData = JSON.parse(savedTransactions);
                // Sort by timestamp (newest first)
                const sortedTransactions = transactionsData.sort((a, b) =>
                    new Date(b.timestamp) - new Date(a.timestamp)
                );
                setTransactions(sortedTransactions);
            } else {
                setTransactions([]);
            }
        } catch (error) {
            console.error('Error loading transactions:', error);
            setTransactions([]);
        } finally {
            setLoading(false);
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

    const filteredTransactions = transactions.filter(transaction => {
        const matchesFilter = filter === 'ALL' || transaction.action === filter;
        const matchesSearch = transaction.symbol.toLowerCase().includes(searchTerm.toLowerCase()) ||
            transaction.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            transaction.type.toLowerCase().includes(searchTerm.toLowerCase());
        return matchesFilter && matchesSearch;
    });

    const totalCashFlow = transactions.reduce((total, transaction) => {
        return total + (transaction.action === 'SELL' ? transaction.total : -transaction.total);
    }, 0);

    const totalInvestment = transactions
        .filter(t => t.action === 'BUY')
        .reduce((total, transaction) => total + transaction.total, 0);

    const totalSales = transactions
        .filter(t => t.action === 'SELL')
        .reduce((total, transaction) => total + transaction.total, 0);

    const totalTrades = transactions.length;
    const buyCount = transactions.filter(t => t.action === 'BUY').length;
    const sellCount = transactions.filter(t => t.action === 'SELL').length;

    const formatDate = (dateString) => {
        const date = new Date(dateString);
        const now = new Date();
        const diffInHours = Math.floor((now - date) / (1000 * 60 * 60));

        if (diffInHours < 24) {
            return date.toLocaleTimeString('en-US', {
                hour: '2-digit',
                minute: '2-digit',
                hour12: true
            });
        } else {
            return date.toLocaleDateString('en-US', {
                month: 'short',
                day: 'numeric',
                year: 'numeric'
            });
        }
    };

    const getTransactionStats = () => {
        return {
            totalTrades,
            buyCount,
            sellCount,
            totalInvestment,
            totalSales,
            netCashFlow: totalCashFlow
        };
    };

    const handleRefresh = () => {
        setLoading(true);
        setTimeout(() => {
            loadTransactions();
        }, 1000);
    };

    const handleClearFilters = () => {
        setFilter('ALL');
        setSearchTerm('');
    };

    const handleForecastNavigation = (symbol = null) => {
        if (symbol) {
            onNavigate('forecast', { symbol });
        } else {
            onNavigate('forecast');
        }
    };

    const getMostTradedStocks = () => {
        const stockCount = {};
        filteredTransactions.forEach(t => {
            stockCount[t.symbol] = (stockCount[t.symbol] || 0) + 1;
        });
        return Object.entries(stockCount)
            .sort((a, b) => b[1] - a[1])
            .slice(0, 3);
    };

    const stats = getTransactionStats();
    const mostTradedStocks = getMostTradedStocks();

    if (loading) {
        return (
            <div className="dashboard-container">
                <div className="sidebar">
                    {/* Same sidebar as below */}
                </div>
                <div className="main-content">
                    <div className="loading-transactions">
                        <div className="loading-spinner"></div>
                        <p>Loading transaction history...</p>
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
                        <div className="balance-label">Available Balance</div>
                        <div className="balance-amount">
                            ${user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                        </div>
                        <div className="account-type-badge">Live Trading</div>
                    </div>

                    <div className="user-stats-section">
                        <div className="user-stat-item">
                            <span className="user-stat-label">Total Trades</span>
                            <span className="stat-value-small">{stats.totalTrades}</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Buy Orders</span>
                            <span className="stat-value-small">{stats.buyCount}</span>
                        </div>
                        <div className="user-stat-item">
                            <span className="user-stat-label">Sell Orders</span>
                            <span className="stat-value-small">{stats.sellCount}</span>
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
                        <button onClick={() => onNavigate('forecast')} className="sidebar-action-button">
                            🔮 FORECAST
                        </button>
                        <button onClick={() => onNavigate('news')} className="sidebar-action-button">
                            📰 NEWS
                        </button>
                        <button onClick={() => onNavigate('transactions')} className="sidebar-action-button primary">
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
                <div className="transactions-container">
                    {/* Market Status Banner */}
                    <div className="market-status-banner">
                        <div className="market-info">
                            <div className={`status-indicator ${marketStatus.includes('open') ? 'open' : 'closed'}`}></div>
                            <span className="market-hours">{marketStatus}</span>
                        </div>
                        <div className="last-updated">
                            Real-time updates • {new Date().toLocaleTimeString()}
                            <button onClick={handleRefresh} className="refresh-btn">
                                🔄 Refresh
                            </button>
                        </div>
                    </div>

                    {/* Transactions Header */}
                    <div className="transactions-header">
                        <div className="header-main">
                            <h1>📋 Transaction History</h1>
                            <p>Complete record of your trading activity and investment history</p>
                        </div>
                        <button
                            onClick={() => handleForecastNavigation()}
                            className="btn-forecast"
                        >
                            🔮 Get AI Forecast
                        </button>
                    </div>

                    {/* Transactions Stats Grid */}
                    <div className="transactions-stats-grid">
                        <div className="stat-card main-stat">
                            <h3>TOTAL TRADES</h3>
                            <div className="stat-value">{stats.totalTrades}</div>
                            <div className="stat-breakdown">
                                <span className="buy-count">{stats.buyCount} Buys</span>
                                <span className="sell-count">{stats.sellCount} Sells</span>
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>NET CASH FLOW</h3>
                            <div className="stat-value">
                                ${stats.netCashFlow.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                            <div className={`stat-change ${stats.netCashFlow >= 0 ? 'positive' : 'negative'}`}>
                                {stats.netCashFlow >= 0 ? 'Profit' : 'Loss'}
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>TOTAL INVESTMENT</h3>
                            <div className="stat-value">
                                ${stats.totalInvestment.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                            <div className="stat-change neutral">
                                Capital Deployed
                            </div>
                        </div>

                        <div className="stat-card">
                            <h3>TOTAL SALES</h3>
                            <div className="stat-value">
                                ${stats.totalSales.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </div>
                            <div className="stat-change positive">
                                Revenue Generated
                            </div>
                        </div>
                    </div>

                    {/* Filters Section */}
                    <div className="filters-section">
                        <div className="filters">
                            <div className="filter-group">
                                <label>Filter by Type:</label>
                                <select value={filter} onChange={(e) => setFilter(e.target.value)}>
                                    <option value="ALL">All Transactions</option>
                                    <option value="BUY">Buy Orders Only</option>
                                    <option value="SELL">Sell Orders Only</option>
                                </select>
                            </div>

                            <div className="search-group">
                                <input
                                    type="text"
                                    placeholder="🔍 Search by symbol, name, or order type..."
                                    value={searchTerm}
                                    onChange={(e) => setSearchTerm(e.target.value)}
                                />
                            </div>

                            <button onClick={handleClearFilters} className="clear-filters">
                                🗑️ Clear Filters
                            </button>

                            <div className="results-count">
                                Showing {filteredTransactions.length} of {transactions.length} transactions
                            </div>
                        </div>
                    </div>

                    {/* Transactions Table */}
                    <div className="transactions-table-container">
                        {filteredTransactions.length === 0 ? (
                            <div className="empty-transactions">
                                <div className="empty-icon">📊</div>
                                <h3>No transactions found</h3>
                                <p>
                                    {transactions.length === 0
                                        ? "You haven't made any trades yet. Start building your portfolio!"
                                        : "No transactions match your current filters. Try adjusting your search criteria."
                                    }
                                </p>
                                <div className="empty-actions">
                                    {transactions.length === 0 && (
                                        <button onClick={() => onNavigate('trade')} className="btn-primary">
                                            🚀 Start Trading
                                        </button>
                                    )}
                                    <button
                                        onClick={() => handleForecastNavigation()}
                                        className="btn-forecast"
                                    >
                                        🔮 Explore Forecasts
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="transactions-table">
                                <div className="table-header">
                                    <h3>Recent Transactions</h3>
                                    <div className="table-actions">
                                        <button className="export-btn">
                                            📥 Export CSV
                                        </button>
                                        <button
                                            onClick={() => handleForecastNavigation()}
                                            className="btn-forecast"
                                        >
                                            🔮 AI Forecast
                                        </button>
                                    </div>
                                </div>
                                <table>
                                    <thead>
                                    <tr>
                                        <th>Action</th>
                                        <th>Symbol</th>
                                        <th>Stock Name</th>
                                        <th>Price</th>
                                        <th>Quantity</th>
                                        <th>Total Amount</th>
                                        <th>Order Type</th>
                                        <th>Status</th>
                                        <th>Date & Time</th>
                                        <th>Actions</th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    {filteredTransactions.map((transaction) => (
                                        <tr key={transaction.id} className="transaction-row">
                                            <td>
                                                <span className={`action-tag ${transaction.action.toLowerCase()}`}>
                                                    {transaction.action === 'BUY' ? '🟢' : '🔴'} {transaction.action}
                                                </span>
                                            </td>
                                            <td>
                                                <strong className="symbol">{transaction.symbol}</strong>
                                            </td>
                                            <td className="stock-name">{transaction.name}</td>
                                            <td className="price">${transaction.price.toFixed(2)}</td>
                                            <td className="quantity">{transaction.quantity}</td>
                                            <td className="total-amount">
                                                <strong className={transaction.action === 'BUY' ? 'negative' : 'positive'}>
                                                    {transaction.action === 'BUY' ? '-' : '+'}${transaction.total.toFixed(2)}
                                                </strong>
                                            </td>
                                            <td>
                                                <span className={`order-type ${transaction.type.toLowerCase()}`}>
                                                    {transaction.type}
                                                </span>
                                            </td>
                                            <td>
                                                <span className="status-badge completed">
                                                    {transaction.status || 'COMPLETED'}
                                                </span>
                                            </td>
                                            <td className="transaction-date">
                                                {formatDate(transaction.timestamp)}
                                            </td>
                                            <td>
                                                <button
                                                    onClick={() => handleForecastNavigation(transaction.symbol)}
                                                    className="btn-forecast-small"
                                                    title={`Get AI forecast for ${transaction.symbol}`}
                                                >
                                                    🔮
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>

                    {/* Transaction Summary */}
                    {filteredTransactions.length > 0 && (
                        <div className="transaction-summary">
                            <div className="summary-header">
                                <h3>📊 Transaction Summary</h3>
                                <button
                                    onClick={() => handleForecastNavigation()}
                                    className="btn-forecast"
                                >
                                    🔮 Get Portfolio Forecast
                                </button>
                            </div>
                            <div className="summary-content">
                                <div className="summary-grid">
                                    <div className="summary-item">
                                        <span>Filtered Transactions</span>
                                        <strong>{filteredTransactions.length}</strong>
                                    </div>
                                    <div className="summary-item">
                                        <span>Total Filtered Amount</span>
                                        <strong>
                                            ${filteredTransactions.reduce((sum, t) => sum + t.total, 0).toFixed(2)}
                                        </strong>
                                    </div>
                                    <div className="summary-item">
                                        <span>Average Trade Size</span>
                                        <strong>
                                            ${(filteredTransactions.reduce((sum, t) => sum + t.total, 0) / filteredTransactions.length).toFixed(2)}
                                        </strong>
                                    </div>
                                    <div className="summary-item">
                                        <span>Most Traded Stock</span>
                                        <strong>
                                            {mostTradedStocks[0] ? mostTradedStocks[0][0] : 'N/A'}
                                        </strong>
                                    </div>
                                </div>

                                {/* Most Traded Stocks */}
                                {mostTradedStocks.length > 0 && (
                                    <div className="most-traded-stocks">
                                        <h4>🎯 Your Most Traded Stocks</h4>
                                        <div className="traded-stocks-list">
                                            {mostTradedStocks.map(([symbol, count]) => (
                                                <div key={symbol} className="traded-stock-item">
                                                    <div className="stock-info">
                                                        <span className="stock-symbol">{symbol}</span>
                                                        <span className="trade-count">{count} trades</span>
                                                    </div>
                                                    <button
                                                        onClick={() => handleForecastNavigation(symbol)}
                                                        className="btn-forecast-small"
                                                        title={`Get AI forecast for ${symbol}`}
                                                    >
                                                        🔮 Forecast
                                                    </button>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    )}

                    {/* Trading Insights */}
                    {filteredTransactions.length > 5 && (
                        <div className="trading-insights">
                            <h3>💡 Trading Insights & AI Suggestions</h3>
                            <div className="insights-grid">
                                <div className="insight-card">
                                    <div className="insight-icon">📈</div>
                                    <div className="insight-content">
                                        <h4>Analyze Your Patterns</h4>
                                        <p>Review your most traded stocks and trading frequency to identify patterns.</p>
                                    </div>
                                </div>
                                <div className="insight-card">
                                    <div className="insight-icon">🔮</div>
                                    <div className="insight-content">
                                        <h4>AI Forecasting</h4>
                                        <p>Use AI predictions to optimize your future trading decisions based on your history.</p>
                                        <button
                                            onClick={() => handleForecastNavigation()}
                                            className="btn-forecast-small"
                                        >
                                            Get AI Insights
                                        </button>
                                    </div>
                                </div>
                                <div className="insight-card">
                                    <div className="insight-icon">💰</div>
                                    <div className="insight-content">
                                        <h4>Performance Review</h4>
                                        <p>Monitor your net cash flow and adjust your strategy for better returns.</p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="transactions-footer">
                        <p>💡 Tip: Use AI forecasting to predict future market movements based on your trading patterns</p>
                        <button
                            onClick={() => handleForecastNavigation()}
                            className="btn-forecast-small"
                        >
                            🔮 Try AI Forecast Now
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Transactions;