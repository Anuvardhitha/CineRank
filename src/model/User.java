package model;

import service.WatchHistory;

public class User {

    private int id;
    private String name;
    private String email;
    private String password;

    private UserPreferences preferences;
    private WatchHistory watchHistory;

    public User(
            int id,
            String name,
            String email,
            String password) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;

        preferences = new UserPreferences();
        watchHistory = new WatchHistory();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public UserPreferences getPreferences() {
        return preferences;
    }

    public WatchHistory getWatchHistory() {
        return watchHistory;
    }
}