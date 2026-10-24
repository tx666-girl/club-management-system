(function () {
  const form = document.getElementById("loginForm");
  const message = document.getElementById("loginMessage");

  Auth.me().then((user) => {
    if (user) {
      redirect(user);
    }
  }).catch(() => {});

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(message, "");
    const button = form.querySelector("button[type=submit]");
    button.disabled = true;
    button.textContent = "登录中...";

    try {
      const user = await Auth.login(
        document.getElementById("username").value.trim(),
        document.getElementById("password").value
      );
      redirect(user);
    } catch (error) {
      setMessage(message, error.message || "登录失败");
    } finally {
      button.disabled = false;
      button.textContent = "登录";
    }
  });

  function redirect(user) {
    location.href = user.role === "ADMIN" ? "admin.html" : "home.html";
  }
})();
