document
    .getElementById("registerForm")
    .addEventListener("submit", function(event) {

        event.preventDefault();


        const name =
            document.getElementById("name").value.trim();


        const email =
            document.getElementById("email").value.trim();


        const password =
            document.getElementById("password").value;


        const confirmPassword =
            document.getElementById("confirmPassword").value;


        const message =
            document.getElementById("message");


        if (password !== confirmPassword) {

            message.innerText =
                "Passwords do not match.";

            return;
        }


        if (password.length < 4) {

            message.innerText =
                "Password must contain at least 4 characters.";

            return;
        }


        fetch(
            "http://localhost:8080/api/auth/register",
            {
                method: "POST",

                headers: {
                    "Content-Type":
                        "application/x-www-form-urlencoded"
                },

                body:
                    "name="
                    + encodeURIComponent(name)
                    + "&email="
                    + encodeURIComponent(email)
                    + "&password="
                    + encodeURIComponent(password)
            }
        )

        .then(response => response.json())

        .then(data => {

            if (data.success) {

                message.style.color =
                    "#4caf50";

                message.innerText =
                    "Account created successfully!";


                localStorage.setItem(
                    "userId",
                    data.userId
                );


                localStorage.setItem(
                    "userName",
                    data.name
                );


                setTimeout(function() {

                    window.location.href =
                        "index.html";

                }, 1000);

            } else {

                message.style.color =
                    "#ff6b6b";

                message.innerText =
                    data.message ||
                    "Registration failed.";

            }

        })

        .catch(error => {

            console.error(error);

            message.style.color =
                "#ff6b6b";

            message.innerText =
                "Cannot connect to CineRank server.";

        });

    });