// ============================================================
//  FakeShield — Dashboard Page Logic
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  initClock();
  loadDashboard();
});

function initClock() {
  const el = document.getElementById('clock');
  if (!el) return;
  const tick = () => {
    el.textContent = new Date().toLocaleTimeString('en-GB', { hour12: false });
  };
  tick();
  setInterval(tick, 1000);
}

async function loadDashboard() {
  // Animate counters
  document.querySelectorAll('.stat-value[data-target]').forEach(el => {
    animateCounter(el, parseInt(el.dataset.target));
  });

  // Load recent detections table
  renderRecentTable(MockData.accounts);
}

function renderRecentTable(accounts) {
  const tbody = document.getElementById('recentTableBody');
  if (!tbody) return;

  tbody.innerHTML = accounts.slice(0, 8).map(acc => {
    const r = acc.latestResult || {};
    const score = r.fakeScore ?? 0;
    const verdict = r.verdict || 'UNKNOWN';
    const conf = r.confidenceLevel || 'LOW';
    const color = scoreColor(score);

    return `
      <tr>
        <td>
          <div class="acc-cell">
            <div class="acc-avatar" style="color:${color}">${avatarInitial(acc)}</div>
            <div>
              <div class="acc-name">${escHtml(acc.displayName || acc.username)}</div>
              <div class="acc-handle">@${escHtml(acc.username)}</div>
            </div>
          </div>
        </td>
        <td>
          <span class="platform-chip">${platformLabel(acc.platform)}</span>
        </td>
        <td>
          <div class="score-cell">
            <div class="score-track">
              <div class="score-fill" style="width:${score}%; background:${color}"></div>
            </div>
            <span class="score-num" style="color:${color}">${score}</span>
          </div>
        </td>
        <td>
          <span class="verdict-badge ${verdictClass(verdict)}">${verdict}</span>
        </td>
        <td>
          <span class="confidence-dot conf-${conf}">
            <span class="conf-pip"></span>${conf}
          </span>
        </td>
        <td>
          <span class="date-cell">${r.analyzedAt ? timeAgo(r.analyzedAt) : '—'}</span>
        </td>
        <td>
          <button class="btn-detail" onclick="window.location.href='accounts.html'">Details →</button>
        </td>
      </tr>
    `;
  }).join('');
}

function escHtml(str) {
  const d = document.createElement('div');
  d.appendChild(document.createTextNode(str || ''));
  return d.innerHTML;
}
