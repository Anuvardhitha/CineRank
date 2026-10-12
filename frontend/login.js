document
    .getElementById("loginForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();

        const email =
            document.getElementById("email").value;

        const password =
            document.getElementById("password").value;

        fetch(
            "http://localhost:8080/api/auth/login",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body:
                    "email="
                    + encodeURIComponent(email)
                    + "&password="
                    + encodeURIComponent(password)
            }
        )

        .then(response => response.json())

        .then(data => {

            if (data.success) {

                localStorage.setItem(
                    "userId",
                    data.userId
                );

                localStorage.setItem(
                    "userName",
                    data.name
                );

                localStorage.setItem(
                    "userEmail",
                    data.email
                );

                window.location.href =
                    "index.html";

            } else {

                document.getElementById(
                    "message"
                ).innerText =
                    "Invalid email or password.";

            }

        })

        .catch(error => {

            console.error(error);

            document.getElementById(
                "message"
            ).innerText =
                "Cannot connect to CineRank server.";

        });

    });


function showRegister() {

    window.location.href =
        "register.html";

}