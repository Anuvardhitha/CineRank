package model;

import java.util.List;

public class Movie {

    private int id;
    private String title;
    private String language;
    private String genre;
    private double rating;
    private int views;
    private String posterUrl;
    private String backdropUrl;
    private String overview;
    private String releaseDate;
    private List<String> cast;

    public Movie(int id,
                 String title,
                 String language,
                 String genre,
                 double rating,
                 int views,
                 String posterUrl,
                 String backdropUrl,
                 String overview,
                 String releaseDate,
                 List<String> cast) {

        this.id = id;
        this.title = title;
        this.language = language;
        this.genre = genre;
        this.rating = rating;
        this.views = views;
        this.posterUrl = posterUrl;
        this.backdropUrl = backdropUrl;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.cast = cast;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLanguage() {
        return language;
    }

    public String getGenre() {
        return genre;
    }

    public double getRating() {
        return rating;
    }

    public int getViews() {
        return views;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getBackdropUrl() {
        return backdropUrl;
    }

    public String getOverview() {
        return overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public List<String> getCast() {
        return cast;
    }

    public void increaseViews() {
        views++;
    }public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
}

public void setBackdropUrl(String backdropUrl) {
    this.backdropUrl = backdropUrl;
}

public void setOverview(String overview) {
    this.overview = overview;
}

public void setReleaseDate(String releaseDate) {
    this.releaseDate = releaseDate;
}

public void setRating(double rating) {
    this.rating = rating;
}
}