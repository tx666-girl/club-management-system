(async function () {
  const user = await requireUser();
  if (!user) {
    return;
  }
  const clubId = queryId(1);
  const message = document.getElementById("clubMemberMessage");

  try {
    const [club, announcements, activities] = await Promise.all([
      apiRequest(`/clubs/${clubId}`),
      apiRequest(`/clubs/${clubId}/announcements`),
      apiRequest(`/clubs/${clubId}/activities`)
    ]);
    document.title = `${club.name} - 社团详情`;
    document.getElementById("clubCategory").textContent = categoryText(club.category);
    document.getElementById("clubName").textContent = club.name;
    document.getElementById("clubDescription").textContent = club.description;
    document.getElementById("clubMeta").innerHTML =
      `<span>${club.memberCount} 名成员</span>` +
      `<span>${club.joined ? "你已加入该社团" : club.pending ? "申请审核中" : "尚未加入"}</span>`;
    renderActivities(activities);
    renderAnnouncements(announcements);
  } catch (error) {
    setMessage(message, error.message || "加载社团失败");
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
          <span>${formatTime(activity.startTime)}</span>
          <span>${escapeHtml(activity.location)}</span>
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
          <span>${escapeHtml(item.publisherName)}</span>
          <span>${formatTime(item.createdAt)}</span>
        </div>
      </article>`).join("");
  }

  function categoryText(value) {
    return { tech: "学术科技", art: "文化艺术", sport: "体育运动", volunteer: "志愿公益" }[value] || "社团";
  }
})();
