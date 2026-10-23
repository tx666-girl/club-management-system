(async function () {
  const user = await requireUser();
  if (!user) {
    return;
  }
  document.getElementById("profileName").textContent = user.name || user.username;
  document.getElementById("profileMeta").textContent =
    `ID:${user.studentNo || "-"} · ${user.grade || "-"} · ${roleText(user.role)}`;

  const editPanel = document.getElementById("profileEditPanel");
  const passwordPanel = document.getElementById("passwordPanel");
  document.getElementById("editProfileButton").addEventListener("click", () => {
    document.getElementById("profileNameInput").value = user.name || "";
    document.getElementById("profileStudentNo").value = user.studentNo || "";
    document.getElementById("profileGrade").value = user.grade || "";
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
      Object.assign(user, updated);
      document.getElementById("profileName").textContent = user.name;
      document.getElementById("profileMeta").textContent =
        `ID:${user.studentNo || "-"} · ${user.grade || "-"} · ${roleText(user.role)}`;
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

  const [applications, clubs, announcements] = await Promise.all([
    apiRequest("/applications/my"),
    apiRequest("/me/clubs"),
    apiRequest("/me/announcements")
  ]);
  document.getElementById("memberClubCount").textContent = clubs.length;
  document.getElementById("memberPendingCount").textContent =
    applications.filter((item) => item.status === "PENDING").length;
  document.getElementById("memberApplicationCount").textContent = applications.length;
  document.getElementById("memberAnnouncementCount").textContent = announcements.length;

  const clubList = document.getElementById("myClubs");
  clubList.innerHTML = clubs.length ? clubs.map((club) => `
    <article class="managed-item">
      <h3>${escapeHtml(club.name)}</h3>
      <p>${escapeHtml(categoryText(club.category))} · ${club.memberCount} 名成员</p>
      <div class="card-actions" style="margin-top: 10px;">
        <a class="button button-secondary" href="club-member.html?id=${club.id}">查看社团</a>
      </div>
    </article>`).join("") : '<div class="empty-text">还没有加入社团</div>';

  const noticeList = document.getElementById("latestAnnouncements");
  noticeList.innerHTML = announcements.length ? announcements.map((item) => `
    <article class="notice-item">
      <h3>${escapeHtml(item.title)}</h3>
      <p>${escapeHtml(item.content)}</p>
      <div class="notice-meta">
        <span>${escapeHtml(item.clubName)}</span>
        <span>${formatTime(item.createdAt)}</span>
      </div>
    </article>`).join("") : '<div class="empty-text">暂无公告</div>';

  function categoryText(value) {
    return { tech: "学术科技", art: "文化艺术", sport: "体育运动", volunteer: "志愿公益" }[value] || "社团";
  }
})();
