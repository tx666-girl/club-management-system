function updateHeader(user) {
  const menu = document.querySelector(".user-menu");
  if (!menu || !user) {
    return;
  }
  const initial = (user.name || user.username || "?").slice(0, 1);
  menu.innerHTML =
    `<span class="avatar">${escapeHtml(initial)}</span>` +
    `<span>${escapeHtml(user.name || user.username)}</span>` +
    `<span class="role-pill">${escapeHtml(roleText(user.role))}</span>` +
    `<button class="button button-ghost" type="button" id="headerLogout">退出</button>`;
  document.getElementById("headerLogout").addEventListener("click", async () => {
    try {
      await Auth.logout();
    } finally {
      location.href = "login.html";
    }
  });
}

async function requireUser() {
  const user = await Auth.me();
  if (!user) {
    location.href = "login.html";
    return null;
  }
  updateHeader(user);
  return user;
}

async function requireAdmin() {
  const user = await requireUser();
  if (!user) {
    return null;
  }
  if (user.role !== "ADMIN") {
    location.href = "personal-member.html";
    return null;
  }
  return user;
}

function queryId(defaultValue = null) {
  const value = new URLSearchParams(location.search).get("id");
  if (!value) {
    return defaultValue;
  }
  const id = Number(value);
  return Number.isInteger(id) && id > 0 ? id : defaultValue;
}
