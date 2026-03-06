package backend.models.data;

import backend.models.Question;
import backend.models.Quiz;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuizData {

    public static Quiz getStockFundamentalsQuiz() {
        Quiz quiz = new Quiz("quiz-fundamentals", 70, 20); // ID, passing score, time limit

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What does a stock represent?");
        q1.setOptions(Arrays.asList(
                "A loan to a company",
                "Ownership in a company",
                "A type of bond",
                "Government security"
        ));
        q1.setCorrectAnswerIndex(1);
        q1.setExplanation("Stocks represent ownership shares in a corporation.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("What is the formula for market capitalization?");
        q2.setOptions(Arrays.asList(
                "Price per share × Total assets",
                "Shares outstanding × Price per share",
                "Revenue × Profit margin",
                "Debt + Equity"
        ));
        q2.setCorrectAnswerIndex(1);
        q2.setExplanation("Market capitalization is calculated as shares outstanding multiplied by stock price.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("Which of these is a characteristic of a bull market?");
        q3.setOptions(Arrays.asList(
                "Falling prices and pessimism",
                "Rising prices and optimism",
                "Stagnant prices and uncertainty",
                "High volatility and fear"
        ));
        q3.setCorrectAnswerIndex(1);
        q3.setExplanation("Bull markets are characterized by rising prices and investor optimism.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("Stock exchange helps in?");
        q4.setOptions(Arrays.asList(
                "Providing liquidity to the existing securities",
                "contributing to economic growth",
                "pricing of securities",
                "All of the above"
        ));
        q4.setCorrectAnswerIndex(3);
        q4.setExplanation("Stock exchage plays a crucial role by enhancing liquidity for investors, facilitating capital formation for economic growth and enabling efficient price discovery of securities.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);
        quiz.setQuestions(questions);
        return quiz;
    }

    public static Quiz getChartReadingQuiz(){
        Quiz quiz = new Quiz("chart-reading", 70, 20);

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What does a candlestick on a stock chart represent?");
        q1.setOptions(Arrays.asList(
                "The company’s daily revenue",
                "The stock’s opening, closing, high, and low prices for a time period",
                "The stock’s average price for a month",
                "The total trading volume in a week"
        ));
        q1.setCorrectAnswerIndex(1);
        q1.setExplanation("Each candlestick visually summarizes how the price moved within a specific time frame — open, close, high, and low.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("What is the purpose of a moving average (MA)?");
        q2.setOptions(Arrays.asList(
                "To predict future prices",
                "To calculate company value",
                "To smooth out price fluctuations and show the trend direction",
                "To find daily returns"
        ));
        q2.setCorrectAnswerIndex(2);
        q2.setExplanation("A moving average filters short-term noise to reveal the underlying market trend.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("Support levels on a chart indicate:");
        q3.setOptions(Arrays.asList(
                "Resistance zones",
                "A company’s base value",
                "Sudden volume spikes",
                "A price area where buying prevents further decline"
        ));
        q3.setCorrectAnswerIndex(3);
        q3.setExplanation("Support acts as a floor — a level where demand typically outweighs supply, stopping the price from falling further.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("An RSI (Relative Strength Index) value above 70 typically indicates?");
        q4.setOptions(Arrays.asList(
                "Overbought condition",
                "Neutral momentum",
                "Oversold condition",
                "Trend reversal"
        ));
        q4.setCorrectAnswerIndex(0);
        q4.setExplanation("When RSI exceeds 70, the stock is considered overbought, suggesting it may be due for a pullback.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);
        quiz.setQuestions(questions);
        return quiz;

    }

    public static Quiz getInvestmentRiskManagementQuiz() {
        Quiz quiz = new Quiz("investment-risk-management", 70, 20);

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What is the primary goal of investment risk management?");
        q1.setOptions(Arrays.asList(
                "To maximize returns regardless of risk",
                "To eliminate all possible risks",
                "To balance potential returns with acceptable levels of risk",
                "To avoid investing in volatile markets"
        ));
        q1.setCorrectAnswerIndex(2);
        q1.setExplanation("Risk management aims to achieve a balance between potential returns and acceptable levels of risk exposure.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("Which of the following best describes diversification?");
        q2.setOptions(Arrays.asList(
                "Investing all money in one asset to maximize profit",
                "Spreading investments across various assets to reduce risk",
                "Avoiding all risky investments entirely",
                "Trading frequently to manage portfolio risk"
        ));
        q2.setCorrectAnswerIndex(1);
        q2.setExplanation("Diversification reduces overall portfolio risk by spreading investments across different asset types and sectors.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("Which type of risk cannot be eliminated through diversification?");
        q3.setOptions(Arrays.asList(
                "Systematic risk",
                "Unsystematic risk",
                "Company-specific risk",
                "Operational risk"
        ));
        q3.setCorrectAnswerIndex(0);
        q3.setExplanation("Systematic risk, such as market or interest rate risk, affects the entire market and cannot be diversified away.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("What does the 'risk-return trade-off' principle imply?");
        q4.setOptions(Arrays.asList(
                "Higher risk guarantees higher return",
                "Lower risk always gives lower returns",
                "Investors must accept higher risk for the possibility of higher returns",
                "Returns are independent of risk level"
        ));
        q4.setCorrectAnswerIndex(2);
        q4.setExplanation("The risk-return trade-off means investors must take on more risk to have the potential for higher returns.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);

        quiz.setQuestions(questions);
        return quiz;
    }

    public static Quiz getIntroductionToOptionsTradingQuiz() {
        Quiz quiz = new Quiz("introduction-to-options-trading", 70, 20);

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What is an option in financial markets?");
        q1.setOptions(Arrays.asList(
                "A contract giving the right but not the obligation to buy or sell an asset at a set price",
                "An obligation to buy an asset at the current market price",
                "A guarantee of profit from a stock trade",
                "A type of bond issued by companies"
        ));
        q1.setCorrectAnswerIndex(0);
        q1.setExplanation("An option is a derivative contract that gives the holder the right—but not the obligation—to buy or sell an asset at a predetermined price before or at expiry.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("What is a 'call option'?");
        q2.setOptions(Arrays.asList(
                "A right to sell an asset at a specified price before expiry",
                "A right to buy an asset at a specified price before expiry",
                "An agreement to buy multiple assets at market price",
                "A financial guarantee against losses"
        ));
        q2.setCorrectAnswerIndex(1);
        q2.setExplanation("A call option gives the holder the right to buy the underlying asset at a specified strike price within a specific time frame.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("If you expect a stock’s price to fall, which position might you take?");
        q3.setOptions(Arrays.asList(
                "Buy a call option",
                "Sell a put option",
                "Buy a put option",
                "Buy both a call and a put"
        ));
        q3.setCorrectAnswerIndex(2);
        q3.setExplanation("Buying a put option profits when the stock price falls, since it gives you the right to sell at a higher strike price.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("What is the 'strike price' in an option contract?");
        q4.setOptions(Arrays.asList(
                "The price paid for buying the option itself",
                "The market price of the asset when the option expires",
                "The highest price the asset reached during the contract term",
                "The price at which the holder can buy or sell the underlying asset"
        ));
        q4.setCorrectAnswerIndex(3);
        q4.setExplanation("The strike price (or exercise price) is the fixed price at which the option holder can buy or sell the underlying asset.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);

        Question q5 = new Question();
        q5.setQuestionText("Which of the following statements about option premiums is TRUE?");
        q5.setOptions(Arrays.asList(
                "The premium decreases as volatility increases",
                "The premium is the total profit from the option",
                "The premium is paid by the option buyer to the seller for the contract",
                "The premium is refunded if the option expires worthless"
        ));
        q5.setCorrectAnswerIndex(2);
        q5.setExplanation("The option premium is the price paid by the buyer to the seller for obtaining the rights of the option contract.");
        q5.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q5);

        quiz.setQuestions(questions);
        return quiz;
    }

    public static Quiz getFundamentalAnalysisOfCompaniesQuiz() {
        Quiz quiz = new Quiz("fundamental-analysis-of-companies", 70, 20);

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What is the main purpose of fundamental analysis?");
        q1.setOptions(Arrays.asList(
                "To predict short-term price movements using charts",
                "To evaluate a company's financial health and intrinsic value",
                "To trade frequently for small profits",
                "To analyze market sentiment exclusively"
        ));
        q1.setCorrectAnswerIndex(1);
        q1.setExplanation("Fundamental analysis focuses on a company's financial statements, performance, and intrinsic value to guide investment decisions.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("Which financial statement shows a company's profitability over a period of time?");
        q2.setOptions(Arrays.asList(
                "Balance Sheet",
                "Cash Flow Statement",
                "Income Statement",
                "Statement of Shareholders' Equity"
        ));
        q2.setCorrectAnswerIndex(2);
        q2.setExplanation("The Income Statement (Profit & Loss Statement) reports a company's revenues, expenses, and profit over a specific period.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("Which ratio indicates how much profit a company generates relative to its shareholders' equity?");
        q3.setOptions(Arrays.asList(
                "Current Ratio",
                "Return on Equity (ROE)",
                "Debt-to-Equity Ratio",
                "Price-to-Earnings (P/E) Ratio"
        ));
        q3.setCorrectAnswerIndex(1);
        q3.setExplanation("Return on Equity (ROE) measures a company's profitability in relation to the equity invested by shareholders.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("What does the Price-to-Earnings (P/E) ratio represent?");
        q4.setOptions(Arrays.asList(
                "The company's total debt compared to equity",
                "The market price per share relative to earnings per share",
                "The ratio of current assets to current liabilities",
                "The company's annual revenue growth rate"
        ));
        q4.setCorrectAnswerIndex(1);
        q4.setExplanation("The P/E ratio shows how much investors are willing to pay per unit of earnings, indicating market valuation of a company's stock.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);

        Question q5 = new Question();
        q5.setQuestionText("Which of the following factors is NOT typically analyzed in fundamental analysis?");
        q5.setOptions(Arrays.asList(
                "Industry and market trends",
                "Company's management quality",
                "Technical price patterns",
                "Financial ratios and statements"
        ));
        q5.setCorrectAnswerIndex(2);
        q5.setExplanation("Technical price patterns are part of technical analysis, not fundamental analysis, which focuses on financial and qualitative factors.");
        q5.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q5);

        quiz.setQuestions(questions);
        return quiz;
    }

    public static Quiz getAdvancedTechnicalAnalysisStrategiesQuiz() {
        Quiz quiz = new Quiz("advanced-technical-analysis-strategies", 70, 20);

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What is the primary purpose of using Fibonacci retracement levels in technical analysis?");
        q1.setOptions(Arrays.asList(
                "To identify potential support and resistance levels based on historical price movements",
                "To calculate the exact future price of an asset",
                "To determine the overall market capitalization",
                "To measure trading volume over time"
        ));
        q1.setCorrectAnswerIndex(0);
        q1.setExplanation("Fibonacci retracements help traders identify potential reversal points by applying ratios derived from the Fibonacci sequence to price charts.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("In Elliott Wave Theory, how many waves are typically in an impulsive move?");
        q2.setOptions(Arrays.asList(
                "Three waves",
                "Five waves",
                "Eight waves",
                "Two waves"
        ));
        q2.setCorrectAnswerIndex(1);
        q2.setExplanation("Elliott Wave Theory describes market movements in five impulsive waves in the direction of the trend, followed by three corrective waves.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("What does the Ichimoku Cloud indicator primarily help traders visualize?");
        q3.setOptions(Arrays.asList(
                "Only historical trading volume",
                "Short-term price momentum",
                "Dynamic support and resistance levels, trend direction, and momentum",
                "Fundamental company earnings"
        ));
        q3.setCorrectAnswerIndex(2);
        q3.setExplanation("The Ichimoku Cloud provides a comprehensive view of support/resistance, trend, and momentum through its multiple components like the Kumo cloud.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("Which advanced strategy involves identifying divergences between price and the MACD indicator?");
        q4.setOptions(Arrays.asList(
                "Trend following with moving averages",
                "Breakout trading with volume",
                "Scalping with tick charts",
                "Momentum reversal anticipation"
        ));
        q4.setCorrectAnswerIndex(3);
        q4.setExplanation("MACD divergences occur when price makes new highs/lows but MACD does not, signaling potential reversals in momentum.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);

        Question q5 = new Question();
        q5.setQuestionText("In Relative Strength Index (RSI) analysis, what does a bullish divergence typically indicate?");
        q5.setOptions(Arrays.asList(
                "Potential upward price reversal despite overbought conditions",
                "Continuation of the downtrend",
                "Immediate sell signal",
                "Market is entering a consolidation phase"
        ));
        q5.setCorrectAnswerIndex(0);
        q5.setExplanation("A bullish RSI divergence happens when price makes lower lows but RSI makes higher lows, suggesting weakening downward momentum and possible reversal.");
        q5.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q5);

        Question q6 = new Question();
        q6.setQuestionText("What does a Bollinger Band 'squeeze' suggest in technical analysis?");
        q6.setOptions(Arrays.asList(
                "High volatility and trend continuation",
                "Impending volatility expansion and potential breakout",
                "Overbought market conditions",
                "Decreasing trading volume"
        ));
        q6.setCorrectAnswerIndex(1);
        q6.setExplanation("A Bollinger Band squeeze occurs when bands contract, indicating low volatility, often preceding a significant price move or breakout.");
        q6.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q6);

        quiz.setQuestions(questions);
        return quiz;
    }

    public static Quiz getProfessionalPortfolioManagementQuiz() {
        Quiz quiz = new Quiz("professional-portfolio-management", 70, 20);

        List<Question> questions = new ArrayList<>();

        Question q1 = new Question();
        q1.setQuestionText("What is the primary objective of professional portfolio management?");
        q1.setOptions(Arrays.asList(
                "To focus solely on short-term gains",
                "To eliminate all investment risks",
                "To achieve the investor's financial goals while managing risk and return",
                "To speculate on high-volatility assets"
        ));
        q1.setCorrectAnswerIndex(2);
        q1.setExplanation("Professional portfolio management aims to meet the client's long-term financial objectives by balancing risk and return through strategic planning.");
        q1.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q1);

        Question q2 = new Question();
        q2.setQuestionText("Which of the following best describes asset allocation in portfolio management?");
        q2.setOptions(Arrays.asList(
                "Distributing investments across different asset classes to optimize risk-return profile",
                "Concentrating all funds in a single high-performing stock",
                "Frequently buying and selling assets for quick profits",
                "Ignoring market trends and holding assets indefinitely"
        ));
        q2.setCorrectAnswerIndex(0);
        q2.setExplanation("Asset allocation involves dividing a portfolio among various asset categories like stocks, bonds, and cash to achieve diversification and align with the investor's risk tolerance.");
        q2.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q2);

        Question q3 = new Question();
        q3.setQuestionText("What does portfolio rebalancing typically involve?");
        q3.setOptions(Arrays.asList(
                "Selling all assets during market downturns",
                "Increasing exposure to underperforming assets",
                "Maintaining the original asset allocation by ignoring drifts",
                "Adjusting the portfolio periodically to restore the desired asset mix"
        ));
        q3.setCorrectAnswerIndex(3);
        q3.setExplanation("Rebalancing ensures the portfolio remains aligned with the target allocation by buying or selling assets that have deviated due to market movements.");
        q3.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q3);

        Question q4 = new Question();
        q4.setQuestionText("The Sharpe ratio is used in portfolio management to measure:");
        q4.setOptions(Arrays.asList(
                "Absolute returns without considering risk",
                "Risk-adjusted performance by comparing excess return to standard deviation",
                "The total number of assets in the portfolio",
                "Liquidity of individual securities"
        ));
        q4.setCorrectAnswerIndex(1);
        q4.setExplanation("The Sharpe ratio evaluates how well a portfolio compensates for the risk taken, calculated as (return - risk-free rate) / standard deviation.");
        q4.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q4);

        Question q5 = new Question();
        q5.setQuestionText("In professional portfolio management, active management strategies primarily aim to:");
        q5.setOptions(Arrays.asList(
                "Outperform a benchmark index through security selection and market timing",
                "Replicate the performance of a market index with minimal costs",
                "Avoid all forms of market analysis",
                "Focus exclusively on fixed-income securities"
        ));
        q5.setCorrectAnswerIndex(0);
        q5.setExplanation("Active management involves research and decisions to beat the market, contrasting with passive strategies that track indices.");
        q5.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q5);

        Question q6 = new Question();
        q6.setQuestionText("Modern Portfolio Theory (MPT), developed by Harry Markowitz, emphasizes:");
        q6.setOptions(Arrays.asList(
                "Investing only in high-risk, high-return assets",
                "Eliminating diversification to focus on top performers",
                "Optimizing portfolios through diversification to maximize return for a given risk level",
                "Predicting short-term market fluctuations accurately"
        ));
        q6.setCorrectAnswerIndex(2);
        q6.setExplanation("MPT introduces the concept of the efficient frontier, where portfolios are constructed to offer the best possible expected return for a defined level of risk via diversification.");
        q6.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q6);

        Question q7 = new Question();
        q7.setQuestionText("How does behavioral finance influence professional portfolio management?");
        q7.setOptions(Arrays.asList(
                "By ignoring psychological factors in decision-making",
                "By helping managers understand and mitigate investor biases like overconfidence or loss aversion",
                "By focusing solely on quantitative models without human input",
                "By encouraging speculative trading based on emotions"
        ));
        q7.setCorrectAnswerIndex(1);
        q7.setExplanation("Behavioral finance studies how cognitive biases affect investor behavior, allowing portfolio managers to design strategies that counteract irrational decisions.");
        q7.setQuestionType("MULTIPLE_CHOICE");
        questions.add(q7);

        quiz.setQuestions(questions);
        return quiz;
    }



}
