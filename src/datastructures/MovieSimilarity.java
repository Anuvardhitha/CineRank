package datastructures;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import model.Movie;

public class MovieSimilarity {

    public int calculateSimilarity(
            Movie movie1,
            Movie movie2) {

        if (movie1.getId() == movie2.getId()) {
            return 0;
        }

        int score = 0;

        // =========================
        // SAME GENRE
        // =========================

        if (movie1.getGenre()
                .equalsIgnoreCase(
                    movie2.getGenre())) {

            score += 2;
        }

        // =========================
        // SAME LANGUAGE
        // =========================

        if (movie1.getLanguage()
                .equalsIgnoreCase(
                    movie2.getLanguage())) {

            score += 1;
        }

        // =========================
        // SHARED CAST
        // =========================

        List<String> cast1 =
            movie1.getCast();

        List<String> cast2 =
            movie2.getCast();

        // Convert cast2 List → HashSet

        Set<String> castSet =
            new HashSet<String>(cast2);

        for (String actor : cast1) {

            if (castSet.contains(actor)) {

                score += 1;
                break;
            }
        }

        return score;
    }

    public boolean areSimilar(
            Movie movie1,
            Movie movie2) {

        return calculateSimilarity(
            movie1,
            movie2
        ) > 0;
    }
}