(function filterClubs() {
  const params = new URLSearchParams(location.search);
  const category = params.get("category") || "all";
  const cards = Array.from(document.querySelectorAll(".club-card[data-category]"));
  const chips = Array.from(document.querySelectorAll(".chip"));
  const count = document.getElementById("clubCount");

  let visibleCount = 0;
  cards.forEach((card) => {
    const visible = category === "all" || card.dataset.category === category;
    card.hidden = !visible;
    if (visible) {
      visibleCount += 1;
    }
  });

  chips.forEach((chip) => {
    const href = new URL(chip.getAttribute("href"), location.href);
    const chipCategory = href.searchParams.get("category") || "all";
    chip.classList.toggle("active", chipCategory === category);
  });

  if (count) {
    count.textContent = `共 ${visibleCount} 个社团`;
  }
})();
