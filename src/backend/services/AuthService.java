package backend.services;

import backend.models.User;
import backend.database.DatabaseHandler;
import backend.util.HashUtil;

public class AuthService {
    private final DatabaseHandler dbHandler;

    public AuthService() {
        this.dbHandler = new DatabaseHandler();
    }

    public AuthService(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }
    public User registerUser(String username, String password, String email) {
        User existingUser = dbHandler.getUserByUsername(username);
        if (existingUser != null) {
            System.out.println("Username already taken: " + username);
            return null;
        }

        String hashedPassword = HashUtil.hashPassword(password);

        User newUser = new User(username, hashedPassword, email, 100000.0);

        boolean success = dbHandler.addUser(newUser);
        return success ? newUser : null;
    }

    public User loginUser(String username, String password) {
        User user = dbHandler.getUserByUsername(username);
        if (user == null) {
            System.out.println("User not found: " + username);
            return null;
        }

        boolean valid = HashUtil.verifyPassword(password, user.getPasswordHash());
        if (!valid) {
            System.out.println("Invalid password for user: " + username);
            return null;
        }

        return user;
    }

    public User getUser(String username) {
        return dbHandler.getUserByUsername(username);
    }

    public boolean updateUserBalance(String username, double newBalance) {
        return dbHandler.updateUserBalance(username, newBalance);
    }
}
