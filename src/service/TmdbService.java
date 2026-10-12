package service;

import model.Movie;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TmdbService {

    private String apiKey;

    public TmdbService() {

        apiKey = System.getenv("TMDB_API_KEY");

        if (apiKey == null ||
            apiKey.trim().isEmpty()) {

            throw new RuntimeException(
                "TMDB_API_KEY is not set."
            );
        }

        System.setProperty(
            "https.protocols",
            "TLSv1.2"
        );
    }

    public void enrichMovie(Movie movie) {

        int maxAttempts = 3;

        for (int attempt = 1;
             attempt <= maxAttempts;
             attempt++) {

            try {

                String encodedTitle =
                    URLEncoder.encode(
                        movie.getTitle(),
                        "UTF-8"
                    );

                String urlString =
                    "https://api.themoviedb.org/3/search/movie"
                    + "?api_key="
                    + apiKey
                    + "&query="
                    + encodedTitle
                    + "&include_adult=false"
                    + "&language=en-US";

                URL url =
                    new URL(urlString);

                HttpURLConnection connection =
                    (HttpURLConnection)
                    url.openConnection();

                connection.setRequestMethod("GET");

                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                connection.setRequestProperty(
                    "Accept",
                    "application/json"
                );

                connection.setRequestProperty(
                    "User-Agent",
                    "CineRank/1.0"
                );

                int responseCode =
                    connection.getResponseCode();

                if (responseCode != 200) {

                    System.out.println(
                        "TMDB request failed for "
                        + movie.getTitle()
                        + " : "
                        + responseCode
                    );

                    connection.disconnect();

                    return;
                }

                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(
                            connection.getInputStream(),
                            "UTF-8"
                        )
                    );

                StringBuilder response =
                    new StringBuilder();

                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                String json =
                    response.toString();

                String posterPath =
                    extractFirstString(
                        json,
                        "\"poster_path\":\"([^\"]*)\""
                    );

                String backdropPath =
                    extractFirstString(
                        json,
                        "\"backdrop_path\":\"([^\"]*)\""
                    );

                String overview =
                    extractFirstString(
                        json,
                        "\"overview\":\"([^\"]*)\""
                    );

                String releaseDate =
                    extractFirstString(
                        json,
                        "\"release_date\":\"([^\"]*)\""
                    );

                String rating =
                    extractFirstString(
                        json,
                        "\"vote_average\":([0-9.]+)"
                    );

                if (posterPath != null &&
                    !posterPath.equals("null") &&
                    !posterPath.isEmpty()) {

                    movie.setPosterUrl(
                        "https://image.tmdb.org/t/p/w500"
                        + posterPath
                    );
                }

                if (backdropPath != null &&
                    !backdropPath.equals("null") &&
                    !backdropPath.isEmpty()) {

                    movie.setBackdropUrl(
                        "https://image.tmdb.org/t/p/w1280"
                        + backdropPath
                    );
                }

                if (overview != null &&
                    !overview.equals("null") &&
                    !overview.isEmpty()) {

                    movie.setOverview(
                        cleanJsonText(overview)
                    );
                }

                if (releaseDate != null &&
                    !releaseDate.equals("null") &&
                    !releaseDate.isEmpty()) {

                    movie.setReleaseDate(
                        releaseDate
                    );
                }

                if (rating != null &&
                    !rating.equals("null") &&
                    !rating.isEmpty()) {

                    try {

                        movie.setRating(
                            Double.parseDouble(rating)
                        );

                    } catch (NumberFormatException ignored) {
                    }
                }

                System.out.println(
                    "TMDB data loaded: "
                    + movie.getTitle()
                );

                return;

            } catch (IOException e) {

                if (attempt < maxAttempts) {

                    System.out.println(
                        "TMDB connection retry for "
                        + movie.getTitle()
                        + " (attempt "
                        + attempt
                        + ")"
                    );

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }

                } else {

                    System.out.println(
                        "TMDB error for "
                        + movie.getTitle()
                        + ": "
                        + e.getMessage()
                    );
                }

            } catch (Exception e) {

                System.out.println(
                    "TMDB error for "
                    + movie.getTitle()
                    + ": "
                    + e.getMessage()
                );

                return;
            }
        }
    }

    private String extractFirstString(
            String json,
            String regex) {

        Pattern pattern =
            Pattern.compile(regex);

        Matcher matcher =
            pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String cleanJsonText(
            String value) {

        return value
            .replace("\\\"", "\"")
            .replace("\\n", " ")
            .replace("\\r", " ")
            .replace("\\\\", "\\");
    }
}