package service;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.HashMap;
import java.util.Map;

import model.Movie;
import model.User;
import model.Recommendation;

import datastructures.MovieGraph;
import datastructures.MovieSimilarity;

public class RecommendationEngine {

    private MovieCatalog catalog;
    private MovieGraph movieGraph;
    private MovieSimilarity similarity;

    public RecommendationEngine(
            MovieCatalog catalog,
            MovieGraph movieGraph) {

        this.catalog = catalog;
        this.movieGraph = movieGraph;
        this.similarity = new MovieSimilarity();
    }

    public List<Recommendation> recommend(
            User user,
            int k) {

        PriorityQueue<Recommendation> maxHeap =
            new PriorityQueue<Recommendation>(
                (a, b) ->
                    Double.compare(
                        b.getScore(),
                        a.getScore()
                    )
            );

        List<Movie> allMovies =
            catalog.getAllMovies();

        int maxViews = 1;

        for (Movie movie : allMovies) {

            if (movie.getViews() > maxViews) {
                maxViews = movie.getViews();
            }
        }

        // =========================
        // CALCULATE SCORE
        // =========================

        for (Movie movie : allMovies) {

            // Don't recommend watched movies

            if (user.getWatchHistory()
                    .containsMovie(movie.getId())) {

                continue;
            }

            // =========================
            // PREFERENCE
            // =========================

            int preference =
                user.getPreferences()
                    .getScore(movie.getId());

            // Never recommend a movie the user explicitly disliked.
            if (preference < 0) {
                continue;
            }

            double preferenceScore =
                preference * 3.0;

            // =========================
            // PERSONALIZED GENRE + SIMILARITY
            // =========================

            double similarityScore = 0;
            double genreScore = 0;

            String reason =
                "Trending";

            for (Movie likedMovie : allMovies) {

                int likedScore =
                    user.getPreferences()
                        .getScore(likedMovie.getId());

                if (likedScore > 0) {

                    // Strong personalization: movies from a genre
                    // the user has explicitly liked receive a boost.
                    if (likedMovie.getGenre() != null &&
                        movie.getGenre() != null &&
                        likedMovie.getGenre()
                            .equalsIgnoreCase(movie.getGenre())) {

                        genreScore = Math.max(genreScore, 5.0);
                        reason =
                            "Based on your liked genre";
                    }

                    int currentSimilarity =
                        similarity.calculateSimilarity(
                            likedMovie,
                            movie
                        );

                    if (currentSimilarity >
                        similarityScore) {

                        similarityScore =
                            currentSimilarity;

                        reason =
                            "Similar to movies you liked";
                    }
                }
            }

            // =========================
            // TRENDING
            // =========================

            double trendingScore =
                (double) movie.getViews()
                / maxViews;

            // =========================
            // FINAL SCORE
            // =========================

            double finalScore =
                preferenceScore
                + genreScore
                + similarityScore
                + trendingScore;

            // =========================
            // CREATE RECOMMENDATION
            // =========================

            Recommendation recommendation =
                new Recommendation(
                    movie,
                    finalScore,
                    reason
                );

            maxHeap.offer(
                recommendation
            );
        }

        // =========================
        // TOP K
        // =========================

        List<Recommendation> recommendations =
            new ArrayList<Recommendation>();

        while (!maxHeap.isEmpty()
                && recommendations.size() < k) {

            recommendations.add(
                maxHeap.poll()
            );
        }

        return recommendations;
    }
}