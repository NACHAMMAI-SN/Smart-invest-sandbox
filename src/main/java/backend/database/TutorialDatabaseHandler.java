package backend.database;

import backend.models.UserProgress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TutorialDatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:smartstock.db";
    private Connection connection;

    public TutorialDatabaseHandler() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            initializeTutorialTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void initializeTutorialTables() {
        String createUserProgressTable = """
            CREATE TABLE IF NOT EXISTS user_progress (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                tutorial_id TEXT NOT NULL,
                completed BOOLEAN DEFAULT FALSE,
                started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                completed_at DATETIME,
                score INTEGER DEFAULT 0,
                time_spent INTEGER DEFAULT 0,
                quiz_score INTEGER DEFAULT 0,
                quiz_passed BOOLEAN DEFAULT FALSE,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                UNIQUE(user_id, tutorial_id),
                FOREIGN KEY (user_id) REFERENCES users(username) ON DELETE CASCADE
            )
        """;

        String createUserBadgesTable = """
            CREATE TABLE IF NOT EXISTS user_badges (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                badge_name TEXT NOT NULL,
                earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(username) ON DELETE CASCADE,
                UNIQUE(user_id, badge_name)
            )
        """;

        String createUserCertificationsTable = """
            CREATE TABLE IF NOT EXISTS user_certifications (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id TEXT NOT NULL,
                certification_id TEXT NOT NULL,
                earned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                score INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(username) ON DELETE CASCADE,
                UNIQUE(user_id, certification_id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUserProgressTable);
            stmt.execute(createUserBadgesTable);
            stmt.execute(createUserCertificationsTable);
            System.out.println("✅ Tutorial tables ready.");
        } catch (SQLException e) {
            System.err.println("❌ Error creating tutorial tables: " + e.getMessage());
        }
    }

    public boolean saveUserProgress(UserProgress progress) {
        String sql = """
            INSERT OR REPLACE INTO user_progress 
            (user_id, tutorial_id, completed, started_at, completed_at, score, time_spent, quiz_score, quiz_passed, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, progress.getUserId());
            pstmt.setString(2, progress.getTutorialId());
            pstmt.setBoolean(3, progress.isCompleted());
            pstmt.setTimestamp(4, new Timestamp(progress.getStartedAt().getTime()));
            pstmt.setTimestamp(5, progress.getCompletedAt() != null ?
                    new Timestamp(progress.getCompletedAt().getTime()) : null);
            pstmt.setInt(6, progress.getScore());
            pstmt.setInt(7, progress.getTimeSpent());
            pstmt.setInt(8, progress.getQuizScore());
            pstmt.setBoolean(9, progress.isQuizPassed());
            pstmt.setTimestamp(10, new Timestamp(System.currentTimeMillis()));

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error saving user progress: " + e.getMessage());
            return false;
        }
    }

    public UserProgress getUserProgress(String userId, String tutorialId) {
        String sql = "SELECT * FROM user_progress WHERE user_id = ? AND tutorial_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, tutorialId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUserProgress(rs);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user progress: " + e.getMessage());
        }
        return null;
    }

    public List<UserProgress> getAllUserProgress(String userId) {
        List<UserProgress> progressList = new ArrayList<>();
        String sql = "SELECT * FROM user_progress WHERE user_id = ? ORDER BY updated_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                progressList.add(mapResultSetToUserProgress(rs));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user progress list: " + e.getMessage());
        }
        return progressList;
    }

    public boolean updateQuizResult(String userId, String tutorialId, int quizScore, boolean quizPassed) {
        String sql = """
            UPDATE user_progress 
            SET quiz_score = ?, quiz_passed = ?, updated_at = ?, completed = ?
            WHERE user_id = ? AND tutorial_id = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, quizScore);
            pstmt.setBoolean(2, quizPassed);
            pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            pstmt.setBoolean(4, quizPassed);
            pstmt.setString(5, userId);
            pstmt.setString(6, tutorialId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                UserProgress progress = new UserProgress(userId, tutorialId);
                progress.setQuizScore(quizScore);
                progress.setQuizPassed(quizPassed);
                progress.setCompleted(quizPassed);
                return saveUserProgress(progress);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error updating quiz result: " + e.getMessage());
            return false;
        }
    }

    public boolean markTutorialComplete(String userId, String tutorialId) {
        String sql = """
            UPDATE user_progress 
            SET completed = TRUE, completed_at = ?, updated_at = ?, score = 100
            WHERE user_id = ? AND tutorial_id = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, userId);
            pstmt.setString(4, tutorialId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                UserProgress progress = new UserProgress(userId, tutorialId);
                progress.setCompleted(true);
                progress.setScore(100);
                return saveUserProgress(progress);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error marking tutorial complete: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTimeSpent(String userId, String tutorialId, int additionalMinutes) {
        String sql = """
            UPDATE user_progress 
            SET time_spent = time_spent + ?, updated_at = ?
            WHERE user_id = ? AND tutorial_id = ?
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, additionalMinutes);
            pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            pstmt.setString(3, userId);
            pstmt.setString(4, tutorialId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                UserProgress progress = new UserProgress(userId, tutorialId);
                progress.setTimeSpent(additionalMinutes);
                return saveUserProgress(progress);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error updating time spent: " + e.getMessage());
            return false;
        }
    }

    public boolean addUserBadge(String userId, String badgeName) {
        String sql = "INSERT OR IGNORE INTO user_badges (user_id, badge_name) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, badgeName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding user badge: " + e.getMessage());
            return false;
        }
    }

    public List<String> getUserBadges(String userId) {
        List<String> badges = new ArrayList<>();
        String sql = "SELECT badge_name FROM user_badges WHERE user_id = ? ORDER BY earned_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                badges.add(rs.getString("badge_name"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user badges: " + e.getMessage());
        }
        return badges;
    }

    public boolean addUserCertification(String userId, String certificationId, int score) {
        String sql = "INSERT OR REPLACE INTO user_certifications (user_id, certification_id, score) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            pstmt.setString(2, certificationId);
            pstmt.setInt(3, score);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error adding user certification: " + e.getMessage());
            return false;
        }
    }

    public List<String> getUserCertifications(String userId) {
        List<String> certifications = new ArrayList<>();
        String sql = "SELECT certification_id FROM user_certifications WHERE user_id = ? ORDER BY earned_at DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                certifications.add(rs.getString("certification_id"));
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting user certifications: " + e.getMessage());
        }
        return certifications;
    }

    public int getCompletedTutorialCount(String userId) {
        String sql = "SELECT COUNT(*) as count FROM user_progress WHERE user_id = ? AND completed = TRUE";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting completed tutorial count: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalTimeSpent(String userId) {
        String sql = "SELECT SUM(time_spent) as total_time FROM user_progress WHERE user_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total_time");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting total time spent: " + e.getMessage());
        }
        return 0;
    }

    public double getAverageScore(String userId) {
        String sql = "SELECT AVG(score) as avg_score FROM user_progress WHERE user_id = ? AND completed = TRUE";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("avg_score");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error getting average score: " + e.getMessage());
        }
        return 0.0;
    }

    private UserProgress mapResultSetToUserProgress(ResultSet rs) throws SQLException {
        UserProgress progress = new UserProgress();
        progress.setId(rs.getLong("id"));
        progress.setUserId(rs.getString("user_id"));
        progress.setTutorialId(rs.getString("tutorial_id"));
        progress.setCompleted(rs.getBoolean("completed"));
        progress.setStartedAt(new java.util.Date(rs.getTimestamp("started_at").getTime()));
        if (rs.getTimestamp("completed_at") != null) {
            progress.setCompletedAt(new java.util.Date(rs.getTimestamp("completed_at").getTime()));
        }
        progress.setScore(rs.getInt("score"));
        progress.setTimeSpent(rs.getInt("time_spent"));
        progress.setQuizScore(rs.getInt("quiz_score"));
        progress.setQuizPassed(rs.getBoolean("quiz_passed"));
        progress.setCreatedAt(new java.util.Date(rs.getTimestamp("created_at").getTime()));
        progress.setUpdatedAt(new java.util.Date(rs.getTimestamp("updated_at").getTime()));
        return progress;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Tutorial database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}