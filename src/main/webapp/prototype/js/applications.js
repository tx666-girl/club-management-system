(async function () {
  const admin = await requireAdmin();
  if (!admin) {
    return;
  }

  const message = document.getElementById("applicationsMessage");
  const table = document.getElementById("applicationTableBody");

  await load();

  async function load() {
    try {
      const [applications, stats] = await Promise.all([
        apiRequest("/admin/applications"),
        apiRequest("/admin/stats")
      ]);
      renderApplications(applications);
      document.getElementById("statPending").textContent = stats.pendingApplications;
      document.getElementById("statClubs").textContent = stats.clubs;
      document.getElementById("statActivities").textContent = stats.activities;
      document.getElementById("statMembers").textContent = stats.members;
    } catch (error) {
      setMessage(message, error.message || "加载申请失败");
    }
  }

  function renderApplications(applications) {
    if (!applications.length) {
      table.innerHTML = '<tr><td colspan="6" class="empty-text">暂无待审批申请</td></tr>';
      return;
    }
    table.innerHTML = applications.map((application) => `
      <tr>
        <td>${escapeHtml(application.applicantName)}</td>
        <td>入团申请</td>
        <td>${escapeHtml(application.clubName)}</td>
        <td>${formatTime(application.createdAt)}</td>
        <td><span class="status pending">${statusText(application.status)}</span></td>
        <td>
          <div class="actions">
            <button class="button button-primary" type="button"
                    data-action="approve" data-id="${application.id}">通过</button>
            <button class="button button-danger" type="button"
                    data-action="reject" data-id="${application.id}">驳回</button>
          </div>
        </td>
      </tr>`).join("");
  }

  table.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-action]");
    if (!button) {
      return;
    }
    button.disabled = true;
    try {
      await apiRequest(
        `/admin/applications/${button.dataset.id}/${button.dataset.action}`,
        { method: "POST" }
      );
      setMessage(message, button.dataset.action === "approve" ? "已通过申请" : "已驳回申请", true);
      await load();
    } catch (error) {
      button.disabled = false;
      setMessage(message, error.message || "处理失败");
    }
  });
})();
