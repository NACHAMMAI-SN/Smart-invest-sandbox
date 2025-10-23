import React, { useState } from 'react';
import { authAPI } from '../services/api';
import '../styles/Login.css';

const Register = ({ onSwitchToLogin, onRegister }) => {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: ''
    });
    const [message, setMessage] = useState('');
    const [loading, setLoading] = useState(false);
    const [passwordStrength, setPasswordStrength] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setMessage('');
        setLoading(true);

        // Validation
        if (!formData.username.trim() || !formData.email.trim() || !formData.password.trim()) {
            setMessage('⚠️ Please fill in all fields');
            setLoading(false);
            return;
        }

        if (formData.password !== formData.confirmPassword) {
            setMessage('⚠️ Passwords do not match');
            setLoading(false);
            return;
        }

        if (formData.password.length < 6) {
            setMessage('⚠️ Password must be at least 6 characters long');
            setLoading(false);
            return;
        }

        // Email validation
        const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
        if (!emailRegex.test(formData.email)) {
            setMessage('⚠️ Please enter a valid email address');
            setLoading(false);
            return;
        }

        // Username validation
        if (formData.username.length < 3) {
            setMessage('⚠️ Username must be at least 3 characters long');
            setLoading(false);
            return;
        }

        try {
            const result = await authAPI.register(formData.username, formData.password, formData.email);

            if (result.success) {
                setMessage('✅ Account created successfully! Redirecting...');
                setTimeout(() => {
                    onRegister(result.data);
                }, 1500);
            } else {
                setMessage('⚠️ ' + result.message);
            }
        } catch (error) {
            console.error('Registration error:', error);
            setMessage('⚠️ Registration failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });

        // Clear message when user starts typing
        if (message) {
            setMessage('');
        }

        // Check password strength
        if (name === 'password') {
            checkPasswordStrength(value);
        }
    };

    const checkPasswordStrength = (password) => {
        let strength = '';
        let score = 0;

        if (password.length >= 6) score++;
        if (password.length >= 8) score++;
        if (/[A-Z]/.test(password)) score++;
        if (/[0-9]/.test(password)) score++;
        if (/[^A-Za-z0-9]/.test(password)) score++;

        switch (score) {
            case 0:
            case 1:
                strength = 'Weak';
                break;
            case 2:
            case 3:
                strength = 'Medium';
                break;
            case 4:
            case 5:
                strength = 'Strong';
                break;
            default:
                strength = '';
        }

        setPasswordStrength(strength);
    };

    const getPasswordStrengthColor = () => {
        switch (passwordStrength) {
            case 'Weak': return '#ef4444';
            case 'Medium': return '#f59e0b';
            case 'Strong': return '#10b981';
            default: return '#6b7280';
        }
    };

    const handleDemoRegister = () => {
        const demoUsername = `demo${Math.floor(Math.random() * 1000)}`;
        const demoEmail = `${demoUsername}@demo.com`;

        setFormData({
            username: demoUsername,
            email: demoEmail,
            password: 'demo123',
            confirmPassword: 'demo123'
        });
        setPasswordStrength('Medium');
    };

    return (
        <div className="auth-container">
            <div className="auth-card">
                <div className="auth-header">
                    <h1>Create Account</h1>
                    <p>Start your investment journey with Smart Invest</p>
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
                            placeholder="Choose a username (min. 3 characters)"
                            required
                            disabled={loading}
                            minLength="3"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email:</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleChange}
                            placeholder="Enter your email address"
                            required
                            disabled={loading}
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
                            placeholder="Create a password (min. 6 characters)"
                            required
                            disabled={loading}
                            minLength="6"
                        />
                        {formData.password && (
                            <div className="password-strength">
                                <span>Strength: </span>
                                <span
                                    className="strength-indicator"
                                    style={{ color: getPasswordStrengthColor() }}
                                >
                                    {passwordStrength}
                                </span>
                            </div>
                        )}
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirm Password:</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleChange}
                            placeholder="Confirm your password"
                            required
                            disabled={loading}
                        />
                        {formData.confirmPassword && formData.password !== formData.confirmPassword && (
                            <div className="password-match error">
                                ⚠️ Passwords do not match
                            </div>
                        )}
                        {formData.confirmPassword && formData.password === formData.confirmPassword && (
                            <div className="password-match success">
                                ✅ Passwords match
                            </div>
                        )}
                    </div>

                    <button
                        type="submit"
                        className="btn-primary"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <div className="loading-spinner-small"></div>
                                Creating Account...
                            </>
                        ) : (
                            'Create Account'
                        )}
                    </button>

                    <div className="demo-section">
                        <p className="demo-text">Want to try it out first?</p>
                        <button
                            type="button"
                            className="btn-demo"
                            onClick={handleDemoRegister}
                            disabled={loading}
                        >
                            Use Demo Credentials
                        </button>
                    </div>

                    <div className="auth-divider">
                        <span>Already have an account?</span>
                    </div>

                    <button
                        type="button"
                        className="btn-secondary"
                        onClick={onSwitchToLogin}
                        disabled={loading}
                    >
                        Sign In to Existing Account
                    </button>

                    {message && (
                        <div className={`message ${message.includes('✅') ? 'success' : 'error'}`}>
                            {message}
                        </div>
                    )}

                    <div className="auth-footer">
                        <p>By creating an account, you agree to our <a href="#" className="footer-link">Terms of Service</a> and <a href="#" className="footer-link">Privacy Policy</a></p>
                    </div>
                </form>
            </div>

            {/* Market Ticker Animation in Background */}
            <div className="market-ticker">
                <div className="ticker-content">
                    <span>🎯 Start with $100,000 virtual balance</span>
                    <span>📈 Practice with real market data</span>
                    <span>💼 Build your investment portfolio</span>
                    <span>📊 Learn risk-free trading</span>
                    <span>🚀 No real money required</span>
                </div>
            </div>
        </div>
    );
};

export default Register;