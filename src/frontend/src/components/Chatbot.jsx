import React, { useState, useRef, useEffect } from 'react';
import '../styles/Chatbot.css';

<<<<<<< HEAD
const Chatbot = ({ onClose, user, transactions = [], portfolio = [] }) => {
    const [messages, setMessages] = useState([
        {
            id: 1,
            text: `Hello ${user?.username || 'there'}! I'm your Smart Invest AI assistant. I can help you with:
• Stock prices & analysis
• Account details & balance
• Portfolio performance
• Transaction history
• Investment advice
• Market forecasts

Try asking: "What is Apple stock price?" or "How is my portfolio doing?"`,
=======
const Chatbot = ({ onClose, user, transactions = [], portfolio = [], onNavigate }) => {
    const [messages, setMessages] = useState([
        {
            id: 1,
            text: `Hello ${user?.username || 'there'}! I'm your Smart Invest AI assistant. I can help you with:\n• Stock prices & analysis\n• Account details & balance\n• Portfolio performance\n• Transaction history\n• Investment advice\n• Market forecasts\n\nTry asking: "What is Apple stock price?" or "How is my portfolio doing?"`,
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
            sender: 'bot',
            timestamp: new Date()
        }
    ]);
    const [inputMessage, setInputMessage] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const messagesEndRef = useRef(null);

<<<<<<< HEAD
    // Comprehensive stock database (static demo data)
=======
    // Comprehensive stock database
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    const stockData = {
        'AAPL': {
            price: 182.63, change: 1.24, changePercent: 0.68,
            name: 'Apple Inc.', sector: 'Technology', market: 'US',
            analysis: "Strong buy. iPhone sales growing, services revenue increasing. New product launches expected.",
            forecast: "Target: $195 (6% upside)"
        },
        'TSLA': {
            price: 234.50, change: -2.30, changePercent: -0.97,
            name: 'Tesla Inc.', sector: 'Automotive', market: 'US',
            analysis: "Hold. Facing increased EV competition. Watch Q4 delivery numbers.",
            forecast: "Short-term volatility expected"
        },
        'GOOGL': {
            price: 138.21, change: 0.85, changePercent: 0.62,
            name: 'Alphabet Inc.', sector: 'Technology', market: 'US',
            analysis: "Buy. Strong cloud growth and AI integration. Advertising revenue stable.",
            forecast: "Target: $155 (12% upside)"
        },
        'MSFT': {
            price: 378.85, change: 2.15, changePercent: 0.57,
            name: 'Microsoft Corporation', sector: 'Technology', market: 'US',
            analysis: "Strong buy. Azure cloud leading market share. AI integration driving growth.",
            forecast: "Target: $420 (11% upside)"
        },
        'AMZN': {
            price: 154.65, change: 1.20, changePercent: 0.78,
            name: 'Amazon.com Inc.', sector: 'E-Commerce', market: 'US',
            analysis: "Buy. AWS cloud dominance. E-commerce margins improving.",
            forecast: "Target: $175 (13% upside)"
        },
        'RELIANCE': {
            price: 2494.80, change: 5.20, changePercent: 0.21,
            name: 'Reliance Industries Ltd', sector: 'Conglomerate', market: 'India',
            analysis: "Strong buy. Jio platforms expanding, retail segment growing.",
            forecast: "Target: ₹2,800 (12% upside)"
        },
        'TCS': {
            price: 3508.83, change: 8.81, changePercent: 0.25,
            name: 'Tata Consultancy Services', sector: 'IT Services', market: 'India',
            analysis: "Hold. Stable IT services growth but margin pressures.",
            forecast: "Target: ₹3,650 (4% upside)"
        },
        'INFY': {
            price: 1496.07, change: 3.93, changePercent: 0.26,
            name: 'Infosys Ltd', sector: 'IT Services', market: 'India',
            analysis: "Buy. Digital transformation projects driving growth.",
            forecast: "Target: ₹1,650 (10% upside)"
        },
        'NVDA': {
            price: 485.09, change: 8.75, changePercent: 1.84,
            name: 'NVIDIA Corporation', sector: 'Semiconductors', market: 'US',
            analysis: "Strong buy. AI chip dominance. Gaming segment stable.",
            forecast: "Target: $550 (13% upside)"
        },
        'META': {
            price: 348.34, change: -1.25, changePercent: -0.36,
            name: 'Meta Platforms Inc.', sector: 'Technology', market: 'US',
            analysis: "Hold. Regulatory concerns. Ad market fluctuations.",
            forecast: "Approach with caution"
        }
    };

<<<<<<< HEAD
    // Default data (used when no real user/portfolio/transactions passed)
    const currentUser = user || {
        username: 'User',
        email: 'user@example.com',
=======
    // Default data
    const currentUser = user || {
        username: 'Aishwarya',
        email: 'aishwarya@gmail.com',
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
        balance: 98809.76
    };

    const currentPortfolio = portfolio.length > 0 ? portfolio : [
        { symbol: 'AAPL', quantity: 10, avgCost: 175.50, currentPrice: 182.63 },
        { symbol: 'TSLA', quantity: 5, avgCost: 240.00, currentPrice: 234.50 },
        { symbol: 'RELIANCE', quantity: 20, avgCost: 2450.00, currentPrice: 2494.80 }
    ];

    const currentTransactions = transactions.length > 0 ? transactions : [
        { symbol: 'AAPL', action: 'BUY', quantity: 10, price: 175.50, total: 1755.00, date: '2024-01-15' },
        { symbol: 'TSLA', action: 'BUY', quantity: 5, price: 240.00, total: 1200.00, date: '2024-01-10' },
        { symbol: 'RELIANCE', action: 'BUY', quantity: 20, price: 2450.00, total: 49000.00, date: '2024-01-05' }
    ];

    const scrollToBottom = () => {
<<<<<<< HEAD
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
=======
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

<<<<<<< HEAD
    // ------------- RESPONSE GENERATION LOGIC -------------

=======
    // SMART RESPONSE GENERATOR - Handles ALL types of questions
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    const generateAIResponse = (userMessage) => {
        const lowerMessage = userMessage.toLowerCase();

        // Extract stock symbols from message
        const stockSymbols = Object.keys(stockData);
        const foundSymbol = stockSymbols.find(symbol =>
            lowerMessage.includes(symbol.toLowerCase()) ||
            lowerMessage.includes(stockData[symbol].name.toLowerCase().split(' ')[0])
        );

        // 1. SPECIFIC STOCK QUERIES
        if (foundSymbol) {
            const stock = stockData[foundSymbol];
<<<<<<< HEAD
            const changeIcon = stock.change >= 0 ? '[UP]' : '[DOWN]';

            return `${foundSymbol} - ${stock.name}

Current Price: ${stock.market === 'India' ? '₹' : '$'}${stock.price}
${changeIcon} Today's Change: ${stock.change >= 0 ? '+' : ''}${stock.change} (${stock.change >= 0 ? '+' : ''}${stock.changePercent}%)
Sector: ${stock.sector}
Market: ${stock.market} Stock Exchange

AI Analysis: ${stock.analysis}
Forecast: ${stock.forecast}
=======
            const changeIcon = stock.change >= 0 ? '🟢' : '🔴';

            return `📊 **${foundSymbol} - ${stock.name}**

💰 **Current Price**: ${stock.market === 'India' ? '₹' : '$'}${stock.price}
${changeIcon} **Today's Change**: ${stock.change >= 0 ? '+' : ''}${stock.change} (${stock.change >= 0 ? '+' : ''}${stock.changePercent}%)
🏷️ **Sector**: ${stock.sector}
📈 **Market**: ${stock.market} Stock Exchange

💡 **AI Analysis**: ${stock.analysis}
🔮 **Forecast**: ${stock.forecast}
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

${getStockAdditionalInfo(foundSymbol)}`;
        }

        // 2. STOCK PRICE QUERIES (general)
        if (lowerMessage.includes('stock') || lowerMessage.includes('price') ||
            lowerMessage.includes('share') || lowerMessage.includes('quote') ||
            lowerMessage.match(/how much is (apple|tesla|google|microsoft|reliance|tcs|infosys)/i)) {
            return generateStockListResponse(lowerMessage);
        }

        // 3. ACCOUNT BALANCE
        if (lowerMessage.includes('balance') || lowerMessage.includes('account') ||
            lowerMessage.includes('how much money') || lowerMessage.includes('funds')) {
<<<<<<< HEAD
            return `Your Account Balance

Username: ${currentUser.username}
Available Balance: $${currentUser.balance.toLocaleString()}
Account Type: Simulation Account
Status: Active and Ready

Your funds are available for immediate trading in your virtual account!`;
=======
            return `💰 **Your Account Balance**

👤 **Username**: ${currentUser.username}
💵 **Available Balance**: $${currentUser.balance.toLocaleString()}
🏦 **Account Type**: Live Trading
📈 **Status**: Active and Ready

Your funds are available for immediate trading! 💹`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
        }

        // 4. PORTFOLIO PERFORMANCE
        if (lowerMessage.includes('portfolio') || lowerMessage.includes('holding') ||
            lowerMessage.includes('investment') || lowerMessage.includes('my stocks')) {
            return generatePortfolioResponse();
        }

        // 5. TRANSACTION HISTORY
        if (lowerMessage.includes('transaction') || lowerMessage.includes('history') ||
            lowerMessage.includes('trade') || lowerMessage.includes('my trades')) {
            return generateTransactionResponse();
        }

        // 6. INVESTMENT ADVICE
        if (lowerMessage.includes('advice') || lowerMessage.includes('recommend') ||
            lowerMessage.includes('should i') || lowerMessage.includes('what to buy') ||
            lowerMessage.includes('best stock')) {
            return generateAdviceResponse();
        }

        // 7. MARKET STATUS
        if (lowerMessage.includes('market') || lowerMessage.includes('status') ||
            lowerMessage.includes('open') || lowerMessage.includes('close')) {
            return generateMarketResponse();
        }

        // 8. FORECAST/PREDICTION
        if (lowerMessage.includes('forecast') || lowerMessage.includes('prediction') ||
            lowerMessage.includes('future') || lowerMessage.includes('outlook')) {
            return generateForecastResponse();
        }

        // 9. GREETINGS
        if (lowerMessage.includes('hello') || lowerMessage.includes('hi') ||
            lowerMessage.includes('hey') || lowerMessage.includes('good morning') ||
            lowerMessage.includes('good afternoon')) {
<<<<<<< HEAD
            return `Hello ${currentUser.username}! I'm your Smart Invest AI. How can I help with your investments today?`;
=======
            return `Hello ${currentUser.username}! 👋 I'm your Smart Invest AI. How can I help with your investments today?`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
        }

        // 10. HELP
        if (lowerMessage.includes('help') || lowerMessage.includes('what can you do') ||
            lowerMessage.includes('how can you help')) {
            return `I'm Smart Invest AI! Here's what I can help you with:

<<<<<<< HEAD
Stock Analysis: Current prices, trends, recommendations
Account Info: Balance, portfolio performance
Portfolio Tracking: Your holdings and returns
Investment Advice: Buy/sell recommendations
Market Forecasts: Short-term predictions
Transaction History: Your trading activity
=======
📈 **Stock Analysis**: Current prices, trends, recommendations
💰 **Account Info**: Balance, portfolio performance  
📊 **Portfolio Tracking**: Your holdings and returns
💡 **Investment Advice**: Buy/sell recommendations
🔮 **Market Forecasts**: Short-term predictions
📋 **Transaction History**: Your trading activity
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

Try asking:
• "What is Apple stock price?"
• "How is my portfolio doing?"
• "Give me investment advice"
• "Market forecast today"`;
        }

        // 11. THANK YOU
        if (lowerMessage.includes('thank') || lowerMessage.includes('thanks')) {
<<<<<<< HEAD
            return `You're welcome, ${currentUser.username}! Is there anything else I can help you with regarding your investments?`;
=======
            return `You're welcome, ${currentUser.username}! 😊 Is there anything else I can help you with regarding your investments?`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
        }

        // 12. RANDOM QUESTIONS ABOUT STOCKS/INVESTING
        if (lowerMessage.includes('what is') || lowerMessage.includes('what are') ||
            lowerMessage.includes('how to') || lowerMessage.includes('can you explain')) {
            return generateEducationalResponse(lowerMessage);
        }

        // 13. DEFAULT - INTELLIGENT FALLBACK
<<<<<<< HEAD
        return `I understand you're asking about "${userMessage}".

As your investment assistant, I specialize in:
• Stock prices and analysis
• Portfolio performance tracking
• Investment recommendations
• Market forecasts and trends
• Account management
=======
        return `I understand you're asking about "${userMessage}". 

As your investment assistant, I specialize in:
• Stock prices and analysis 📈
• Portfolio performance tracking 💼
• Investment recommendations 💡
• Market forecasts and trends 🔮
• Account management 💰
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

Could you be more specific about what you'd like to know? For example:
• "What is Apple's current stock price?"
• "How is my portfolio performing?"
• "Give me investment advice"
• "Show me market forecasts"`;
    };

    const getStockAdditionalInfo = (symbol) => {
        const info = {
<<<<<<< HEAD
            'AAPL': "Recent News: New iPhone launch expected next quarter. Services revenue growing at 15% YoY.",
            'TSLA': "Recent News: New model announcements expected. EV tax credits extended.",
            'GOOGL': "Recent News: AI integration across products. Cloud growth accelerating.",
            'MSFT': "Recent News: Azure market share increasing. AI partnerships expanding.",
            'RELIANCE': "Recent News: Retail expansion continuing. Jio 5G rollout progressing well.",
            'TCS': "Recent News: Large deal wins in digital transformation. Margin improvement expected.",
            'INFY': "Recent News: Strong deal pipeline. Focus on AI and cloud services."
        };
        return info[symbol] || "Consider this for long-term portfolio diversification.";
=======
            'AAPL': "📱 **Recent News**: New iPhone launch expected next quarter. Services revenue growing at 15% YoY.",
            'TSLA': "⚡ **Recent News**: New model announcements expected. EV tax credits extended.",
            'GOOGL': "🤖 **Recent News**: AI integration across products. Cloud growth accelerating.",
            'MSFT': "☁️ **Recent News**: Azure market share increasing. AI partnerships expanding.",
            'RELIANCE': "🛒 **Recent News**: Retail expansion continuing. Jio 5G rollout progressing well.",
            'TCS': "💼 **Recent News**: Large deal wins in digital transformation. Margin improvement expected.",
            'INFY': "🚀 **Recent News**: Strong deal pipeline. Focus on AI and cloud services."
        };
        return info[symbol] || "💡 Consider this for long-term portfolio diversification.";
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    };

    const generateStockListResponse = (message) => {
        // Check if user is asking about specific types of stocks
        if (message.includes('indian') || message.includes('india')) {
<<<<<<< HEAD
            return `Indian Stocks - Current Prices

• RELIANCE: ₹${stockData.RELIANCE.price} ${stockData.RELIANCE.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.RELIANCE.changePercent}%
• TCS: ₹${stockData.TCS.price} ${stockData.TCS.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.TCS.changePercent}%
• INFY: ₹${stockData.INFY.price} ${stockData.INFY.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.INFY.changePercent}%

Indian markets showing strength with steady growth.`;
        }

        if (message.includes('tech') || message.includes('technology')) {
            return `Technology Stocks - Current Prices

• AAPL: $${stockData.AAPL.price} ${stockData.AAPL.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.AAPL.changePercent}%
• MSFT: $${stockData.MSFT.price} ${stockData.MSFT.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.MSFT.changePercent}%
• GOOGL: $${stockData.GOOGL.price} ${stockData.GOOGL.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.GOOGL.changePercent}%
• NVDA: $${stockData.NVDA.price} ${stockData.NVDA.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.NVDA.changePercent}%

Technology sector leading market gains.`;
        }

        // General stock list
        return `Popular Stocks - Current Prices

US Stocks:
• AAPL (Apple): $${stockData.AAPL.price} ${stockData.AAPL.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.AAPL.changePercent}%
• MSFT (Microsoft): $${stockData.MSFT.price} ${stockData.MSFT.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.MSFT.changePercent}%
• TSLA (Tesla): $${stockData.TSLA.price} ${stockData.TSLA.change >= 0 ? '[UP]' : '[DOWN]'} ${Math.abs(stockData.TSLA.changePercent)}%
• GOOGL (Google): $${stockData.GOOGL.price} ${stockData.GOOGL.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.GOOGL.changePercent}%

Indian Stocks:
• RELIANCE: ₹${stockData.RELIANCE.price} ${stockData.RELIANCE.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.RELIANCE.changePercent}%
• TCS: ₹${stockData.TCS.price} ${stockData.TCS.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.TCS.changePercent}%
• INFY: ₹${stockData.INFY.price} ${stockData.INFY.change >= 0 ? '[UP]' : '[DOWN]'} ${stockData.INFY.changePercent}%

Ask about any specific stock for detailed analysis!`;
=======
            return `🇮🇳 **Indian Stocks - Current Prices**

• RELIANCE: ₹${stockData.RELIANCE.price} ${stockData.RELIANCE.change >= 0 ? '🟢' : '🔴'} ${stockData.RELIANCE.changePercent}%
• TCS: ₹${stockData.TCS.price} ${stockData.TCS.change >= 0 ? '🟢' : '🔴'} ${stockData.TCS.changePercent}%
• INFY: ₹${stockData.INFY.price} ${stockData.INFY.change >= 0 ? '🟢' : '🔴'} ${stockData.INFY.changePercent}%

Indian markets showing strength with steady growth. 📈`;
        }

        if (message.includes('tech') || message.includes('technology')) {
            return `🤖 **Technology Stocks - Current Prices**

• AAPL: $${stockData.AAPL.price} ${stockData.AAPL.change >= 0 ? '🟢' : '🔴'} ${stockData.AAPL.changePercent}%
• MSFT: $${stockData.MSFT.price} ${stockData.MSFT.change >= 0 ? '🟢' : '🔴'} ${stockData.MSFT.changePercent}%
• GOOGL: $${stockData.GOOGL.price} ${stockData.GOOGL.change >= 0 ? '🟢' : '🔴'} ${stockData.GOOGL.changePercent}%
• NVDA: $${stockData.NVDA.price} ${stockData.NVDA.change >= 0 ? '🟢' : '🔴'} ${stockData.NVDA.changePercent}%

Technology sector leading market gains. 💻`;
        }

        // General stock list
        return `📈 **Popular Stocks - Current Prices**

🇺🇸 **US Stocks**:
• AAPL (Apple): $${stockData.AAPL.price} ${stockData.AAPL.change >= 0 ? '🟢' : '🔴'} ${stockData.AAPL.changePercent}%
• MSFT (Microsoft): $${stockData.MSFT.price} ${stockData.MSFT.change >= 0 ? '🟢' : '🔴'} ${stockData.MSFT.changePercent}%
• TSLA (Tesla): $${stockData.TSLA.price} ${stockData.TSLA.change >= 0 ? '🟢' : '🔴'} ${Math.abs(stockData.TSLA.changePercent)}%
• GOOGL (Google): $${stockData.GOOGL.price} ${stockData.GOOGL.change >= 0 ? '🟢' : '🔴'} ${stockData.GOOGL.changePercent}%

🇮🇳 **Indian Stocks**:
• RELIANCE: ₹${stockData.RELIANCE.price} ${stockData.RELIANCE.change >= 0 ? '🟢' : '🔴'} ${stockData.RELIANCE.changePercent}%
• TCS: ₹${stockData.TCS.price} ${stockData.TCS.change >= 0 ? '🟢' : '🔴'} ${stockData.TCS.changePercent}%
• INFY: ₹${stockData.INFY.price} ${stockData.INFY.change >= 0 ? '🟢' : '🔴'} ${stockData.INFY.changePercent}%

Ask about any specific stock for detailed analysis! 🔍`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    };

    const generatePortfolioResponse = () => {
        const totalValue = currentPortfolio.reduce((sum, stock) => sum + (stock.currentPrice * stock.quantity), 0);
        const totalInvestment = currentPortfolio.reduce((sum, stock) => sum + (stock.avgCost * stock.quantity), 0);
        const totalGainLoss = totalValue - totalInvestment;
        const totalReturnPercent = (totalGainLoss / totalInvestment) * 100;

<<<<<<< HEAD
        let response = `Your Portfolio Performance

Total Portfolio Value: $${totalValue.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
Total Return: ${totalReturnPercent.toFixed(2)}% ${totalReturnPercent >= 0 ? '[UP]' : '[DOWN]'}
Total Gain/Loss: $${totalGainLoss.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })} ${totalGainLoss >= 0 ? '[UP]' : '[DOWN]'}
Total Investment: $${totalInvestment.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
Number of Holdings: ${currentPortfolio.length} stocks
Available Cash: $${currentUser.balance.toLocaleString()}
=======
        let response = `📈 **Your Portfolio Performance**

💰 **Total Portfolio Value**: $${totalValue.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
📊 **Total Return**: ${totalReturnPercent.toFixed(2)}% ${totalReturnPercent >= 0 ? '🟢' : '🔴'}
💸 **Total Gain/Loss**: $${totalGainLoss.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})} ${totalGainLoss >= 0 ? '🟢' : '🔴'}
🏦 **Total Investment**: $${totalInvestment.toLocaleString(undefined, {minimumFractionDigits: 2, maximumFractionDigits: 2})}
📦 **Number of Holdings**: ${currentPortfolio.length} stocks
💵 **Available Cash**: $${currentUser.balance.toLocaleString()}
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

`;

        if (currentPortfolio.length > 0) {
<<<<<<< HEAD
            response += `Your Current Holdings:\n\n`;
=======
            response += `📊 **Your Current Holdings**:\n\n`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
            currentPortfolio.forEach(stock => {
                const currentValue = stock.currentPrice * stock.quantity;
                const investment = stock.avgCost * stock.quantity;
                const gainLoss = currentValue - investment;
                const returnPercent = (gainLoss / investment) * 100;

<<<<<<< HEAD
                response += `• ${stock.symbol}: ${stock.quantity} shares
   Current: $${stock.currentPrice} | Return: ${returnPercent.toFixed(2)}% ${returnPercent >= 0 ? '[UP]' : '[DOWN]'}
=======
                response += `• **${stock.symbol}**: ${stock.quantity} shares
   Current: $${stock.currentPrice} | Return: ${returnPercent.toFixed(2)}% ${returnPercent >= 0 ? '🟢' : '🔴'}
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
   Value: $${currentValue.toLocaleString()}\n\n`;
            });
        }

<<<<<<< HEAD
        response += `Portfolio Advice: Consider diversifying into technology and renewable energy sectors for better returns.`;
=======
        response += `💡 **Portfolio Advice**: Consider diversifying into technology and renewable energy sectors for better returns.`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

        return response;
    };

    const generateTransactionResponse = () => {
<<<<<<< HEAD
        let response = `Your Transaction History

Total Transactions: ${currentTransactions.length}
Buy Orders: ${currentTransactions.filter(t => t.action === 'BUY').length}
Sell Orders: ${currentTransactions.filter(t => t.action === 'SELL').length}
=======
        let response = `📋 **Your Transaction History**

🔢 **Total Transactions**: ${currentTransactions.length}
🟢 **Buy Orders**: ${currentTransactions.filter(t => t.action === 'BUY').length}
🔴 **Sell Orders**: ${currentTransactions.filter(t => t.action === 'SELL').length}
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

`;

        if (currentTransactions.length > 0) {
<<<<<<< HEAD
            response += `Recent Activity:\n\n`;
            currentTransactions.slice(0, 5).forEach(transaction => {
                response += `• ${transaction.action} ${transaction.quantity} ${transaction.symbol}
=======
            response += `📅 **Recent Activity**:\n\n`;
            currentTransactions.slice(0, 5).forEach(transaction => {
                response += `• ${transaction.action} ${transaction.quantity} ${transaction.symbol} 
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
   @ $${transaction.price} = $${transaction.total}
   Date: ${transaction.date}\n\n`;
            });
        }

        return response;
    };

    const generateAdviceResponse = () => {
<<<<<<< HEAD
        return `Investment Recommendations

Based on current market analysis:

Consider Buying:
• MSFT - Strong cloud growth and AI integration
• AAPL - Stable with consistent innovation
• RELIANCE - Indian market leader with diversified business
• NVDA - AI chip dominance

Exercise Caution:
• TSLA - High volatility and competitive pressures
• META - Regulatory concerns

Portfolio Strategy:
=======
        return `💡 **Investment Recommendations**

Based on current market analysis:

🟢 **Consider Buying**:
• **MSFT** - Strong cloud growth and AI integration
• **AAPL** - Stable with consistent innovation
• **RELIANCE** - Indian market leader with diversified business
• **NVDA** - AI chip dominance

🔴 **Exercise Caution**:
• **TSLA** - High volatility and competitive pressures
• **META** - Regulatory concerns

📊 **Portfolio Strategy**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Diversify across technology, banking, and consumer sectors
• Maintain 15-20% cash for opportunities
• Consider systematic investment plans
• Rebalance portfolio quarterly

<<<<<<< HEAD
Long-term Outlook: Technology and renewable energy show strong growth potential.`;
    };

    const generateMarketResponse = () => {
        return `Market Status Update

Current Status: Markets Closed
Last Updated: ${new Date().toLocaleString()}
Next Trading Session: Tomorrow at market open

Market Overview:
=======
💎 **Long-term Outlook**: Technology and renewable energy show strong growth potential.`;
    };

    const generateMarketResponse = () => {
        return `🏛️ **Market Status Update**

📅 **Current Status**: Markets Closed 🔴
⏰ **Last Updated**: ${new Date().toLocaleString()}
🕒 **Next Trading Session**: Tomorrow at market open

📊 **Market Overview**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• US Markets: Mixed with technology leading
• Indian Markets: Showing strength with steady growth
• Global Sentiment: Cautiously optimistic

<<<<<<< HEAD
Trading Hours:
• US Market: 9:30 AM - 4:00 PM EST
• Indian Market: 9:15 AM - 3:30 PM IST

Markets reopen tomorrow. Use this time to research!`;
    };

    const generateForecastResponse = () => {
        return `Market Forecast & Predictions

Short-term Outlook (1-2 weeks):
=======
💡 **Trading Hours**:
• US Market: 9:30 AM - 4:00 PM EST
• Indian Market: 9:15 AM - 3:30 PM IST

Markets reopen tomorrow. Use this time to research! 📈`;
    };

    const generateForecastResponse = () => {
        return `🔮 **Market Forecast & Predictions**

📈 **Short-term Outlook (1-2 weeks)**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Technology stocks expected to continue outperforming
• Indian markets showing bullish momentum
• US markets may see slight correction before next rally

<<<<<<< HEAD
Stock Predictions:
• AAPL: Target $195 (6% upside)
• MSFT: Target $420 (11% upside)
• RELIANCE: Target ₹2,800 (12% upside)
• NVDA: Target $550 (13% upside)

Risk Factors:
=======
🎯 **Stock Predictions**:
• **AAPL**: Target $195 (6% upside)
• **MSFT**: Target $420 (11% upside) 
• **RELIANCE**: Target ₹2,800 (12% upside)
• **NVDA**: Target $550 (13% upside)

⚠️ **Risk Factors**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Geopolitical tensions
• Inflation concerns
• Interest rate decisions

<<<<<<< HEAD
Recommendation: Consider accumulating quality stocks on dips.`;
=======
💡 **Recommendation**: Consider accumulating quality stocks on dips.`;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    };

    const generateEducationalResponse = (message) => {
        if (message.includes('stock') || message.includes('share')) {
<<<<<<< HEAD
            return `What are Stocks?

Stocks (also called shares or equities) represent ownership in a company. When you buy a stock, you become a partial owner of that company.

Key Concepts:
• Stock Price: Current market value of one share
• Market Cap: Total company value (Price × Total Shares)
• Dividends: Company profits shared with shareholders
• Bull Market: Rising prices
• Bear Market: Falling prices

Why Invest in Stocks?:
=======
            return `📚 **What are Stocks?**

Stocks (also called shares or equities) represent ownership in a company. When you buy a stock, you become a partial owner of that company.

**Key Concepts**:
• **Stock Price**: Current market value of one share
• **Market Cap**: Total company value (Price × Total Shares)
• **Dividends**: Company profits shared with shareholders
• **Bull Market**: Rising prices
• **Bear Market**: Falling prices

**Why Invest in Stocks?**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Potential for high returns
• Ownership in growing companies
• Dividend income
• Portfolio diversification

<<<<<<< HEAD
Want to know about a specific stock? Just ask!`;
        }

        if (message.includes('portfolio') || message.includes('diversif')) {
            return `About Portfolio Diversification

Diversification means spreading your investments across different assets to reduce risk.

Why Diversify?:
=======
Want to know about a specific stock? Just ask! 💡`;
        }

        if (message.includes('portfolio') || message.includes('diversif')) {
            return `📊 **About Portfolio Diversification**

Diversification means spreading your investments across different assets to reduce risk.

**Why Diversify?**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Reduces impact of one stock's poor performance
• Balances risk across sectors and markets
• Provides more stable returns over time

<<<<<<< HEAD
Diversification Strategies:
=======
**Diversification Strategies**:
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Invest across different sectors (Tech, Healthcare, Finance)
• Mix of large and small companies
• Include both growth and value stocks
• Consider international markets

<<<<<<< HEAD
A well-diversified portfolio can help you weather market volatility!`;
        }

        return `I'd be happy to explain investment concepts!

I can help you understand:
• What are stocks and how they work
• Portfolio diversification strategies
=======
A well-diversified portfolio can help you weather market volatility! 🌟`;
        }

        return `I'd be happy to explain investment concepts! 

I can help you understand:
• What are stocks and how they work
• Portfolio diversification strategies  
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
• Risk management in investing
• Different types of investments
• Market analysis techniques

<<<<<<< HEAD
What specific investment topic would you like me to explain?`;
    };

    // ------------- MESSAGE HANDLING -------------

    const sendAndRespond = (text) => {
        const userMessage = {
            id: Date.now(),
            text,
=======
What specific investment topic would you like me to explain? 📚`;
    };

    const handleSendMessage = async () => {
        if (!inputMessage.trim() || isLoading) return;

        const userMessage = {
            id: Date.now(),
            text: inputMessage,
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
            sender: 'user',
            timestamp: new Date()
        };

        setMessages(prev => [...prev, userMessage]);
<<<<<<< HEAD
        setIsLoading(true);

        setTimeout(() => {
            const botResponse = generateAIResponse(text);
=======
        setInputMessage('');
        setIsLoading(true);

        // Simulate AI processing time
        setTimeout(() => {
            const botResponse = generateAIResponse(inputMessage);
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
            const botMessage = {
                id: Date.now() + 1,
                text: botResponse,
                sender: 'bot',
                timestamp: new Date()
            };

            setMessages(prev => [...prev, botMessage]);
            setIsLoading(false);
        }, 1000);
    };

<<<<<<< HEAD
    const handleSendMessage = () => {
        if (!inputMessage.trim() || isLoading) return;
        const text = inputMessage;
        setInputMessage('');
        sendAndRespond(text);
    };

    const handleQuickAction = (action) => {
        if (isLoading) return;
        sendAndRespond(action);
=======
    const handleQuickAction = (action) => {
        const userMessage = {
            id: Date.now(),
            text: action,
            sender: 'user',
            timestamp: new Date()
        };

        setMessages(prev => [...prev, userMessage]);
        setIsLoading(true);

        setTimeout(() => {
            const botResponse = generateAIResponse(action);
            const botMessage = {
                id: Date.now() + 1,
                text: botResponse,
                sender: 'bot',
                timestamp: new Date()
            };
            setMessages(prev => [...prev, botMessage]);
            setIsLoading(false);
        }, 1000);
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSendMessage();
        }
    };

    const quickActions = [
        "What's my account balance?",
        "How is my portfolio performing?",
        "Show me stock prices",
        "Give me investment advice",
        "Market forecast today",
        "Best stocks to buy now"
    ];

    return (
        <div className="chatbot-container">
            <div className="chatbot-header">
                <div className="chatbot-title">
<<<<<<< HEAD
                    <div className="chatbot-avatar">AI</div>
=======
                    <div className="chatbot-avatar">🤖</div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                    <div>
                        <h3>Smart Invest AI</h3>
                        <span className="chatbot-status">Online • AI Powered</span>
                    </div>
                </div>
                <button className="chatbot-close" onClick={onClose}>×</button>
            </div>

            <div className="chatbot-messages">
                {messages.map((message) => (
                    <div key={message.id} className={`message ${message.sender}`}>
                        <div className="message-content">
                            <p>{message.text}</p>
                            <span className="message-time">
                                {message.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </span>
                        </div>
                    </div>
                ))}
                {isLoading && (
                    <div className="message bot">
                        <div className="message-content">
                            <div className="typing-indicator">
                                <span></span>
                                <span></span>
                                <span></span>
                            </div>
                        </div>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            <div className="quick-actions">
                {quickActions.map((action, index) => (
                    <button
                        key={index}
                        className="quick-action-btn"
                        onClick={() => handleQuickAction(action)}
                    >
                        {action}
                    </button>
                ))}
            </div>

            <div className="chatbot-input">
                <input
                    type="text"
                    placeholder="Ask me anything about stocks, portfolio, or investments..."
                    value={inputMessage}
                    onChange={(e) => setInputMessage(e.target.value)}
                    onKeyPress={handleKeyPress}
                    disabled={isLoading}
                />
                <button
                    onClick={handleSendMessage}
                    disabled={isLoading || !inputMessage.trim()}
                    className="send-btn"
                >
<<<<<<< HEAD
                    {isLoading ? '...' : 'Send'}
=======
                    {isLoading ? '⏳' : '📤'}
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
                </button>
            </div>
        </div>
    );
};

<<<<<<< HEAD
export default Chatbot;
=======
export default Chatbot;
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
