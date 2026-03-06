import React, { useState } from 'react';
import { authAPI } from '../services/api';
<<<<<<< HEAD
import '../styles/Login.css'; // Import the enhanced CSS
=======
import '../styles/Login.css';
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

const Login = ({ onSwitchToRegister, onLogin }) => {
    const [formData, setFormData] = useState({
        username: '',
        password: ''
    });
    const [message, setMessage] = useState('');
<<<<<<< HEAD
=======
    const [loading, setLoading] = useState(false);
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
<<<<<<< HEAD

        try {
            const result = await authAPI.login(formData.username, formData.password);

            if (result.success) {
                setMessage('✅ Login successful!');
                onLogin(result.data);
            } else {
                setMessage('❌ ' + result.message);
            }
        } catch (error) {
            setMessage('❌ Login failed. Please try again.');
=======
        setLoading(true);

        // Basic validation
        if (!formData.username.trim() || !formData.password.trim()) {
            setMessage('❌ Please fill in all fields');
            setLoading(false);
            return;
        }

        try {
            // For demo account, use mock login instead of API call
            if (formData.username === 'demo' && formData.password === 'demo123') {
                // Mock successful demo login
                setTimeout(() => {
                    const demoUser = {
                        id: 1,
                        username: 'demo',
                        name: 'Demo Trader',
                        email: 'demo@smartinvest.com',
                        experience: 'Beginner',
                        joinDate: '2024-01-01',
                        portfolioValue: 10000,
                        cashBalance: 5000,
                        totalProfit: 1250.50
                    };
                    setMessage('✅ Demo login successful!');
                    onLogin(demoUser);
                }, 1000);
            } else {
                // Regular login with API
                const result = await authAPI.login(formData.username, formData.password);

                if (result.success) {
                    setMessage('✅ Login successful!');
                    setTimeout(() => {
                        onLogin(result.data);
                    }, 1000);
                } else {
                    setMessage('❌ ' + result.message);
                }
            }
        } catch (error) {
            console.error('Login error:', error);
            setMessage('❌ Login failed. Please try again.');
        } finally {
            setLoading(false);
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
        }
    };

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
<<<<<<< HEAD
=======
        // Clear message when user starts typing
        if (message) {
            setMessage('');
        }
    };

    const handleDemoLogin = () => {
        setFormData({
            username: 'demo',
            password: 'demo123'
        });
        setMessage('ℹ️ Demo credentials loaded. Click SIGN IN to continue.');
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
    };

    return (
        <div className="auth-container">
<<<<<<< HEAD
            <div className="auth-card">
                <div className="auth-header">
                    <h1>Welcome Back</h1>
                    <p>Sign in to your account</p>
                </div>

                <form onSubmit={handleSubmit} className="auth-form">
                    <div className="form-group">
                        <label htmlFor="username">Username:</label>
                        <input
                            type="text"
                            id="username"
                            name="username"
                            value={formData.username}
                            onChange={handleChange}
                            placeholder="Enter your username"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password:</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleChange}
                            placeholder="Enter your password"
                            required
                        />
                    </div>

                    <button type="submit" className="btn-primary">
                        Sign In
                    </button>

                    <button type="button" className="btn-secondary" onClick={onSwitchToRegister}>
                        Create Account
                    </button>

                    {message && (
                        <div className={`message ${message.includes('✅') ? 'success' : 'error'}`}>
                            {message}
                        </div>
                    )}
                </form>
=======
            {/* Background Elements */}
            <div className="floating-chart"></div>
            <div className="floating-chart"></div>
            <div className="floating-chart"></div>

            <div className="auth-card">
                {/* Left Side - Login Form */}
                <div className="auth-form-side">
                    <div className="auth-header">
                        <h1>Welcome Back</h1>
                        <p>Sign in to your Smart Invest account</p>
                    </div>

                    <form onSubmit={handleSubmit} className="auth-form">
                        <div className="form-group">
                            <label htmlFor="username">USERNAME</label>
                            <input
                                type="text"
                                id="username"
                                name="username"
                                value={formData.username}
                                onChange={handleChange}
                                placeholder="Enter your username"
                                required
                                disabled={loading}
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="password">PASSWORD</label>
                            <input
                                type="password"
                                id="password"
                                name="password"
                                value={formData.password}
                                onChange={handleChange}
                                placeholder="Enter your password"
                                required
                                disabled={loading}
                            />
                        </div>

                        <button
                            type="submit"
                            className="btn-primary"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <div className="loading-spinner-small"></div>
                                    SIGNING IN...
                                </>
                            ) : (
                                'SIGN IN'
                            )}
                        </button>

                        {message && (
                            <div className={`message ${message.includes('✅') ? 'success' : message.includes('ℹ️') ? 'info' : 'error'}`}>
                                {message}
                            </div>
                        )}

                        <div className="demo-section">
                            <button
                                type="button"
                                className="btn-demo"
                                onClick={handleDemoLogin}
                                disabled={loading}
                            >
                                🚀 Use Demo Account
                            </button>
                        </div>

                        <div className="auth-divider">
                            <span>or</span>
                        </div>

                        <button
                            type="button"
                            className="btn-secondary"
                            onClick={onSwitchToRegister}
                            disabled={loading}
                        >
                            CREATE NEW ACCOUNT
                        </button>

                        <div className="auth-footer">
                            <p>By signing in, you agree to our Terms of Service and Privacy Policy</p>
                        </div>
                    </form>
                </div>

                {/* Right Side - Features & Information */}
                <div className="auth-features-side">
                    <div className="features-container">
                        <div className="welcome-message">
                            <h2>Start Your Trading Journey</h2>
                            <p>Join thousands of successful traders using our platform</p>
                        </div>

                        <div className="features-grid">
                            <div className="feature-card">
                                <div className="feature-icon">📈</div>
                                <h3>Real-time Data</h3>
                                <p>Live market prices and real-time portfolio tracking</p>
                            </div>

                            <div className="feature-card">
                                <div className="feature-icon">🛡️</div>
                                <h3>Secure Trading</h3>
                                <p>Bank-level security for all your investments</p>
                            </div>

                            <div className="feature-card">
                                <div className="feature-icon">💡</div>
                                <h3>Learn & Grow</h3>
                                <p>Educational resources and simulated trading</p>
                            </div>

                            <div className="feature-card">
                                <div className="feature-icon">⚡</div>
                                <h3>Fast Execution</h3>
                                <p>Instant trade execution with no delays</p>
                            </div>
                        </div>

                        <div className="platform-stats">
                            <div className="stat-item">
                                <span className="stat-number">10K+</span>
                                <span className="stat-label">Active Traders</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-number">$50M+</span>
                                <span className="stat-label">Volume Traded</span>
                            </div>
                            <div className="stat-item">
                                <span className="stat-number">99.9%</span>
                                <span className="stat-label">Platform Uptime</span>
                            </div>
                        </div>

                        <div className="market-highlights">
                            <h4>Today's Market</h4>
                            <div className="ticker-item">
                                <span className="ticker-symbol">AAPL</span>
                                <span className="ticker-price">$182.34</span>
                                <span className="ticker-change negative">-0.8%</span>
                            </div>
                            <div className="ticker-item">
                                <span className="ticker-symbol">MSFT</span>
                                <span className="ticker-price">$415.21</span>
                                <span className="ticker-change positive">+2.1%</span>
                            </div>
                            <div className="ticker-item">
                                <span className="ticker-symbol">TSLA</span>
                                <span className="ticker-price">$245.67</span>
                                <span className="ticker-change negative">-1.5%</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Market Ticker Animation in Background */}
            <div className="market-ticker">
                <div className="ticker-content">
                    <span>📈 JPM: $197.56 (+1.2%)</span>
                    <span>📉 AAPL: $182.34 (-0.8%)</span>
                    <span>📈 MSFT: $415.21 (+2.1%)</span>
                    <span>📈 GOOGL: $175.89 (+0.9%)</span>
                    <span>📉 TSLA: $245.67 (-1.5%)</span>
                </div>
>>>>>>> 98ed4d710cfe2d70ee93b475890af0489edd38ce
            </div>
        </div>
    );
};

export default Login;