package service;

import java.util.List;

import model.Movie;
import model.Recommendation;
import model.User;

import datastructures.MovieGraph;

public class MovieService {

    private MovieCatalog catalog;
    private RecommendationEngine recommendationEngine;

    public MovieService(
            MovieCatalog catalog,
            MovieGraph movieGraph) {

        this.catalog = catalog;

        recommendationEngine =
            new RecommendationEngine(
                catalog,
                movieGraph
            );
    }


    // =========================
    // SEARCH MOVIES
    // =========================

    public List<Movie> searchMovies(String prefix) {

        return catalog.searchMovies(prefix);
    }


    // =========================
    // GET MOVIE DETAILS
    // =========================

    public Movie getMovieDetails(int movieId) {

        return catalog.getMovieById(movieId);
    }


    // =========================
    // WATCH MOVIE
    // =========================

    public void watchMovie(
            User user,
            int movieId) {

        Movie movie =
            catalog.getMovieById(movieId);

        if (movie == null) {
            return;
        }

        user.getWatchHistory()
            .addMovie(movieId);

        movie.increaseViews();
    }


    // =========================
    // LIKE MOVIE
    // =========================

    public void likeMovie(
            User user,
            int movieId) {

        Movie movie =
            catalog.getMovieById(movieId);

        if (movie == null) {
            return;
        }

        user.getPreferences()
            .likeMovie(movieId);
    }


    // =========================
    // DISLIKE MOVIE
    // =========================

    public void dislikeMovie(
            User user,
            int movieId) {

        Movie movie =
            catalog.getMovieById(movieId);

        if (movie == null) {
            return;
        }

        user.getPreferences()
            .dislikeMovie(movieId);
    }


    // =========================
    // RECOMMENDATIONS
    // =========================

    public List<Recommendation> getRecommendations(
            User user,
            int k) {

        return recommendationEngine.recommend(
            user,
            k
        );
    }
}