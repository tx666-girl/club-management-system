const API_BASE = "../api";

async function apiRequest(path, options = {}) {
  const config = {
    method: options.method || "GET",
    credentials: "same-origin",
    headers: { ...(options.headers || {}) }
  };

  if (options.body !== undefined) {
    config.headers["Content-Type"] = "application/json";
    config.body = JSON.stringify(options.body);
  }

  const response = await fetch(API_BASE + path, config);
  const payload = await response.json().catch(() => null);

  if (!response.ok) {
    const error = new Error(payload?.message || `请求失败（${response.status}）`);
    error.status = response.status;
    throw error;
  }
  return payload?.data;
}

const Auth = {
  async me() {
    try {
      return await apiRequest("/auth/me");
    } catch (error) {
      if (error.status === 401) {
        return null;
      }
      throw error;
    }
  },

  login(username, password) {
    return apiRequest("/auth/login", {
      method: "POST",
      body: { username, password }
    });
  },

  register(data) {
    return apiRequest("/auth/register", {
      method: "POST",
      body: data
    });
  },

  logout() {
    return apiRequest("/auth/logout", { method: "POST" });
  }
};

function escapeHtml(value) {
  const replacements = {
    "&": "&amp;",
    "<": "&lt;",
    ">": "&gt;",
    '"': "&quot;",
    "'": "&#39;"
  };
  return String(value ?? "").replace(/[&<>"']/g, (character) => replacements[character]);
}

function formatTime(value) {
  if (!value) {
    return "";
  }
  const date = new Date(String(value).replace(" ", "T"));
  return Number.isNaN(date.getTime())
    ? String(value)
    : date.toLocaleString("zh-CN", {
        year: "numeric",
        month: "2-digit",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit"
      });
}

function statusText(status) {
  return {
    PENDING: "待审批",
    APPROVED: "已通过",
    REJECTED: "已拒绝"
  }[status] || status;
}

function statusClass(status) {
  return {
    PENDING: "pending",
    APPROVED: "approved",
    REJECTED: "rejected"
  }[status] || "";
}

function roleText(role) {
  return role === "ADMIN" ? "管理员" : "普通成员";
}

function setMessage(element, text, success = false) {
  if (!element) {
    return;
  }
  element.textContent = text || "";
  element.classList.toggle("success", success);
}
