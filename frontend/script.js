function getCurrentUserId() {

    const userId =
        localStorage.getItem("userId");

    if (!userId) {
        console.error("No logged-in user found.");
        return null;
    }

    return userId;
}


function handleSearch(event) {

    if (event.key === "Enter") {
        searchMovies();
    }

}


function searchMovies() {

    const input =
        document.getElementById("searchInput");

    const prefix =
        input.value.trim();

    if (prefix === "") {
        return;
    }

    fetch(
        "http://localhost:8080/api/movies/search?prefix="
        + encodeURIComponent(prefix)
    )

    .then(response => response.json())

    .then(movies => {

        displayMovies(movies);

    })

    .catch(error => {

        console.error(
            "Search error:",
            error
        );

        alert(
            "Could not connect to CineRank server."
        );

    });

}


function displayMovies(movies) {

    const container =
        document.getElementById(
            "movieContainer"
        );

    const title =
        document.getElementById(
            "sectionTitle"
        );

    container.innerHTML = "";

    title.innerText =
        "Search Results";


    if (movies.length === 0) {

        container.innerHTML =
            "<p>No movies found.</p>";

        return;
    }


    movies.forEach(movie => {

        container.appendChild(
            createMovieCard(movie)
        );

    });

}


function createMovieCard(movie) {

    const card =
        document.createElement("div");

    card.className =
        "movie-card";


    card.onclick = function () {

        showMovieDetails(movie);

    };


    card.innerHTML = `

        <img
            class="movie-poster"
            src="${movie.posterUrl}"
            alt="${movie.title}"
        >

        <h3>
            ${movie.title}
        </h3>

        <p>
            ${movie.genre}
            •
            ${movie.language}
        </p>

        <p class="rating">
            ⭐ ${movie.rating}
        </p>

        <div class="movie-actions">

            <button
                onclick="event.stopPropagation(); watchMovie(${movie.id})">
                ▶ Watch
            </button>

            <button
                onclick="event.stopPropagation(); likeMovie(${movie.id})">
                👍 Like
            </button>

            <button
                onclick="event.stopPropagation(); dislikeMovie(${movie.id})">
                👎 Dislike
            </button>

        </div>

    `;


    return card;
}


function loadTrending() {

    fetch(
        "http://localhost:8080/api/movies/trending"
    )

    .then(response => response.json())

    .then(movies => {

        displayTrending(movies);

    })

    .catch(error => {

        console.error(
            "Trending error:",
            error
        );

    });

}


function displayTrending(movies) {

    const container =
        document.getElementById(
            "trendingContainer"
        );

    container.innerHTML = "";


    if (movies.length === 0) {

        container.innerHTML =
            "<p>No trending movies available.</p>";

        return;
    }


    movies.forEach(movie => {

        container.appendChild(
            createTrendingCard(movie)
        );

    });

}


function createTrendingCard(movie) {

    const card =
        document.createElement("div");

    card.className =
        "movie-card";


    card.onclick = function () {

        showMovieDetails(movie);

    };


    card.innerHTML = `

        <img
            class="movie-poster"
            src="${movie.posterUrl}"
            alt="${movie.title}"
        >

        <h3>
            ${movie.title}
        </h3>

        <p>
            ${movie.genre}
            •
            ${movie.language}
        </p>

        <p class="rating">
            ⭐ ${movie.rating}
        </p>

        <p>
            👁 ${movie.views} views
        </p>

        <div class="movie-actions">

            <button
                onclick="event.stopPropagation(); watchMovie(${movie.id})">
                ▶ Watch
            </button>

            <button
                onclick="event.stopPropagation(); likeMovie(${movie.id})">
                👍 Like
            </button>

            <button
                onclick="event.stopPropagation(); dislikeMovie(${movie.id})">
                👎 Dislike
            </button>

        </div>

    `;


    return card;
}


function loadRecommendations() {

    fetch(
        "http://localhost:8080/api/movies/recommendations?userId="
        + encodeURIComponent(getCurrentUserId())
    )

    .then(response => response.json())

    .then(movies => {

        displayRecommendations(
            movies
        );

    })

    .catch(error => {

        console.error(
            "Recommendation error:",
            error
        );

    });

}


function displayRecommendations(movies) {

    const container =
        document.getElementById(
            "recommendationContainer"
        );

    container.innerHTML = "";


    if (movies.length === 0) {

        container.innerHTML =
            "<p>No recommendations available.</p>";

        return;
    }


    movies.forEach(movie => {

        container.appendChild(
            createRecommendationCard(movie)
        );

    });

}


function createRecommendationCard(movie) {

    const card =
        document.createElement("div");

    card.className =
        "movie-card";


    card.onclick = function () {

        showMovieDetails(movie);

    };


    card.innerHTML = `

        <img
            class="movie-poster"
            src="${movie.posterUrl}"
            alt="${movie.title}"
        >

        <h3>
            ${movie.title}
        </h3>

        <p>
            ${movie.genre}
            •
            ${movie.language}
        </p>

        <p class="rating">
            ⭐ ${movie.rating}
        </p>

        <p>
            ${movie.reason}
        </p>

        <div class="movie-actions">

            <button
                onclick="event.stopPropagation(); watchMovie(${movie.id})">
                ▶ Watch
            </button>

            <button
                onclick="event.stopPropagation(); likeMovie(${movie.id})">
                👍 Like
            </button>

            <button
                onclick="event.stopPropagation(); dislikeMovie(${movie.id})">
                👎 Dislike
            </button>

        </div>

    `;


    return card;
}


function showMovieDetails(movie) {

    document.getElementById(
        "modalPoster"
    ).src =
        movie.posterUrl;


    document.getElementById(
        "modalTitle"
    ).innerText =
        movie.title;


    document.getElementById(
        "modalMeta"
    ).innerText =
        "⭐ "
        + movie.rating
        + "   •   "
        + movie.genre
        + "   •   "
        + movie.language
        + "   •   "
        + movie.releaseDate;


    document.getElementById(
        "modalOverview"
    ).innerText =
        movie.overview ||
        "No overview available.";


    const cast =
        movie.cast || [];


    document.getElementById(
        "modalCast"
    ).innerHTML =
        "<strong>Cast:</strong> "
        + (
            cast.length > 0
            ? cast.join(", ")
            : "Cast information unavailable."
        );


    document.getElementById(
        "modalActions"
    ).innerHTML = `

        <button
            onclick="watchMovie(${movie.id})">
            ▶ Watch
        </button>

        <button
            onclick="likeMovie(${movie.id})">
            👍 Like
        </button>

        <button
            onclick="dislikeMovie(${movie.id})">
            👎 Dislike
        </button>

    `;


    document.getElementById(
        "movieModal"
    ).style.display =
        "flex";
}


function closeMovieModal() {

    document.getElementById(
        "movieModal"
    ).style.display =
        "none";
}


function closeModal(event) {

    if (
        event.target.id ===
        "movieModal"
    ) {

        closeMovieModal();

    }

}


function watchMovie(movieId) {

    fetch(
        "http://localhost:8080/api/movies/watch?id="
        + movieId
        + "&userId="
        + encodeURIComponent(getCurrentUserId())
    )

    .then(response => response.json())

    .then(data => {

        alert(
            "Movie added to your watch history!"
        );

        closeMovieModal();

        loadRecommendations();

        loadTrending();

    })

    .catch(error => {

        console.error(
            "Watch error:",
            error
        );

    });

}


function likeMovie(movieId) {

    fetch(
        "http://localhost:8080/api/movies/like?id="
        + movieId
        + "&userId="
        + encodeURIComponent(getCurrentUserId())
    )

    .then(response => response.json())

    .then(data => {

        alert(
            "👍 Movie liked! Recommendations updated."
        );

        closeMovieModal();

        loadRecommendations();

    })

    .catch(error => {

        console.error(
            "Like error:",
            error
        );

    });

}


function dislikeMovie(movieId) {

    fetch(
        "http://localhost:8080/api/movies/dislike?id="
        + movieId
        + "&userId="
        + encodeURIComponent(getCurrentUserId())
    )

    .then(response => response.json())

    .then(data => {

        alert(
            "👎 Preference recorded."
        );

        closeMovieModal();

        loadRecommendations();

    })

    .catch(error => {

        console.error(
            "Dislike error:",
            error
        );

    });

}


loadTrending();

loadRecommendations();

// =========================
// TOP 10 MOVIES BY GENRE
// =========================

function loadGenreTop10() {

    fetch(
        "http://localhost:8080/api/movies/genre-top10"
    )

    .then(response => response.json())

    .then(genreData => {
        displayGenreTop10(genreData);
    })

    .catch(error => {
        console.error(
            "Genre Top 10 error:",
            error
        );
    });
}


function displayGenreTop10(genreData) {

    const container =
        document.getElementById(
            "genreContainer"
        );

    container.innerHTML = "";

    const genres = Object.keys(genreData);

    genres.forEach(genre => {

        const movies = genreData[genre];

        if (movies.length === 0) {
            return;
        }

        const section =
            document.createElement("div");

        section.className =
            "genre-row-section";

        const title =
            document.createElement("h3");

        title.className =
            "genre-row-title";

        title.innerText =
            "🎬 Top 10 " + genre + " Movies";

        const row =
            document.createElement("div");

        row.className =
            "movie-row";

        movies.forEach(movie => {
            row.appendChild(
                createMovieCard(movie)
            );
        });

        section.appendChild(title);
        section.appendChild(row);
        container.appendChild(section);
    });
}


loadGenreTop10();
