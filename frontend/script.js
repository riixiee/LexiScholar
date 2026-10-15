// Change this to your Railway URL when you deploy the backend
const API_BASE = "https://lexischolar-production.up.railway.app";
const form = document.getElementById("search-form");
const input = document.getElementById("query");
const statusEl = document.getElementById("status");
const resultsEl = document.getElementById("results");
const statsEl = document.getElementById("stats");

const modal = document.getElementById("modal");
const modalTitle = document.getElementById("modal-title");
const modalMeta = document.getElementById("modal-meta");
const modalText = document.getElementById("modal-text");

let lastTerms = [];

function escapeHtml(text) {
  return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function highlight(text, terms) {
  let safe = escapeHtml(text);
  terms.forEach(term => {
    safe = safe.replace(new RegExp("\\b(" + term + "\\w*)", "gi"), "<mark>$1</mark>");
  });
  return safe;
}

function queryTerms(query) {
  return query.toLowerCase().split(/[^a-z0-9]+/).filter(t => t.length > 1);
}

async function loadStats() {
  try {
    const res = await fetch(API_BASE + "/stats");
    const s = await res.json();
    statsEl.textContent =
      s.documents + " documents indexed · " + s.uniqueTerms + " unique terms · index built in " +
      s.indexBuildMs.toFixed(1) + " ms";
  } catch (err) {
    statsEl.textContent = "Search engine is offline.";
  }
}

async function search(query) {
  statusEl.className = "";
  statusEl.textContent = "Searching...";
  resultsEl.innerHTML = "";
  lastTerms = queryTerms(query);

  try {
    const res = await fetch(API_BASE + "/search?q=" + encodeURIComponent(query) + "&k=10");
    if (!res.ok) throw new Error("Server returned " + res.status);
    const data = await res.json();
    render(data);
  } catch (err) {
    statusEl.className = "error";
    statusEl.textContent = "Could not reach the search server. Is the backend running?";
  }
}

function render(data) {
  if (data.count === 0) {
    statusEl.textContent = "No documents matched \"" + data.query + "\".";
    return;
  }

  statusEl.textContent = data.count + " result" + (data.count > 1 ? "s" : "") +
    " in " + data.tookMs.toFixed(2) + " ms";

  const topScore = data.results[0].score;

  resultsEl.innerHTML = data.results.map(r => {
    const pct = Math.max(5, Math.round((r.score / topScore) * 100));
    return `
      <div class="card">
        <h3>${highlight(r.title, lastTerms)}</h3>
        <p>${highlight(r.snippet, lastTerms)}</p>
        <div class="score-row">
          <span>Relevance</span>
          <div class="bar"><div class="bar-fill" style="width:${pct}%"></div></div>
          <span>${r.score.toFixed(4)}</span>
        </div>
        <button class="view-btn" type="button" data-id="${r.id}">View full document</button>
      </div>`;
  }).join("");
}

async function openDocument(id) {
  try {
    const res = await fetch(API_BASE + "/doc/" + id);
    if (!res.ok) throw new Error("Server returned " + res.status);
    const doc = await res.json();

    const matches = (doc.title + " " + doc.text).match(/<mark>/g);
    modalTitle.innerHTML = highlight(doc.title, lastTerms);
    modalText.innerHTML = highlight(doc.text, lastTerms);

    const count = (modalTitle.innerHTML + modalText.innerHTML).split("<mark>").length - 1;
    modalMeta.textContent = "Document #" + doc.id + " · " + count + " matched word" +
      (count === 1 ? "" : "s") + " highlighted";

    modal.classList.remove("hidden");
    document.body.style.overflow = "hidden";
  } catch (err) {
    statusEl.className = "error";
    statusEl.textContent = "Could not load the full document.";
  }
}

function closeModal() {
  modal.classList.add("hidden");
  document.body.style.overflow = "";
}

form.addEventListener("submit", e => {
  e.preventDefault();
  const q = input.value.trim();
  if (q) search(q);
});

document.querySelectorAll(".chip").forEach(chip => {
  chip.addEventListener("click", () => {
    input.value = chip.textContent;
    search(chip.textContent);
  });
});

resultsEl.addEventListener("click", e => {
  const btn = e.target.closest(".view-btn");
  if (btn) openDocument(btn.dataset.id);
});

document.getElementById("modal-close").addEventListener("click", closeModal);
modal.addEventListener("click", e => { if (e.target === modal) closeModal(); });
document.addEventListener("keydown", e => { if (e.key === "Escape") closeModal(); });

loadStats();