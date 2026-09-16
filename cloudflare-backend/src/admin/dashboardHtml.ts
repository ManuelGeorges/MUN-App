// src/admin/dashboardHtml.ts
export const ADMIN_DASHBOARD_HTML = `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MIANU-SM IV • Admin Operations Command Center</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600;700&display=swap" rel="stylesheet">
  <style>
    :root {
      --bg: #07090E;
      --bg-surface: #0E121A;
      --bg-raised: #151B26;
      --bg-card: rgba(21, 27, 38, 0.75);
      --border: #232B3B;
      --border-accent: #3A465D;
      --text: #F3F4F6;
      --text-muted: #9CA3AF;
      --text-dim: #6B7280;
      --accent: #3B82F6;
      --accent-glow: rgba(59, 130, 246, 0.25);
      --success: #10B981;
      --success-glow: rgba(16, 185, 129, 0.2);
      --warning: #F59E0B;
      --danger: #EF4444;
      --danger-glow: rgba(239, 68, 68, 0.2);
      --font: 'JetBrains Mono', ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    }

    * { box-sizing: border-box; margin: 0; padding: 0; }

    /* Custom Modern Dark Scrollbar */
    ::-webkit-scrollbar {
      width: 6px;
      height: 6px;
    }
    ::-webkit-scrollbar-track {
      background: var(--bg);
    }
    ::-webkit-scrollbar-thumb {
      background: var(--border-accent);
      border-radius: 4px;
    }
    ::-webkit-scrollbar-thumb:hover {
      background: #4B5563;
    }

    body {
      background-color: var(--bg);
      color: var(--text);
      font-family: var(--font);
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      font-size: 13px;
      line-height: 1.5;
    }

    /* SVG Icon Helpers */
    .icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      vertical-align: middle;
      flex-shrink: 0;
    }
    .icon svg {
      width: 14px;
      height: 14px;
    }
    .icon-lg svg {
      width: 18px;
      height: 18px;
    }
    .icon-sm svg {
      width: 12px;
      height: 12px;
    }

    /* Ambient Constellation Glow */
    .ambient-glow {
      position: fixed;
      top: 0; left: 0; right: 0; height: 350px;
      background: radial-gradient(circle at 50% -20%, rgba(59, 130, 246, 0.12), transparent 70%);
      pointer-events: none;
      z-index: 0;
    }

    /* Layout Shell */
    header {
      border-bottom: 1px solid var(--border);
      background: rgba(14, 18, 26, 0.85);
      backdrop-filter: blur(12px);
      position: sticky;
      top: 0;
      z-index: 40;
      padding: 0.75rem 1.5rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 0.85rem;
    }
    .brand-badge {
      width: 34px;
      height: 34px;
      background: #1E293B;
      border: 1px solid var(--border-accent);
      border-radius: 8px;
      display: grid;
      place-items: center;
      font-weight: 700;
      color: #60A5FA;
      box-shadow: 0 0 15px var(--accent-glow);
    }
    .brand-title {
      font-weight: 700;
      letter-spacing: -0.02em;
      font-size: 15px;
      color: #fff;
    }
    .brand-subtitle {
      font-size: 11px;
      color: var(--text-muted);
      letter-spacing: 0.05em;
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 1rem;
    }
    .admin-pill {
      background: rgba(239, 68, 68, 0.12);
      border: 1px solid rgba(239, 68, 68, 0.35);
      color: #F87171;
      padding: 0.25rem 0.65rem;
      border-radius: 6px;
      font-size: 11px;
      font-weight: 600;
      letter-spacing: 0.05em;
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }
    .live-status {
      display: flex;
      align-items: center;
      gap: 0.4rem;
      font-size: 11px;
      color: var(--success);
    }
    .live-pulse {
      width: 8px;
      height: 8px;
      background: var(--success);
      border-radius: 50%;
      box-shadow: 0 0 8px var(--success);
      animation: pulse 2s infinite ease-in-out;
    }
    @keyframes pulse {
      0%, 100% { opacity: 1; transform: scale(1); }
      50% { opacity: 0.4; transform: scale(0.85); }
    }

    .btn {
      font-family: var(--font);
      font-size: 12px;
      padding: 0.4rem 0.85rem;
      border-radius: 6px;
      border: 1px solid var(--border);
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      font-weight: 500;
      transition: all 0.15s ease;
      background: var(--bg-surface);
      color: var(--text);
    }
    .btn:hover {
      background: var(--bg-raised);
      border-color: var(--border-accent);
    }
    .btn:active { transform: translateY(1px) scale(0.97); }
    .btn-primary {
      background: var(--accent);
      border-color: var(--accent);
      color: #fff;
    }
    .btn-primary:hover {
      background: #2563EB;
      box-shadow: 0 0 12px var(--accent-glow);
    }
    .btn-danger {
      background: rgba(239, 68, 68, 0.15);
      border-color: rgba(239, 68, 68, 0.4);
      color: #F87171;
    }
    .btn-danger:hover {
      background: var(--danger);
      color: #fff;
    }
    .btn-success {
      background: rgba(16, 185, 129, 0.15);
      border-color: rgba(16, 185, 129, 0.4);
      color: #34D399;
    }
    .btn-success:hover {
      background: var(--success);
      color: #fff;
    }
    .btn-sm {
      font-size: 11px;
      padding: 0.25rem 0.5rem;
    }

    /* Sub-nav tabs */
    .nav-bar {
      border-bottom: 1px solid var(--border);
      background: var(--bg-surface);
      padding: 0 1.5rem;
      display: flex;
      gap: 0.5rem;
      overflow-x: auto;
      z-index: 30;
    }
    .nav-tab {
      padding: 0.75rem 0.9rem;
      border-bottom: 2px solid transparent;
      color: var(--text-muted);
      cursor: pointer;
      font-weight: 500;
      font-size: 12px;
      display: inline-flex;
      align-items: center;
      gap: 0.45rem;
      text-decoration: none;
      white-space: nowrap;
      transition: all 0.15s ease;
    }
    .nav-tab:hover {
      color: var(--text);
    }
    .nav-tab.active {
      color: #60A5FA;
      border-bottom-color: var(--accent);
    }

    /* Main Container */
    main {
      flex: 1;
      padding: 1.5rem;
      max-width: 1600px;
      margin: 0 auto;
      width: 100%;
      z-index: 10;
    }

    .view-container {
      display: none;
      animation: fadeIn 0.15s ease;
    }
    .view-container.active {
      display: block;
    }
    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(4px); }
      to { opacity: 1; transform: translateY(0); }
    }

    /* KPI Cards */
    .kpi-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1rem;
      margin-bottom: 1.5rem;
    }
    .kpi-card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 1.25rem;
      backdrop-filter: blur(8px);
      position: relative;
      overflow: hidden;
    }
    .kpi-card::before {
      content: '';
      position: absolute;
      top: 0; left: 0; right: 0; height: 2px;
      background: linear-gradient(90deg, transparent, var(--border-accent), transparent);
    }
    .kpi-label {
      font-size: 11px;
      color: var(--text-muted);
      letter-spacing: 0.05em;
      text-transform: uppercase;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .kpi-value {
      font-size: 28px;
      font-weight: 700;
      color: #fff;
      margin: 0.5rem 0 0.25rem 0;
      font-variant-numeric: tabular-nums;
    }
    .kpi-desc {
      font-size: 11px;
      color: var(--text-dim);
    }

    /* Generic Grid / Card Layout */
    .section-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(360px, 1fr));
      gap: 1.25rem;
      margin-bottom: 1.5rem;
    }
    .card {
      background: var(--bg-card);
      border: 1px solid var(--border);
      border-radius: 10px;
      padding: 1.25rem;
      backdrop-filter: blur(8px);
    }
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1rem;
      padding-bottom: 0.75rem;
      border-bottom: 1px solid var(--border);
    }
    .card-title {
      font-size: 14px;
      font-weight: 600;
      color: #fff;
      display: flex;
      align-items: center;
      gap: 0.5rem;
    }
    .card-subtitle {
      font-size: 11px;
      color: var(--text-muted);
      margin-top: 0.15rem;
    }

    /* Tables */
    .table-wrap {
      overflow-x: auto;
      margin: 0.5rem 0;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 12px;
      text-align: left;
    }
    th {
      color: var(--text-muted);
      font-weight: 600;
      padding: 0.65rem 0.75rem;
      border-bottom: 1px solid var(--border);
      font-size: 11px;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      white-space: nowrap;
    }
    td {
      padding: 0.65rem 0.75rem;
      border-bottom: 1px solid rgba(35, 43, 59, 0.5);
      color: var(--text);
      white-space: nowrap;
    }
    tr:hover td {
      background: rgba(255, 255, 255, 0.015);
    }

    /* Badges & Status */
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      padding: 0.15rem 0.5rem;
      border-radius: 4px;
      font-size: 10px;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .badge-admin { background: rgba(239, 68, 68, 0.15); color: #F87171; border: 1px solid rgba(239, 68, 68, 0.3); }
    .badge-chief { background: rgba(59, 130, 246, 0.15); color: #60A5FA; border: 1px solid rgba(59, 130, 246, 0.3); }
    .badge-organizer { background: rgba(245, 158, 11, 0.15); color: #FBBF24; border: 1px solid rgba(245, 158, 11, 0.3); }
    .badge-delegate { background: rgba(156, 163, 175, 0.12); color: #D1D5DB; border: 1px solid rgba(156, 163, 175, 0.25); }
    .badge-success { background: rgba(16, 185, 129, 0.15); color: #34D399; border: 1px solid rgba(16, 185, 129, 0.3); }
    .badge-warning { background: rgba(245, 158, 11, 0.15); color: #FBBF24; border: 1px solid rgba(245, 158, 11, 0.3); }
    .badge-danger { background: rgba(239, 68, 68, 0.15); color: #F87171; border: 1px solid rgba(239, 68, 68, 0.3); }

    /* Capacity Progress Bars */
    .progress-bar {
      height: 6px;
      background: #1A2232;
      border-radius: 3px;
      overflow: hidden;
      margin-top: 0.4rem;
    }
    .progress-fill {
      height: 100%;
      background: var(--accent);
      transition: width 0.3s ease;
    }
    .progress-fill.warning { background: var(--warning); }
    .progress-fill.danger { background: var(--danger); }

    /* Forms */
    .input {
      font-family: var(--font);
      background: var(--bg-surface);
      border: 1px solid var(--border);
      border-radius: 6px;
      padding: 0.45rem 0.75rem;
      color: var(--text);
      font-size: 12px;
      width: 100%;
      outline: none;
      transition: border-color 0.15s ease;
    }
    .input:focus {
      border-color: var(--accent);
      box-shadow: 0 0 0 1px var(--accent-glow);
    }
    .input-group {
      display: flex;
      gap: 0.5rem;
    }

    /* Modal Dialogs */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.75);
      backdrop-filter: blur(4px);
      z-index: 100;
      display: none;
      align-items: center;
      justify-content: center;
      padding: 1.5rem;
    }
    .modal-overlay.open {
      display: flex;
    }
    .modal {
      background: var(--bg-surface);
      border: 1px solid var(--border-accent);
      border-radius: 12px;
      width: 100%;
      max-width: 520px;
      padding: 1.5rem;
      box-shadow: 0 10px 40px rgba(0,0,0,0.8);
      position: relative;
    }
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.25rem;
    }
    .modal-title {
      font-size: 15px;
      font-weight: 700;
      color: #fff;
    }
    .modal-close {
      cursor: pointer;
      color: var(--text-muted);
      background: none;
      border: none;
      font-family: var(--font);
      font-size: 16px;
    }
    .modal-close:hover { color: #fff; }

    /* Toast Notifications */
    #toast {
      position: fixed;
      bottom: 1.5rem;
      right: 1.5rem;
      background: rgba(30, 41, 59, 0.9);
      backdrop-filter: blur(12px);
      border: 1px solid var(--border-accent);
      color: #fff;
      padding: 0.75rem 1.25rem;
      border-radius: 8px;
      font-size: 12px;
      box-shadow: 0 12px 35px rgba(0,0,0,0.7), 0 0 15px rgba(59, 130, 246, 0.15);
      display: none;
      align-items: center;
      gap: 0.6rem;
      z-index: 200;
      animation: slideUp 0.25s cubic-bezier(0.16, 1, 0.3, 1);
    }
    @keyframes slideUp {
      from { transform: translateY(16px) scale(0.96); opacity: 0; }
      to { transform: translateY(0) scale(1); opacity: 1; }
    }

    /* Auth gate overlay */
    #auth-gate {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: var(--bg);
      z-index: 999;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 1rem;
    }
    .login-box {
      width: 100%;
      max-width: 400px;
      background: var(--bg-surface);
      border: 1px solid var(--border-accent);
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 10px 40px rgba(0,0,0,0.8);
    }
  </style>
</head>
<body>
  <div class="ambient-glow"></div>

  <!-- Sign In Gate -->
  <div id="auth-gate">
    <div class="login-box">
      <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1.5rem;">
        <div class="brand-badge">M</div>
        <div>
          <h2 style="font-size: 16px; font-weight: 700; color: #fff;">MIANU-SM IV</h2>
          <p style="font-size: 11px; color: var(--text-muted);">Admin Operations Command Center</p>
        </div>
      </div>

      <form id="login-form" onsubmit="handleLogin(event)">
        <div style="margin-bottom: 1rem;">
          <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Username / Email</label>
          <input type="text" id="login-username" class="input" placeholder="admin@mianu.org" required>
        </div>
        <div style="margin-bottom: 1.5rem;">
          <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Password</label>
          <input type="password" id="login-password" class="input" placeholder="••••••••" required>
        </div>
        <button type="submit" id="login-btn" class="btn btn-primary" style="width: 100%; justify-content: center; padding: 0.6rem;">
          Sign In to Command Center
        </button>
        <p id="login-error" style="color: var(--danger); font-size: 11px; margin-top: 0.75rem; display: none;"></p>

        <div style="margin-top: 1.5rem; padding-top: 1rem; border-top: 1px solid var(--border); text-align: center;">
          <span style="font-size: 11px; color: var(--text-dim);">Conférence MIANU-SM IV • 2026</span>
        </div>
      </form>
    </div>
  </div>

  <!-- Command Center Header -->
  <header>
    <div class="brand">
      <div class="brand-badge">M</div>
      <div>
        <div class="brand-title">MIANU-SM IV • OPERATIONS COMMAND CENTER</div>
        <div class="brand-subtitle">Cloudflare Edge Engine • 2, 3 & 4 Octobre 2026</div>
      </div>
    </div>
    <div class="header-actions">
      <div class="live-status">
        <div class="live-pulse"></div>
        <span>EDGE TELEMETRY LIVE</span>
      </div>
      <div class="admin-pill" id="current-user-badge">
        <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-8 9-8 9s-8-4-8-9V5l8-3 8 3Z"/></svg></span>
        <span id="user-display-name">Secrétariat Général</span>
      </div>
      <button class="btn btn-sm" onclick="handleSignOut()">Sign Out</button>
    </div>
  </header>

  <!-- Navigation Bar with Custom SVG Icons -->
  <nav class="nav-bar">
    <a class="nav-tab active" onclick="switchView('overview')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/></svg></span>
      Overview
    </a>
    <a class="nav-tab" onclick="switchView('meals')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg></span>
      Meal Telemetry
    </a>
    <a class="nav-tab" onclick="switchView('locations')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg></span>
      Venue Locations
    </a>
    <a class="nav-tab" onclick="switchView('teams')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v8h4"/><path d="M18 9h2a2 2 0 0 1 2 2v11h-4"/><path d="M10 6h4"/><path d="M10 10h4"/><path d="M10 14h4"/><path d="M10 18h4"/></svg></span>
      Committees & Teams
    </a>
    <a class="nav-tab" onclick="switchView('broadcast')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m3 11 18-5v12L3 13v-2z"/><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/></svg></span>
      Broadcast Dispatch
    </a>
    <a class="nav-tab" onclick="switchView('logs')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>
      Event Stream
    </a>
    <a class="nav-tab" onclick="switchView('settings')">
      <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="4" x2="20" y1="21" y2="21"/><line x1="4" x2="20" y1="14" y2="14"/><line x1="4" x2="20" y1="7" y2="7"/><circle cx="14" cy="21" r="2"/><circle cx="8" cy="14" r="2"/><circle cx="16" cy="7" r="2"/></svg></span>
      Settings
    </a>
  </nav>

  <main>
    <!-- VIEW: OVERVIEW -->
    <div id="view-overview" class="view-container active">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Meals Served Today</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg></span>
          </div>
          <div class="kpi-value" id="kpi-meals">--</div>
          <div class="kpi-desc">Breakfast & lunch swipes recorded today</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-label">
            <span>Door Access Scans</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V6a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v14"/><path d="M2 20h20"/><path d="M14 12v.01"/></svg></span>
          </div>
          <div class="kpi-value" id="kpi-access">--</div>
          <div class="kpi-desc">Hall entry and exit permissions logged</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-label">
            <span>People Inside Venue</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg></span>
          </div>
          <div class="kpi-value" id="kpi-inside" style="color: #60A5FA;">--</div>
          <div class="kpi-desc">Live on-site occupancy in conference halls</div>
        </div>

        <div class="kpi-card">
          <div class="kpi-label">
            <span>Active Accounts</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></span>
          </div>
          <div class="kpi-value" id="kpi-active" style="color: #34D399;">--</div>
          <div class="kpi-desc">Provisioned delegates & staff in database</div>
        </div>
      </div>

      <div class="section-grid">
        <!-- Live Hall Capacities Widget -->
        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">
                <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg></span>
                Live Room Capacities
              </div>
              <div class="card-subtitle">Real-time room occupancy meters</div>
            </div>
            <button class="btn btn-sm" onclick="switchView('locations')">Manage Halls</button>
          </div>
          <div id="overview-halls-list" style="display: flex; flex-direction: column; gap: 0.75rem;">
            <div style="color: var(--text-dim); text-align: center; padding: 1rem;">Loading hall metrics…</div>
          </div>
        </div>

        <!-- Recent Activity Stream Widget -->
        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">
                <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>
                Recent Scans Stream
              </div>
              <div class="card-subtitle">Latest NFC door & meal events</div>
            </div>
            <button class="btn btn-sm" onclick="switchView('logs')">Full Stream</button>
          </div>
          <div id="overview-scans-list" style="display: flex; flex-direction: column; gap: 0.5rem;">
            <div style="color: var(--text-dim); text-align: center; padding: 1rem;">Loading events…</div>
          </div>
        </div>
      </div>
    </div>

    <!-- VIEW: MEALS TELEMETRY -->
    <div id="view-meals" class="view-container">
      <div class="kpi-grid">
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Breakfast Served</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="M20 12h2"/><path d="m19.07 4.93-1.41 1.41"/><path d="M15.95 16A6 6 0 0 0 6 12H4a8 8 0 0 1 15.95 4Z"/><path d="M2 20h20"/></svg></span>
          </div>
          <div class="kpi-value" id="meal-kpi-breakfast">--</div>
          <div class="kpi-desc">Morning distribution scans</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Lunch Served</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg></span>
          </div>
          <div class="kpi-value" id="meal-kpi-lunch">--</div>
          <div class="kpi-desc">Afternoon lunch scans</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Total Meals Served</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg></span>
          </div>
          <div class="kpi-value" id="meal-kpi-total">--</div>
          <div class="kpi-desc">Total meal distribution across conference</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Remaining Credits</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/></svg></span>
          </div>
          <div class="kpi-value" id="meal-kpi-remaining" style="color: #60A5FA;">--</div>
          <div class="kpi-desc">Unclaimed meal quota across all attendees</div>
        </div>
      </div>

      <!-- Manual Meal Terminal -->
      <div class="card" style="margin-bottom: 1.5rem;">
        <div class="card-header">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 2v7c0 1.1.9 2 2 2h4a2 2 0 0 0 2-2V2"/><path d="M7 2v20"/><path d="M21 15V2v0a5 5 0 0 0-5 5v6c0 1.1.9 2 2 2h3Zm0 0v7"/></svg></span>
              Manual Meal Override Scanner
            </div>
            <div class="card-subtitle">Dispatch meal credit deductions manually by card badge UID</div>
          </div>
        </div>
        <form id="manual-meal-form" onsubmit="handleManualMeal(event)" style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
          <select id="manual-meal-type" class="input" style="width: auto; min-width: 140px;">
            <option value="breakfast">Breakfast</option>
            <option value="lunch" selected>Lunch</option>
          </select>
          <input type="text" id="manual-meal-card" class="input" style="flex: 1; min-width: 220px;" placeholder="Badge Card UID (e.g. 04A2B3C4D5)" required>
          <button type="submit" class="btn btn-primary">Process Meal Swipe</button>
        </form>
      </div>

      <!-- Delegate Meal Allowances Table -->
      <div class="card">
        <div class="card-header">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></span>
              Delegate Meal Allowances & Balance Control
            </div>
            <div class="card-subtitle">Directly add (+1), remove (-1), or override attendee meal allowances</div>
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <input type="text" id="meal-search" class="input" placeholder="Search attendee…" oninput="filterMealTable()" style="width: 200px;">
          </div>
        </div>
        <div class="table-wrap">
          <table id="table-meals">
            <thead>
              <tr>
                <th>Attendee</th>
                <th>Role</th>
                <th>Committee</th>
                <th>Card UID</th>
                <th>Meals Remaining</th>
                <th style="text-align: right;">Allowance Adjustment</th>
              </tr>
            </thead>
            <tbody>
              <tr><td colspan="6" style="text-align:center; color: var(--text-dim);">Loading delegate records…</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- VIEW: VENUE LOCATIONS (WITH FULL CRUD) -->
    <div id="view-locations" class="view-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
        <div>
          <h2 style="font-size: 16px; font-weight: 700; color: #fff; display: flex; align-items: center; gap: 0.5rem;">
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z"/><circle cx="12" cy="10" r="3"/></svg></span>
            Conference Halls & Venue Locations
          </h2>
          <p style="font-size: 11px; color: var(--text-muted);">Manage rooms, set capacity thresholds, and track live occupancy</p>
        </div>
        <div style="display: flex; gap: 0.5rem;">
          <button class="btn btn-primary" onclick="openAddHallModal()">
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg></span>
            Add Location / Hall
          </button>
        </div>
      </div>

      <!-- Live Cards Grid for Halls -->
      <div id="locations-halls-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
        <div style="color: var(--text-dim); padding: 1rem;">Loading conference halls…</div>
      </div>

      <!-- Conference-Wide Attendance Telemetry (In Sum) -->
      <div class="kpi-grid" style="margin-bottom: 1.25rem;">
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Attendees in Sum</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg></span>
          </div>
          <div class="kpi-value" style="color: #34D399;"><span id="att-kpi-present">--</span> <span style="font-size: 16px; color: var(--text-muted); font-weight: normal;">/ <span id="att-kpi-total">--</span></span></div>
          <div class="kpi-desc"><strong id="att-kpi-rate" style="color: #60A5FA;">--%</strong> overall conference attendance on-site</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Absent / Awaiting Arrival</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" x2="12" y1="8" y2="12"/><line x1="12" x2="12.01" y1="16" y2="16"/></svg></span>
          </div>
          <div class="kpi-value" id="att-kpi-absent" style="color: #F87171;">--</div>
          <div class="kpi-desc">Registered attendees not yet checked in</div>
        </div>
        <div class="kpi-card">
          <div class="kpi-label">
            <span>Main Turnstile Swipes</span>
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 20V6a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v14"/><path d="M2 20h20"/><path d="M14 12v.01"/></svg></span>
          </div>
          <div class="kpi-value" id="att-kpi-swipes" style="color: #60A5FA;">--</div>
          <div class="kpi-desc">Entrance badge scans recorded today</div>
        </div>
      </div>

      <!-- Main Entrance Conference Attendance Swiper Terminal -->
      <div class="card" style="margin-bottom: 1.5rem;">
        <div class="card-header">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect width="20" height="14" x="2" y="5" rx="2"/><line x1="2" x2="22" y1="10" y2="10"/></svg></span>
              Main Entrance • NFC Conference Attendance Scanner
            </div>
            <div class="card-subtitle">Swipe attendee smartcard badge to register overall conference attendance (not hall-specific)</div>
          </div>
        </div>
        <form id="attendance-swipe-form" onsubmit="handleAttendanceSwipe(event)" style="display: flex; gap: 0.75rem; flex-wrap: wrap; align-items: center;">
          <select id="att-swipe-action" class="input" style="width: auto; min-width: 140px;">
            <option value="check_in" selected>Check In (Present)</option>
            <option value="check_out">Check Out (Absent)</option>
            <option value="toggle">Toggle Presence</option>
          </select>
          <input type="text" id="att-swipe-card" class="input" style="flex: 1; min-width: 220px;" placeholder="Badge Card UID (e.g. 04A2B3C4D5)" required>
          <select id="att-quick-picker" class="input" style="width: auto; min-width: 200px;" onchange="fillCardFromPicker(this.value)">
            <option value="">Quick Select Attendee…</option>
          </select>
          <button type="submit" class="btn btn-primary" id="att-swipe-btn">
            Register Attendance
          </button>
        </form>
        <div id="att-swipe-feedback" style="display: none; margin-top: 0.75rem; padding: 0.6rem 0.85rem; border-radius: 6px; font-size: 11px;"></div>
      </div>

      <!-- Conference Attendance & Venue Presence Directory -->
      <div class="card">
        <div class="card-header" style="flex-wrap: wrap; gap: 0.75rem;">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg></span>
              Conference Attendance & Venue Presence Directory
            </div>
            <div class="card-subtitle">Real-time attendance ledger with single and bulk administrator controls</div>
          </div>
          <div style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center;">
            <select id="att-filter-status" class="input" style="width: auto;" onchange="renderLocationsTable()">
              <option value="all">All Attendees</option>
              <option value="present">Present Only</option>
              <option value="absent">Absent Only</option>
            </select>
            <input type="text" id="loc-search" class="input" placeholder="Search person, room…" oninput="filterLocationsTable()" style="width: 180px;">
          </div>
        </div>

        <!-- Bulk Action Toolbar -->
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.65rem 0.85rem; background: var(--bg-surface); border: 1px solid var(--border); border-radius: 6px; margin-bottom: 0.75rem; flex-wrap: wrap; gap: 0.5rem;">
          <div style="display: flex; align-items: center; gap: 0.5rem; font-size: 11px; color: var(--text-muted);">
            <label style="display: flex; align-items: center; gap: 0.35rem; cursor: pointer;">
              <input type="checkbox" id="chk-select-all" onchange="toggleSelectAllAttendance(this.checked)">
              <span style="font-weight: 600; color: #fff;">Select All</span>
            </label>
            <span id="selected-att-count" style="color: #60A5FA; margin-left: 0.35rem;">(0 selected)</span>
          </div>
          <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
            <button class="btn btn-sm" onclick="handleBulkAttendance('present', false)" title="Mark selected attendees present">
              Mark Selected Present
            </button>
            <button class="btn btn-sm btn-danger" onclick="handleBulkAttendance('absent', false)" title="Mark selected attendees absent">
              Mark Selected Absent
            </button>
            <span style="color: var(--border-accent); margin: 0 0.2rem; align-self: center;">|</span>
            <button class="btn btn-sm" style="background: rgba(16, 185, 129, 0.2); border-color: rgba(16, 185, 129, 0.4); color: #34D399;" onclick="handleBulkAttendance('present', true)" title="Mark all attendees present">
              Mark ALL Present
            </button>
            <button class="btn btn-sm" style="background: rgba(239, 68, 68, 0.15); border-color: rgba(239, 68, 68, 0.3); color: #F87171;" onclick="handleBulkAttendance('absent', true)" title="Reset all attendees to absent">
              Reset ALL Absent
            </button>
          </div>
        </div>

        <div class="table-wrap">
          <table id="table-locations">
            <thead>
              <tr>
                <th style="width: 32px;"><input type="checkbox" id="chk-head" onchange="toggleSelectAllAttendance(this.checked)"></th>
                <th>Attendee</th>
                <th>Role</th>
                <th>Committee</th>
                <th>Conference Attendance</th>
                <th>Check-In Time</th>
                <th>Current Room</th>
                <th style="text-align: right;">Admin Action</th>
              </tr>
            </thead>
            <tbody>
              <tr><td colspan="8" style="text-align:center; color: var(--text-dim);">Loading presence…</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- VIEW: COMMITTEES & TEAMS (WITH FULL CRUD) -->
    <div id="view-teams" class="view-container">
      <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
        <div>
          <h2 style="font-size: 16px; font-weight: 700; color: #fff; display: flex; align-items: center; gap: 0.5rem;">
            <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z"/><path d="M6 12H4a2 2 0 0 0-2 2v8h4"/><path d="M18 9h2a2 2 0 0 1 2 2v11h-4"/></svg></span>
            Delegation Committees & Rosters
          </h2>
          <p style="font-size: 11px; color: var(--text-muted);">Manage conference committees, member quotas, and delegate assignments</p>
        </div>
        <button class="btn btn-primary" onclick="openCreateTeamModal()">
          <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg></span>
          Add Committee
        </button>
      </div>

      <div id="teams-grid" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 1.25rem;">
        <div style="color: var(--text-dim); padding: 1rem;">Loading committees…</div>
      </div>
    </div>

    <!-- VIEW: BROADCAST DISPATCH -->
    <div id="view-broadcast" class="view-container">
      <div class="section-grid">
        <!-- Composer -->
        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">
                <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="m3 11 18-5v12L3 13v-2z"/><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/></svg></span>
                Broadcast Dispatcher
              </div>
              <div class="card-subtitle">Dispatch announcements to all attendees or targeted teams</div>
            </div>
          </div>
          <form id="broadcast-form" onsubmit="handleSendBroadcast(event)" style="display: flex; flex-direction: column; gap: 1rem;">
            <div>
              <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Audience Target</label>
              <select id="broadcast-audience" class="input" onchange="toggleBroadcastTarget()">
                <option value="all">All Conference Attendees</option>
                <option value="team">Specific Committee / Team</option>
                <option value="individual">Individual Attendee</option>
              </select>
            </div>
            <div id="broadcast-team-row" style="display: none;">
              <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Target Committee</label>
              <select id="broadcast-team-id" class="input"></select>
            </div>
            <div id="broadcast-user-row" style="display: none;">
              <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Target Attendee</label>
              <select id="broadcast-user-id" class="input"></select>
            </div>
            <div>
              <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Announcement Message</label>
              <textarea id="broadcast-message" class="input" rows="4" placeholder="Enter formal conference announcement…" required></textarea>
            </div>
            <div style="display: flex; justify-content: flex-end;">
              <button type="submit" class="btn btn-primary">Dispatch Announcement</button>
            </div>
          </form>
        </div>

        <!-- Dispatched Feed -->
        <div class="card">
          <div class="card-header">
            <div>
              <div class="card-title">
                <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>
                Broadcast Feed History
              </div>
              <div class="card-subtitle">Past dispatched conference announcements</div>
            </div>
          </div>
          <div id="broadcast-history" style="display: flex; flex-direction: column; gap: 0.75rem; max-height: 500px; overflow-y: auto;">
            <div style="color: var(--text-dim); padding: 1rem; text-align: center;">Loading broadcast history…</div>
          </div>
        </div>
      </div>
    </div>

    <!-- VIEW: EVENT LOGS -->
    <div id="view-logs" class="view-container">
      <div class="card">
        <div class="card-header">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg></span>
              Real-Time NFC Event Stream
            </div>
            <div class="card-subtitle">Chronological ledger of meal swipes and access control doors</div>
          </div>
          <div style="display: flex; gap: 0.5rem;">
            <select id="log-filter" class="input" style="width: auto;" onchange="fetchActivity()">
              <option value="all">All Events</option>
              <option value="meals">Meal Swipes Only</option>
              <option value="access">Door Access Only</option>
            </select>
            <button class="btn btn-sm" onclick="fetchActivity()">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"/><path d="M16 21h5v-5"/></svg></span>
              Refresh
            </button>
          </div>
        </div>
        <div class="table-wrap">
          <table id="table-logs">
            <thead>
              <tr>
                <th>Event Type</th>
                <th>Attendee</th>
                <th>Role</th>
                <th>Location / Service</th>
                <th>Status</th>
                <th>Timestamp</th>
              </tr>
            </thead>
            <tbody>
              <tr><td colspan="6" style="text-align:center; color: var(--text-dim);">Loading stream…</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- VIEW: SETTINGS -->
    <div id="view-settings" class="view-container">
      <div class="card" style="max-width: 680px; margin-bottom: 1.5rem;">
        <div class="card-header">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="4" x2="20" y1="21" y2="21"/><line x1="4" x2="20" y1="14" y2="14"/><line x1="4" x2="20" y1="7" y2="7"/><circle cx="14" cy="21" r="2"/><circle cx="8" cy="14" r="2"/><circle cx="16" cy="7" r="2"/></svg></span>
              Service Schedule & Meal Windows
            </div>
            <div class="card-subtitle">Define valid operational hours for meal swipe acceptance</div>
          </div>
        </div>
        <form id="settings-form" onsubmit="handleSaveMealWindows(event)" style="display: flex; flex-direction: column; gap: 1.25rem;">
          <div>
            <div style="font-weight: 600; color: #fff; margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.4rem;">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2v2"/><path d="m4.93 4.93 1.41 1.41"/><path d="M20 12h2"/><path d="m19.07 4.93-1.41 1.41"/><path d="M15.95 16A6 6 0 0 0 6 12H4a8 8 0 0 1 15.95 4Z"/><path d="M2 20h20"/></svg></span>
              Breakfast Service Window
            </div>
            <div class="input-group">
              <input type="text" id="win-b-start" class="input" placeholder="07:00" required>
              <span style="color: var(--text-dim); align-self: center;">to</span>
              <input type="text" id="win-b-end" class="input" placeholder="10:00" required>
            </div>
          </div>
          <div>
            <div style="font-weight: 600; color: #fff; margin-bottom: 0.5rem; display: flex; align-items: center; gap: 0.4rem;">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg></span>
              Lunch Service Window
            </div>
            <div class="input-group">
              <input type="text" id="win-l-start" class="input" placeholder="12:00" required>
              <span style="color: var(--text-dim); align-self: center;">to</span>
              <input type="text" id="win-l-end" class="input" placeholder="15:00" required>
            </div>
          </div>
          <div style="display: flex; justify-content: flex-end;">
            <button type="submit" class="btn btn-primary">Save Operational Windows</button>
          </div>
        </form>
      </div>

      <div class="card" style="max-width: 680px;">
        <div class="card-header">
          <div>
            <div class="card-title">
              <span class="icon"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 13c0 5-8 9-8 9s-8-4-8-9V5l8-3 8 3Z"/></svg></span>
              Security & Environment Status
            </div>
            <div class="card-subtitle">Production Edge Worker deployment telemetry</div>
          </div>
        </div>
        <div style="display: flex; flex-direction: column; gap: 0.75rem; font-size: 12px;">
          <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem;">
            <span style="color: var(--text-muted);">Database Engine</span>
            <span style="font-weight: 600; color: #fff;">Cloudflare D1 Serverless SQL (mianu-org-db)</span>
          </div>
          <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem;">
            <span style="color: var(--text-muted);">Runtime Target</span>
            <span style="font-weight: 600; color: #fff;">Cloudflare Workers V8 Isolates</span>
          </div>
          <div style="display: flex; justify-content: space-between; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem;">
            <span style="color: var(--text-muted);">API Base</span>
            <span style="font-weight: 600; color: #60A5FA;">/api/v1 (CORS Wildcard Enabled)</span>
          </div>
          <div style="display: flex; justify-content: space-between;">
            <span style="color: var(--text-muted);">Authentication Standard</span>
            <span style="font-weight: 600; color: #34D399;">PBKDF2-SHA256 (100,000 Iterations)</span>
          </div>
        </div>
      </div>
    </div>
  </main>

  <!-- MODAL: ADD / EDIT LOCATION (HALL) -->
  <div id="modal-hall" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <div class="modal-title" id="hall-modal-title">Add Conference Location / Hall</div>
        <button class="modal-close" onclick="closeModal('modal-hall')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
      </div>
      <form id="hall-form" onsubmit="handleSaveHall(event)" style="display: flex; flex-direction: column; gap: 0.85rem;">
        <input type="hidden" id="hall-edit-id">
        <div>
          <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Hall / Room Name</label>
          <input type="text" id="hall-name" class="input" placeholder="e.g. Salle Plénière (General Assembly)" required>
        </div>
        <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.75rem;">
          <div>
            <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Capacity Threshold</label>
            <input type="number" id="hall-cap" class="input" value="50" min="1" required>
          </div>
          <div>
            <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Current Occupancy</label>
            <input type="number" id="hall-occ" class="input" value="0" min="0" required>
          </div>
        </div>
        <div>
          <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.35rem;">Allowed Roles (Access Control)</label>
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.4rem; font-size: 11px;">
            <label><input type="checkbox" id="role-chk-user" checked> Delegates (User)</label>
            <label><input type="checkbox" id="role-chk-organizer" checked> Organizers</label>
            <label><input type="checkbox" id="role-chk-admin" checked> Administrators</label>
            <label><input type="checkbox" id="role-chk-chief" checked> Chief Organizers</label>
            <label><input type="checkbox" id="role-chk-lead" checked> Team Leaders</label>
            <label><input type="checkbox" id="role-chk-member" checked> Team Members</label>
          </div>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <button type="button" class="btn btn-secondary" onclick="closeModal('modal-hall')">Cancel</button>
          <button type="submit" class="btn btn-primary" id="hall-submit-btn">Save Location</button>
        </div>
      </form>
    </div>
  </div>

  <!-- MODAL: ADD / EDIT COMMITTEE TEAM -->
  <div id="modal-team" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <div class="modal-title" id="team-modal-title">Add Committee / Team</div>
        <button class="modal-close" onclick="closeModal('modal-team')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
      </div>
      <form id="create-team-form" onsubmit="handleSaveTeam(event)" style="display: flex; flex-direction: column; gap: 0.85rem;">
        <input type="hidden" id="team-edit-id">
        <div>
          <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Committee Name</label>
          <input type="text" id="new-team-name" class="input" placeholder="e.g. CIJ (Cour Internationale de Justice)" required>
        </div>
        <div>
          <label style="font-size: 11px; color: var(--text-muted); display: block; margin-bottom: 0.25rem;">Seat Capacity</label>
          <input type="number" id="new-team-cap" class="input" value="30" min="0" required>
        </div>
        <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
          <button type="button" class="btn btn-secondary" onclick="closeModal('modal-team')">Cancel</button>
          <button type="submit" class="btn btn-primary" id="team-submit-btn">Save Committee</button>
        </div>
      </form>
    </div>
  </div>

  <!-- MODAL: VIEW COMMITTEE MEMBERS -->
  <div id="modal-team-members" class="modal-overlay">
    <div class="modal" style="max-width: 600px;">
      <div class="modal-header">
        <div class="modal-title" id="team-members-modal-title">Committee Members</div>
        <button class="modal-close" onclick="closeModal('modal-team-members')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
      </div>
      <div id="team-members-list" style="max-height: 400px; overflow-y: auto; display: flex; flex-direction: column; gap: 0.5rem; margin-bottom: 1rem;">
        <div style="color: var(--text-dim); text-align: center; padding: 1rem;">Loading members…</div>
      </div>
      <div style="display: flex; justify-content: flex-end;">
        <button type="button" class="btn btn-secondary" onclick="closeModal('modal-team-members')">Close</button>
      </div>
    </div>
  </div>

  <!-- MODAL: SET EXACT MEAL ALLOWANCE -->
  <div id="modal-adjust-meals" class="modal-overlay">
    <div class="modal">
      <div class="modal-header">
        <div class="modal-title">Override Meal Allowance</div>
        <button class="modal-close" onclick="closeModal('modal-adjust-meals')"><svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg></button>
      </div>
      <div id="adjust-meals-content"></div>
    </div>
  </div>

  <!-- Toast Notification Bar -->
  <div id="toast">
    <span id="toast-text">Message</span>
  </div>

  <!-- Command Center Core Logic -->
  <script>
    const API_BASE = '/api/v1';

    const APP_STATE = {
      user: null,
      token: null,
      overview: null,
      halls: [],
      users: [],
      teams: [],
      locations: [],
      activity: [],
      announcements: [],
      attendance: [],
      attendanceSummary: null,
      selectedAttendees: new Set()
    };

    /* Inline SVG Icon Constants */
    const SVG = {
      edit: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/><path d="m15 5 4 4"/></svg>',
      trash: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 6h18"/><path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"/><path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/><line x1="10" x2="10" y1="11" y2="17"/><line x1="14" x2="14" y1="11" y2="17"/></svg>',
      plus: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>',
      minus: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="5" y1="12" x2="19" y2="12"/></svg>',
      shield: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M20 13c0 5-8 9-8 9s-8-4-8-9V5l8-3 8 3Z"/></svg>',
      users: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>',
      refresh: '<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/><path d="M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16"/><path d="M16 21h5v-5"/></svg>'
    };

    function showToast(msg, isError = false) {
      const toast = document.getElementById('toast');
      const text = document.getElementById('toast-text');
      text.textContent = msg;
      toast.style.borderColor = isError ? 'var(--danger)' : 'var(--border-accent)';
      toast.style.color = isError ? '#F87171' : '#fff';
      toast.style.display = 'flex';
      setTimeout(() => { toast.style.display = 'none'; }, 3500);
    }

    async function api(path, options = {}) {
      const headers = {
        'Content-Type': 'application/json',
        ...(APP_STATE.token ? { 'Authorization': 'Bearer ' + APP_STATE.token } : {}),
        ...(options.headers || {})
      };

      const res = await fetch(API_BASE + path, {
        ...options,
        headers,
        body: options.body ? JSON.stringify(options.body) : undefined
      });

      if (res.status === 204) return null;
      const data = await res.json().catch(() => null);

      if (!res.ok) {
        throw new Error(data?.detail || 'API request failed (' + res.status + ')');
      }
      return data;
    }

    /* Auth & Session */
    function quickFill(user, pass) {
      document.getElementById('login-username').value = user;
      document.getElementById('login-password').value = pass;
    }

    async function handleLogin(e) {
      e.preventDefault();
      const u = document.getElementById('login-username').value.trim();
      const p = document.getElementById('login-password').value;
      const err = document.getElementById('login-error');
      const btn = document.getElementById('login-btn');

      err.style.display = 'none';
      btn.disabled = true;
      btn.textContent = 'Verifying Credentials…';

      try {
        const session = await api('/auth/login', {
          method: 'POST',
          body: { username: u, password: p }
        });

        APP_STATE.token = session.access_token;
        APP_STATE.user = session;
        sessionStorage.setItem('mianu_admin_session', JSON.stringify(session));

        document.getElementById('auth-gate').style.display = 'none';
        document.getElementById('user-display-name').textContent = session.name || 'Admin';
        showToast('Authenticated successfully as ' + session.name);

        initDashboard();
      } catch (error) {
        err.textContent = error.message;
        err.style.display = 'block';
      } finally {
        btn.disabled = false;
        btn.textContent = 'Sign In to Command Center';
      }
    }

    function handleSignOut() {
      sessionStorage.removeItem('mianu_admin_session');
      APP_STATE.token = null;
      APP_STATE.user = null;
      document.getElementById('auth-gate').style.display = 'flex';
    }

    /* Navigation & Tabs */
    function switchView(viewName) {
      document.querySelectorAll('.nav-tab').forEach(tab => tab.classList.remove('active'));
      document.querySelectorAll('.view-container').forEach(view => view.classList.remove('active'));
      
      const targetView = document.getElementById('view-' + viewName);
      if (targetView) targetView.classList.add('active');

      const tabs = Array.from(document.querySelectorAll('.nav-tab'));
      const activeTab = tabs.find(t => t.textContent.toLowerCase().includes(viewName));
      if (activeTab) activeTab.classList.add('active');

      refreshCurrentView(viewName);
    }

    function refreshCurrentView(viewName) {
      if (viewName === 'overview') fetchOverview();
      else if (viewName === 'meals') { fetchUsers(); fetchOverview(); }
      else if (viewName === 'locations') { fetchLocations(); fetchHalls(); fetchAttendance(); }
      else if (viewName === 'teams') fetchTeams();
      else if (viewName === 'broadcast') { fetchAnnouncements(); fetchTeams(); fetchUsers(); }
      else if (viewName === 'logs') fetchActivity();
      else if (viewName === 'settings') fetchMealWindows();
    }

    /* Modals */
    function closeModal(id) { document.getElementById(id).classList.remove('open'); }
    function openModal(id) { document.getElementById(id).classList.add('open'); }

    /* Fetchers */
    async function fetchOverview() {
      try {
        const data = await api('/dashboard/overview');
        APP_STATE.overview = data;
        document.getElementById('kpi-meals').textContent = data.total_meals_today;
        document.getElementById('kpi-access').textContent = data.total_access_scans_today;
        document.getElementById('kpi-inside').textContent = data.people_inside_halls;
        document.getElementById('kpi-active').textContent = data.active_users;

        // Also update meals view KPI
        document.getElementById('meal-kpi-total').textContent = data.total_meals_today;
        
        await fetchHalls();
        await fetchActivity();
      } catch (err) { console.error('Overview fetch failed:', err); }
    }

    async function fetchHalls() {
      try {
        const halls = await api('/access/halls');
        APP_STATE.halls = halls;
        renderOverviewHalls(halls);
        renderLocationsHalls(halls);
      } catch (err) { console.error('Halls fetch failed:', err); }
    }

    async function fetchUsers() {
      try {
        const users = await api('/users');
        APP_STATE.users = users;
        renderMealsTable();
        populateBroadcastUsers(users);

        // Update remaining meals stat
        let totalRemaining = users.reduce((acc, u) => acc + (u.meals_balance || 0), 0);
        document.getElementById('meal-kpi-remaining').textContent = totalRemaining;
      } catch (err) { console.error('Users fetch failed:', err); }
    }

    async function fetchTeams() {
      try {
        const teams = await api('/teams');
        APP_STATE.teams = teams;
        renderTeams(teams);
        populateTeamSelects(teams);
      } catch (err) { console.error('Teams fetch failed:', err); }
    }

    async function fetchLocations() {
      try {
        const data = await api('/operations/locations');
        APP_STATE.locations = data;
        renderLocationsTable();
      } catch (err) { console.error('Locations fetch failed:', err); }
    }

    async function fetchAttendance() {
      try {
        const data = await api('/attendance');
        APP_STATE.attendance = data.attendees || [];
        APP_STATE.attendanceSummary = data.summary || null;

        if (data.summary) {
          const s = data.summary;
          const pEl = document.getElementById('att-kpi-present');
          const tEl = document.getElementById('att-kpi-total');
          const rEl = document.getElementById('att-kpi-rate');
          const aEl = document.getElementById('att-kpi-absent');
          const swEl = document.getElementById('att-kpi-swipes');

          if (pEl) pEl.textContent = s.total_present;
          if (tEl) tEl.textContent = s.total_registered;
          if (rEl) rEl.textContent = s.attendance_rate + '%';
          if (aEl) aEl.textContent = s.total_absent;
          if (swEl) swEl.textContent = s.total_swipes !== undefined ? s.total_swipes : '0';
        }

        populateAttendancePicker(data.attendees || []);
      } catch (err) { console.error('Attendance fetch failed:', err); }
    }

    async function fetchActivity() {
      const filter = document.getElementById('log-filter') ? document.getElementById('log-filter').value : 'all';
      try {
        const data = await api('/operations/activity?type=' + filter);
        const events = Array.isArray(data) ? data : (data.events || []);
        APP_STATE.activity = events;
        renderOverviewScans(events.slice(0, 5));
        renderLogsTable(events);

        // Compute breakfast vs lunch today
        let bCount = 0, lCount = 0;
        events.forEach(e => {
          if (e.scan_type === 'meal_swipe') {
            if (e.location_or_service && e.location_or_service.toLowerCase().includes('breakfast')) bCount++;
            else if (e.location_or_service && e.location_or_service.toLowerCase().includes('lunch')) lCount++;
          }
        });
        document.getElementById('meal-kpi-breakfast').textContent = bCount;
        document.getElementById('meal-kpi-lunch').textContent = lCount;
      } catch (err) { console.error('Activity fetch failed:', err); }
    }

    async function fetchAnnouncements() {
      try {
        const data = await api('/notifications?limit=25');
        APP_STATE.announcements = data;
        renderAnnouncements(data);
      } catch (err) { console.error('Announcements fetch failed:', err); }
    }

    async function fetchMealWindows() {
      try {
        const data = await api('/settings/meal-windows');
        document.getElementById('win-b-start').value = data.breakfast_start || '07:00';
        document.getElementById('win-b-end').value = data.breakfast_end || '10:00';
        document.getElementById('win-l-start').value = data.lunch_start || '12:00';
        document.getElementById('win-l-end').value = data.lunch_end || '15:00';
      } catch (err) { console.error('Meal windows fetch failed:', err); }
    }

    /* Renderers */
    function renderOverviewHalls(halls) {
      const c = document.getElementById('overview-halls-list');
      if (!c) return;
      if (!halls.length) { c.innerHTML = '<div style="color: var(--text-dim);">No halls configured</div>'; return; }

      c.innerHTML = halls.map(h => {
        const ratio = Math.min(100, Math.round(((h.current_occupancy || 0) / (h.capacity_threshold || 1)) * 100));
        const isDanger = ratio >= 95;
        const isWarning = ratio >= 80 && !isDanger;
        const fillClass = isDanger ? 'danger' : isWarning ? 'warning' : '';
        const badgeClass = isDanger ? 'badge-danger' : isWarning ? 'badge-warning' : 'badge-success';
        const badgeText = isDanger ? 'AT CAPACITY' : isWarning ? 'FILLING UP' : 'AVAILABLE';

        return \`
          <div style="background: var(--bg-surface); border: 1px solid var(--border); border-radius: 8px; padding: 0.85rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.25rem;">
              <span style="font-weight: 600; color: #fff;">\${h.name}</span>
              <span class="badge \${badgeClass}">\${badgeText}</span>
            </div>
            <div style="display: flex; justify-content: space-between; font-size: 11px; color: var(--text-muted); margin-top: 0.35rem;">
              <span>Inside: <strong>\${h.current_occupancy || 0}</strong></span>
              <span>Capacity: \${h.capacity_threshold} (\${ratio}%)</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill \${fillClass}" style="width: \${ratio}%;"></div>
            </div>
          </div>
        \`;
      }).join('');
    }

    function renderLocationsHalls(halls) {
      const grid = document.getElementById('locations-halls-grid');
      if (!grid) return;
      if (!halls.length) {
        grid.innerHTML = '<div style="color: var(--text-dim); grid-column: 1/-1;">No halls created yet. Click Add Location above to provision one.</div>';
        return;
      }

      grid.innerHTML = halls.map(h => {
        const occ = h.current_occupancy || 0;
        const cap = h.capacity_threshold || 1;
        const ratio = Math.min(100, Math.round((occ / cap) * 100));
        const isDanger = ratio >= 95;
        const isWarning = ratio >= 80 && !isDanger;
        const fillClass = isDanger ? 'danger' : isWarning ? 'warning' : '';
        const badgeClass = isDanger ? 'badge-danger' : isWarning ? 'badge-warning' : 'badge-success';
        const badgeText = isDanger ? 'AT CAPACITY' : isWarning ? 'FILLING UP' : 'AVAILABLE';

        return \`
          <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                <div style="font-weight: 700; font-size: 14px; color: #fff;">\${h.name}</div>
                <span class="badge \${badgeClass}">\${badgeText}</span>
              </div>
              <div style="font-size: 11px; color: var(--text-muted); margin-bottom: 0.75rem;">
                Permitted: \${(h.allowed_roles || []).join(', ') || 'All Roles'}
              </div>
              <div style="display: flex; justify-content: space-between; font-size: 12px; margin-bottom: 0.25rem;">
                <span>Current Occupancy: <strong style="color: #fff;">\${occ}</strong></span>
                <span>Max Limit: \${cap} (\${ratio}%)</span>
              </div>
              <div class="progress-bar" style="margin-bottom: 1rem;">
                <div class="progress-fill \${fillClass}" style="width: \${ratio}%;"></div>
              </div>
            </div>

            <!-- Quick Occupancy & CRUD Controls -->
            <div style="display: flex; justify-content: space-between; align-items: center; pt: 0.5rem; border-top: 1px solid var(--border); padding-top: 0.75rem;">
              <div style="display: flex; gap: 0.35rem;">
                <button class="btn btn-sm" onclick="adjustHallOccupancy('\${h.id}', -1)" title="Remove 1 person">\${SVG.minus} 1</button>
                <button class="btn btn-sm" onclick="adjustHallOccupancy('\${h.id}', 1)" title="Add 1 person">\${SVG.plus} 1</button>
                <button class="btn btn-sm" onclick="resetHallOccupancy('\${h.id}')" title="Reset occupancy to 0">Reset</button>
              </div>
              <div style="display: flex; gap: 0.35rem;">
                <button class="btn btn-sm" onclick="openEditHallModal('\${h.id}')" title="Edit Hall">\${SVG.edit}</button>
                <button class="btn btn-sm btn-danger" onclick="deleteHall('\${h.id}', '\${h.name.replace(/'/g, "\\\\'")}')" title="Delete Hall">\${SVG.trash}</button>
              </div>
            </div>
          </div>
        \`;
      }).join('');
    }

    function renderOverviewScans(events) {
      const c = document.getElementById('overview-scans-list');
      if (!c) return;
      if (!events.length) { c.innerHTML = '<div style="color: var(--text-dim);">No scan telemetry available</div>'; return; }

      c.innerHTML = events.map(e => {
        const isMeal = e.scan_type === 'meal_swipe';
        const typeLabel = isMeal ? 'MEAL SWIPE' : e.scan_type.replace('_', ' ').toUpperCase();
        const badgeStyle = isMeal ? 'badge-warning' : 'badge-chief';
        const timeStr = new Date(e.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });

        return \`
          <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--bg-surface); border: 1px solid var(--border); border-radius: 6px; font-size: 11px;">
            <div style="display: flex; align-items: center; gap: 0.5rem;">
              <span class="badge \${badgeStyle}">\${typeLabel}</span>
              <span style="color: #fff; font-weight: 500;">\${e.delegate_name}</span>
            </div>
            <div style="display: flex; align-items: center; gap: 0.75rem; color: var(--text-muted);">
              <span>\${e.location_or_service}</span>
              <span style="color: var(--text-dim);">\${timeStr}</span>
            </div>
          </div>
        \`;
      }).join('');
    }

    function renderMealsTable() {
      const tbody = document.querySelector('#table-meals tbody');
      if (!tbody) return;

      const q = (document.getElementById('meal-search')?.value || '').toLowerCase();
      const filtered = (APP_STATE.users || []).filter(u => {
        return (u.name || '').toLowerCase().includes(q) || (u.email || '').toLowerCase().includes(q) || (u.card_uid || '').toLowerCase().includes(q);
      });

      if (!filtered.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-dim); padding: 1.5rem;">No attendee records found matching search.</td></tr>';
        return;
      }

      tbody.innerHTML = filtered.map(u => {
        const remaining = typeof u.meals_balance === 'number' ? u.meals_balance : 6;
        const total = u.meal_allowance || 6;
        const roleBadge = getRoleBadge(u.role);
        const cardDisplay = u.card_uid ? \`<span style="color: #60A5FA; font-family: monospace;">\${u.card_uid}</span>\` : '<span style="color: var(--text-dim);">Unlinked</span>';

        return \`
          <tr>
            <td>
              <div style="font-weight: 600; color: #fff;">\${u.name}</div>
              <div style="font-size: 11px; color: var(--text-dim);">\${u.email || 'No email'}</div>
            </td>
            <td>\${roleBadge}</td>
            <td style="color: var(--text-muted);">\${u.team_name || 'Individual'}</td>
            <td>\${cardDisplay}</td>
            <td>
              <strong style="color: \${remaining <= 1 ? 'var(--danger)' : 'var(--success)'}; font-size: 14px;">\${remaining}</strong>
              <span style="color: var(--text-dim);"> / \${total}</span>
            </td>
            <td style="text-align: right;">
              <div style="display: inline-flex; gap: 0.35rem;">
                <button class="btn btn-sm" onclick="adjustMeals('\${u.id}', -1)" title="Deduct 1 meal">\${SVG.minus} 1</button>
                <button class="btn btn-sm" onclick="adjustMeals('\${u.id}', 1)" title="Add 1 meal">\${SVG.plus} 1</button>
                <button class="btn btn-sm" onclick="openSetMealsModal('\${u.id}', '\${u.name.replace(/'/g, "\\\\'")}', \${remaining})">Set</button>
              </div>
            </td>
          </tr>
        \`;
      }).join('');
    }

    function renderLocationsTable() {
      const tbody = document.querySelector('#table-locations tbody');
      if (!tbody) return;

      const q = (document.getElementById('loc-search')?.value || '').toLowerCase();
      const statusFilter = document.getElementById('att-filter-status')?.value || 'all';

      const filtered = (APP_STATE.locations || []).filter(l => {
        const matchesQuery = (l.name || '').toLowerCase().includes(q) || 
                             (l.current_hall_name || '').toLowerCase().includes(q) || 
                             (l.card_uid || '').toLowerCase().includes(q) ||
                             (l.team_name || '').toLowerCase().includes(q) ||
                             (l.role || '').toLowerCase().includes(q);
        const matchesStatus = statusFilter === 'all' || l.attendance_status === statusFilter;
        return matchesQuery && matchesStatus;
      });

      if (!filtered.length) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; color: var(--text-dim); padding: 1.5rem;">No attendee attendance or location records found.</td></tr>';
        return;
      }

      // Sync select all headers
      const allFilteredSelected = filtered.length > 0 && filtered.every(l => APP_STATE.selectedAttendees.has(l.user_id));
      const chkHead = document.getElementById('chk-head');
      const chkSelectAll = document.getElementById('chk-select-all');
      if (chkHead) chkHead.checked = allFilteredSelected;
      if (chkSelectAll) chkSelectAll.checked = allFilteredSelected;

      tbody.innerHTML = filtered.map(l => {
        const isSelected = APP_STATE.selectedAttendees.has(l.user_id);
        const isPresent = l.attendance_status === 'present';
        const attendanceBadge = isPresent 
          ? '<span class="badge badge-success"><span style="width:6px;height:6px;border-radius:50%;background:#10B981;display:inline-block;box-shadow:0 0 6px #10B981;margin-right:4px;"></span>PRESENT</span>'
          : '<span class="badge badge-danger"><span style="width:6px;height:6px;border-radius:50%;background:#EF4444;display:inline-block;margin-right:4px;"></span>ABSENT</span>';

        const roomDisplay = l.current_hall_name ? \`<strong style="color: #60A5FA;">\${l.current_hall_name}</strong>\` : '<span style="color: var(--text-dim);">Not in hall</span>';
        const checkInTimeStr = l.attendance_time 
          ? new Date(l.attendance_time).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) + ' (' + (l.attendance_method || 'nfc') + ')' 
          : '<span style="color: var(--text-dim);">--</span>';
        const cardDisplay = l.card_uid 
          ? \`<span style="color: #60A5FA; font-family: monospace;">\${l.card_uid}</span>\` 
          : '<span style="color: var(--text-dim);">No Card</span>';

        const actionBtn = isPresent
          ? \`<button class="btn btn-sm btn-danger" onclick="setSingleAttendance('\${l.user_id}', 'absent')" title="Mark Absent">Mark Absent</button>\`
          : \`<button class="btn btn-sm btn-success" onclick="setSingleAttendance('\${l.user_id}', 'present')" title="Mark Present">Mark Present</button>\`;

        return \`
          <tr style="\${isSelected ? 'background: rgba(59, 130, 246, 0.05);' : ''}">
            <td>
              <input type="checkbox" class="chk-att-row" \${isSelected ? 'checked' : ''} onchange="toggleAttendeeSelect('\${l.user_id}', this.checked)">
            </td>
            <td>
              <div style="font-weight: 600; color: #fff;">\${l.name}</div>
              <div style="font-size: 11px; color: var(--text-dim);">\${cardDisplay}</div>
            </td>
            <td>\${getRoleBadge(l.role)}</td>
            <td style="color: var(--text-muted);">\${l.team_name || 'Individual'}</td>
            <td>\${attendanceBadge}</td>
            <td style="color: var(--text-muted); font-size: 11px;">\${checkInTimeStr}</td>
            <td>\${roomDisplay}</td>
            <td style="text-align: right;">
              \${actionBtn}
            </td>
          </tr>
        \`;
      }).join('');
    }

    function renderTeams(teams) {
      const grid = document.getElementById('teams-grid');
      if (!grid) return;
      if (!teams.length) { grid.innerHTML = '<div style="color: var(--text-dim); grid-column: 1/-1;">No committees configured. Click Add Committee to create one.</div>'; return; }

      grid.innerHTML = teams.map(t => {
        const size = t.current_size || 0;
        const cap = t.capacity || 0;
        const ratio = cap > 0 ? Math.min(100, Math.round((size / cap) * 100)) : 0;
        const fillClass = ratio >= 90 ? 'danger' : ratio >= 75 ? 'warning' : '';

        return \`
          <div class="card" style="display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
                <div style="font-weight: 700; font-size: 14px; color: #fff;">\${t.name}</div>
                <span class="badge badge-chief">\${size} / \${cap || '∞'}</span>
              </div>
              <div style="font-size: 11px; color: var(--text-muted); margin-bottom: 0.5rem;">
                Roster Allocation: \${size} delegate members
              </div>
              <div class="progress-bar" style="margin-bottom: 1rem;">
                <div class="progress-fill \${fillClass}" style="width: \${ratio}%;"></div>
              </div>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; border-top: 1px solid var(--border); padding-top: 0.75rem;">
              <button class="btn btn-sm" onclick="openTeamMembersModal('\${t.id}', '\${t.name.replace(/'/g, "\\\\'")}')">
                \${SVG.users} View Members (\${size})
              </button>
              <div style="display: flex; gap: 0.35rem;">
                <button class="btn btn-sm" onclick="openEditTeamModal('\${t.id}', '\${t.name.replace(/'/g, "\\\\'")}', \${cap})">\${SVG.edit}</button>
                <button class="btn btn-sm btn-danger" onclick="deleteTeam('\${t.id}', '\${t.name.replace(/'/g, "\\\\'")}')">\${SVG.trash}</button>
              </div>
            </div>
          </div>
        \`;
      }).join('');
    }

    function renderLogsTable(events) {
      const tbody = document.querySelector('#table-logs tbody');
      if (!tbody) return;
      if (!events.length) {
        tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-dim); padding: 1.5rem;">No telemetry events in the current log stream.</td></tr>';
        return;
      }

      tbody.innerHTML = events.map(e => {
        const isMeal = e.scan_type === 'meal_swipe';
        const typeBadge = isMeal ? '<span class="badge badge-warning">MEAL SWIPE</span>' : '<span class="badge badge-chief">' + e.scan_type.toUpperCase().replace('_', ' ') + '</span>';
        const statusBadge = e.allowed ? '<span class="badge badge-success">ALLOWED</span>' : '<span class="badge badge-danger">DENIED</span>';
        const timeStr = new Date(e.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) + ' (' + new Date(e.timestamp).toLocaleDateString() + ')';

        return \`
          <tr>
            <td>\${typeBadge}</td>
            <td>
              <div style="font-weight: 600; color: #fff;">\${e.delegate_name}</div>
              <div style="font-size: 11px; color: var(--text-dim);">\${e.team_name || 'Individual'}</div>
            </td>
            <td>\${getRoleBadge(e.delegate_role)}</td>
            <td>\${e.location_or_service}</td>
            <td>\${statusBadge}</td>
            <td style="color: var(--text-muted); font-size: 11px;">\${timeStr}</td>
          </tr>
        \`;
      }).join('');
    }

    function renderAnnouncements(list) {
      const feed = document.getElementById('broadcast-history');
      if (!feed) return;
      if (!list.length) { feed.innerHTML = '<div style="color: var(--text-dim); padding: 1rem; text-align: center;">No broadcast announcements issued yet.</div>'; return; }

      feed.innerHTML = list.map(a => {
        const timeStr = new Date(a.timestamp).toLocaleString();
        return \`
          <div style="background: var(--bg-surface); border: 1px solid var(--border); border-radius: 8px; padding: 0.85rem;">
            <div style="display: flex; justify-content: space-between; margin-bottom: 0.35rem;">
              <span class="badge badge-chief">\${a.audience.toUpperCase()}</span>
              <div style="display: flex; align-items: center; gap: 0.5rem;">
                <span style="font-size: 10px; color: var(--text-dim);">\${timeStr}</span>
                <button class="btn btn-sm btn-danger" style="padding: 0.15rem 0.4rem;" onclick="deleteBroadcast('\${a.id}')" title="Delete Broadcast">\${SVG.trash}</button>
              </div>
            </div>
            <div style="color: #fff; font-size: 12px; margin-top: 0.35rem;">\${a.message}</div>
          </div>
        \`;
      }).join('');
    }

    /* Actions: Meals */
    async function adjustMeals(userId, delta) {
      try {
        const res = await api('/users/' + userId + '/meals/adjust', {
          method: 'POST',
          body: { delta }
        });
        showToast('Updated ' + res.name + ' balance to ' + res.meals_balance + ' meals');
        fetchUsers();
      } catch (err) { showToast(err.message, true); }
    }

    function openSetMealsModal(userId, name, current) {
      const c = document.getElementById('adjust-meals-content');
      c.innerHTML = \`
        <form onsubmit="handleSetExactMeals(event, '\${userId}')" style="display: flex; flex-direction: column; gap: 0.85rem;">
          <p style="color: var(--text-muted); font-size: 12px;">Set exact meal allowance balance for <strong>\${name}</strong>:</p>
          <input type="number" id="exact-meal-val" class="input" value="\${current}" min="0" max="30" required>
          <div style="display: flex; justify-content: flex-end; gap: 0.5rem; margin-top: 0.5rem;">
            <button type="button" class="btn btn-secondary" onclick="closeModal('modal-adjust-meals')">Cancel</button>
            <button type="submit" class="btn btn-primary">Save Balance</button>
          </div>
        </form>
      \`;
      openModal('modal-adjust-meals');
    }

    async function handleSetExactMeals(e, userId) {
      e.preventDefault();
      const val = parseInt(document.getElementById('exact-meal-val').value, 10);
      try {
        const res = await api('/users/' + userId + '/meals/adjust', {
          method: 'POST',
          body: { meals_balance: val }
        });
        closeModal('modal-adjust-meals');
        showToast('Set balance for ' + res.name + ' to ' + res.meals_balance + ' meals');
        fetchUsers();
      } catch (err) { showToast(err.message, true); }
    }

    async function handleManualMeal(e) {
      e.preventDefault();
      const meal_type = document.getElementById('manual-meal-type').value;
      const card_uid = document.getElementById('manual-meal-card').value.trim();

      try {
        const res = await api('/meals/swipe', {
          method: 'POST',
          body: { card_uid, meal_type }
        });
        showToast('Success! ' + meal_type + ' recorded. ' + res.meals_remaining + ' meals left.');
        document.getElementById('manual-meal-card').value = '';
        fetchOverview();
        fetchUsers();
      } catch (err) { showToast(err.message, true); }
    }

    /* Actions: Locations / Halls CRUD */
    function openAddHallModal() {
      document.getElementById('hall-modal-title').textContent = 'Add Conference Location / Hall';
      document.getElementById('hall-edit-id').value = '';
      document.getElementById('hall-name').value = '';
      document.getElementById('hall-cap').value = '50';
      document.getElementById('hall-occ').value = '0';
      ['user', 'organizer', 'admin', 'chief', 'lead', 'member'].forEach(r => {
        const chk = document.getElementById('role-chk-' + r);
        if (chk) chk.checked = true;
      });
      document.getElementById('hall-submit-btn').textContent = 'Create Location';
      openModal('modal-hall');
    }

    function openEditHallModal(hallId) {
      const h = (APP_STATE.halls || []).find(x => x.id === hallId);
      if (!h) return;

      document.getElementById('hall-modal-title').textContent = 'Edit Conference Location';
      document.getElementById('hall-edit-id').value = h.id;
      document.getElementById('hall-name').value = h.name;
      document.getElementById('hall-cap').value = h.capacity_threshold;
      document.getElementById('hall-occ').value = h.current_occupancy || 0;

      const roles = h.allowed_roles || [];
      document.getElementById('role-chk-user').checked = roles.includes('user');
      document.getElementById('role-chk-organizer').checked = roles.includes('organizer');
      document.getElementById('role-chk-admin').checked = roles.includes('admin');
      document.getElementById('role-chk-chief').checked = roles.includes('chief_organizer');
      document.getElementById('role-chk-lead').checked = roles.includes('team_leader');
      document.getElementById('role-chk-member').checked = roles.includes('team_member');

      document.getElementById('hall-submit-btn').textContent = 'Save Changes';
      openModal('modal-hall');
    }

    async function handleSaveHall(e) {
      e.preventDefault();
      const hallId = document.getElementById('hall-edit-id').value;
      const name = document.getElementById('hall-name').value.trim();
      const capacity_threshold = parseInt(document.getElementById('hall-cap').value, 10);
      const current_occupancy = parseInt(document.getElementById('hall-occ').value, 10);

      const allowed_roles = [];
      if (document.getElementById('role-chk-user').checked) allowed_roles.push('user');
      if (document.getElementById('role-chk-organizer').checked) allowed_roles.push('organizer');
      if (document.getElementById('role-chk-admin').checked) allowed_roles.push('admin');
      if (document.getElementById('role-chk-chief').checked) allowed_roles.push('chief_organizer');
      if (document.getElementById('role-chk-lead').checked) allowed_roles.push('team_leader');
      if (document.getElementById('role-chk-member').checked) allowed_roles.push('team_member');

      try {
        if (hallId) {
          await api('/access/halls/' + hallId, {
            method: 'PUT',
            body: { name, capacity_threshold, allowed_roles, current_occupancy }
          });
          showToast('Updated location ' + name);
        } else {
          await api('/access/halls', {
            method: 'POST',
            body: { name, capacity_threshold, allowed_roles }
          });
          showToast('Created location ' + name);
        }
        closeModal('modal-hall');
        fetchHalls();
      } catch (err) { showToast(err.message, true); }
    }

    async function adjustHallOccupancy(hallId, delta) {
      try {
        await api('/access/halls/' + hallId + '/occupancy', {
          method: 'POST',
          body: { delta }
        });
        fetchHalls();
      } catch (err) { showToast(err.message, true); }
    }

    async function resetHallOccupancy(hallId) {
      if (!confirm('Reset occupancy count to 0?')) return;
      try {
        await api('/access/halls/' + hallId + '/occupancy', {
          method: 'POST',
          body: { set_to: 0 }
        });
        showToast('Occupancy reset to 0');
        fetchHalls();
      } catch (err) { showToast(err.message, true); }
    }

    async function deleteHall(hallId, name) {
      if (!confirm('Are you sure you want to permanently delete location ' + name + '?')) return;
      try {
        await api('/access/halls/' + hallId, { method: 'DELETE' });
        showToast('Deleted location ' + name);
        fetchHalls();
      } catch (err) { showToast(err.message, true); }
    }

    /* Actions: Conference Attendance */
    function populateAttendancePicker(attendees) {
      const picker = document.getElementById('att-quick-picker');
      if (!picker) return;
      const current = picker.value;
      picker.innerHTML = '<option value="">Quick Select Attendee…</option>' + 
        attendees
          .filter(function(a) { return a.card_uid; })
          .map(function(a) { 
            return '<option value="' + a.card_uid + '">' + a.name + ' (' + a.card_uid + ') - ' + a.status.toUpperCase() + '</option>';
          })
          .join('');
      if (current) picker.value = current;
    }

    function fillCardFromPicker(cardUid) {
      if (cardUid) {
        document.getElementById('att-swipe-card').value = cardUid;
      }
    }

    async function handleAttendanceSwipe(e) {
      e.preventDefault();
      const cardUid = document.getElementById('att-swipe-card').value.trim();
      const action = document.getElementById('att-swipe-action').value;
      const btn = document.getElementById('att-swipe-btn');
      const feedback = document.getElementById('att-swipe-feedback');

      if (!cardUid) return;

      btn.disabled = true;
      btn.textContent = 'Registering…';
      feedback.style.display = 'none';

      try {
        const res = await api('/attendance/swipe', {
          method: 'POST',
          body: { card_uid: cardUid, action: action }
        });

        const isPresent = res.user.status === 'present';
        const actionLabel = res.user.action === 'check_in' ? 'CHECKED IN (PRESENT)' : 'CHECKED OUT (ABSENT)';
        feedback.style.display = 'block';
        feedback.style.background = isPresent ? 'rgba(16, 185, 129, 0.15)' : 'rgba(239, 68, 68, 0.15)';
        feedback.style.border = isPresent ? '1px solid rgba(16, 185, 129, 0.4)' : '1px solid rgba(239, 68, 68, 0.4)';
        feedback.style.color = isPresent ? '#34D399' : '#F87171';
        feedback.innerHTML = '<strong>' + res.user.name + '</strong> (' + res.user.role + ') has been <strong>' + actionLabel + '</strong>. Card: <code>' + res.user.card_uid + '</code>';

        showToast(res.user.name + ': ' + actionLabel);
        document.getElementById('att-swipe-card').value = '';
        if (document.getElementById('att-quick-picker')) document.getElementById('att-quick-picker').value = '';

        await Promise.all([fetchLocations(), fetchAttendance(), fetchActivity()]);
      } catch (err) {
        feedback.style.display = 'block';
        feedback.style.background = 'rgba(239, 68, 68, 0.15)';
        feedback.style.border = '1px solid rgba(239, 68, 68, 0.4)';
        feedback.style.color = '#F87171';
        feedback.textContent = 'Swipe failed: ' + err.message;
        showToast(err.message, true);
      } finally {
        btn.disabled = false;
        btn.textContent = 'Register Attendance';
      }
    }

    async function setSingleAttendance(userId, status) {
      try {
        const res = await api('/attendance/single', {
          method: 'POST',
          body: { user_id: userId, status: status }
        });
        showToast('Marked ' + res.name + ' as ' + status.toUpperCase());
        await Promise.all([fetchLocations(), fetchAttendance(), fetchActivity()]);
      } catch (err) {
        showToast(err.message, true);
      }
    }

    function toggleAttendeeSelect(userId, checked) {
      if (checked) {
        APP_STATE.selectedAttendees.add(userId);
      } else {
        APP_STATE.selectedAttendees.delete(userId);
      }
      updateSelectedCount();
    }

    function toggleSelectAllAttendance(checked) {
      const q = (document.getElementById('loc-search')?.value || '').toLowerCase();
      const statusFilter = document.getElementById('att-filter-status')?.value || 'all';

      const filtered = (APP_STATE.locations || []).filter(function(l) {
        const matchesQuery = (l.name || '').toLowerCase().indexOf(q) !== -1 || 
                             (l.current_hall_name || '').toLowerCase().indexOf(q) !== -1 || 
                             (l.card_uid || '').toLowerCase().indexOf(q) !== -1 ||
                             (l.team_name || '').toLowerCase().indexOf(q) !== -1 ||
                             (l.role || '').toLowerCase().indexOf(q) !== -1;
        const matchesStatus = statusFilter === 'all' || l.attendance_status === statusFilter;
        return matchesQuery && matchesStatus;
      });

      filtered.forEach(function(l) {
        if (checked) {
          APP_STATE.selectedAttendees.add(l.user_id);
        } else {
          APP_STATE.selectedAttendees.delete(l.user_id);
        }
      });

      document.querySelectorAll('.chk-att-row').forEach(function(cb) {
        cb.checked = checked;
      });
      const chkHead = document.getElementById('chk-head');
      const chkSelectAll = document.getElementById('chk-select-all');
      if (chkHead) chkHead.checked = checked;
      if (chkSelectAll) chkSelectAll.checked = checked;

      updateSelectedCount();
    }

    function updateSelectedCount() {
      const countEl = document.getElementById('selected-att-count');
      if (countEl) {
        countEl.textContent = '(' + APP_STATE.selectedAttendees.size + ' selected)';
      }
    }

    async function handleBulkAttendance(status, all) {
      const targetCount = all ? (APP_STATE.locations || []).length : APP_STATE.selectedAttendees.size;
      if (!all && targetCount === 0) {
        showToast('Please select at least one attendee first', true);
        return;
      }

      const actionText = status === 'present' ? 'PRESENT' : 'ABSENT';
      const promptText = all 
        ? ('Are you sure you want to mark ALL ' + targetCount + ' conference attendees as ' + actionText + '?')
        : ('Mark ' + targetCount + ' selected attendee(s) as ' + actionText + '?');

      if (!confirm(promptText)) return;

      try {
        const payload = all 
          ? { all: true, status: status } 
          : { user_ids: Array.from(APP_STATE.selectedAttendees), status: status };

        const res = await api('/attendance/bulk', {
          method: 'POST',
          body: payload
        });

        showToast('Successfully marked ' + res.count + ' attendee(s) ' + actionText);
        if (!all) {
          APP_STATE.selectedAttendees.clear();
          const chkHead = document.getElementById('chk-head');
          const chkSelectAll = document.getElementById('chk-select-all');
          if (chkHead) chkHead.checked = false;
          if (chkSelectAll) chkSelectAll.checked = false;
          updateSelectedCount();
        }
        await Promise.all([fetchLocations(), fetchAttendance(), fetchActivity()]);
      } catch (err) {
        showToast(err.message, true);
      }
    }

    /* Actions: Teams / Committees CRUD */
    function openCreateTeamModal() {
      document.getElementById('team-modal-title').textContent = 'Add Committee / Team';
      document.getElementById('team-edit-id').value = '';
      document.getElementById('new-team-name').value = '';
      document.getElementById('new-team-cap').value = '30';
      document.getElementById('team-submit-btn').textContent = 'Create Committee';
      openModal('modal-team');
    }

    function openEditTeamModal(teamId, name, cap) {
      document.getElementById('team-modal-title').textContent = 'Edit Committee / Team';
      document.getElementById('team-edit-id').value = teamId;
      document.getElementById('new-team-name').value = name;
      document.getElementById('new-team-cap').value = cap;
      document.getElementById('team-submit-btn').textContent = 'Save Changes';
      openModal('modal-team');
    }

    async function handleSaveTeam(e) {
      e.preventDefault();
      const teamId = document.getElementById('team-edit-id').value;
      const name = document.getElementById('new-team-name').value.trim();
      const capacity = parseInt(document.getElementById('new-team-cap').value, 10);

      try {
        if (teamId) {
          await api('/teams/' + teamId, {
            method: 'PUT',
            body: { name, capacity }
          });
          showToast('Updated committee ' + name);
        } else {
          await api('/teams', {
            method: 'POST',
            body: { name, capacity }
          });
          showToast('Created committee ' + name);
        }
        closeModal('modal-team');
        fetchTeams();
      } catch (err) { showToast(err.message, true); }
    }

    async function deleteTeam(teamId, name) {
      if (!confirm('Are you sure you want to delete committee ' + name + '?')) return;
      try {
        await api('/teams/' + teamId, { method: 'DELETE' });
        showToast('Team ' + name + ' deleted');
        fetchTeams();
      } catch (err) { showToast(err.message, true); }
    }

    async function openTeamMembersModal(teamId, teamName) {
      document.getElementById('team-members-modal-title').textContent = 'Members: ' + teamName;
      const listEl = document.getElementById('team-members-list');
      listEl.innerHTML = '<div style="color: var(--text-dim); text-align: center; padding: 1rem;">Loading members…</div>';
      openModal('modal-team-members');

      try {
        const members = await api('/teams/' + teamId + '/members');
        if (!members.length) {
          listEl.innerHTML = '<div style="color: var(--text-dim); text-align: center; padding: 1rem;">No members currently assigned to this committee.</div>';
          return;
        }

        listEl.innerHTML = members.map(m => \`
          <div style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0.75rem; background: var(--bg-surface); border: 1px solid var(--border); border-radius: 6px;">
            <div>
              <div style="font-weight: 600; color: #fff;">\${m.name}</div>
              <div style="font-size: 11px; color: var(--text-dim);">\${m.email || 'No email'} • \${m.role}</div>
            </div>
            <button class="btn btn-sm btn-danger" onclick="removeTeamMember('\${teamId}', '\${m.id}', '\${teamName.replace(/'/g, "\\\\'")}')">Remove</button>
          </div>
        \`).join('');
      } catch (err) {
        listEl.innerHTML = '<div style="color: var(--danger);">' + err.message + '</div>';
      }
    }

    async function removeTeamMember(teamId, userId, teamName) {
      try {
        await api('/teams/' + teamId + '/members/' + userId, { method: 'DELETE' });
        showToast('Member removed from committee');
        openTeamMembersModal(teamId, teamName);
        fetchTeams();
      } catch (err) { showToast(err.message, true); }
    }

    /* Actions: Broadcast Announcements */
    function toggleBroadcastTarget() {
      const audience = document.getElementById('broadcast-audience').value;
      document.getElementById('broadcast-team-row').style.display = audience === 'team' ? 'block' : 'none';
      document.getElementById('broadcast-user-row').style.display = audience === 'individual' ? 'block' : 'none';
    }

    async function handleSendBroadcast(e) {
      e.preventDefault();
      const audience = document.getElementById('broadcast-audience').value;
      const team_id = audience === 'team' ? document.getElementById('broadcast-team-id').value : null;
      const recipient_id = audience === 'individual' ? document.getElementById('broadcast-user-id').value : null;
      const message = document.getElementById('broadcast-message').value.trim();

      try {
        await api('/notifications', {
          method: 'POST',
          body: { audience, team_id, recipient_id, message }
        });
        showToast('Announcement broadcast sent successfully');
        document.getElementById('broadcast-message').value = '';
        fetchAnnouncements();
      } catch (err) { showToast(err.message, true); }
    }

    async function deleteBroadcast(notifId) {
      if (!confirm('Are you sure you want to delete this announcement?')) return;
      try {
        await api('/notifications/' + notifId, { method: 'DELETE' });
        showToast('Announcement deleted');
        fetchAnnouncements();
      } catch (err) { showToast(err.message, true); }
    }

    /* Actions: Settings */
    async function handleSaveMealWindows(e) {
      e.preventDefault();
      const breakfast_start = document.getElementById('win-b-start').value.trim();
      const breakfast_end = document.getElementById('win-b-end').value.trim();
      const lunch_start = document.getElementById('win-l-start').value.trim();
      const lunch_end = document.getElementById('win-l-end').value.trim();

      try {
        await api('/settings/meal-windows', {
          method: 'PUT',
          body: { breakfast_start, breakfast_end, lunch_start, lunch_end }
        });
        showToast('Meal windows saved successfully');
      } catch (err) { showToast(err.message, true); }
    }

    /* Helpers */
    function populateTeamSelects(teams, specificId) {
      const targets = specificId ? [document.getElementById(specificId)] : [
        document.getElementById('new-del-team'),
        document.getElementById('broadcast-team-id')
      ];

      targets.forEach(sel => {
        if (!sel) return;
        sel.innerHTML = '<option value="">None (Individual)</option>' + teams.map(t => \`<option value="\${t.id}">\${t.name}</option>\`).join('');
      });
    }

    function populateBroadcastUsers(users) {
      const uSel = document.getElementById('broadcast-user-id');
      if (uSel) {
        uSel.innerHTML = users.map(u => \`<option value="\${u.id}">\${u.name} (\${u.role})</option>\`).join('');
      }
    }

    function getRoleBadge(role) {
      switch(role) {
        case 'admin': return '<span class="badge badge-admin">Admin</span>';
        case 'chief_organizer': return '<span class="badge badge-chief">Chief Org</span>';
        case 'organizer': return '<span class="badge badge-organizer">Organizer</span>';
        case 'team_leader': return '<span class="badge badge-warning">Leader</span>';
        case 'team_member': return '<span class="badge badge-delegate">Member</span>';
        default: return '<span class="badge badge-delegate">Delegate</span>';
      }
    }

    function filterMealTable() { renderMealsTable(); }
    function filterLocationsTable() { renderLocationsTable(); }

    /* Boot Sequence */
    function initDashboard() {
      fetchOverview();
      fetchHalls();
      fetchUsers();
      fetchTeams();
      fetchLocations();
      fetchAttendance();
      fetchActivity();
      fetchAnnouncements();
      fetchMealWindows();

      // Real-time telemetry heartbeat every 5 seconds
      setInterval(() => {
        if (APP_STATE.token) {
          fetchOverview();
          const activeView = document.querySelector('.view-container.active');
          if (activeView && activeView.id === 'view-locations') {
            fetchLocations();
            fetchAttendance();
          }
        }
      }, 5000);
    }

    window.addEventListener('DOMContentLoaded', () => {
      const saved = sessionStorage.getItem('mianu_admin_session');
      if (saved) {
        try {
          const session = JSON.parse(saved);
          if (session?.access_token) {
            APP_STATE.token = session.access_token;
            APP_STATE.user = session;
            document.getElementById('auth-gate').style.display = 'none';
            document.getElementById('user-display-name').textContent = session.name || 'Secrétariat Général';
            initDashboard();
            return;
          }
        } catch {}
      }
      document.getElementById('auth-gate').style.display = 'flex';
    });
  </script>
</body>
</html>
`;
