// ============================================================
//  FakeShield — API Service Layer
//  All HTTP calls to the Java Spring Boot backend
// ============================================================

const API_BASE = 'http://localhost:8080/api';

// ─── Token Management ─────────────────────────────────────────
const Auth = {
  getToken:    ()     => localStorage.getItem('fs_token'),
  setToken:    (t)    => localStorage.setItem('fs_token', t),
  removeToken: ()     => localStorage.removeItem('fs_token'),
  getUser:     ()     => JSON.parse(localStorage.getItem('fs_user') || 'null'),
  setUser:     (u)    => localStorage.setItem('fs_user', JSON.stringify(u)),
  isLoggedIn:  ()     => !!localStorage.getItem('fs_token'),
};

// ─── Core Fetch Wrapper ───────────────────────────────────────
async function apiFetch(endpoint, options = {}) {
  const token = Auth.getToken();
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...(options.headers || {}),
  };

  const config = { ...options, headers };

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, config);

    if (res.status === 401) {
      Auth.removeToken();
      window.location.href = 'login.html';
      return;
    }

    const contentType = res.headers.get('content-type');
    const data = contentType && contentType.includes('application/json')
      ? await res.json()
      : await res.text();

    if (!res.ok) {
      throw new ApiError(res.status, data?.message || data || 'Request failed');
    }

    return data;
  } catch (err) {
    if (err instanceof ApiError) throw err;
    throw new ApiError(0, 'Network error — is the backend running?');
  }
}

class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
    this.name = 'ApiError';
  }
}

// ─── Auth API ─────────────────────────────────────────────────
const AuthAPI = {
  async login(username, password) {
    const data = await apiFetch('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    });
    Auth.setToken(data.accessToken);
    Auth.setUser({ username: data.username, role: data.role });
    return data;
  },

  async register(username, email, password) {
    return apiFetch('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ username, email, password }),
    });
  },

  logout() {
    Auth.removeToken();
    Auth.setUser(null);
    window.location.href = 'login.html';
  },
};

// ─── Social Accounts API ──────────────────────────────────────
const AccountsAPI = {
  async getAll(page = 0, size = 20, search = '') {
    const params = new URLSearchParams({ page, size });
    if (search) params.set('search', search);
    return apiFetch(`/accounts?${params}`);
  },

  async getById(id) {
    return apiFetch(`/accounts/${id}`);
  },

  async create(accountData) {
    return apiFetch('/accounts', {
      method: 'POST',
      body: JSON.stringify(accountData),
    });
  },

  async delete(id) {
    return apiFetch(`/accounts/${id}`, { method: 'DELETE' });
  },

  async analyze(id) {
    return apiFetch(`/accounts/${id}/analyze`, { method: 'POST' });
  },

  async getResults(id) {
    return apiFetch(`/accounts/${id}/results`);
  },
};

// ─── Stats API ────────────────────────────────────────────────
const StatsAPI = {
  async getSummary() {
    return apiFetch('/stats/summary');
  },
};

// ─── Demo / Mock Data (used when backend is offline) ─────────
const MockData = {
  stats: {
    totalAnalyzed: 1482,
    totalFake: 584,
    totalReal: 667,
    totalSuspicious: 231,
    averageFakeScore: 47.3,
  },

  accounts: [
    {
      id: '1', platform: 'TWITTER',   username: 'suspicious_acc1',
      displayName: 'Free Money Giveaway!!!',
      followersCount: 120, followingCount: 4800, postsCount: 3,
      accountAgeDays: 2, isVerified: false, hasProfilePic: false,
      bio: 'Click link for FREE rewards!!!',
      latestResult: { fakeScore: 88, verdict: 'FAKE', confidenceLevel: 'HIGH', analyzedAt: new Date().toISOString() }
    },
    {
      id: '2', platform: 'INSTAGRAM', username: 'real_user_99',
      displayName: 'John Traveller',
      followersCount: 3200, followingCount: 400, postsCount: 210,
      accountAgeDays: 730, isVerified: false, hasProfilePic: true,
      bio: 'Travel photographer | Coffee lover | Coimbatore 📷',
      latestResult: { fakeScore: 12, verdict: 'REAL', confidenceLevel: 'HIGH', analyzedAt: new Date().toISOString() }
    },
    {
      id: '3', platform: 'FACEBOOK',  username: 'bot_account_x',
      displayName: 'News Updates Daily',
      followersCount: 890, followingCount: 1200, postsCount: 45,
      accountAgeDays: 15, isVerified: false, hasProfilePic: false,
      bio: '',
      latestResult: { fakeScore: 61, verdict: 'SUSPICIOUS', confidenceLevel: 'MEDIUM', analyzedAt: new Date().toISOString() }
    },
    {
      id: '4', platform: 'TWITTER',   username: 'verified_brand',
      displayName: 'TechCorp Official',
      followersCount: 125000, followingCount: 300, postsCount: 1820,
      accountAgeDays: 1200, isVerified: true, hasProfilePic: true,
      bio: 'Official account of TechCorp. Building the future.',
      latestResult: { fakeScore: 5, verdict: 'REAL', confidenceLevel: 'HIGH', analyzedAt: new Date().toISOString() }
    },
    {
      id: '5', platform: 'TIKTOK',    username: 'clickbait_master',
      displayName: 'Click Here WIN NOW!!',
      followersCount: 50, followingCount: 3100, postsCount: 7,
      accountAgeDays: 4, isVerified: false, hasProfilePic: false,
      bio: 'FREE crypto giveaway DM me NOW!! Investment opportunity!!',
      latestResult: { fakeScore: 95, verdict: 'FAKE', confidenceLevel: 'HIGH', analyzedAt: new Date().toISOString() }
    },
    {
      id: '6', platform: 'LINKEDIN',  username: 'professional_joe',
      displayName: 'Joe Smith, MBA',
      followersCount: 780, followingCount: 320, postsCount: 95,
      accountAgeDays: 600, isVerified: false, hasProfilePic: true,
      bio: 'Senior Product Manager at FinCo | MBA Graduate | 10+ years experience',
      latestResult: { fakeScore: 18, verdict: 'REAL', confidenceLevel: 'MEDIUM', analyzedAt: new Date().toISOString() }
    },
    {
      id: '7', platform: 'INSTAGRAM', username: 'bot_follower_xyz',
      displayName: '',
      followersCount: 5, followingCount: 6200, postsCount: 1,
      accountAgeDays: 1, isVerified: false, hasProfilePic: false,
      bio: '',
      latestResult: { fakeScore: 97, verdict: 'FAKE', confidenceLevel: 'HIGH', analyzedAt: new Date().toISOString() }
    },
    {
      id: '8', platform: 'TWITTER',   username: 'midlevel_user',
      displayName: 'Sarah K.',
      followersCount: 420, followingCount: 380, postsCount: 55,
      accountAgeDays: 90, isVerified: false, hasProfilePic: true,
      bio: 'Just sharing thoughts on tech and coffee.',
      latestResult: { fakeScore: 34, verdict: 'SUSPICIOUS', confidenceLevel: 'LOW', analyzedAt: new Date().toISOString() }
    },
  ],

  featureScores: [
    { featureName: 'account_age_days',         featureScore: 90, featureCategory: 'ACTIVITY',   featureValue: 2 },
    { featureName: 'follower_following_ratio',  featureScore: 85, featureCategory: 'NETWORK',    featureValue: 0.025 },
    { featureName: 'suspicious_keywords',       featureScore: 80, featureCategory: 'CONTENT',    featureValue: 3 },
    { featureName: 'has_profile_pic',           featureScore: 80, featureCategory: 'PROFILE',    featureValue: 0 },
    { featureName: 'bio_quality',               featureScore: 60, featureCategory: 'PROFILE',    featureValue: 35 },
    { featureName: 'posts_per_day',             featureScore: 55, featureCategory: 'ACTIVITY',   featureValue: 1.5 },
    { featureName: 'is_verified',               featureScore: 30, featureCategory: 'PROFILE',    featureValue: 0 },
    { featureName: 'follower_count',            featureScore: 70, featureCategory: 'NETWORK',    featureValue: 120 },
  ],
};

// ─── Helpers ──────────────────────────────────────────────────
function formatNumber(n) {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M';
  if (n >= 1_000)     return (n / 1_000).toFixed(1) + 'K';
  return String(n);
}

function timeAgo(dateStr) {
  const diff = Date.now() - new Date(dateStr).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 60)    return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24)     return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

function scoreColor(score) {
  if (score >= 70) return 'var(--danger)';
  if (score >= 40) return 'var(--warn)';
  return 'var(--safe)';
}

function verdictClass(verdict) {
  return `verdict-${verdict}`;
}

function platformLabel(p) {
  const map = { TWITTER:'𝕏 Twitter', INSTAGRAM:'◉ Instagram', FACEBOOK:'f Facebook', TIKTOK:'♪ TikTok', LINKEDIN:'in LinkedIn', OTHER:'🌐 Other' };
  return map[p] || p;
}

function avatarInitial(account) {
  return (account.displayName || account.username || '?')[0].toUpperCase();
}

// ─── Toast Notifications ─────────────────────────────────────
function showToast(message, type = 'info') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  const icons = { success: '✓', error: '✕', info: 'ℹ' };
  toast.className = `toast ${type}`;
  toast.innerHTML = `<span>${icons[type] || 'ℹ'}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity 0.3s'; setTimeout(() => toast.remove(), 300); }, 3500);
}

// ─── Counter Animations ───────────────────────────────────────
function animateCounter(el, target, duration = 1200) {
  const start = performance.now();
  const from = parseInt(el.textContent) || 0;
  function step(now) {
    const progress = Math.min((now - start) / duration, 1);
    const eased = 1 - Math.pow(1 - progress, 3);
    el.textContent = Math.round(from + (target - from) * eased).toLocaleString();
    if (progress < 1) requestAnimationFrame(step);
  }
  requestAnimationFrame(step);
}
