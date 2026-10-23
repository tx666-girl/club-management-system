(async function () {
  const user = await requireUser();
  if (!user) {
    return;
  }

  const message = document.getElementById("myApplicationsMessage");
  const table = document.getElementById("myApplicationTable");
  document.getElementById("profileName").textContent = user.name || user.username;
  document.getElementById("profileMeta").textContent =
    `ID:${user.studentNo || "-"} · ${user.grade || "-"} · ${roleText(user.role)}`;

  try {
    const [applications, clubs] = await Promise.all([
      apiRequest("/applications/my"),
      apiRequest("/me/clubs")
    ]);
    const pending = applications.filter((item) => item.status === "PENDING").length;
    document.getElementById("joinedCount").textContent = `已加入 ${clubs.length} 个社团`;
    document.getElementById("pendingCount").textContent = `${pending} 条申请中`;
    render(applications);
  } catch (error) {
    setMessage(message, error.message || "加载申请失败");
  }

  function render(applications) {
    if (!applications.length) {
      table.innerHTML = '<tr><td colspan="5" class="empty-text">还没有提交过入团申请</td></tr>';
      return;
    }
    table.innerHTML = applications.map((application) => `
      <tr>
        <td>${escapeHtml(application.clubName)}</td>
        <td>入团申请</td>
        <td>${formatTime(application.createdAt)}</td>
        <td><span class="status ${statusClass(application.status)}">${statusText(application.status)}</span></td>
        <td>${application.reviewedAt ? formatTime(application.reviewedAt) : "尚未处理"}</td>
      </tr>`).join("");
  }
})();
