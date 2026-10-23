(async function () {
  const admin = await requireAdmin();
  if (!admin) {
    return;
  }

  const message = document.getElementById("adminMessage");
  const table = document.getElementById("adminClubTableBody");

  try {
    const [stats, clubs] = await Promise.all([
      apiRequest("/admin/stats"),
      apiRequest("/admin/clubs")
    ]);

    document.getElementById("adminClubCount").textContent = stats.clubs;
    document.getElementById("adminPendingCount").textContent = stats.pendingApplications;
    document.getElementById("adminActivityCount").textContent = stats.activities;
    document.getElementById("adminMemberCount").textContent = stats.members;
    renderClubs(clubs);
  } catch (error) {
    table.innerHTML = '<tr><td colspan="4" class="empty-text">社团数据加载失败</td></tr>';
    setMessage(message, error.message || "加载管理中心失败");
  }

  function renderClubs(clubs) {
    if (!clubs.length) {
      table.innerHTML = '<tr><td colspan="4" class="empty-text">暂无社团</td></tr>';
      return;
    }

    table.innerHTML = clubs.map((club) => `
      <tr>
        <td>${escapeHtml(club.name)}</td>
        <td>${escapeHtml(categoryText(club.category))}</td>
        <td>${club.memberCount} 名</td>
        <td>
          <a class="button button-primary" href="club-admin.html?id=${club.id}">管理社团</a>
        </td>
      </tr>`).join("");
  }

  function categoryText(value) {
    return {
      tech: "学术科技",
      art: "文化艺术",
      sport: "体育运动",
      volunteer: "志愿公益"
    }[value] || "社团";
  }
})();
