const form = document.getElementById("loginForm");
const passwordInput = document.getElementById("password");
const togglePasswordButton = document.getElementById("togglePassword");
const loginButton = document.getElementById("loginButton");

togglePasswordButton.addEventListener("click", function () {
    const isPassword = passwordInput.type === "password";
    passwordInput.type = isPassword ? "text" : "password";
    togglePasswordButton.textContent = isPassword ? "Hide" : "Show";
});

form.addEventListener("submit", async function(e){
     e.preventDefault();
    const userId = document.getElementById("userId").value.trim();
    const password = passwordInput.value.trim();
    const errormessage = document.getElementById("error-message");
    errormessage.textContent = "";

    loginButton.disabled = true;
    loginButton.textContent = "Signing In...";

    try{
        const response = await fetch("/login",{
        method:"POST",
        headers:{
        "Content-type":"application/json"
        },
        body: JSON.stringify({
            userId: userId,
            password: password
        })
        });
        if(!response.ok)
        {
            throw new Error("Invalid User ID or password");
        }
        const data = await response.json();
        const token = data.token;
        localStorage.setItem("token", token);
        const payload = parseJwt(token);
        const role = payload.role;
        if(role == "TEACHER"){
        window.location.href = "../pages/teacherdashboard.html"
        }
        else if(role=== "ADMIN")
        {
            window.location.href="../pages/admindashboard.html"
        }
        else {
        window.location.href = "../pages/studentdashboard.html"
        }

    }
    catch(error)
    {
        errormessage.textContent = error.message;
    } finally {
        loginButton.disabled = false;
        loginButton.textContent = "Sign In";
    }

});
function parseJwt(token)
{
    const payload = token.split('.')[1];
    const decodedPayload = atob(payload);
    return JSON.parse(decodedPayload);
}