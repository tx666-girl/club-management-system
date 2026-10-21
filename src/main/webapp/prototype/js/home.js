(async function () {
  const user = await requireUser();
  if (!user) {
    return;
  }

  if (user.role === "ADMIN") {
    document.querySelector(".nav").innerHTML =
      '<a class="nav-link active" href="home.html">首页</a>' +
      '<a class="nav-link" href="admin.html">管理员原型</a>' +
      '<a class="nav-link" href="personal-admin.html">个人中心</a>';
  }

  const params = new URLSearchParams(location.search);
  const category = params.get("category") || "all";
  const grid = document.getElementById("clubGrid");
  const message = document.getElementById("homeMessage");
  const count = document.getElementById("clubCount");

  document.querySelectorAll(".chip").forEach((chip) => {
    const href = new URL(chip.getAttribute("href"), location.href);
    chip.classList.toggle("active", (href.searchParams.get("category") || "all") === category);
  });

  try {
    const query = category === "all" ? "" : "?category=" + encodeURIComponent(category);
    const clubs = await apiRequest("/clubs" + query);
    grid.innerHTML = clubs.map((club) => cardHtml(club, user)).join("");
    count.textContent = `共 ${clubs.length} 个社团`;
    grid.querySelectorAll("img").forEach((image) => {
      image.addEventListener("error", () => {
        image.src = "assets/clubs/tech.svg";
      }, { once: true });
    });
    grid.querySelectorAll("[data-apply]").forEach((button) => {
      button.addEventListener("click", () => apply(Number(button.dataset.apply), button));
    });
  } catch (error) {
    grid.innerHTML = "";
    setMessage(message, error.message || "加载社团失败");
  }

  async function apply(clubId, button) {
    button.disabled = true;
    try {
      await apiRequest("/applications", {
        method: "POST",
        body: { clubId: clubId }
      });
      button.className = "status-badge pending";
      button.textContent = "审核中";
    } catch (error) {
      button.disabled = false;
      setMessage(message, error.message || "申请失败");
    }
  }

  function cardHtml(club, currentUser) {
    const admin = currentUser.role === "ADMIN";
    const detail = admin
      ? `club-admin.html?id=${club.id}`
      : `club-member.html?id=${club.id}`;
    let action;
    if (admin) {
      action = `<a class="button button-primary" href="${detail}">管理社团</a>`;
    } else if (club.joined) {
      action = '<span class="status-badge approved">已加入</span>';
    } else if (club.pending) {
      action = '<span class="status-badge pending">审核中</span>';
    } else {
      action = `<button class="button button-primary" type="button" data-apply="${club.id}">申请加入</button>`;
    }

    return `
      <article class="club-card" data-category="${escapeHtml(club.category)}">
        <div class="club-cover">
          <img src="${escapeHtml(club.imageUrl)}" alt="${escapeHtml(club.name)}插图">
        </div>
        <div class="club-card-body">
          <span class="tag">${categoryText(club.category)}</span>
          <h3>${escapeHtml(club.name)}</h3>
          <p>${escapeHtml(club.description)}</p>
          <div class="club-meta">
            <span>${club.memberCount} 名成员</span>
            <span>${admin ? "管理员视角" : "可申请加入"}</span>
          </div>
          <div class="card-actions">
            <a class="button button-secondary" href="${detail}">查看详情</a>
            ${action}
          </div>
        </div>
      </article>`;
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
