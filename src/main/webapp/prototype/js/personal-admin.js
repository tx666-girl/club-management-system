(async function () {
  const admin = await requireAdmin();
  if (!admin) {
    return;
  }
  document.getElementById("profileName").textContent = admin.name || admin.username;
  document.getElementById("profileMeta").textContent =
    `ID:${admin.studentNo || "-"} · ${admin.grade || "-"} · 管理员`;

  const editPanel = document.getElementById("profileEditPanel");
  const passwordPanel = document.getElementById("passwordPanel");
  document.getElementById("editProfileButton").addEventListener("click", () => {
    document.getElementById("profileNameInput").value = admin.name || "";
    document.getElementById("profileStudentNo").value = admin.studentNo || "";
    document.getElementById("profileGrade").value = admin.grade || "";
    editPanel.hidden = false;
  });
  document.getElementById("changePasswordButton").addEventListener("click", () => {
    passwordPanel.hidden = false;
  });
  document.getElementById("pageLogout").addEventListener("click", async () => {
    await Auth.logout();
    location.href = "login.html";
  });
  document.getElementById("profileForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      const updated = await apiRequest("/me/profile", {
        method: "PUT",
        body: {
          name: document.getElementById("profileNameInput").value.trim(),
          studentNo: document.getElementById("profileStudentNo").value.trim(),
          grade: document.getElementById("profileGrade").value.trim()
        }
      });
      Object.assign(admin, updated);
      document.getElementById("profileName").textContent = admin.name;
      document.getElementById("profileMeta").textContent =
        `ID:${admin.studentNo || "-"} · ${admin.grade || "-"} · 管理员`;
      editPanel.hidden = true;
    } catch (error) {
      alert(error.message || "保存失败");
    }
  });
  document.getElementById("passwordForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    try {
      await apiRequest("/auth/password", {
        method: "PUT",
        body: {
          oldPassword: document.getElementById("oldPassword").value,
          newPassword: document.getElementById("newPassword").value
        }
      });
      event.target.reset();
      passwordPanel.hidden = true;
      alert("密码修改成功");
    } catch (error) {
      alert(error.message || "修改密码失败");
    }
  });

  const [stats, applications, clubs] = await Promise.all([
    apiRequest("/admin/stats"),
    apiRequest("/admin/applications"),
    apiRequest("/admin/clubs")
  ]);
  document.getElementById("adminClubCount").textContent = stats.clubs;
  document.getElementById("adminActivityCount").textContent = stats.activities;
  document.getElementById("adminPendingCount").textContent = stats.pendingApplications;
  document.getElementById("adminMemberCount").textContent = stats.members;

  const clubList = document.getElementById("managedClubs");
  clubList.innerHTML = clubs.length ? clubs.map((club) => `
    <article class="managed-item">
      <h3>${escapeHtml(club.name)}</h3>
      <p>${escapeHtml(categoryText(club.category))} · ${club.memberCount} 名成员</p>
      <div class="card-actions" style="margin-top: 10px;">
        <a class="button button-primary" href="club-admin.html?id=${club.id}">管理社团</a>
      </div>
    </article>`).join("") : '<div class="empty-text">暂无社团</div>';

  const pendingList = document.getElementById("adminPendingList");
  pendingList.innerHTML = applications.length ? applications.slice(0, 3).map((item) => `
    <article class="notice-item">
      <h3>${escapeHtml(item.applicantName)} · ${escapeHtml(item.clubName)}</h3>
      <p>入团申请，等待管理员处理。</p>
      <div class="notice-meta">
        <span>${formatTime(item.createdAt)}</span>
        <span class="status pending">待审批</span>
      </div>
    </article>`).join("") : '<div class="empty-text">暂无待审批申请</div>';

  function categoryText(value) {
    return { tech: "学术科技", art: "文化艺术", sport: "体育运动", volunteer: "志愿公益" }[value] || "社团";
  }
})();
