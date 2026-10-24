(async function () {
  const admin = await requireAdmin();
  if (!admin) {
    return;
  }
  const clubId = queryId(1);
  const message = document.getElementById("clubAdminMessage");

  await loadAll();

  async function loadAll() {
    try {
      const [club, announcements, activities, members] = await Promise.all([
        apiRequest(`/clubs/${clubId}`),
        apiRequest(`/clubs/${clubId}/announcements`),
        apiRequest(`/clubs/${clubId}/activities`),
        apiRequest(`/admin/clubs/${clubId}/members`)
      ]);
      renderClub(club);
      renderAnnouncements(announcements);
      renderActivities(activities);
      renderMembers(members, club.memberCount);
    } catch (error) {
      setMessage(message, error.message || "加载社团管理数据失败");
    }
  }

  function renderClub(club) {
    document.title = `${club.name} - 社团管理`;
    document.getElementById("clubCategory").textContent = categoryText(club.category);
    document.getElementById("clubName").textContent = club.name;
    document.getElementById("clubDescription").textContent = club.description;
    document.getElementById("clubMeta").innerHTML =
      `<span>${club.memberCount} 名成员</span>` +
      `<span>管理员管理全部社团</span>`;
  }

  function renderActivities(activities) {
    const list = document.getElementById("activityList");
    if (!activities.length) {
      list.innerHTML = '<div class="empty-text">暂无活动</div>';
      return;
    }
    list.innerHTML = activities.map((activity) => `
      <article class="activity-item">
        <h3>${escapeHtml(activity.title)}</h3>
        <p>${escapeHtml(activity.description)}</p>
        <div class="activity-meta">
          <span>${formatTime(activity.startTime)} · ${escapeHtml(activity.location)}</span>
          <button class="button button-danger" type="button"
                  data-delete-activity="${activity.id}">删除活动</button>
        </div>
      </article>`).join("");
  }

  function renderAnnouncements(announcements) {
    const list = document.getElementById("announcementList");
    if (!announcements.length) {
      list.innerHTML = '<div class="empty-text">暂无公告</div>';
      return;
    }
    list.innerHTML = announcements.map((item) => `
      <article class="notice-item">
        <h3>${escapeHtml(item.title)}</h3>
        <p>${escapeHtml(item.content)}</p>
        <div class="notice-meta">
          <span>${formatTime(item.createdAt)}</span>
          <button class="button button-danger" type="button"
                  data-delete-announcement="${item.id}">删除公告</button>
        </div>
      </article>`).join("");
  }

  function renderMembers(members, memberCount) {
    document.getElementById("memberCount").textContent = `${memberCount} 名成员`;
    const table = document.getElementById("memberTableBody");
    if (!members.length) {
      table.innerHTML = '<tr><td colspan="4" class="empty-text">暂无成员</td></tr>';
      return;
    }
    table.innerHTML = members.map((member) => `
      <tr>
        <td>${escapeHtml(member.name || member.username)}</td>
        <td>${formatTime(member.joinedAt)}</td>
        <td><span class="status approved">正常</span></td>
        <td>
          <button class="button button-danger" type="button"
                  data-remove-member="${member.userId}">移出社团</button>
        </td>
      </tr>`).join("");
  }

  document.getElementById("announcementCreateForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.target.querySelector("button[type=submit]");
    button.disabled = true;
    try {
      await apiRequest("/admin/announcements", {
        method: "POST",
        body: {
          clubId,
          title: document.getElementById("announcementTitle").value.trim(),
          content: document.getElementById("announcementContent").value.trim()
        }
      });
      event.target.reset();
      setMessage(message, "公告已发布", true);
      renderAnnouncements(await apiRequest(`/clubs/${clubId}/announcements`));
    } catch (error) {
      setMessage(message, error.message || "发布公告失败");
    } finally {
      button.disabled = false;
    }
  });

  document.getElementById("activityCreateForm").addEventListener("submit", async (event) => {
    event.preventDefault();
    const button = event.target.querySelector("button[type=submit]");
    button.disabled = true;
    try {
      await apiRequest("/admin/activities", {
        method: "POST",
        body: {
          clubId,
          title: document.getElementById("activityTitle").value.trim(),
          description: document.getElementById("activityDescription").value.trim(),
          startTime: document.getElementById("activityStartTime").value.replace("T", " ") + ":00",
          location: document.getElementById("activityLocation").value.trim()
        }
      });
      event.target.reset();
      setMessage(message, "活动已发布", true);
      renderActivities(await apiRequest(`/clubs/${clubId}/activities`));
    } catch (error) {
      setMessage(message, error.message || "发布活动失败");
    } finally {
      button.disabled = false;
    }
  });

  document.addEventListener("click", async (event) => {
    const activityButton = event.target.closest("[data-delete-activity]");
    if (activityButton) {
      await remove("/admin/activities/" + activityButton.dataset.deleteActivity, "活动已删除");
      renderActivities(await apiRequest(`/clubs/${clubId}/activities`));
      return;
    }
    const announcementButton = event.target.closest("[data-delete-announcement]");
    if (announcementButton) {
      await remove("/admin/announcements/" + announcementButton.dataset.deleteAnnouncement, "公告已删除");
      renderAnnouncements(await apiRequest(`/clubs/${clubId}/announcements`));
      return;
    }
    const memberButton = event.target.closest("[data-remove-member]");
    if (memberButton) {
      await remove(
        `/admin/clubs/${clubId}/members/${memberButton.dataset.removeMember}`,
        "成员已移出社团"
      );
      await loadAll();
    }
  });

  async function remove(path, successMessage) {
    if (!confirm("确定执行这个操作吗？")) {
      return;
    }
    try {
      await apiRequest(path, { method: "DELETE" });
      setMessage(message, successMessage, true);
    } catch (error) {
      setMessage(message, error.message || "操作失败");
    }
  }

  function categoryText(value) {
    return { tech: "学术科技", art: "文化艺术", sport: "体育运动", volunteer: "志愿公益" }[value] || "社团";
  }
})();
