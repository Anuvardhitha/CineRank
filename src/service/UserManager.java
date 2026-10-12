package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import model.User;

public class UserManager {

    private HashMap<Integer, User> usersById;
    private HashMap<String, User> usersByEmail;
    private Connection connection;

    public UserManager() throws Exception {

        usersById = new HashMap<Integer, User>();
        usersByEmail = new HashMap<String, User>();

        String url = System.getenv("CINERANK_DB_URL");
        String user = System.getenv("CINERANK_DB_USER");
        String password = System.getenv("CINERANK_DB_PASSWORD");

        if (url == null || url.trim().isEmpty()) {
            url = "jdbc:postgresql://localhost:5432/cinerank";
        }

        if (user == null || user.trim().isEmpty()) {
            user = "postgres";
        }

        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("CINERANK_DB_PASSWORD is not set.");
        }

        connection = DriverManager.getConnection(url, user, password);
        createUserTables();
        loadUsers();
    }

    private void createUserTables() throws Exception {

        Statement statement = connection.createStatement();

        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY, " +
            "name VARCHAR(100) NOT NULL, " +
            "email VARCHAR(200) UNIQUE NOT NULL, " +
            "password VARCHAR(200) NOT NULL" +
            ")"
        );

        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS user_preferences (" +
            "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
            "movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE, " +
            "score INTEGER NOT NULL, " +
            "PRIMARY KEY (user_id, movie_id)" +
            ")"
        );

        statement.executeUpdate(
            "CREATE TABLE IF NOT EXISTS watch_history (" +
            "user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE, " +
            "movie_id INTEGER NOT NULL REFERENCES movies(id) ON DELETE CASCADE, " +
            "watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")"
        );

        statement.close();
    }

    private void loadUsers() throws Exception {

        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT id, name, email, password FROM users ORDER BY id"
            );

        ResultSet result = statement.executeQuery();

        while (result.next()) {

            User user = new User(
                result.getInt("id"),
                result.getString("name"),
                result.getString("email"),
                result.getString("password")
            );

            loadUserData(user);
            addUserToMemory(user);
        }

        result.close();
        statement.close();
    }

    private void addUserToMemory(User user) {
        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail().toLowerCase(), user);
    }

    public void addUser(User user) {
        addUserToMemory(user);
    }

    public User getUserById(int id) {
        return usersById.get(id);
    }

    public User login(String email, String password) {

        User user = usersByEmail.get(email.toLowerCase());

        if (user != null && user.getPassword().equals(password)) {
            return user;
        }

        return null;
    }

    public boolean emailExists(String email) {
        return usersByEmail.containsKey(email.toLowerCase());
    }

    public User createUser(String name, String email, String password) throws Exception {

        int id = getNextUserId();

        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO users (id, name, email, password) VALUES (?, ?, ?, ?)"
            );

        statement.setInt(1, id);
        statement.setString(2, name);
        statement.setString(3, email);
        statement.setString(4, password);
        statement.executeUpdate();
        statement.close();

        User user = new User(id, name, email, password);
        addUserToMemory(user);

        return user;
    }

    private int getNextUserId() throws Exception {

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(
            "SELECT COALESCE(MAX(id), 0) + 1 FROM users"
        );

        result.next();
        int id = result.getInt(1);

        result.close();
        statement.close();

        return id;
    }

    public User getOrCreateDemoUser() throws Exception {

        User existing = usersByEmail.get("demo@cinerank.com");

        if (existing != null) {
            return existing;
        }

        return createUser(
            "Demo User",
            "demo@cinerank.com",
            "1234"
        );
    }

    public void savePreference(int userId, int movieId, int score) throws Exception {

        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO user_preferences (user_id, movie_id, score) " +
                "VALUES (?, ?, ?) " +
                "ON CONFLICT (user_id, movie_id) DO UPDATE SET score = EXCLUDED.score"
            );

        statement.setInt(1, userId);
        statement.setInt(2, movieId);
        statement.setInt(3, score);
        statement.executeUpdate();
        statement.close();
    }

    public void saveWatch(int userId, int movieId) throws Exception {

        PreparedStatement statement =
            connection.prepareStatement(
                "INSERT INTO watch_history (user_id, movie_id) VALUES (?, ?)"
            );

        statement.setInt(1, userId);
        statement.setInt(2, movieId);
        statement.executeUpdate();
        statement.close();
    }

    private void loadUserData(User user) throws Exception {

        PreparedStatement preferenceStatement =
            connection.prepareStatement(
                "SELECT movie_id, score FROM user_preferences WHERE user_id = ?"
            );

        preferenceStatement.setInt(1, user.getId());
        ResultSet preferences = preferenceStatement.executeQuery();

        while (preferences.next()) {
            int movieId = preferences.getInt("movie_id");
            int score = preferences.getInt("score");

            if (score > 0) {
                user.getPreferences().likeMovie(movieId);
            } else if (score < 0) {
                user.getPreferences().dislikeMovie(movieId);
            }
        }

        preferences.close();
        preferenceStatement.close();

        PreparedStatement historyStatement =
            connection.prepareStatement(
                "SELECT movie_id FROM watch_history WHERE user_id = ? ORDER BY watched_at ASC"
            );

        historyStatement.setInt(1, user.getId());
        ResultSet history = historyStatement.executeQuery();

        while (history.next()) {
            user.getWatchHistory().addMovie(history.getInt("movie_id"));
        }

        history.close();
        historyStatement.close();
    }

    public int getUserCount() {
        return usersById.size();
    }
}
