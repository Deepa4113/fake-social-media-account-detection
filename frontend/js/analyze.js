// ============================================================
//  FakeShield — Analyze Page Logic + Client-Side Scoring
// ============================================================

document.addEventListener('DOMContentLoaded', () => {
  initClock();
});

function initClock() {
  const el = document.getElementById('clock');
  if (!el) return;
  const tick = () => el.textContent = new Date().toLocaleTimeString('en-GB', { hour12: false });
  tick(); setInterval(tick, 1000);
}

// ─── Client-Side Scoring Engine ──────────────────────────────
// Mirrors the Java DetectionEngineService logic
function computeScore(acc) {
  const features = [];

  // 1. Profile picture
  features.push({
    name: 'Has Profile Picture',
    category: 'PROFILE',
    value: acc.hasProfilePic ? 1 : 0,
    score: acc.hasProfilePic ? 5 : 80,
  });

  // 2. Bio quality
  const bioLen = (acc.bio || '').length;
  let bioScore = 60;
  if (bioLen >= 20) bioScore = 10;
  else if (bioLen >= 5) bioScore = 40;
  features.push({ name: 'Bio Quality', category: 'PROFILE', value: bioLen, score: bioScore });

  // 3. Verified
  features.push({
    name: 'Verification Status',
    category: 'PROFILE',
    value: acc.isVerified ? 1 : 0,
    score: acc.isVerified ? 5 : 30,
  });

  // 4. Follower / Following ratio
  const followers = parseInt(acc.followersCount) || 0;
  const following = parseInt(acc.followingCount) || 1;
  const ratio = followers / Math.max(following, 1);
  let ratioScore;
  if (ratio < 0.05) ratioScore = 90;
  else if (ratio < 0.2) ratioScore = 70;
  else if (ratio <= 10) ratioScore = 12;
  else ratioScore = 20;
  features.push({ name: 'Follower/Following Ratio', category: 'NETWORK', value: parseFloat(ratio.toFixed(2)), score: ratioScore });

  // 5. Raw follower count
  let follScore = 70;
  if (followers > 10000)     follScore = 20;
  else if (followers > 1000) follScore = 30;
  else if (followers > 100)  follScore = 45;
  features.push({ name: 'Follower Count', category: 'NETWORK', value: followers, score: follScore });

  // 6. Account age
  const age = parseInt(acc.accountAgeDays) || 0;
  let ageScore;
  if (age < 7)        ageScore = 90;
  else if (age < 30)  ageScore = 65;
  else if (age < 180) ageScore = 35;
  else                ageScore = 10;
  features.push({ name: 'Account Age', category: 'ACTIVITY', value: age, score: ageScore });

  // 7. Posts per day
  const posts = parseInt(acc.postsCount) || 0;
  const ppd = posts / Math.max(age, 1);
  let ppdScore;
  if (ppd > 50)        ppdScore = 90;
  else if (ppd > 20)   ppdScore = 65;
  else if (ppd < 0.05) ppdScore = 55;
  else                 ppdScore = 15;
  features.push({ name: 'Posts per Day', category: 'ACTIVITY', value: parseFloat(ppd.toFixed(2)), score: ppdScore });

  // 8. Suspicious keywords
  const keywords = ['click here', 'free money', 'giveaway', 'win now', 'dm me', 'crypto', 'investment', '!!'];
  const combined = ((acc.bio || '') + ' ' + (acc.displayName || '')).toLowerCase();
  const matches  = keywords.filter(k => combined.includes(k)).length;
  features.push({ name: 'Suspicious Keywords', category: 'CONTENT', value: matches, score: Math.min(95, matches * 20) });

  // ─── Weighted average ──────────────────────────────────────
  const weights = { PROFILE: 1.5, NETWORK: 2.0, ACTIVITY: 2.0, CONTENT: 1.8, BEHAVIORAL: 1.2 };
  let wSum = 0, wTotal = 0;
  features.forEach(f => {
    const w = weights[f.category] || 1;
    wSum   += f.score * w;
    wTotal += w;
  });

  const fakeScore = Math.round(wSum / wTotal);

  let verdict, verdictColor;
  if (fakeScore >= 70)      { verdict = 'FAKE';       verdictColor = 'var(--danger)'; }
  else if (fakeScore >= 40) { verdict = 'SUSPICIOUS'; verdictColor = 'var(--warn)';   }
  else                      { verdict = 'REAL';       verdictColor = 'var(--safe)';   }

  const highRisk = features.filter(f => f.score > 60).length;
  const confidence = highRisk >= 5 ? 'HIGH' : highRisk >= 3 ? 'MEDIUM' : 'LOW';

  const notes = `Fake score: ${fakeScore}/100. ` +
    features.filter(f => f.score > 60).map(f => `High risk: ${f.name}.`).join(' ');

  return { fakeScore, verdict, verdictColor, confidence, features, notes };
}

// ─── Run Analysis ─────────────────────────────────────────────
function runAnalysis() {
  const username = document.getElementById('a_username').value.trim();
  if (!username) { showToast('Username is required', 'error'); return; }

  const btn = document.getElementById('analyzeBtn');
  btn.disabled = true;
  btn.innerHTML = '<span class="spinner"></span> ANALYZING...';

  const acc = {
    username:       username,
    displayName:    document.getElementById('a_displayName').value,
    bio:            document.getElementById('a_bio').value,
    followersCount: document.getElementById('a_followers').value,
    followingCount: document.getElementById('a_following').value,
    postsCount:     document.getElementById('a_posts').value,
    accountAgeDays: document.getElementById('a_age').value,
    hasProfilePic:  document.getElementById('a_hasPic').value === 'true',
    isVerified:     document.getElementById('a_verified').value === 'true',
  };

  // Simulate backend latency (or call real API)
  setTimeout(() => {
    const result = computeScore(acc);
    displayResult(result);
    btn.disabled = false;
    btn.textContent = '◎ RUN DETECTION ANALYSIS';
    showToast(`Analysis complete — ${result.verdict}`, result.verdict === 'REAL' ? 'success' : 'error');
  }, 1400);
}

// ─── Display Result ───────────────────────────────────────────
function displayResult(result) {
  const panel = document.getElementById('resultPanel');
  const placeholder = document.getElementById('resultPlaceholder');
  const content = document.getElementById('resultContent');
  const tag = document.getElementById('resultTag');

  placeholder.style.display = 'none';
  content.style.display     = 'block';
  panel.classList.add('has-result');

  tag.textContent = result.verdict;
  tag.style.color = result.verdictColor;
  tag.style.borderColor = result.verdictColor + '55';
  tag.style.background  = result.verdictColor + '15';

  // Score circle
  const circ = Math.round((result.fakeScore / 100) * 263.9);
  const fill  = document.getElementById('scoreCircleFill');
  fill.style.stroke = result.verdictColor;
  fill.setAttribute('stroke-dasharray', `0 263.9`);
  setTimeout(() => fill.setAttribute('stroke-dasharray', `${circ} 263.9`), 50);

  const scoreEl = document.getElementById('scoreNum');
  animateCounter(scoreEl, result.fakeScore, 1000);
  scoreEl.style.color = result.verdictColor;

  document.getElementById('resultVerdict').textContent = result.verdict;
  document.getElementById('resultVerdict').style.color = result.verdictColor;
  document.getElementById('resultConf').textContent    = result.confidence + ' CONFIDENCE';

  // Feature list
  const featureList = document.getElementById('featureList');
  featureList.innerHTML = result.features.map(f => {
    const fc = scoreColor(f.score);
    return `
      <div class="feature-item">
        <div style="display:flex;justify-content:space-between">
          <span class="feature-name">${f.name.toUpperCase()}</span>
          <span style="font-size:9px;color:var(--text-muted);font-family:var(--font-mono)">${f.category}</span>
        </div>
        <div class="feature-score-row">
          <div class="feature-bar-bg">
            <div class="feature-bar-fill" style="width:0%; background:${fc}" data-target="${f.score}"></div>
          </div>
          <span class="feature-score-val">${f.score}</span>
        </div>
      </div>`;
  }).join('');

  // Animate bars
  setTimeout(() => {
    featureList.querySelectorAll('.feature-bar-fill').forEach(bar => {
      bar.style.width = bar.dataset.target + '%';
    });
  }, 100);

  document.getElementById('analysisNotes').textContent = result.notes;
}
