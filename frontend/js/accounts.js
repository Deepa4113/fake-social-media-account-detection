// ============================================================
//  FakeShield — Accounts Page Logic
// ============================================================

let allAccounts = [...MockData.accounts];
let activeFilter = 'ALL';
let searchQuery  = '';

document.addEventListener('DOMContentLoaded', () => {
  initClock();
  renderAccounts();
  setupSearch();
  setupFilters();
});

function initClock() {
  const el = document.getElementById('clock');
  if (!el) return;
  const tick = () => el.textContent = new Date().toLocaleTimeString('en-GB', { hour12: false });
  tick(); setInterval(tick, 1000);
}

// ─── Render Accounts Grid ─────────────────────────────────────
function renderAccounts() {
  const grid  = document.getElementById('accountsGrid');
  const empty = document.getElementById('emptyState');

  const filtered = allAccounts.filter(a => {
    const matchSearch = !searchQuery ||
      a.username.toLowerCase().includes(searchQuery) ||
      (a.displayName || '').toLowerCase().includes(searchQuery);
    const matchFilter = activeFilter === 'ALL' || (a.latestResult?.verdict === activeFilter);
    return matchSearch && matchFilter;
  });

  if (filtered.length === 0) {
    grid.innerHTML = '';
    empty.style.display = 'block';
    return;
  }

  empty.style.display = 'none';

  grid.innerHTML = filtered.map((acc, i) => {
    const r       = acc.latestResult || {};
    const score   = r.fakeScore ?? 0;
    const verdict = r.verdict || 'UNKNOWN';
    const color   = scoreColor(score);
    const circ    = Math.round((score / 100) * 100.53);  // 2*pi*16 ≈ 100.53

    return `
      <div class="account-card card-${verdict} animate-in" style="--delay:${0.04 * i}s"
           onclick="openDetailModal('${acc.id}')">
        <div class="account-card-top">
          <div class="account-identity">
            <div class="account-avatar-lg" style="color:${color};border:2px solid ${color}22">
              ${avatarInitial(acc)}
            </div>
            <div>
              <div class="account-card-name">${escHtml(acc.displayName || acc.username)}</div>
              <div class="account-card-handle">@${escHtml(acc.username)}</div>
            </div>
          </div>
          <span class="platform-chip">${platformLabel(acc.platform)}</span>
        </div>

        <div class="account-card-stats">
          <div>
            <div class="acc-stat-lbl">FOLLOWERS</div>
            <div class="acc-stat-val">${formatNumber(acc.followersCount || 0)}</div>
          </div>
          <div>
            <div class="acc-stat-lbl">FOLLOWING</div>
            <div class="acc-stat-val">${formatNumber(acc.followingCount || 0)}</div>
          </div>
          <div>
            <div class="acc-stat-lbl">AGE (DAYS)</div>
            <div class="acc-stat-val">${acc.accountAgeDays || 0}</div>
          </div>
        </div>

        <div class="account-card-score">
          <div class="score-arc">
            <svg viewBox="0 0 36 36">
              <circle class="score-arc-bg"   cx="18" cy="18" r="16"/>
              <circle class="score-arc-fill" cx="18" cy="18" r="16"
                stroke="${color}"
                stroke-dasharray="${circ} 100.53"
                stroke-dashoffset="0"/>
            </svg>
            <div class="score-arc-text" style="color:${color}">${score}</div>
          </div>
          <div class="score-info">
            <div class="score-info-label">FAKE SCORE</div>
            <div class="score-info-verdict" style="color:${color}">${verdict}</div>
            <div style="font-family:var(--font-mono);font-size:10px;color:var(--text-muted);margin-top:2px">
              ${r.confidenceLevel || ''} CONFIDENCE
            </div>
          </div>
          <button class="btn-detail" onclick="event.stopPropagation(); triggerAnalyze('${acc.id}', this)">
            Analyze
          </button>
        </div>
      </div>
    `;
  }).join('');
}

// ─── Search ───────────────────────────────────────────────────
function setupSearch() {
  const input = document.getElementById('searchInput');
  if (!input) return;
  input.addEventListener('input', e => {
    searchQuery = e.target.value.trim().toLowerCase();
    renderAccounts();
  });
}

// ─── Filters ──────────────────────────────────────────────────
function setupFilters() {
  document.querySelectorAll('.filter-chip').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.filter-chip').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      activeFilter = btn.dataset.filter;
      renderAccounts();
    });
  });
}

// ─── Analyze Trigger ──────────────────────────────────────────
function triggerAnalyze(id, btn) {
  const original = btn.textContent;
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span>';

  setTimeout(() => {
    const idx = allAccounts.findIndex(a => a.id === id);
    if (idx !== -1) {
      // Simulate updated score
      const prev = allAccounts[idx].latestResult?.fakeScore ?? 50;
      allAccounts[idx].latestResult = {
        ...allAccounts[idx].latestResult,
        analyzedAt: new Date().toISOString(),
      };
    }
    btn.disabled = false;
    btn.textContent = original;
    renderAccounts();
    showToast('Analysis complete!', 'success');
  }, 1600);
}

// ─── Detail Modal ─────────────────────────────────────────────
function openDetailModal(id) {
  const acc = allAccounts.find(a => a.id === id);
  if (!acc) return;
  const r     = acc.latestResult || {};
  const score = r.fakeScore ?? 0;
  const color = scoreColor(score);
  const circ  = Math.round((score / 100) * 263.9); // 2*pi*42

  const overlay = document.createElement('div');
  overlay.className = 'modal-overlay';
  overlay.onclick = e => { if (e.target === overlay) overlay.remove(); };

  const featureRows = MockData.featureScores.map(f => {
    const fc = scoreColor(f.featureScore);
    return `
      <div class="feature-item">
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span class="feature-name">${f.featureName.replace(/_/g,' ').toUpperCase()}</span>
          <span style="font-size:9px;color:var(--text-muted);font-family:var(--font-mono)">${f.featureCategory}</span>
        </div>
        <div class="feature-score-row">
          <div class="feature-bar-bg">
            <div class="feature-bar-fill" style="width:${f.featureScore}%; background:${fc}"></div>
          </div>
          <span class="feature-score-val">${f.featureScore}</span>
        </div>
      </div>`;
  }).join('');

  overlay.innerHTML = `
    <div class="modal" style="width:540px">
      <div class="modal-header">
        <div class="modal-title">Account Detail</div>
        <button class="modal-close" onclick="this.closest('.modal-overlay').remove()">✕</button>
      </div>
      <div class="modal-body">
        <div style="display:flex;align-items:center;gap:16px;margin-bottom:20px;padding-bottom:20px;border-bottom:1px solid var(--border)">
          <div class="account-avatar-lg" style="width:56px;height:56px;font-size:22px;color:${color};border:2px solid ${color}33">
            ${avatarInitial(acc)}
          </div>
          <div style="flex:1">
            <div style="font-size:18px;font-weight:800">${escHtml(acc.displayName || acc.username)}</div>
            <div style="font-family:var(--font-mono);font-size:11px;color:var(--text-muted);margin-top:2px">@${escHtml(acc.username)}</div>
            <div style="margin-top:6px">${acc.bio ? `<span style="font-size:12px;color:var(--text-muted)">${escHtml(acc.bio)}</span>` : ''}</div>
          </div>
          <div style="text-align:center">
            <div style="font-family:var(--font-mono);font-size:32px;font-weight:800;color:${color}">${score}</div>
            <div style="font-family:var(--font-mono);font-size:9px;color:var(--text-muted)">FAKE SCORE</div>
            <span class="verdict-badge ${verdictClass(r.verdict || 'UNKNOWN')}" style="margin-top:6px;display:inline-block">${r.verdict || 'UNKNOWN'}</span>
          </div>
        </div>

        <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:12px;margin-bottom:20px">
          ${[
            ['FOLLOWERS', formatNumber(acc.followersCount||0)],
            ['FOLLOWING', formatNumber(acc.followingCount||0)],
            ['POSTS',     acc.postsCount||0],
            ['AGE (DAYS)',acc.accountAgeDays||0],
          ].map(([l,v]) => `
            <div style="background:var(--bg3);border-radius:6px;padding:10px 12px;border:1px solid var(--border)">
              <div style="font-family:var(--font-mono);font-size:9px;color:var(--text-muted);margin-bottom:4px">${l}</div>
              <div style="font-size:16px;font-weight:800">${v}</div>
            </div>`).join('')}
        </div>

        <div style="font-family:var(--font-mono);font-size:10px;color:var(--text-muted);letter-spacing:1px;margin-bottom:12px">
          FEATURE ANALYSIS
        </div>
        <div class="feature-list">${featureRows}</div>

        <div style="display:flex;gap:10px;margin-top:20px">
          <button class="btn-analyze" style="flex:1" onclick="triggerAnalyzeFromModal('${acc.id}', this)">
            RE-ANALYZE
          </button>
          <button class="btn-detail" style="padding:12px 20px" onclick="this.closest('.modal-overlay').remove()">
            Close
          </button>
        </div>
      </div>
    </div>`;

  document.body.appendChild(overlay);
}

function triggerAnalyzeFromModal(id, btn) {
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> ANALYZING...';
  setTimeout(() => {
    btn.disabled = false;
    btn.textContent = 'RE-ANALYZE';
    showToast('Analysis updated!', 'success');
  }, 1800);
}

// ─── Add Account Modal ────────────────────────────────────────
function openAddModal() {
  document.getElementById('addModal').style.display = 'flex';
}

function closeAddModal() {
  document.getElementById('addModal').style.display = 'none';
}

function closeAddModalIfOverlay(e) {
  if (e.target === document.getElementById('addModal')) closeAddModal();
}

function submitAddAccount() {
  const username = document.getElementById('m_username').value.trim();
  if (!username) { showToast('Username is required', 'error'); return; }

  const btn = document.getElementById('addSubmitBtn');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> ADDING...';

  setTimeout(() => {
    const newAcc = {
      id:             Date.now().toString(),
      platform:       document.getElementById('m_platform').value,
      username:       username.replace('@',''),
      displayName:    document.getElementById('m_displayName').value,
      followersCount: parseInt(document.getElementById('m_followers').value) || 0,
      followingCount: parseInt(document.getElementById('m_following').value) || 0,
      postsCount:     parseInt(document.getElementById('m_posts').value) || 0,
      accountAgeDays: parseInt(document.getElementById('m_age').value) || 0,
      bio:            document.getElementById('m_bio').value,
      hasProfilePic:  document.getElementById('m_hasPic').value === 'true',
      isVerified:     document.getElementById('m_verified').value === 'true',
      latestResult:   { fakeScore: 0, verdict: 'UNKNOWN', confidenceLevel: 'LOW', analyzedAt: new Date().toISOString() }
    };

    allAccounts.unshift(newAcc);
    closeAddModal();
    renderAccounts();
    showToast('Account added successfully!', 'success');

    // Reset form
    ['m_username','m_displayName','m_followers','m_following','m_posts','m_age','m_bio'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
    btn.disabled = false;
    btn.textContent = 'ADD & ANALYZE';
  }, 1200);
}

// ─── Helpers ─────────────────────────────────────────────────
function escHtml(str) {
  const d = document.createElement('div');
  d.appendChild(document.createTextNode(str || ''));
  return d.innerHTML;
}
