package datastructures;

import java.util.PriorityQueue;
import java.util.List;
import java.util.ArrayList;

import model.Movie;

public class TrendingMovies {

    private PriorityQueue<Movie> minHeap;
    private int k;

    public TrendingMovies(int k) {

        this.k = k;

        minHeap = new PriorityQueue<Movie>(
            (movie1, movie2) ->
                movie1.getViews() - movie2.getViews()
        );
    }

    public void addMovie(Movie movie) {

        minHeap.offer(movie);

        if (minHeap.size() > k) {
            minHeap.poll();
        }
    }

    public List<Movie> getTrendingMovies() {

        List<Movie> results =
            new ArrayList<Movie>(minHeap);

        return results;
    }
}