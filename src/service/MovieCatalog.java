package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import model.Movie;
import datastructures.MovieTrie;

public class MovieCatalog {

    private HashMap<Integer, Movie> movies;
    private MovieTrie movieTrie;

    public MovieCatalog() {
        movies = new HashMap<Integer, Movie>();
        movieTrie = new MovieTrie();
    }

    public static MovieCatalog loadFromDatabase() throws Exception {

        MovieCatalog catalog = new MovieCatalog();

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

        Connection connection =
            DriverManager.getConnection(url, user, password);

        PreparedStatement statement =
            connection.prepareStatement(
                "SELECT id, title, language, genre, rating, views, " +
                "poster_url, backdrop_url, overview, release_date " +
                "FROM movies ORDER BY id"
            );

        ResultSet result = statement.executeQuery();

        while (result.next()) {

            java.sql.Date date = result.getDate("release_date");

            Movie movie = new Movie(
                result.getInt("id"),
                result.getString("title"),
                result.getString("language"),
                result.getString("genre"),
                result.getDouble("rating"),
                result.getInt("views"),
                result.getString("poster_url"),
                result.getString("backdrop_url"),
                result.getString("overview"),
                date == null ? "" : date.toString(),
                new ArrayList<String>()
            );

            catalog.addMovie(movie);
        }

        result.close();
        statement.close();
        connection.close();

        System.out.println(
            "Loaded " + catalog.getMovieCount() +
            " movies from PostgreSQL."
        );

        return catalog;
    }

    public void addMovie(Movie movie) {
        movies.put(movie.getId(), movie);
        movieTrie.insert(movie.getTitle(), movie.getId());
    }

    public Movie getMovieById(int id) {
        return movies.get(id);
    }

    public List<Movie> searchMovies(String prefix) {
        List<Integer> ids = movieTrie.searchPrefix(prefix);
        List<Movie> results = new ArrayList<Movie>();

        for (Integer id : ids) {
            Movie movie = movies.get(id);
            if (movie != null) {
                results.add(movie);
            }
        }
        return results;
    }

    public int getMovieCount() {
        return movies.size();
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<Movie>(movies.values());
    }
}
