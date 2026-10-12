package model;

public class Recommendation {

    private Movie movie;
    private double score;
    private String reason;

    public Recommendation(
            Movie movie,
            double score,
            String reason) {

        this.movie = movie;
        this.score = score;
        this.reason = reason;
    }

    public Movie getMovie() {
        return movie;
    }

    public double getScore() {
        return score;
    }

    public String getReason() {
        return reason;
    }
}