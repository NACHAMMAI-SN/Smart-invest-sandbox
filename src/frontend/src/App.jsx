import React, { useState, useEffect } from 'react';
import Login from './components/Login';
import Register from './components/Register';
import Home from './components/Home';
import Portfolio from './components/Portfolio';
import Trade from './components/Trade';
import Forecast from './components/Forecast';
import News from './components/News';
import Transactions from './components/Transactions';
import TutorialPage from './components/TutorialPage'; // NEW: Tutorial system
import Chatbot from './components/Chatbot';
<<<<<<< HEAD
import './App.css';
=======
import './styles/App.css';
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

function App() {
    const [currentView, setCurrentView] = useState('login');
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [currentUser, setCurrentUser] = useState(null);
    const [loading, setLoading] = useState(false);
    const [showChatbot, setShowChatbot] = useState(false);
    const [transactions, setTransactions] = useState([]);
    const [portfolio, setPortfolio] = useState([]);

    // Check if user is already logged in (from localStorage)
    useEffect(() => {
        const savedUser = localStorage.getItem('currentUser');
        if (savedUser) {
            try {
                const userData = JSON.parse(savedUser);
                setCurrentUser(userData);
                setIsAuthenticated(true);
                setCurrentView('dashboard');

                // Load user transactions and portfolio
                loadUserData();
            } catch (error) {
                console.error('Error parsing saved user data:', error);
                localStorage.removeItem('currentUser');
            }
        }
    }, []);

    // Load transactions and portfolio data for chatbot
    const loadUserData = () => {
        try {
            // Load transactions from localStorage
            const savedTransactions = localStorage.getItem('userTransactions');
            if (savedTransactions) {
                const transactionsData = JSON.parse(savedTransactions);
                setTransactions(transactionsData);
            }

            // Load portfolio from localStorage
            const savedPortfolio = localStorage.getItem('userPortfolio');
            if (savedPortfolio) {
                const portfolioData = JSON.parse(savedPortfolio);
                setPortfolio(portfolioData);
            }
        } catch (error) {
            console.error('Error loading user data:', error);
        }
    };

    const handleLogin = (userData) => {
        setLoading(true);
        try {
            setIsAuthenticated(true);
            const user = userData.data || userData;
            setCurrentUser(user);
            setCurrentView('dashboard');

            // Save user data to localStorage
            localStorage.setItem('currentUser', JSON.stringify(user));

            // Load user transactions and portfolio for chatbot
            loadUserData();
        } catch (error) {
            console.error('Login error:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = (userData) => {
        setLoading(true);
        try {
            // After successful registration, show success message and switch to login
            if (userData.success) {
                setCurrentView('login');
                // You could show a success message here
            }
        } catch (error) {
            console.error('Registration error:', error);
        } finally {
            setLoading(false);
        }
    };

    const handleLogout = () => {
        setIsAuthenticated(false);
        setCurrentUser(null);
        setCurrentView('login');
        setShowChatbot(false);

        // Clear user data from localStorage
        localStorage.removeItem('currentUser');
    };

    const navigateTo = (view) => {
        setCurrentView(view);
    };

    // Update transactions when they change (for chatbot)
    const updateTransactions = (newTransactions) => {
        setTransactions(newTransactions);
    };

    // Update portfolio when it changes (for chatbot)
    const updatePortfolio = (newPortfolio) => {
        setPortfolio(newPortfolio);
    };

    if (loading) {
        return (
            <div className="loading-screen">
                <div className="loading-spinner"></div>
                <p>Loading...</p>
            </div>
        );
    }

    if (isAuthenticated) {
        // Enhanced common props with data for chatbot and tutorial navigation
        const commonProps = {
            user: currentUser,
            onLogout: handleLogout,
            onNavigate: navigateTo,
            transactions: transactions,
            portfolio: portfolio,
            onUpdateTransactions: updateTransactions,
            onUpdatePortfolio: updatePortfolio,
            // NEW: Add tutorial navigation
            onNavigateToTutorials: () => navigateTo('tutorials')
        };

        // Render current view component
        let CurrentComponent;
        switch (currentView) {
            case 'dashboard':
                CurrentComponent = Home;
                break;
            case 'portfolio':
                CurrentComponent = Portfolio;
                break;
            case 'trade':
                CurrentComponent = Trade;
                break;
            case 'forecast':
                CurrentComponent = Forecast;
                break;
            case 'news':
                CurrentComponent = News;
                break;
            case 'transactions':
                CurrentComponent = Transactions;
                break;
            case 'tutorials': // NEW: Tutorial page
                CurrentComponent = TutorialPage;
                break;
            default:
                CurrentComponent = Home;
        }

        // Special props for TutorialPage
        const componentProps = currentView === 'tutorials'
            ? {
                onBackToHome: () => navigateTo('dashboard'),
                user: currentUser
            }
            : commonProps;

        return (
            <>
                <CurrentComponent {...componentProps} />

                {/* Chatbot Component */}
                {showChatbot && (
                    <Chatbot
                        onClose={() => setShowChatbot(false)}
                        user={currentUser}
                        transactions={transactions}
                        portfolio={portfolio}
                        onNavigate={navigateTo}
                    />
                )}

                {/* Chatbot Toggle Button - Only show when authenticated */}
                {!showChatbot && isAuthenticated && (
                    <button
                        className="chatbot-toggle"
                        onClick={() => setShowChatbot(true)}
                        title="Smart Invest AI Assistant"
                    >
                        🤖
                    </button>
                )}
            </>
        );
    }

    // Authentication views (login/register)
    return (
        <div className="App">
            {currentView === 'login' && (
                <Login
                    onLogin={handleLogin}
                    onSwitchToRegister={() => setCurrentView('register')}
                    loading={loading}
                />
            )}
            {currentView === 'register' && (
                <Register
                    onRegister={handleRegister}
                    onSwitchToLogin={() => setCurrentView('login')}
                    loading={loading}
                />
            )}
        </div>
    );
}

export default App;