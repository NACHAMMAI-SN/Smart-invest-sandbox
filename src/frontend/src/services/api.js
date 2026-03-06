import axios from 'axios';

const AUTH_BASE = 'http://localhost:8080/api/auth';
const STOCK_BASE = 'http://localhost:8080/api/stocks';
const PORTFOLIO_BASE = 'http://localhost:8080/api';
const TUTORIAL_BASE = 'http://localhost:8080/api';

// Configure axios defaults
axios.defaults.headers.common['Content-Type'] = 'application/json';

export const authAPI = {
    // Register a new user
    register: async (username, password, email) => {
        try {
            const response = await axios.post(`${AUTH_BASE}/register`, {
                username,
                password,
                email
            });
            return response.data;
        } catch (error) {
            console.error('Registration error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Registration failed'
            };
        }
    },

    // Login an existing user
    login: async (username, password) => {
        try {
            const response = await axios.post(`${AUTH_BASE}/login`, {
                username,
                password
            });
            return response.data;
        } catch (error) {
            console.error('Login error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Login failed'
            };
        }
    },

    // Fetch user info by username
    getUser: async (username) => {
        try {
            const response = await axios.get(`${AUTH_BASE}/user?username=${encodeURIComponent(username)}`);
            return response.data;
        } catch (error) {
            console.error('Get user error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to fetch user data'
            };
        }
    },

    // Update user balance
    updateBalance: async (username, balance) => {
        try {
            const response = await axios.post(`${AUTH_BASE}/user/balance`, { username, balance });
            return response.data;
        } catch (error) {
            console.error('Update balance error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to update balance'
            };
        }
    }
};
export const getForecast = async (symbol, days = 7) => {
    try {
        const response = await fetch(
            `http://localhost:8080/api/forecast?symbol=${symbol}&days=${days}`
        );
        return response.json();
    } catch (error) {
        console.error('Forecast API error:', error);
        throw error;
    }
};
export const stockAPI = {
    // Fetch stock data by symbol
    getStock: async (symbol) => {
        try {
            const response = await axios.get(`${STOCK_BASE}/${encodeURIComponent(symbol)}`);
            return response.data;
        } catch (error) {
            console.error('Stock fetch error:', error);
            throw new Error(error.response?.data?.error || 'Failed to fetch stock data');
        }
    },

    // Search stocks by keyword
    searchStocks: async (keyword) => {
        try {
            const response = await axios.get(`${STOCK_BASE}/search?keywords=${encodeURIComponent(keyword)}`);
            return response.data;
        } catch (error) {
            console.error('Stock search error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to search stocks'
            };
        }
    }
};

export const portfolioAPI = {
    // Get user portfolio
    getPortfolio: async (username) => {
        try {
            const response = await axios.get(`${PORTFOLIO_BASE}/portfolio?username=${encodeURIComponent(username)}`);
            return response.data;
        } catch (error) {
            console.error('Portfolio fetch error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to fetch portfolio',
                portfolio: [],
                totalValue: 0,
                totalGainLoss: 0
            };
        }
    },

    // Get user transactions
    getTransactions: async (username) => {
        try {
            const response = await axios.get(`${PORTFOLIO_BASE}/transactions?username=${encodeURIComponent(username)}`);
            return response.data;
        } catch (error) {
            console.error('Transactions fetch error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to fetch transactions',
                transactions: []
            };
        }
    },

    // Buy stock (full trade payload)
    buyStock: async (tradeData) => {
        try {
            const response = await axios.post(`${PORTFOLIO_BASE}/trade/buy`, tradeData);
            return response.data;
        } catch (error) {
            console.error('Buy stock error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to execute buy order'
            };
        }
    },

    // Sell stock (full trade payload)
    sellStock: async (tradeData) => {
        try {
            const response = await axios.post(`${PORTFOLIO_BASE}/trade/sell`, tradeData);
            return response.data;
        } catch (error) {
            console.error('Sell stock error:', error);
            return {
                success: false,
                message: error.response?.data?.error || 'Failed to execute sell order'
            };
        }
    },

    // Get portfolio statistics (derived on frontend from portfolio + transactions)
    getPortfolioStats: async (username) => {
        try {
            const portfolioResponse = await portfolioAPI.getPortfolio(username);
            const transactionsResponse = await portfolioAPI.getTransactions(username);

            if (portfolioResponse.success && transactionsResponse.success) {
                const totalInvestment = transactionsResponse.transactions
                    .filter(t => t.type === 'BUY')
                    .reduce((total, transaction) => total + transaction.totalAmount, 0);

                const totalCashFlow = transactionsResponse.transactions
                    .reduce((total, transaction) => {
                        return total + (transaction.type === 'SELL' ? transaction.totalAmount : -transaction.totalAmount);
                    }, 0);

                return {
                    success: true,
                    totalValue: portfolioResponse.totalValue,
                    totalGainLoss: portfolioResponse.totalGainLoss,
                    totalInvestment,
                    totalCashFlow,
                    portfolioCount: portfolioResponse.portfolio.length,
                    transactionCount: transactionsResponse.transactions.length
                };
            } else {
                return {
                    success: false,
                    message: 'Failed to calculate portfolio statistics'
                };
            }
        } catch (error) {
            console.error('Portfolio stats error:', error);
            return {
                success: false,
                message: 'Failed to calculate portfolio statistics'
            };
        }
    },

    // Get aggregated portfolio analytics from backend
    getPortfolioAnalytics: async (username) => {
        try {
            const response = await axios.get(`${PORTFOLIO_BASE}/portfolio/analytics`, {
                params: { username }
            });
            return response.data;
        } catch (error) {
            console.error('Portfolio analytics fetch error:', error);
            return {
                totalInvestment: 0,
                portfolioValue: 0,
                profitLoss: 0,
                holdings: 0,
                topStock: ''
            };
        }
    },

    // Simple helpers for common trade & analytics operations
    buyStockSimple: async (username, symbol, quantity, stockName = symbol, orderType = 'MARKET', duration = 'DAY') => {
        return portfolioAPI.buyStock({ username, symbol, stockName, quantity, orderType, duration });
    },

    sellStockSimple: async (username, symbol, quantity, stockName = symbol, orderType = 'MARKET', duration = 'DAY') => {
        return portfolioAPI.sellStock({ username, symbol, stockName, quantity, orderType, duration });
    },

    fetchPortfolio: async (username) => {
        return portfolioAPI.getPortfolio(username);
    },

    fetchPortfolioAnalytics: async (username) => {
        return portfolioAPI.getPortfolioAnalytics(username);
    }
};

export const tutorialAPI = {
    // Get all tutorials
    getAllTutorials: async () => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials`);
            return response.data.data;
        } catch (error) {
            console.error('Error fetching tutorials:', error);
            return getLocalTutorials();
        }
    },

    // Get tutorials by level
    getTutorialsByLevel: async (level) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/level/${level}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching ${level} tutorials:`, error);
            return [];
        }
    },

    // Get tutorials by category
    getTutorialsByCategory: async (category) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/category/${category}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching ${category} tutorials:`, error);
            return [];
        }
    },

    // Search tutorials
    searchTutorials: async (query) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/search?q=${encodeURIComponent(query)}`);
            return response.data.data;
        } catch (error) {
            console.error('Error searching tutorials:', error);
            return [];
        }
    },

    // Get specific tutorial
    getTutorial: async (id) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/${id}`);
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching tutorial ${id}:`, error);
            const localTutorials = getLocalTutorials();
            return localTutorials.find(tutorial => tutorial.id === id) || localTutorials[0];
        }
    },

    // Submit quiz
    submitQuiz: async (tutorialId, answers, username) => {
        try {
            console.log(' Submitting quiz...');
            console.log('Tutorial ID:', tutorialId);
            console.log('Username:', username);
            console.log('Answers:', answers);

            const requestData = {
                tutorialId: tutorialId,
                username: username,
                answers: JSON.stringify(answers)
            };

            console.log('Request data:', requestData);

            const response = await axios.post(`${TUTORIAL_BASE}/tutorial/quiz`,
                requestData,
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    }
                }
            );

            console.log('Quiz.java submitted successfully:', response.data);
            return response.data.data;
        } catch (error) {
            console.error('Error submitting quiz:', error);

            return {
                success: false,
                error: error.response?.data?.error || 'Failed to submit quiz',
                score: 0,
                passed: false,
                correctAnswers: 0,
                totalQuestions: 0
            };
        }
    },

    // Get tutorial quiz
    getQuiz: async (tutorialId) => {
        try {
            console.log(` Fetching quiz for tutorial: ${tutorialId}`);
            const response = await axios.get(`${TUTORIAL_BASE}/tutorial/quiz`, {
                params: { tutorialId }
            });
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching quiz for ${tutorialId}:`, error);
            return getFallbackQuiz(tutorialId);
        }
    },

    // Get tutorial exercise
    getExercise: async (tutorialId) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorial/exercise`, {
                params: { tutorialId }
            });
            return response.data.data;
        } catch (error) {
            console.error(`Error fetching exercise for ${tutorialId}:`, error);
            return null;
        }
    },

    // Validate exercise
    validateExercise: async (tutorialId, answer, username) => {
        if (!tutorialId || !answer || !username) {
            console.error("Missing fields for exercise validation", { tutorialId, answer, username });
            return false;
        }

        try {
            console.log("Sending exercise validation:", { tutorialId, answer, username });

            const response = await axios.post(
                `${TUTORIAL_BASE}/tutorial/${tutorialId}/validate`,
                { answer, username },
                { headers: { "Content-Type": "application/json" } }
            );

            console.log("Backend response:", response.data);
            return response.data.correct;
        } catch (error) {
            console.error('Error validating exercise:', error.response?.data || error.message);
            return false;
        }
    },

    // Get user progress
    getUserProgress: async (username) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/progress`, {
                params: { username }
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching user progress:', error);
            return {
                data: {},
                statistics: {
                    completedTutorials: 0,
                    currentLevel: 'Beginner',
                    badges: [],
                    certifications: [],
                    totalTimeSpent: 0,
                    averageScore: 0
                }
            };
        }
    },

    // Get tutorial progress
    getTutorialProgress: async (username, tutorialId) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/progress/tutorial`, {
                params: { username, tutorialId }
            });
            return response.data.data;
        } catch (error) {
            console.error('Error fetching tutorial progress:', error);
            // Return fallback structure
            return {
                completed: false,
                score: 0,
                quizScore: 0,
                quizPassed: false,
                timeSpent: 0
            };
        }
    },

    // Mark tutorial complete
    markTutorialComplete: async (username, tutorialId) => {
        try {
            const requestBody = {
                username: username,
                tutorialId: tutorialId
            };

            console.log('Request body:', requestBody);

            const response = await axios.post(`${TUTORIAL_BASE}/tutorials/progress/complete`,
                requestBody,
                {
                    headers: {
                        'Content-Type': 'application/json',
                        'Accept': 'application/json'
                    },
                    timeout: 10000
                }
            );

            console.log('Mark complete response:', response.data);
            return response.data;
        } catch (error) {
            console.error('Error marking tutorial complete:', error);
            console.error('Error response:', error.response?.data);
            console.error('Error status:', error.response?.status);
            console.error('Error headers:', error.response?.headers);

            return {
                success: false,
                error: error.response?.data?.error || error.message || 'Failed to mark tutorial complete'
            };
        }
    },

    // Update time spent
    updateTimeSpent: async (username, tutorialId, minutes) => {
        try {
            console.log(`Time spent updated: User ${username}, Tutorial ${tutorialId}, ${minutes} minutes`);
            return { success: true };
        } catch (error) {
            console.error('Error updating time spent:', error);
            return { success: false };
        }
    },

    // Get recommended tutorials
    getRecommendedTutorials: async (username) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/recommended/${username}`);
            return response.data.data;
        } catch (error) {
            console.error('Error fetching recommended tutorials:', error);
            return [];
        }
    },

    // Get user statistics
    getUserStatistics: async (username) => {
        try {
            const response = await axios.get(`${TUTORIAL_BASE}/tutorials/stats/${username}`);
            return response.data.data;
        } catch (error) {
            console.error('Error fetching user statistics:', error);
            return {
                completedTutorials: 0,
                totalTimeSpent: 0,
                averageScore: 0,
                badges: [],
                certifications: [],
                currentLevel: 'Beginner'
            };
        }
    }
};

// Utility functions
export const apiUtils = {
    // Format currency
    formatCurrency: (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(amount);
    },

    // Format percentage
    formatPercentage: (value) => {
        return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
    },

    // Format date
    formatDate: (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    },

    // Calculate estimated total
    calculateEstimatedTotal: (price, quantity) => {
        return parseFloat(price) * parseInt(quantity);
    },

    // Validate trade data
    validateTrade: (tradeData, userBalance, action) => {
        const errors = [];

        if (!tradeData.symbol || tradeData.symbol.trim() === '') {
            errors.push('Stock symbol is required');
        }

        if (!tradeData.quantity || tradeData.quantity <= 0) {
            errors.push('Quantity must be greater than 0');
        }

        if (!tradeData.orderType) {
            errors.push('Order type is required');
        }

        if (!tradeData.duration) {
            errors.push('Duration is required');
        }

        if (action === 'BUY') {
            const estimatedTotal = tradeData.price * tradeData.quantity;
            if (estimatedTotal > userBalance) {
                errors.push('Insufficient balance for this trade');
            }
        }

        return errors;
    }
};

// Local fallback data
const getLocalTutorials = () => {
    return [
        {
            id: 'stock-fundamentals',
            title: 'Stock Market Fundamentals',
            description: 'Learn the basic concepts of stock market investing and how markets work',
            level: 'BEGINNER',
            category: 'FOUNDATION',
            estimatedMinutes: 45,
            content: '<h3>Welcome to Stock Market Investing</h3><p>Learn how to build wealth through intelligent investing...</p>',
            keyPoints: [
                'Stocks represent ownership in companies',
                'Markets can be bullish (rising) or bearish (falling)',
                'Diversification reduces risk'
            ],
            exercise: {
                question: 'Calculate the market capitalization: A company has 1 million shares outstanding trading at $50 per share.',
                answer: '50000000',
                hint: 'Market Cap = Shares Outstanding × Price Per Share',
                type: 'CALCULATION'
            },
            hasVideo: true,
            hasQuiz: true,
            completionRate: 85.5
        },
        {
            id: 'chart-reading',
            title: 'Reading Stock Charts & Technical Analysis',
            description: 'Learn how to interpret stock charts and identify trading patterns',
            level: 'BEGINNER',
            category: 'TECHNICAL_ANALYSIS',
            estimatedMinutes: 60,
            hasVideo: true,
            hasQuiz: true
        }
    ];
};

// Fallback quiz data
const getFallbackQuiz = (tutorialId) => {
    const quizzes = {
        'stock-fundamentals': {
            id: 'quiz-fundamentals',
            title: 'Stock Market Fundamentals Quiz.java',
            questions: [
                {
                    id: 1,
                    question: 'What does a stock represent?',
                    options: [
                        'A loan to a company',
                        'Ownership in a company',
                        'A government bond',
                        'A savings account'
                    ],
                    correctAnswer: 1
                },
                {
                    id: 2,
                    question: 'What is market capitalization?',
                    options: [
                        'The price of one share',
                        'Total value of all company shares',
                        'The company\'s annual revenue',
                        'The stock exchange location'
                    ],
                    correctAnswer: 1
                }
            ],
            passingScore: 70
        }
    };
    return quizzes[tutorialId] || quizzes['stock-fundamentals'];
};
//Extra
const API_BASE = 'http://localhost:8080/api';

export const apiService = {
    // Stock data
    async getStockData(symbol) {
        const response = await fetch(`${API_BASE}/stocks/${symbol}`);
        return await response.json();
    },

    // Portfolio data
    async getPortfolio(username) {
        const response = await fetch(`${API_BASE}/portfolio/${username}`);
        return await response.json();
    },

    // Transactions
    async getTransactions(username) {
        const response = await fetch(`${API_BASE}/transactions/${username}`);
        return await response.json();
    },

    // News
    async getMarketNews() {
        const response = await fetch(`${API_BASE}/news/market`);
        return await response.json();
    },

    // User data
    async getUser(username) {
        const response = await fetch(`${API_BASE}/users/${username}`);
        return await response.json();
    },

    // Execute trade
    async executeTrade(tradeData) {
        const response = await fetch(`${API_BASE}/trade`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(tradeData)
        });
        return await response.json();
    }
};
// Default export with all APIs
export default {
    auth: authAPI,
    stock: stockAPI,
    portfolio: portfolioAPI,
    tutorial: tutorialAPI,
    utils: apiUtils
};