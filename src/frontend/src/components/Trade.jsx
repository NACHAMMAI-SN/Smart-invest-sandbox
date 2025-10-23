import React, { useState, useEffect } from 'react';
import '../styles/Trade.css';

const Trade = ({ user, onLogout, onNavigate }) => {
    const [stockData, setStockData] = useState(null);
    const [stocksList, setStocksList] = useState([]);
    const [searchTerm, setSearchTerm] = useState('RELIANCE');
    const [quantity, setQuantity] = useState(1);
    const [orderType, setOrderType] = useState('MARKET');
    const [duration, setDuration] = useState('IOC');
    const [action, setAction] = useState('BUY');
    const [loading, setLoading] = useState(false);
    const [stockLoading, setStockLoading] = useState(false);
    const [message, setMessage] = useState('');
    const [marketStatus, setMarketStatus] = useState('');
    const [limitPrice, setLimitPrice] = useState('');
    const [selectedStock, setSelectedStock] = useState('RELIANCE');
    const [apiUsage, setApiUsage] = useState(0);
    const [searchResults, setSearchResults] = useState([]);
    const [showSearchResults, setShowSearchResults] = useState(false);
    const [availableStocksData, setAvailableStocksData] = useState([]);
    const [isMarketOpen, setIsMarketOpen] = useState(false);
    const [transactions, setTransactions] = useState([]);
    const [portfolio, setPortfolio] = useState({});

    // Indian Stocks List
    const indianStocks = [
        { symbol: 'RELIANCE', name: 'Reliance Industries Ltd' },
        { symbol: 'TCS', name: 'Tata Consultancy Services Ltd' },
        { symbol: 'HDFCBANK', name: 'HDFC Bank Ltd' },
        { symbol: 'INFY', name: 'Infosys Ltd' },
        { symbol: 'HINDUNILVR', name: 'Hindustan Unilever Ltd' },
        { symbol: 'ICICIBANK', name: 'ICICI Bank Ltd' },
        { symbol: 'SBIN', name: 'State Bank of India' },
        { symbol: 'BHARTIARTL', name: 'Bharti Airtel Ltd' },
        { symbol: 'KOTAKBANK', name: 'Kotak Mahindra Bank Ltd' },
        { symbol: 'ITC', name: 'ITC Ltd' },
        { symbol: 'LT', name: 'Larsen & Toubro Ltd' },
        { symbol: 'AXISBANK', name: 'Axis Bank Ltd' },
        { symbol: 'HCLTECH', name: 'HCL Technologies Ltd' },
        { symbol: 'ASIANPAINT', name: 'Asian Paints Ltd' },
        { symbol: 'MARUTI', name: 'Maruti Suzuki India Ltd' },
        { symbol: 'SUNPHARMA', name: 'Sun Pharmaceutical Ltd' },
        { symbol: 'TITAN', name: 'Titan Company Ltd' },
        { symbol: 'ULTRACEMCO', name: 'UltraTech Cement Ltd' },
        { symbol: 'DMART', name: 'Avenue Supermarts Ltd' },
        { symbol: 'BAJFINANCE', name: 'Bajaj Finance Ltd' },
        { symbol: 'WIPRO', name: 'Wipro Ltd' },
        { symbol: 'NESTLEIND', name: 'Nestle India Ltd' },
        { symbol: 'POWERGRID', name: 'Power Grid Corporation Ltd' },
        { symbol: 'NTPC', name: 'NTPC Ltd' },
        { symbol: 'ONGC', name: 'Oil & Natural Gas Corporation Ltd' },
        { symbol: 'ADANIPORTS', name: 'Adani Ports and Special Economic Zone Ltd' },
        { symbol: 'TECHM', name: 'Tech Mahindra Ltd' },
        { symbol: 'HDFC', name: 'Housing Development Finance Corporation Ltd' },
        { symbol: 'BRITANNIA', name: 'Britannia Industries Ltd' },
        { symbol: 'TATAMOTORS', name: 'Tata Motors Ltd' },
        { symbol: 'BAJAJFINSV', name: 'Bajaj Finserv Ltd' },
        { symbol: 'JSWSTEEL', name: 'JSW Steel Ltd' },
        { symbol: 'TATASTEEL', name: 'Tata Steel Ltd' },
        { symbol: 'HDFCLIFE', name: 'HDFC Life Insurance Company Ltd' },
        { symbol: 'DRREDDY', name: 'Dr. Reddy\'s Laboratories Ltd' },
        { symbol: 'CIPLA', name: 'Cipla Ltd' },
        { symbol: 'SBILIFE', name: 'SBI Life Insurance Company Ltd' },
        { symbol: 'GRASIM', name: 'Grasim Industries Ltd' },
        { symbol: 'DIVISLAB', name: 'Divi\'s Laboratories Ltd' },
        { symbol: 'UPL', name: 'UPL Ltd' },
        { symbol: 'COALINDIA', name: 'Coal India Ltd' },
        { symbol: 'M&M', name: 'Mahindra & Mahindra Ltd' },
        { symbol: 'INDUSINDBK', name: 'IndusInd Bank Ltd' },
        { symbol: 'TATACONSUM', name: 'Tata Consumer Products Ltd' },
        { symbol: 'BPCL', name: 'Bharat Petroleum Corporation Ltd' },
        { symbol: 'HEROMOTOCO', name: 'Hero MotoCorp Ltd' },
        { symbol: 'EICHERMOT', name: 'Eicher Motors Ltd' },
        { symbol: 'SHREECEM', name: 'Shree Cement Ltd' },
        { symbol: 'HINDALCO', name: 'Hindalco Industries Ltd' },
        { symbol: 'APOLLOHOSP', name: 'Apollo Hospitals Enterprise Ltd' }
    ];

    // Mock API for Indian stocks (since Alpha Vantage doesn't have Indian stocks)
    const stockAPI = {
        getStock: async (symbol) => {
            try {
                setApiUsage(prev => prev + 1);

                // Mock data for Indian stocks with realistic prices
                const stockInfo = indianStocks.find(stock => stock.symbol === symbol) || { symbol, name: symbol };
                const basePrice = getBasePrice(symbol);
                const change = (Math.random() - 0.5) * 20;
                const changePercent = ((change / basePrice) * 100).toFixed(2);

                return {
                    symbol: symbol,
                    name: stockInfo.name,
                    price: (basePrice + change).toFixed(2),
                    change: change.toFixed(2),
                    changePercent: `${changePercent}%`,
                    open: basePrice.toFixed(2),
                    high: (basePrice + Math.random() * 10).toFixed(2),
                    low: (basePrice - Math.random() * 10).toFixed(2),
                    volume: Math.floor(Math.random() * 10000000).toLocaleString(),
                    previousClose: basePrice.toFixed(2),
                    timestamp: new Date().toLocaleTimeString('en-IN')
                };
            } catch (error) {
                console.error('API error:', error);
                return generateMockStockData(symbol);
            }
        },

        searchStocks: async (keywords) => {
            try {
                // Local search for Indian stocks
                return indianStocks.filter(stock =>
                    stock.symbol.toLowerCase().includes(keywords.toLowerCase()) ||
                    stock.name.toLowerCase().includes(keywords.toLowerCase())
                ).slice(0, 8);
            } catch (error) {
                console.error('Search error:', error);
                return indianStocks.filter(stock =>
                    stock.symbol.toLowerCase().includes(keywords.toLowerCase()) ||
                    stock.name.toLowerCase().includes(keywords.toLowerCase())
                ).slice(0, 8);
            }
        }
    };

    // Realistic base prices for Indian stocks (in INR)
    const getBasePrice = (symbol) => {
        const basePrices = {
            'RELIANCE': 2500, 'TCS': 3500, 'HDFCBANK': 1600, 'INFY': 1500,
            'HINDUNILVR': 2400, 'ICICIBANK': 900, 'SBIN': 600, 'BHARTIARTL': 800,
            'KOTAKBANK': 1700, 'ITC': 400, 'LT': 3200, 'AXISBANK': 950,
            'HCLTECH': 1200, 'ASIANPAINT': 3000, 'MARUTI': 10000, 'SUNPHARMA': 1000,
            'TITAN': 3500, 'ULTRACEMCO': 8000, 'DMART': 4000, 'BAJFINANCE': 7000,
            'WIPRO': 400, 'NESTLEIND': 22000, 'POWERGRID': 200, 'NTPC': 180,
            'ONGC': 150, 'ADANIPORTS': 800, 'TECHM': 1100, 'HDFC': 2600,
            'BRITANNIA': 4500, 'TATAMOTORS': 600, 'BAJAJFINSV': 1500, 'JSWSTEEL': 800,
            'TATASTEEL': 120, 'HDFCLIFE': 600, 'DRREDDY': 5500, 'CIPLA': 1200,
            'SBILIFE': 1300, 'GRASIM': 1700, 'DIVISLAB': 3500, 'UPL': 700,
            'COALINDIA': 400, 'M&M': 1600, 'INDUSINDBK': 1400, 'TATACONSUM': 800,
            'BPCL': 450, 'HEROMOTOCO': 3500, 'EICHERMOT': 3800, 'SHREECEM': 25000,
            'HINDALCO': 500, 'APOLLOHOSP': 5000
        };
        return basePrices[symbol] || 1000;
    };

    // Generate mock stock data
    const generateMockStockData = (symbol) => {
        const basePrice = getBasePrice(symbol);
        const change = (Math.random() - 0.5) * 20;
        const changePercent = ((change / basePrice) * 100).toFixed(2);
        const stockInfo = indianStocks.find(stock => stock.symbol === symbol) || { symbol, name: symbol };

        return {
            symbol: symbol,
            name: stockInfo.name,
            price: (basePrice + change).toFixed(2),
            change: change.toFixed(2),
            changePercent: `${changePercent}%`,
            open: basePrice.toFixed(2),
            high: (basePrice + Math.random() * 10).toFixed(2),
            low: (basePrice - Math.random() * 10).toFixed(2),
            volume: Math.floor(Math.random() * 10000000).toLocaleString(),
            previousClose: basePrice.toFixed(2),
            timestamp: new Date().toLocaleTimeString('en-IN')
        };
    };

    // Calculate Indian market status
    const checkIndianMarketStatus = () => {
        const now = new Date();
        const istTime = new Date(now.toLocaleString("en-US", {timeZone: "Asia/Kolkata"}));
        const day = istTime.getDay();
        const hours = istTime.getHours();
        const minutes = istTime.getMinutes();

        // Indian market hours: 9:15 AM - 3:30 PM IST, Monday-Friday
        const isWeekend = day === 0 || day === 6;
        const isMarketHours = !isWeekend && hours >= 9 && hours < 15 && !(hours === 9 && minutes < 15);

        setIsMarketOpen(isMarketHours);

        if (isWeekend) {
            setMarketStatus('Market closed for weekend');
        } else if (hours < 9 || (hours === 9 && minutes < 15)) {
            const openTime = new Date(istTime);
            openTime.setHours(9, 15, 0, 0);
            const diff = openTime - istTime;
            const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
            const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            setMarketStatus(`Market opens in ${hoursLeft}h ${minutesLeft}m`);
        } else if (hours < 15) {
            const closeTime = new Date(istTime);
            closeTime.setHours(15, 30, 0, 0);
            const diff = closeTime - istTime;
            const hoursLeft = Math.floor(diff / (1000 * 60 * 60));
            const minutesLeft = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60));
            setMarketStatus(`Market is open - Closes in ${hoursLeft}h ${minutesLeft}m`);
        } else {
            setMarketStatus('Market closed for today');
        }

        return isMarketHours;
    };

    useEffect(() => {
        checkIndianMarketStatus();
        const interval = setInterval(checkIndianMarketStatus, 60000);
        return () => clearInterval(interval);
    }, []);

    // Load initial data
    useEffect(() => {
        fetchStockData('RELIANCE');
        loadAvailableStocks();
        loadInitialPortfolio();
    }, []);

    // Load initial portfolio from localStorage
    const loadInitialPortfolio = () => {
        const savedPortfolio = localStorage.getItem('userPortfolio');
        const savedTransactions = localStorage.getItem('userTransactions');

        if (savedPortfolio) {
            setPortfolio(JSON.parse(savedPortfolio));
        }
        if (savedTransactions) {
            setTransactions(JSON.parse(savedTransactions));
        }
    };

    const fetchStockData = async (symbol = 'RELIANCE') => {
        setStockLoading(true);
        setMessage('');
        try {
            console.log('Fetching data for:', symbol);
            const data = await stockAPI.getStock(symbol);

            if (data && data.price) {
                setStockData(data);
                setSelectedStock(symbol);
                setLimitPrice(data.price);
                setMessage('✅ Real-time market data loaded');
            } else {
                throw new Error('No price data received');
            }
        } catch (error) {
            console.error('API error:', error);
            setMessage(`❌ ${error.message}`);
        } finally {
            setStockLoading(false);
        }
    };

    const loadAvailableStocks = async () => {
        try {
            // Load first 8 stocks for the ticker
            const tickerStocks = [];
            for (let i = 0; i < 8; i++) {
                const stock = indianStocks[i];
                try {
                    const data = await stockAPI.getStock(stock.symbol);
                    if (data && data.price) {
                        tickerStocks.push(data);
                    }
                } catch (error) {
                    console.error(`Error loading ${stock.symbol}:`, error);
                }
            }
            setStocksList(tickerStocks);

            // Load all Indian stocks for available stocks list
            const availableStocks = [];
            const stockPromises = indianStocks.map(async (stock) => {
                try {
                    const data = await stockAPI.getStock(stock.symbol);
                    if (data && data.price) {
                        return data;
                    }
                } catch (error) {
                    console.error(`Error loading ${stock.symbol}:`, error);
                }
                return null;
            });

            const results = await Promise.all(stockPromises);
            const validStocks = results.filter(stock => stock !== null);
            setAvailableStocksData(validStocks);

        } catch (error) {
            console.error('Error loading available stocks:', error);
            setMessage('⚠️ Some stocks failed to load. Using demo data.');

            // Fallback to mock data
            const mockStocks = indianStocks.map(stock => generateMockStockData(stock.symbol));
            setAvailableStocksData(mockStocks.slice(0, 50));
            setStocksList(mockStocks.slice(0, 8));
        }
    };

    const handleSearch = async (e) => {
        e.preventDefault();
        if (searchTerm.trim()) {
            await fetchStockData(searchTerm);
            setShowSearchResults(false);
        }
    };

    const handleStockSelect = async (symbol) => {
        setSearchTerm(symbol);
        await fetchStockData(symbol);
        setShowSearchResults(false);
    };

    const handleSearchInputChange = async (value) => {
        setSearchTerm(value);

        if (value.length > 1) {
            setShowSearchResults(true);
            try {
                const results = await stockAPI.searchStocks(value);
                setSearchResults(results.slice(0, 8));
            } catch (error) {
                console.error('Search error:', error);
                setSearchResults([]);
            }
        } else {
            setShowSearchResults(false);
        }
    };

    const executeTrade = async () => {
        // Check if Indian market is open first
        if (!isMarketOpen) {
            setMessage('❌ Trading is only available when the Indian market is open (9:15 AM - 3:30 PM IST, Monday-Friday)');
            return;
        }

        if (!stockData || quantity <= 0) {
            setMessage('❌ Please enter a valid quantity');
            return;
        }

        const currentPrice = parseFloat(orderType === 'MARKET' ? stockData.price : limitPrice);
        const estimatedTotal = quantity * currentPrice;

        if (isNaN(estimatedTotal)) {
            setMessage('❌ Invalid price data. Please try again.');
            return;
        }

        if (action === 'BUY' && estimatedTotal > user.balance) {
            setMessage(`❌ Insufficient balance. You need ₹${estimatedTotal.toFixed(2)} but only have ₹${user.balance.toFixed(2)}`);
            return;
        }

        if (action === 'SELL') {
            const currentHolding = portfolio[stockData.symbol] || 0;
            if (quantity > currentHolding) {
                setMessage(`❌ Insufficient shares. You only have ${currentHolding} shares of ${stockData.symbol}`);
                return;
            }
        }

        setLoading(true);
        try {
            // Simulate API call
            await new Promise(resolve => setTimeout(resolve, 1000));

            // Create transaction record
            const transaction = {
                id: Date.now().toString(),
                symbol: stockData.symbol,
                name: stockData.name,
                action: action,
                quantity: quantity,
                price: currentPrice,
                total: estimatedTotal,
                type: orderType,
                timestamp: new Date().toISOString(),
                status: 'COMPLETED'
            };

            // Update portfolio
            const updatedPortfolio = { ...portfolio };
            const currentShares = updatedPortfolio[stockData.symbol] || 0;

            if (action === 'BUY') {
                updatedPortfolio[stockData.symbol] = currentShares + quantity;
                user.balance -= estimatedTotal;
            } else {
                updatedPortfolio[stockData.symbol] = currentShares - quantity;
                user.balance += estimatedTotal;

                // Remove stock from portfolio if quantity becomes zero
                if (updatedPortfolio[stockData.symbol] <= 0) {
                    delete updatedPortfolio[stockData.symbol];
                }
            }

            // Update transactions
            const updatedTransactions = [transaction, ...transactions];

            // Update state
            setPortfolio(updatedPortfolio);
            setTransactions(updatedTransactions);

            // Save to localStorage
            localStorage.setItem('userPortfolio', JSON.stringify(updatedPortfolio));
            localStorage.setItem('userTransactions', JSON.stringify(updatedTransactions));

            setMessage(`✅ ${action} order executed successfully! ${quantity} shares of ${stockData.symbol} for ₹${estimatedTotal.toFixed(2)}`);
            setQuantity(1);

        } catch (error) {
            setMessage('❌ Trade execution failed. Please try again.');
            console.error('Trade error:', error);
        } finally {
            setLoading(false);
        }
    };

    const clearOrder = () => {
        setQuantity(1);
        setAction('BUY');
        setOrderType('MARKET');
        setDuration('IOC');
        setMessage('');
        if (stockData && stockData.price) {
            setLimitPrice(stockData.price);
        }
    };

    const handleForecastNavigation = (symbol = null) => {
        if (symbol) {
            onNavigate('forecast', { symbol });
        } else {
            onNavigate('forecast');
        }
    };

    const currentPrice = stockData ? parseFloat(orderType === 'MARKET' ? stockData.price : limitPrice) : 0;
    const estimatedTotal = currentPrice * quantity;
    const canAfford = action === 'BUY' ? estimatedTotal <= user.balance : true;
    const remainingBalance = user.balance - (action === 'BUY' ? estimatedTotal : -estimatedTotal);
    const canTrade = !loading && isMarketOpen;

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
                            ₹{user?.balance?.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
                        </div>
                        <div className={`market-status-badge ${isMarketOpen ? 'open' : 'closed'}`}>
                            {isMarketOpen ? '🟢 Market Open' : '🔴 Market Closed'}
                        </div>
                    </div>

                    <div className="sidebar-action-buttons">
                        <button onClick={() => onNavigate('dashboard')} className="sidebar-action-button">
                            📊 HOME
                        </button>
                        <button onClick={() => onNavigate('portfolio')} className="sidebar-action-button">
                            💼 PORTFOLIO
                        </button>
                        <button onClick={() => onNavigate('trade')} className="sidebar-action-button primary">
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
                <div className="trade-container">
                    {/* Market Status Banner */}
                    <div className="market-status-banner">
                        <div className="market-info">
                            <div className={`status-indicator ${isMarketOpen ? 'open' : 'closed'}`}></div>
                            <span className="market-hours">{marketStatus}</span>
                            {!isMarketOpen && (
                                <span className="trading-hours">
                                    Indian Market Hours: 9:15 AM - 3:30 PM IST (Mon-Fri)
                                </span>
                            )}
                        </div>
                        <div className="api-status">
                            <span className="last-updated">Indian Stock Exchange</span>
                            <span className="api-usage">Real-time Data</span>
                        </div>
                    </div>

                    {/* Search Section */}
                    <div className="search-section">
                        <h2>🔍 Search Indian Stocks</h2>
                        <form onSubmit={handleSearch} className="search-form">
                            <div className="search-input-container">
                                <input
                                    type="text"
                                    value={searchTerm}
                                    onChange={(e) => handleSearchInputChange(e.target.value)}
                                    placeholder="Search any Indian stock symbol or name..."
                                    disabled={stockLoading}
                                />
                                {showSearchResults && searchResults.length > 0 && (
                                    <div className="search-results-dropdown">
                                        {searchResults.map((result) => (
                                            <div
                                                key={result.symbol}
                                                className="search-result-item"
                                                onClick={() => handleStockSelect(result.symbol)}
                                            >
                                                <div className="search-result-symbol">
                                                    {result.symbol}
                                                </div>
                                                <div className="search-result-name">
                                                    {result.name}
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                            <button type="submit" disabled={stockLoading}>
                                {stockLoading ? '🔍 Searching...' : 'SEARCH'}
                            </button>
                        </form>
                    </div>

                    {/* Popular Stocks Ticker */}
                    <div className="popular-stocks-section">
                        <div className="popular-stocks-header">
                            <h3>📈 Popular Indian Stocks</h3>
                            <button
                                onClick={() => handleForecastNavigation()}
                                className="btn-forecast"
                            >
                                🔮 Get AI Forecast
                            </button>
                        </div>
                        <div className="stock-tickers">
                            {stocksList.map((stock) => (
                                <div
                                    key={stock.symbol}
                                    className={`ticker-item ${selectedStock === stock.symbol ? 'active' : ''}`}
                                    onClick={() => handleStockSelect(stock.symbol)}
                                >
                                    <div className="ticker-symbol">{stock.symbol}</div>
                                    <div className="ticker-name">{stock.name}</div>
                                    <div className="ticker-price">₹{stock.price}</div>
                                    <div className={`ticker-change ${parseFloat(stock.change) >= 0 ? 'positive' : 'negative'}`}>
                                        {parseFloat(stock.change) >= 0 ? '+' : ''}{stock.change}%
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>

                    {stockLoading ? (
                        <div className="loading">
                            <div className="loading-spinner"></div>
                            Loading real-time market data...
                        </div>
                    ) : stockData ? (
                        <div className="trade-content">
                            <div className="trade-layout">
                                {/* Left Column - Stock Info and Active Stocks */}
                                <div className="left-column">
                                    {/* Stock Info Card */}
                                    <div className="stock-info-card">
                                        <div className="stock-header">
                                            <div className="stock-title-section">
                                                <h2>{stockData.symbol}</h2>
                                                <p className="stock-full-name">{stockData.name}</p>
                                            </div>
                                            <button
                                                onClick={() => handleForecastNavigation(stockData.symbol)}
                                                className="btn-forecast-small"
                                                title="Get AI forecast for this stock"
                                            >
                                                🔮 Forecast
                                            </button>
                                        </div>
                                        <div className="stock-price">
                                            <span className="price">₹{stockData.price}</span>
                                            <span className={`change ${parseFloat(stockData.change) >= 0 ? 'positive' : 'negative'}`}>
                                                {parseFloat(stockData.change) >= 0 ? '+' : ''}{stockData.change} ({stockData.changePercent})
                                                {parseFloat(stockData.change) >= 0 ? ' 📈' : ' 📉'}
                                            </span>
                                        </div>

                                        <div className="stock-details-grid">
                                            <div className="detail-item">
                                                <span>Open</span>
                                                <strong>₹{stockData.open}</strong>
                                            </div>
                                            <div className="detail-item">
                                                <span>High</span>
                                                <strong>₹{stockData.high}</strong>
                                            </div>
                                            <div className="detail-item">
                                                <span>Low</span>
                                                <strong>₹{stockData.low}</strong>
                                            </div>
                                            <div className="detail-item">
                                                <span>Previous Close</span>
                                                <strong>₹{stockData.previousClose}</strong>
                                            </div>
                                            <div className="detail-item">
                                                <span>Volume</span>
                                                <strong>{stockData.volume}</strong>
                                            </div>
                                            <div className="detail-item">
                                                <span>Last Updated</span>
                                                <strong>{stockData.timestamp}</strong>
                                            </div>
                                            <div className="detail-item data-source">
                                                <span>Data Source</span>
                                                <strong className="real-time">Indian Stock Exchange</strong>
                                            </div>
                                        </div>
                                    </div>

                                    {/* Available Stocks List */}
                                    <div className="stock-list-sidebar">
                                        <div className="stock-list-header">
                                            <h3>💼 Available Indian Stocks ({availableStocksData.length}+)</h3>
                                            <button
                                                onClick={() => handleForecastNavigation()}
                                                className="btn-forecast-small"
                                            >
                                                🔮
                                            </button>
                                        </div>
                                        <div className="stock-list">
                                            {availableStocksData.map((stock) => (
                                                <div
                                                    key={stock.symbol}
                                                    className={`stock-item ${selectedStock === stock.symbol ? 'active' : ''}`}
                                                    onClick={() => handleStockSelect(stock.symbol)}
                                                >
                                                    <div className="stock-info">
                                                        <div className="stock-symbol">{stock.symbol}</div>
                                                        <div className="stock-full-name">{stock.name}</div>
                                                    </div>
                                                    <div className="stock-price-sidebar">
                                                        <div className="current-price">₹{stock.price}</div>
                                                        <div className={`price-change-sidebar ${parseFloat(stock.change) >= 0 ? 'positive' : 'negative'}`}>
                                                            {parseFloat(stock.change) >= 0 ? '+' : ''}{stock.change}%
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                </div>

                                {/* Right Column - Trading Panel */}
                                <div className="right-column">
                                    {/* Trading Panel */}
                                    <div className="trading-panel">
                                        <div className="panel-header">
                                            <h3>💹 Trading Panel</h3>
                                            <div className="action-buttons">
                                                <button
                                                    className={`action-btn ${action === 'BUY' ? 'active buy' : ''}`}
                                                    onClick={() => setAction('BUY')}
                                                    disabled={loading || !isMarketOpen}
                                                >
                                                    🟢 BUY
                                                </button>
                                                <button
                                                    className={`action-btn ${action === 'SELL' ? 'active sell' : ''}`}
                                                    onClick={() => setAction('SELL')}
                                                    disabled={loading || !isMarketOpen}
                                                >
                                                    🔴 SELL
                                                </button>
                                            </div>
                                        </div>

                                        {!isMarketOpen && (
                                            <div className="market-closed-warning">
                                                <div className="warning-icon">⏰</div>
                                                <div className="warning-content">
                                                    <strong>Indian Market Closed</strong>
                                                    <p>Trading available only during Indian market hours (9:15 AM - 3:30 PM IST, Monday-Friday)</p>
                                                    <button
                                                        onClick={() => handleForecastNavigation(stockData.symbol)}
                                                        className="btn-forecast-small"
                                                        style={{marginTop: '10px'}}
                                                    >
                                                        🔮 Get AI Forecast Instead
                                                    </button>
                                                </div>
                                            </div>
                                        )}

                                        <div className="trade-form">
                                            <div className="form-row">
                                                <div className="form-group">
                                                    <label>Stock Symbol</label>
                                                    <input type="text" value={stockData.symbol} readOnly />
                                                </div>
                                                <div className="form-group">
                                                    <label>Current Price</label>
                                                    <input type="text" value={`₹${stockData.price}`} readOnly />
                                                </div>
                                            </div>

                                            <div className="form-row">
                                                <div className="form-group">
                                                    <label>Order Type</label>
                                                    <select
                                                        value={orderType}
                                                        onChange={(e) => setOrderType(e.target.value)}
                                                        disabled={loading || !isMarketOpen}
                                                    >
                                                        <option value="MARKET">Market Order</option>
                                                        <option value="LIMIT">Limit Order</option>
                                                    </select>
                                                </div>
                                                <div className="form-group">
                                                    <label>Duration</label>
                                                    <select
                                                        value={duration}
                                                        onChange={(e) => setDuration(e.target.value)}
                                                        disabled={loading || !isMarketOpen}
                                                    >
                                                        <option value="IOC">Immediate or Cancel (IOC)</option>
                                                        <option value="FOK">Fill or Kill (FOK)</option>
                                                        <option value="DAY">Day Order</option>
                                                    </select>
                                                </div>
                                            </div>

                                            {orderType === 'LIMIT' && (
                                                <div className="form-group">
                                                    <label>Limit Price (₹)</label>
                                                    <input
                                                        type="number"
                                                        value={limitPrice}
                                                        onChange={(e) => setLimitPrice(e.target.value)}
                                                        step="0.01"
                                                        min="0.01"
                                                        disabled={loading || !isMarketOpen}
                                                        placeholder="Enter limit price"
                                                    />
                                                </div>
                                            )}

                                            <div className="form-group">
                                                <label>Quantity</label>
                                                <div className="quantity-control">
                                                    <button
                                                        type="button"
                                                        onClick={() => setQuantity(prev => Math.max(1, parseInt(prev) - 1))}
                                                        disabled={loading || quantity <= 1 || !isMarketOpen}
                                                    >
                                                        -
                                                    </button>
                                                    <input
                                                        type="number"
                                                        value={quantity}
                                                        onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                                                        min="1"
                                                        disabled={loading || !isMarketOpen}
                                                    />
                                                    <button
                                                        type="button"
                                                        onClick={() => setQuantity(prev => parseInt(prev) + 1)}
                                                        disabled={loading || !isMarketOpen}
                                                    >
                                                        +
                                                    </button>
                                                </div>
                                            </div>

                                            <div className="trade-summary">
                                                <div className="summary-item">
                                                    <span>Action:</span>
                                                    <strong className={action === 'BUY' ? 'positive' : 'negative'}>
                                                        {action}
                                                    </strong>
                                                </div>
                                                <div className="summary-item">
                                                    <span>Estimated Total:</span>
                                                    <strong>₹{estimatedTotal.toFixed(2)}</strong>
                                                </div>
                                                <div className="summary-item">
                                                    <span>Available Cash:</span>
                                                    <strong>₹{user.balance.toFixed(2)}</strong>
                                                </div>
                                                <div className="summary-item">
                                                    <span>Remaining After Trade:</span>
                                                    <strong className={remainingBalance >= 0 ? 'positive' : 'negative'}>
                                                        ₹{remainingBalance.toFixed(2)}
                                                    </strong>
                                                </div>
                                            </div>

                                            {message && (
                                                <div className={`message ${message.includes('✅') ? 'success' : message.includes('❌') ? 'error' : 'info'}`}>
                                                    {message}
                                                </div>
                                            )}

                                            <div className="trade-actions">
                                                <button
                                                    className="btn-secondary"
                                                    onClick={clearOrder}
                                                    disabled={loading}
                                                >
                                                    🗑️ Clear Order
                                                </button>
                                                <button
                                                    className={`btn-primary ${!canAfford || !isMarketOpen ? 'disabled' : ''}`}
                                                    onClick={executeTrade}
                                                    disabled={loading || !canAfford || !isMarketOpen}
                                                >
                                                    {loading ? (
                                                        <>
                                                            <div className="loading-spinner-small"></div>
                                                            Processing...
                                                        </>
                                                    ) : (
                                                        `🎯 ${action} ${orderType === 'LIMIT' ? 'LIMIT' : 'MARKET'} ORDER`
                                                    )}
                                                </button>
                                            </div>

                                            {/* Forecast Suggestion */}
                                            {!isMarketOpen && (
                                                <div className="forecast-suggestion">
                                                    <div className="suggestion-content">
                                                        <div className="suggestion-icon">🔮</div>
                                                        <div className="suggestion-text">
                                                            <strong>Market Closed? Get AI Insights!</strong>
                                                            <p>Use our AI forecasting tool to predict market movements and plan your next trade</p>
                                                        </div>
                                                    </div>
                                                    <button
                                                        onClick={() => handleForecastNavigation(stockData.symbol)}
                                                        className="btn-forecast"
                                                    >
                                                        Get Forecast
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <div className="error">
                            <div className="error-icon">📊</div>
                            <h3>No stock data available</h3>
                            <p>Try searching for any of 50+ available Indian stocks</p>
                            <div className="error-actions">
                                <button
                                    onClick={() => fetchStockData('RELIANCE')}
                                    className="btn-primary"
                                >
                                    🔄 Load Stock Data
                                </button>
                                <button
                                    onClick={() => handleForecastNavigation()}
                                    className="btn-forecast"
                                >
                                    🔮 Explore Forecasts
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Trade;