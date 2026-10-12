package service;

import java.util.ArrayDeque;
import java.util.Deque;

import model.Movie;

public class WatchHistory {

    private Deque<Integer> history;

    public WatchHistory() {
        history = new ArrayDeque<Integer>();
    }

    public void addMovie(int movieId) {
        history.addFirst(movieId);
    }

    public Integer getMostRecentMovie() {
        return history.peekFirst();
    }

    public Integer removeOldestMovie() {
        return history.pollLast();
    }

    public int getHistorySize() {
        return history.size();
    }

    public boolean containsMovie(int movieId) {
        return history.contains(movieId);
    }

    public Movie getMostRecentMovie(MovieCatalog catalog) {

        Integer movieId = history.peekFirst();

        if (movieId == null) {
            return null;
        }

        return catalog.getMovieById(movieId);
    }
}