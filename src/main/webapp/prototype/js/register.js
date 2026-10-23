(function () {
  const form = document.getElementById("registerForm");
  const message = document.getElementById("registerMessage");

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    setMessage(message, "");

    const password = document.getElementById("password").value;
    if (password !== document.getElementById("confirmPassword").value) {
      setMessage(message, "两次输入的密码不一致");
      return;
    }

    const button = form.querySelector("button[type=submit]");
    button.disabled = true;
    button.textContent = "注册中...";

    try {
      const user = await Auth.register({
        username: document.getElementById("username").value.trim(),
        password: password,
        name: document.getElementById("name").value.trim(),
        studentNo: document.getElementById("studentNo").value.trim(),
        grade: document.getElementById("grade").value.trim()
      });
      location.href = user.role === "ADMIN" ? "admin.html" : "home.html";
    } catch (error) {
      setMessage(message, error.message || "注册失败");
    } finally {
      button.disabled = false;
      button.textContent = "注册";
    }
  });
})();
