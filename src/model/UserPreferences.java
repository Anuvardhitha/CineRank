package model;

import java.util.HashMap;

public class UserPreferences {

    private HashMap<Integer, Integer> movieScores;

    public UserPreferences() {

        movieScores = new HashMap<Integer, Integer>();
    }

    public void likeMovie(int movieId) {

        movieScores.put(
            movieId,
            1
        );
    }

    public void dislikeMovie(int movieId) {

        movieScores.put(
            movieId,
            -1
        );
    }

    public int getScore(int movieId) {

        if (movieScores.containsKey(movieId)) {
            return movieScores.get(movieId);
        }

        return 0;
    }
}