import model.Movie;
import model.User;
import model.Recommendation;

import service.MovieCatalog;
import service.UserManager;
import service.MovieService;

import datastructures.TrendingMovies;
import datastructures.MovieGraph;
import datastructures.MovieSimilarity;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        // =========================
        // MOVIE CATALOG
        // =========================

        MovieCatalog catalog =
            new MovieCatalog();

        Movie movie1 = new Movie(
            101,
            "Interstellar",
            "English",
            "Sci-Fi",
            8.7,
            9500,
            "poster-url-1",
            "backdrop-url-1",
            "A science fiction movie.",
            "2014-11-07",
            Arrays.asList(
                "Matthew McConaughey",
                "Anne Hathaway"
            )
        );

        Movie movie2 = new Movie(
            102,
            "RRR",
            "Telugu",
            "Action",
            8.0,
            12000,
            "poster-url-2",
            "backdrop-url-2",
            "An action drama film.",
            "2022-03-25",
            Arrays.asList(
                "N. T. Rama Rao Jr.",
                "Ram Charan"
            )
        );

        Movie movie3 = new Movie(
            103,
            "Premalu",
            "Malayalam",
            "Romance",
            7.8,
            10000,
            "poster-url-3",
            "backdrop-url-3",
            "A romantic comedy film.",
            "2024-02-09",
            Arrays.asList(
                "Naslen",
                "Mamitha Baiju"
            )
        );

        Movie movie4 = new Movie(
            104,
            "Pushpa",
            "Telugu",
            "Action",
            7.6,
            15000,
            "poster-url-4",
            "backdrop-url-4",
            "An action drama film.",
            "2021-12-17",
            Arrays.asList(
                "Allu Arjun",
                "Rashmika Mandanna"
            )
        );

        Movie movie5 = new Movie(
            105,
            "Pushpa 2",
            "Telugu",
            "Action",
            6.5,
            18000,
            "poster-url-5",
            "backdrop-url-5",
            "A sequel to Pushpa.",
            "2024-12-05",
            Arrays.asList(
                "Allu Arjun",
                "Rashmika Mandanna"
            )
        );

        catalog.addMovie(movie1);
        catalog.addMovie(movie2);
        catalog.addMovie(movie3);
        catalog.addMovie(movie4);
        catalog.addMovie(movie5);


        // =========================
        // MOVIE SEARCH
        // =========================

        System.out.println(
            "===== MOVIE CATALOG ====="
        );

        System.out.println(
            "Total movies: "
            + catalog.getMovieCount()
        );

        System.out.println(
            "\nSearch results for 'pu':"
        );

        List<Movie> results =
            catalog.searchMovies("pu");

        for (Movie movie : results) {

            System.out.println(
                movie.getTitle()
                + " | "
                + movie.getLanguage()
                + " | "
                + movie.getRating()
            );
        }


        // =========================
        // USER SYSTEM
        // =========================

        UserManager userManager =
            new UserManager();

        User user1 = new User(
            1,
            "Anu",
            "anu@gmail.com",
            "1234"
        );

        User user2 = new User(
            2,
            "Rahul",
            "rahul@gmail.com",
            "5678"
        );

        userManager.addUser(user1);
        userManager.addUser(user2);

        System.out.println(
            "\n===== USER SYSTEM ====="
        );

        System.out.println(
            "Total users: "
            + userManager.getUserCount()
        );


        // =========================
        // LOGIN
        // =========================

        System.out.println(
            "\n===== LOGIN TEST ====="
        );

        User loggedInUser =
            userManager.login(
                "anu@gmail.com",
                "1234"
            );

        if (loggedInUser != null) {

            System.out.println(
                "Login successful!"
            );

            System.out.println(
                "Welcome "
                + loggedInUser.getName()
            );

        } else {

            System.out.println(
                "Invalid email or password."
            );
        }


        // =========================
        // MOVIE GRAPH
        // =========================

        System.out.println(
            "\n===== BUILDING MOVIE GRAPH ====="
        );

        MovieGraph movieGraph =
            new MovieGraph();

        MovieSimilarity similarity =
            new MovieSimilarity();

        List<Movie> allMovies =
            catalog.getAllMovies();

        for (Movie movie : allMovies) {

            movieGraph.addMovie(
                movie.getId()
            );
        }

        for (int i = 0;
             i < allMovies.size();
             i++) {

            for (int j = i + 1;
                 j < allMovies.size();
                 j++) {

                Movie firstMovie =
                    allMovies.get(i);

                Movie secondMovie =
                    allMovies.get(j);

                if (similarity.areSimilar(
                        firstMovie,
                        secondMovie)) {

                    movieGraph.addConnection(
                        firstMovie.getId(),
                        secondMovie.getId()
                    );
                }
            }
        }

        System.out.println(
            "Movie graph created."
        );


        // =========================
        // MOVIE SERVICE
        // =========================

        MovieService movieService =
            new MovieService(
                catalog,
                movieGraph
            );


        // =========================
        // USER PREFERENCES
        // =========================

        System.out.println(
            "\n===== USER PREFERENCES ====="
        );

        movieService.likeMovie(
            user1,
            102
        );

        movieService.likeMovie(
            user1,
            104
        );

        movieService.dislikeMovie(
            user1,
            103
        );

        System.out.println(
            "RRR score: "
            + user1.getPreferences()
                     .getScore(102)
        );

        System.out.println(
            "Pushpa score: "
            + user1.getPreferences()
                     .getScore(104)
        );

        System.out.println(
            "Premalu score: "
            + user1.getPreferences()
                     .getScore(103)
        );


        // =========================
        // WATCH HISTORY
        // =========================

        System.out.println(
            "\n===== WATCH HISTORY ====="
        );

        movieService.watchMovie(
            user1,
            101
        );

        movieService.watchMovie(
            user1,
            102
        );

        movieService.watchMovie(
            user1,
            104
        );

        movieService.watchMovie(
            user2,
            103
        );

        System.out.println(
            "Anu's most recent movie ID: "
            + user1.getWatchHistory()
                     .getMostRecentMovie()
        );

        System.out.println(
            "Anu's history size: "
            + user1.getWatchHistory()
                     .getHistorySize()
        );


        // =========================
        // RECENT MOVIE
        // =========================

        Movie recentMovie =
            user1.getWatchHistory()
                 .getMostRecentMovie(
                     catalog
                 );

        if (recentMovie != null) {

            System.out.println(
                "\nRecently watched: "
                + recentMovie.getTitle()
            );
        }


        // =========================
        // TRENDING MOVIES
        // =========================

        System.out.println(
            "\n===== TOP 3 TRENDING MOVIES ====="
        );

        TrendingMovies trending =
            new TrendingMovies(3);

        for (Movie movie :
                catalog.getAllMovies()) {

            trending.addMovie(movie);
        }

        List<Movie> trendingMovies =
            trending.getTrendingMovies();

        for (Movie movie :
                trendingMovies) {

            System.out.println(
                movie.getTitle()
                + " | Views: "
                + movie.getViews()
            );
        }


        // =========================
        // SIMILAR MOVIES
        // =========================

        System.out.println(
            "\n===== SIMILAR MOVIES ====="
        );

        List<Integer> similarMovies =
            movieGraph.getSimilarMovies(102);

        System.out.println(
            "Movies similar to RRR:"
        );

        for (Integer movieId :
                similarMovies) {

            Movie movie =
                catalog.getMovieById(movieId);

            if (movie != null) {

                System.out.println(
                    movie.getTitle()
                );
            }
        }


        // =========================
        // BFS
        // =========================

        System.out.println(
            "\n===== BFS FROM RRR ====="
        );

        List<Integer> bfsResult =
            movieGraph.bfs(102);

        for (Integer movieId :
                bfsResult) {

            Movie movie =
                catalog.getMovieById(movieId);

            if (movie != null) {

                System.out.println(
                    movie.getTitle()
                );
            }
        }


        // =========================
        // RECOMMENDATIONS
        // =========================

        System.out.println(
            "\n===== PERSONALIZED RECOMMENDATIONS ====="
        );

        List<Recommendation> recommendations =
            movieService.getRecommendations(
                user1,
                3
            );

        int rank = 1;

        for (Recommendation recommendation :
                recommendations) {

            Movie movie =
                recommendation.getMovie();

            System.out.println(
                "\n" + rank
                + ". "
                + movie.getTitle()
            );

            System.out.println(
                "   Score: "
                + String.format(
                    "%.2f",
                    recommendation.getScore()
                )
            );

            System.out.println(
                "   Reason: "
                + recommendation.getReason()
            );

            System.out.println(
                "   Genre: "
                + movie.getGenre()
            );

            System.out.println(
                "   Language: "
                + movie.getLanguage()
            );

            rank++;
        }


        // =========================
        // FINAL TEST
        // =========================

        System.out.println(
            "\n===== MOVIE DETAILS TEST ====="
        );

        Movie selectedMovie =
            movieService.getMovieDetails(105);

        if (selectedMovie != null) {

            System.out.println(
                "Title: "
                + selectedMovie.getTitle()
            );

            System.out.println(
                "Rating: "
                + selectedMovie.getRating()
            );

            System.out.println(
                "Genre: "
                + selectedMovie.getGenre()
            );

            System.out.println(
                "Language: "
                + selectedMovie.getLanguage()
            );
        }
    }
}