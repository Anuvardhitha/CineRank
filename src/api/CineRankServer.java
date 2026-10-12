package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import model.Movie;
import model.User;
import model.Recommendation;

import service.MovieCatalog;
import service.MovieService;
import service.UserManager;
import service.TmdbService;

import datastructures.MovieGraph;
import datastructures.MovieSimilarity;
import datastructures.TrendingMovies;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CineRankServer {

    private static MovieCatalog catalog;
    private static MovieService movieService;
    private static UserManager userManager;
    private static TmdbService tmdbService;
    public static void main(String[] args)
            throws Exception {

        catalog = MovieCatalog.loadFromDatabase();

        tmdbService = new TmdbService();

        for (Movie movie : catalog.getAllMovies()) {
            tmdbService.enrichMovie(movie);
        }


        MovieGraph movieGraph =
            buildMovieGraph(catalog);


        movieService =
            new MovieService(
                catalog,
                movieGraph
            );


        userManager =
            new UserManager();


        User demoUser =
            userManager.getOrCreateDemoUser();


        if (demoUser.getPreferences().getScore(104) == 0) {
            movieService.likeMovie(
                demoUser,
                104
            );

            userManager.savePreference(
                demoUser.getId(),
                104,
                1
            );
        }


        HttpServer server =
            HttpServer.create(
                new InetSocketAddress(8080),
                0
            );


        server.createContext(
            "/api/auth/login",
            CineRankServer::login
        );


        server.createContext(
            "/api/auth/register",
            CineRankServer::register
        );


        server.createContext(
            "/api/movies/search",
            CineRankServer::searchMovies
        );


        server.createContext(
            "/api/movies/recommendations",
            CineRankServer::getRecommendations
        );


        server.createContext(
            "/api/movies/trending",
            CineRankServer::getTrendingMovies
        );


        server.createContext(
            "/api/movies/genre-top10",
            CineRankServer::getGenreTop10
        );


        server.createContext(
            "/api/movies/watch",
            CineRankServer::watchMovie
        );


        server.createContext(
            "/api/movies/like",
            CineRankServer::likeMovie
        );


        server.createContext(
            "/api/movies/dislike",
            CineRankServer::dislikeMovie
        );


        server.setExecutor(null);

        server.start();


        System.out.println(
            "================================"
        );

        System.out.println(
            "          CINE RANK"
        );

        System.out.println(
            "================================"
        );

        System.out.println(
            "Server started successfully!"
        );

        System.out.println();

        System.out.println(
            "Server URL:"
        );

        System.out.println(
            "http://localhost:8080"
        );

        System.out.println();

        System.out.println(
            "Personalized recommendations enabled."
        );
    }


    // =========================
    // LOGIN
    // =========================

    private static void login(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String body =
            readRequestBody(exchange);


        String email =
            getBodyParameter(
                body,
                "email"
            );


        String password =
            getBodyParameter(
                body,
                "password"
            );


        User user =
            userManager.login(
                email,
                password
            );


        if (user == null) {

            sendResponse(
                exchange,
                "{\"success\":false}"
            );

            return;
        }


        String response =
            "{"
            + "\"success\":true,"
            + "\"userId\":"
            + user.getId()
            + ","
            + "\"name\":\""
            + escapeJson(
                user.getName()
            )
            + "\","
            + "\"email\":\""
            + escapeJson(
                user.getEmail()
            )
            + "\""
            + "}";


        sendResponse(
            exchange,
            response
        );
    }


    // =========================
    // REGISTER
    // =========================

    private static void register(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String body =
            readRequestBody(exchange);


        String name =
            getBodyParameter(
                body,
                "name"
            );


        String email =
            getBodyParameter(
                body,
                "email"
            );


        String password =
            getBodyParameter(
                body,
                "password"
            );


        if (name == null ||
            email == null ||
            password == null ||
            name.isEmpty() ||
            email.isEmpty() ||
            password.isEmpty()) {

            sendResponse(
                exchange,
                "{\"success\":false,\"message\":\"All fields are required.\"}"
            );

            return;
        }


        if (userManager.emailExists(email)) {

            sendResponse(
                exchange,
                "{\"success\":false,\"message\":\"Email already exists.\"}"
            );

            return;
        }


        User newUser;

        try {
            newUser = userManager.createUser(
                name,
                email,
                password
            );
        } catch (Exception e) {
            sendResponse(
                exchange,
                "{\"success\":false,\"message\":\"Could not create account.\"}"
            );
            return;
        }


        int createdUserId =
            newUser.getId();


        String response =
            "{"
            + "\"success\":true,"
            + "\"userId\":"
            + createdUserId
            + ","
            + "\"name\":\""
            + escapeJson(name)
            + "\""
            + "}";


        sendResponse(
            exchange,
            response
        );
    }


    // =========================
    // READ REQUEST BODY
    // =========================

    private static String readRequestBody(
            HttpExchange exchange)
            throws IOException {

        InputStream input =
            exchange.getRequestBody();


        ByteArrayOutputStream buffer =
            new ByteArrayOutputStream();


        int data;


        while ((data = input.read()) != -1) {
            buffer.write(data);
        }


        input.close();


        return new String(
            buffer.toByteArray(),
            "UTF-8"
        );
    }


    // =========================
    // BODY PARAMETER
    // =========================

    private static String getBodyParameter(
            String body,
            String parameter) {

        if (body == null ||
            body.isEmpty()) {

            return "";
        }


        String[] parts =
            body.split("&");


        for (String part : parts) {

            String[] keyValue =
                part.split(
                    "=",
                    2
                );


            if (keyValue.length == 2 &&
                keyValue[0].equals(parameter)) {

                try {

                    return URLDecoder.decode(
                        keyValue[1],
                        "UTF-8"
                    );

                } catch (Exception e) {

                    return "";
                }
            }
        }


        return "";
    }


    // =========================
    // QUERY PARAMETER
    // =========================

    private static String getQueryParameter(
            String query,
            String parameter) {

        if (query == null ||
            query.isEmpty()) {

            return null;
        }


        String[] parts =
            query.split("&");


        for (String part : parts) {

            String[] keyValue =
                part.split(
                    "=",
                    2
                );


            if (keyValue.length == 2 &&
                keyValue[0].equals(parameter)) {

                try {

                    return URLDecoder.decode(
                        keyValue[1],
                        "UTF-8"
                    );

                } catch (Exception e) {

                    return null;
                }
            }
        }


        return null;
    }


    // =========================
    // GET USER ID
    // =========================

    private static int getUserIdFromQuery(
            String query) {

        String value =
            getQueryParameter(
                query,
                "userId"
            );


        if (value == null) {

            return 1;
        }


        try {

            return Integer.parseInt(value);

        } catch (NumberFormatException e) {

            return 1;
        }
    }


    // =========================
    // GET MOVIE ID
    // =========================

    private static int getMovieIdFromQuery(
            String query) {

        String value =
            getQueryParameter(
                query,
                "id"
            );


        if (value == null) {

            return -1;
        }


        try {

            return Integer.parseInt(value);

        } catch (NumberFormatException e) {

            return -1;
        }
    }


    // =========================
    // SEARCH
    // =========================

    private static void searchMovies(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String query =
            exchange.getRequestURI()
                    .getQuery();


        String prefix =
            getQueryParameter(
                query,
                "prefix"
            );


        if (prefix == null) {
            prefix = "";
        }


        List<Movie> movies =
            movieService.searchMovies(
                prefix
            );


        StringBuilder json =
            new StringBuilder();


        json.append("[");


        for (int i = 0;
             i < movies.size();
             i++) {

            json.append(
                movieToJson(
                    movies.get(i)
                )
            );


            if (i < movies.size() - 1) {
                json.append(",");
            }
        }


        json.append("]");


        sendResponse(
            exchange,
            json.toString()
        );
    }


    // =========================
    // TRENDING
    // =========================

    private static void getTrendingMovies(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        TrendingMovies trendingMovies =
            new TrendingMovies(8);


        List<Movie> movies =
            catalog.getAllMovies();


        for (Movie movie : movies) {

            trendingMovies.addMovie(
                movie
            );
        }


        List<Movie> results =
            trendingMovies.getTrendingMovies();


        Collections.sort(
            results,
            new Comparator<Movie>() {

                @Override
                public int compare(
                        Movie movie1,
                        Movie movie2) {

                    return Integer.compare(
                        movie2.getViews(),
                        movie1.getViews()
                    );
                }
            }
        );


        StringBuilder json =
            new StringBuilder();


        json.append("[");


        for (int i = 0;
             i < results.size();
             i++) {

            json.append(
                movieToJsonWithViews(
                    results.get(i)
                )
            );


            if (i < results.size() - 1) {
                json.append(",");
            }
        }


        json.append("]");


        sendResponse(
            exchange,
            json.toString()
        );
    }


    // =========================
    // PERSONALIZED RECOMMENDATIONS
    // =========================

    private static void getGenreTop10(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);

        HashMap<String, List<Movie>> genreMap =
            new HashMap<String, List<Movie>>();

        List<Movie> movies =
            catalog.getAllMovies();

        for (Movie movie : movies) {

            String genre = movie.getGenre();

            if (!genreMap.containsKey(genre)) {
                genreMap.put(
                    genre,
                    new ArrayList<Movie>()
                );
            }

            genreMap.get(genre).add(movie);
        }

        StringBuilder json =
            new StringBuilder();

        json.append("{");

        List<String> genres =
            new ArrayList<String>(genreMap.keySet());

        Collections.sort(genres);

        for (int i = 0; i < genres.size(); i++) {

            String genre = genres.get(i);

            List<Movie> genreMovies =
                genreMap.get(genre);

            Collections.sort(
                genreMovies,
                new Comparator<Movie>() {

                    @Override
                    public int compare(
                            Movie movie1,
                            Movie movie2) {

                        int ratingCompare =
                            Double.compare(
                                movie2.getRating(),
                                movie1.getRating()
                            );

                        if (ratingCompare != 0) {
                            return ratingCompare;
                        }

                        return Integer.compare(
                            movie2.getViews(),
                            movie1.getViews()
                        );
                    }
                }
            );

            int limit =
                Math.min(10, genreMovies.size());

            json.append("\"");
            json.append(escapeJson(genre));
            json.append("\":[");

            for (int j = 0; j < limit; j++) {

                json.append(
                    movieToJson(
                        genreMovies.get(j)
                    )
                );

                if (j < limit - 1) {
                    json.append(",");
                }
            }

            json.append("]");

            if (i < genres.size() - 1) {
                json.append(",");
            }
        }

        json.append("}");

        sendResponse(
            exchange,
            json.toString()
        );
    }


    private static void getRecommendations(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String query =
            exchange.getRequestURI()
                    .getQuery();


        int userId =
            getUserIdFromQuery(
                query
            );


        User user =
            userManager.getUserById(
                userId
            );


        if (user == null) {

            sendResponse(
                exchange,
                "[]"
            );

            return;
        }


        List<Recommendation> recommendations =
            movieService.getRecommendations(
                user,
                6
            );


        StringBuilder json =
            new StringBuilder();


        json.append("[");


        for (int i = 0;
             i < recommendations.size();
             i++) {

            json.append(
                recommendationToJson(
                    recommendations.get(i)
                )
            );


            if (i < recommendations.size() - 1) {
                json.append(",");
            }
        }


        json.append("]");


        sendResponse(
            exchange,
            json.toString()
        );
    }


    // =========================
    // WATCH
    // =========================

    private static void watchMovie(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String query =
            exchange.getRequestURI()
                    .getQuery();


        int movieId =
            getMovieIdFromQuery(
                query
            );


        int userId =
            getUserIdFromQuery(
                query
            );


        User user =
            userManager.getUserById(
                userId
            );


        if (user == null) {

            sendResponse(
                exchange,
                "{\"success\":false}"
            );

            return;
        }


        movieService.watchMovie(
            user,
            movieId
        );

        try {
            userManager.saveWatch(
                userId,
                movieId
            );
        } catch (Exception e) {
            sendResponse(
                exchange,
                "{\"success\":false,\"message\":\"Could not save watch history.\"}"
            );
            return;
        }


        sendResponse(
            exchange,
            "{\"success\":true,\"action\":\"watch\"}"
        );
    }


    // =========================
    // LIKE
    // =========================

    private static void likeMovie(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String query =
            exchange.getRequestURI()
                    .getQuery();


        int movieId =
            getMovieIdFromQuery(
                query
            );


        int userId =
            getUserIdFromQuery(
                query
            );


        User user =
            userManager.getUserById(
                userId
            );


        if (user == null) {

            sendResponse(
                exchange,
                "{\"success\":false}"
            );

            return;
        }


        movieService.likeMovie(
            user,
            movieId
        );

        try {
            userManager.savePreference(
                userId,
                movieId,
                1
            );
        } catch (Exception e) {
            sendResponse(
                exchange,
                "{\"success\":false,\"message\":\"Could not save preference.\"}"
            );
            return;
        }


        sendResponse(
            exchange,
            "{\"success\":true,\"action\":\"like\"}"
        );
    }


    // =========================
    // DISLIKE
    // =========================

    private static void dislikeMovie(
            HttpExchange exchange)
            throws IOException {

        setJsonHeaders(exchange);


        String query =
            exchange.getRequestURI()
                    .getQuery();


        int movieId =
            getMovieIdFromQuery(
                query
            );


        int userId =
            getUserIdFromQuery(
                query
            );


        User user =
            userManager.getUserById(
                userId
            );


        if (user == null) {

            sendResponse(
                exchange,
                "{\"success\":false}"
            );

            return;
        }


        movieService.dislikeMovie(
            user,
            movieId
        );

        try {
            userManager.savePreference(
                userId,
                movieId,
                -1
            );
        } catch (Exception e) {
            sendResponse(
                exchange,
                "{\"success\":false,\"message\":\"Could not save preference.\"}"
            );
            return;
        }


        sendResponse(
            exchange,
            "{\"success\":true,\"action\":\"dislike\"}"
        );
    }


    // =========================
    // MOVIE JSON
    // =========================

    private static String movieToJson(
            Movie movie) {

        StringBuilder json =
            new StringBuilder();


        json.append("{");


        json.append(
            "\"id\":"
            + movie.getId()
            + ","
        );


        json.append(
            "\"title\":\""
            + escapeJson(
                movie.getTitle()
            )
            + "\","
        );


        json.append(
            "\"genre\":\""
            + escapeJson(
                movie.getGenre()
            )
            + "\","
        );


        json.append(
            "\"language\":\""
            + escapeJson(
                movie.getLanguage()
            )
            + "\","
        );


        json.append(
            "\"rating\":"
            + movie.getRating()
            + ","
        );


        json.append(
            "\"posterUrl\":\""
            + escapeJson(
                movie.getPosterUrl()
            )
            + "\","
        );


        json.append(
            "\"backdropUrl\":\""
            + escapeJson(
                movie.getBackdropUrl()
            )
            + "\","
        );


        json.append(
            "\"overview\":\""
            + escapeJson(
                movie.getOverview()
            )
            + "\","
        );


        json.append(
            "\"releaseDate\":\""
            + escapeJson(
                movie.getReleaseDate()
            )
            + "\","
        );


        json.append(
            "\"cast\":"
            + castToJson(
                movie.getCast()
            )
        );


        json.append("}");


        return json.toString();
    }


    // =========================
    // TRENDING JSON
    // =========================

    private static String movieToJsonWithViews(
            Movie movie) {

        StringBuilder json =
            new StringBuilder();


        json.append("{");


        json.append(
            "\"id\":"
            + movie.getId()
            + ","
        );


        json.append(
            "\"title\":\""
            + escapeJson(
                movie.getTitle()
            )
            + "\","
        );


        json.append(
            "\"genre\":\""
            + escapeJson(
                movie.getGenre()
            )
            + "\","
        );


        json.append(
            "\"language\":\""
            + escapeJson(
                movie.getLanguage()
            )
            + "\","
        );


        json.append(
            "\"rating\":"
            + movie.getRating()
            + ","
        );


        json.append(
            "\"views\":"
            + movie.getViews()
            + ","
        );


        json.append(
            "\"posterUrl\":\""
            + escapeJson(
                movie.getPosterUrl()
            )
            + "\","
        );


        json.append(
            "\"backdropUrl\":\""
            + escapeJson(
                movie.getBackdropUrl()
            )
            + "\","
        );


        json.append(
            "\"overview\":\""
            + escapeJson(
                movie.getOverview()
            )
            + "\","
        );


        json.append(
            "\"releaseDate\":\""
            + escapeJson(
                movie.getReleaseDate()
            )
            + "\","
        );


        json.append(
            "\"cast\":"
            + castToJson(
                movie.getCast()
            )
        );


        json.append("}");


        return json.toString();
    }


    // =========================
    // RECOMMENDATION JSON
    // =========================

    private static String recommendationToJson(
            Recommendation recommendation) {

        Movie movie =
            recommendation.getMovie();


        StringBuilder json =
            new StringBuilder();


        json.append("{");


        json.append(
            "\"id\":"
            + movie.getId()
            + ","
        );


        json.append(
            "\"title\":\""
            + escapeJson(
                movie.getTitle()
            )
            + "\","
        );


        json.append(
            "\"genre\":\""
            + escapeJson(
                movie.getGenre()
            )
            + "\","
        );


        json.append(
            "\"language\":\""
            + escapeJson(
                movie.getLanguage()
            )
            + "\","
        );


        json.append(
            "\"rating\":"
            + movie.getRating()
            + ","
        );


        json.append(
            "\"score\":"
            + recommendation.getScore()
            + ","
        );


        json.append(
            "\"reason\":\""
            + escapeJson(
                recommendation.getReason()
            )
            + "\","
        );


        json.append(
            "\"posterUrl\":\""
            + escapeJson(
                movie.getPosterUrl()
            )
            + "\","
        );


        json.append(
            "\"backdropUrl\":\""
            + escapeJson(
                movie.getBackdropUrl()
            )
            + "\","
        );


        json.append(
            "\"overview\":\""
            + escapeJson(
                movie.getOverview()
            )
            + "\","
        );


        json.append(
            "\"releaseDate\":\""
            + escapeJson(
                movie.getReleaseDate()
            )
            + "\","
        );


        json.append(
            "\"cast\":"
            + castToJson(
                movie.getCast()
            )
        );


        json.append("}");


        return json.toString();
    }


    // =========================
    // CAST JSON
    // =========================

    private static String castToJson(
            List<String> cast) {

        StringBuilder json =
            new StringBuilder();


        json.append("[");


        if (cast != null) {

            for (int i = 0;
                 i < cast.size();
                 i++) {

                json.append("\"");


                json.append(
                    escapeJson(
                        cast.get(i)
                    )
                );


                json.append("\"");


                if (i < cast.size() - 1) {
                    json.append(",");
                }
            }
        }


        json.append("]");


        return json.toString();
    }


    // =========================
    // HEADERS
    // =========================

    private static void setJsonHeaders(
            HttpExchange exchange) {

        exchange.getResponseHeaders()
                .add(
                    "Access-Control-Allow-Origin",
                    "*"
                );


        exchange.getResponseHeaders()
                .add(
                    "Content-Type",
                    "application/json"
                );
    }


    // =========================
    // SEND RESPONSE
    // =========================

    private static void sendResponse(
            HttpExchange exchange,
            String response)
            throws IOException {

        byte[] data =
            response.getBytes("UTF-8");


        exchange.sendResponseHeaders(
            200,
            data.length
        );


        OutputStream output =
            exchange.getResponseBody();


        output.write(data);

        output.close();
    }


    // =========================
    // ESCAPE JSON
    // =========================

    private static String escapeJson(
            String value) {

        if (value == null) {
            return "";
        }


        return value
            .replace(
                "\\",
                "\\\\"
            )
            .replace(
                "\"",
                "\\\""
            )
            .replace(
                "\n",
                " "
            )
            .replace(
                "\r",
                " "
            );
    }


    // =========================
    // MOVIE CATALOG
    // =========================

    private static MovieCatalog createMovieCatalog() {

        MovieCatalog catalog =
            new MovieCatalog();


        catalog.addMovie(
            new Movie(
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
            )
        );


        catalog.addMovie(
            new Movie(
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
            )
        );


        catalog.addMovie(
            new Movie(
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
            )
        );


        catalog.addMovie(
            new Movie(
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
            )
        );


        catalog.addMovie(
            new Movie(
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
            )
        );


        catalog.addMovie(
            new Movie(
                106,
                "Baahubali: The Beginning",
                "Telugu",
                "Action",
                8.0,
                14000,
                "poster-url-6",
                "backdrop-url-6",
                "An epic action adventure.",
                "2015-07-10",
                Arrays.asList(
                    "Prabhas",
                    "Rana Daggubati"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                107,
                "Baahubali 2",
                "Telugu",
                "Action",
                8.2,
                16000,
                "poster-url-7",
                "backdrop-url-7",
                "The epic conclusion to the Baahubali story.",
                "2017-04-28",
                Arrays.asList(
                    "Prabhas",
                    "Rana Daggubati"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                108,
                "KGF",
                "Kannada",
                "Action",
                8.2,
                13500,
                "poster-url-8",
                "backdrop-url-8",
                "An action drama about ambition and power.",
                "2018-12-21",
                Arrays.asList(
                    "Yash",
                    "Srinidhi Shetty"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                109,
                "KGF Chapter 2",
                "Kannada",
                "Action",
                8.3,
                15500,
                "poster-url-9",
                "backdrop-url-9",
                "The continuation of Rocky's journey.",
                "2022-04-14",
                Arrays.asList(
                    "Yash",
                    "Sanjay Dutt"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                110,
                "Kantara",
                "Kannada",
                "Thriller",
                8.0,
                12500,
                "poster-url-10",
                "backdrop-url-10",
                "A mysterious story rooted in tradition and conflict.",
                "2022-09-30",
                Arrays.asList(
                    "Rishab Shetty",
                    "Sapthami Gowda"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                111,
                "Salaar",
                "Telugu",
                "Action",
                7.0,
                14500,
                "poster-url-11",
                "backdrop-url-11",
                "An action drama centered on friendship and power.",
                "2023-12-22",
                Arrays.asList(
                    "Prabhas",
                    "Prithviraj Sukumaran"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                112,
                "Devara",
                "Telugu",
                "Action",
                6.2,
                11500,
                "poster-url-12",
                "backdrop-url-12",
                "An action drama set against a coastal backdrop.",
                "2024-09-27",
                Arrays.asList(
                    "N. T. Rama Rao Jr.",
                    "Saif Ali Khan"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                113,
                "Hi Nanna",
                "Telugu",
                "Romance",
                8.0,
                9000,
                "poster-url-13",
                "backdrop-url-13",
                "A heartfelt romantic family drama.",
                "2023-12-07",
                Arrays.asList(
                    "Nani",
                    "Mrunal Thakur"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                114,
                "Sita Ramam",
                "Telugu",
                "Romance",
                8.5,
                11000,
                "poster-url-14",
                "backdrop-url-14",
                "A romantic drama told through letters.",
                "2022-08-05",
                Arrays.asList(
                    "Dulquer Salmaan",
                    "Mrunal Thakur"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                115,
                "Arjun Reddy",
                "Telugu",
                "Drama",
                7.1,
                10500,
                "poster-url-15",
                "backdrop-url-15",
                "A dramatic story of love and personal conflict.",
                "2017-08-25",
                Arrays.asList(
                    "Vijay Deverakonda",
                    "Shalini Pandey"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                116,
                "Jersey",
                "Telugu",
                "Drama",
                8.0,
                8500,
                "poster-url-16",
                "backdrop-url-16",
                "A former cricketer attempts to make a comeback.",
                "2019-04-19",
                Arrays.asList(
                    "Nani",
                    "Shraddha Srinath"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                117,
                "Maharaja",
                "Tamil",
                "Thriller",
                8.0,
                13000,
                "poster-url-17",
                "backdrop-url-17",
                "A barber searches for what was taken from him.",
                "2024-06-14",
                Arrays.asList(
                    "Vijay Sethupathi",
                    "Anurag Kashyap"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                118,
                "3 Idiots",
                "Hindi",
                "Comedy",
                8.4,
                15000,
                "poster-url-18",
                "backdrop-url-18",
                "Three friends navigate college, friendship and life.",
                "2009-12-25",
                Arrays.asList(
                    "Aamir Khan",
                    "R. Madhavan"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                119,
                "Inception",
                "English",
                "Sci-Fi",
                8.8,
                17000,
                "poster-url-19",
                "backdrop-url-19",
                "A skilled team enters dreams to perform an impossible mission.",
                "2010-07-16",
                Arrays.asList(
                    "Leonardo DiCaprio",
                    "Joseph Gordon-Levitt"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                120,
                "The Dark Knight",
                "English",
                "Action",
                9.0,
                17500,
                "poster-url-20",
                "backdrop-url-20",
                "Batman faces a criminal mastermind who plunges Gotham into chaos.",
                "2008-07-18",
                Arrays.asList(
                    "Christian Bale",
                    "Heath Ledger"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                121,
                "Animal",
                "Hindi",
                "Drama",
                6.1,
                16500,
                "poster-url-21",
                "backdrop-url-21",
                "A complex family drama centered on an intense relationship between a father and son.",
                "2023-12-01",
                Arrays.asList(
                    "Ranbir Kapoor",
                    "Anil Kapoor"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                122,
                "Dhurandhar",
                "Hindi",
                "Action",
                8.0,
                14500,
                "poster-url-22",
                "backdrop-url-22",
                "An action thriller.",
                "2025-12-05",
                Arrays.asList(
                    "Ranveer Singh",
                    "Akshaye Khanna"
                )
            )
        );


        catalog.addMovie(
            new Movie(
                123,
                "Dhurandhar: The Revenge",
                "Hindi",
                "Action",
                8.0,
                12500,
                "poster-url-23",
                "backdrop-url-23",
                "The continuation of the Dhurandhar story.",
                "2026-03-19",
                Arrays.asList(
                    "Ranveer Singh",
                    "Akshaye Khanna"
                )
            )
        );


        return catalog;
    }


    // =========================
    // BUILD MOVIE GRAPH
    // =========================

    private static MovieGraph buildMovieGraph(
            MovieCatalog catalog) {

        MovieGraph graph =
            new MovieGraph();


        MovieSimilarity similarity =
            new MovieSimilarity();


        List<Movie> movies =
            catalog.getAllMovies();


        for (Movie movie : movies) {

            graph.addMovie(
                movie.getId()
            );
        }


        for (int i = 0;
             i < movies.size();
             i++) {

            for (int j = i + 1;
                 j < movies.size();
                 j++) {

                Movie movie1 =
                    movies.get(i);


                Movie movie2 =
                    movies.get(j);


                if (similarity.areSimilar(
                        movie1,
                        movie2)) {

                    graph.addConnection(
                        movie1.getId(),
                        movie2.getId()
                    );
                }
            }
        }


        return graph;
    }
}