// MIANUCOM operations dashboard — shell, session and routing.
//
// Deliberately build-free: ES modules straight off the static mount, so the FastAPI server that
// serves the API also serves this with no toolchain in between.

import { routeFor, routesFor } from './views/registry.js';
import { P, can, canAny } from './permissions.js';

export { P, can, canAny };

// Same origin as the API when served on workers.dev, otherwise points directly to the live Cloudflare edge backend.
export const API =
  location.origin.includes('workers.dev')
    ? `${location.origin}/api/v1`
    : 'https://mianu-backend.karimshacker1234.workers.dev/api/v1';

/* ---------------------------------------------------------------- session */

const SESSION_KEY = 'mianu.session';

export const session = {
  get current() {
    try {
      return JSON.parse(sessionStorage.getItem(SESSION_KEY) || 'null');
    } catch {
      return null;
    }
  },
  set(value) {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify(value));
  },
  clear() {
    sessionStorage.removeItem(SESSION_KEY);
  },
};

// sessionStorage rather than localStorage: this holds a bearer token, and a shared operations
// laptop should not keep an admin signed in after the tab closes.

/* ------------------------------------------------------------- api client */

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}

export async function api(path, { method = 'GET', body, params } = {}) {
  const url = new URL(`${API}${path}`, location.origin);
  for (const [key, value] of Object.entries(params || {})) {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value);
    }
  }

  const token = session.current?.access_token;
  const response = await fetch(url, {
    method,
    headers: {
      ...(body ? { 'Content-Type': 'application/json' } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  if (response.status === 204) return null;

  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    throw new ApiError(detailOf(payload) || `Request failed (${response.status})`, response.status);
  }
  return payload;
}

// FastAPI reports validation errors as a list of per-field objects and everything else as a plain
// string, both under `detail`. Flatten to one line so callers can just render it.
function detailOf(payload) {
  const detail = payload?.detail;
  if (!detail) return null;
  if (typeof detail === 'string') return detail;
  if (Array.isArray(detail)) {
    return detail.map((d) => d.msg || JSON.stringify(d)).join('; ');
  }
  return JSON.stringify(detail);
}

/* ----------------------------------------------------------------- roles */

export const ROLE_LABELS = {
  admin: 'Admin',
  chief_organizer: 'Chief organizer',
  organizer: 'Organizer',
  team_leader: 'Team leader',
  team_member: 'Team member',
  user: 'Delegate',
};

const ROLE_STYLES = {
  admin: 'bg-danger/12 text-danger',
  chief_organizer: 'bg-info/15 text-info',
  organizer: 'bg-accent/12 text-accent',
  team_leader: 'bg-warning/15 text-warning',
  team_member: 'bg-success/12 text-success',
  user: 'bg-fg-muted/12 text-fg-muted',
};

// Permission constants and `can`/`canAny` live in ./permissions.js (re-exported at the top of this
// file) so registry.js can import them without a load-order cycle through app.js.

/* -------------------------------------------------------------------- ui */

export function toast(message, tone = 'success') {
  const host = document.getElementById('toast-host');
  const el = document.createElement('div');
  const palette = {
    success: 'bg-success text-white',
    error: 'bg-danger text-white',
    info: 'bg-surface-overlay text-fg border border-subtle',
  }[tone];
  el.className = `pointer-events-auto rounded-xl px-4 py-2.5 text-sm font-medium shadow-raised animate-fade-in-up max-w-sm ${palette}`;
  el.textContent = message;
  host.appendChild(el);
  setTimeout(() => {
    el.style.transition = 'opacity .3s, transform .3s';
    el.style.opacity = '0';
    el.style.transform = 'translateY(6px)';
    setTimeout(() => el.remove(), 300);
  }, 3600);
}

export function el(html) {
  const template = document.createElement('template');
  template.innerHTML = html.trim();
  return template.content.firstElementChild;
}

/** Escapes interpolation into innerHTML. Names and messages are user-supplied. */
export function esc(value) {
  return String(value ?? '').replace(
    /[&<>"']/g,
    (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c],
  );
}

export function initials(name) {
  return String(name || '?')
    .split(/[\s-]+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('');
}

export function formatWhen(iso) {
  if (!iso) return '';
  const date = new Date(iso.endsWith('Z') ? iso : `${iso}Z`);
  const minutes = Math.round((Date.now() - date.getTime()) / 60000);
  if (minutes < 1) return 'just now';
  if (minutes < 60) return `${minutes}m ago`;
  if (minutes < 1440) return `${Math.round(minutes / 60)}h ago`;
  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

export function skeleton(rows = 3) {
  return `<div class="space-y-3">${Array.from(
    { length: rows },
    () => '<div class="h-16 rounded-xl bg-surface-raised shimmer border border-subtle"></div>',
  ).join('')}</div>`;
}

export function emptyState(title, description) {
  return `
    <div class="text-center py-14 px-6">
      <div class="w-12 h-12 rounded-2xl bg-surface-sunken border border-subtle grid place-items-center mx-auto mb-3 text-fg-muted">—</div>
      <p class="font-display font-semibold">${esc(title)}</p>
      <p class="text-sm text-fg-muted mt-1 max-w-sm mx-auto">${esc(description)}</p>
    </div>`;
}

export function errorState(message, retryLabel = 'Retry') {
  return `
    <div class="rounded-2xl border border-danger/30 bg-danger/5 p-6 text-center">
      <p class="font-display font-semibold text-danger">Couldn't load this</p>
      <p class="text-sm text-fg-muted mt-1">${esc(message)}</p>
      <button data-retry class="mt-4 rounded-lg border border-subtle bg-surface-raised px-4 py-2 text-sm font-medium hover:bg-surface-hover transition">${esc(retryLabel)}</button>
    </div>`;
}

/* ---------------------------------------------------------------- routing */

const view = () => document.getElementById('view');

export function navigate(name) {
  if (location.hash.slice(1) !== name) {
    location.hash = name;
  } else {
    render();
  }
}

async function render() {
  const current = session.current;
  if (!current) return;

  const allowed = routesFor(current.permissions);
  const wanted = location.hash.slice(1);
  const route = allowed.find((r) => r.name === wanted) || allowed[0];
  if (!route) {
    view().innerHTML = emptyState('Nothing available', 'This account has no dashboard sections.');
    return;
  }

  if (location.hash.slice(1) !== route.name) {
    location.replace(`#${route.name}`);
  }

  paintNav(allowed, route.name);
  document.getElementById('shell-subtitle').textContent = route.label;
  view().innerHTML = skeleton(3);

  try {
    const module = await routeFor(route.name);
    // Views own their subtree. Passing the container rather than returning HTML lets them wire
    // their own listeners without the shell knowing anything about their internals.
    await module.mount(view(), { session: current });
  } catch (error) {
    view().innerHTML = errorState(error.message || String(error));
    view()
      .querySelector('[data-retry]')
      ?.addEventListener('click', render);
  }
}

function paintNav(routes, active) {
  const sidebar = document.getElementById('sidebar');
  sidebar.innerHTML = routes
    .map(
      (route) => `
        <a href="#${route.name}"
           class="group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium transition ${
             route.name === active
               ? 'bg-accent/12 text-accent'
               : 'text-fg-muted hover:bg-surface-hover hover:text-fg'
           }">
          <span class="w-5 grid place-items-center opacity-80">${route.icon}</span>
          ${esc(route.label)}
        </a>`,
    )
    .join('');
}

/* ------------------------------------------------------------------ shell */

function showShell(current) {
  document.getElementById('auth-gate').classList.add('hidden');
  const shell = document.getElementById('app-shell');
  shell.classList.remove('hidden');
  shell.classList.add('flex');

  document.getElementById('session-name').textContent = current.name || '';
  document.getElementById('session-avatar').textContent = initials(current.name);
  const badge = document.getElementById('session-role');
  badge.textContent = ROLE_LABELS[current.role] || current.role;
  badge.className = `hidden sm:inline text-[11px] font-semibold uppercase tracking-wider px-2.5 py-1 rounded-full ${
    ROLE_STYLES[current.role] || ROLE_STYLES.user
  }`;

  render();
}

function showLogin() {
  document.getElementById('auth-gate').classList.remove('hidden');
  const shell = document.getElementById('app-shell');
  shell.classList.add('hidden');
  shell.classList.remove('flex');
}

/* -------------------------------------------------------------------- run */

document.getElementById('login-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const button = document.getElementById('login-submit');
  const errorEl = document.getElementById('login-error');
  const username = document.getElementById('login-username').value.trim();
  if (!username) return;

  button.disabled = true;
  button.textContent = 'Signing in…';
  errorEl.classList.add('hidden');

  try {
    const token = await api('/auth/login', {
      method: 'POST',
      body: { username, password: document.getElementById('login-password').value || 'demo' },
    });
    session.set(token);
    showShell(token);
  } catch (error) {
    errorEl.textContent = error.message;
    errorEl.classList.remove('hidden');
  } finally {
    button.disabled = false;
    button.textContent = 'Sign in';
  }
});

document.querySelectorAll('[data-demo]').forEach((chip) =>
  chip.addEventListener('click', () => {
    document.getElementById('login-username').value = chip.dataset.demo;
    if (chip.dataset.pwd) {
      document.getElementById('login-password').value = chip.dataset.pwd;
    }
    document.getElementById('login-form').requestSubmit();
  }),
);

document.getElementById('sign-out').addEventListener('click', () => {
  session.clear();
  location.hash = '';
  showLogin();
});

document.getElementById('nav-toggle').addEventListener('click', () => {
  const sidebar = document.getElementById('sidebar');
  sidebar.classList.toggle('hidden');
  sidebar.classList.toggle('flex');
  // On mobile the sidebar overlays rather than sits beside the content.
  sidebar.classList.toggle('absolute');
  sidebar.classList.toggle('z-30');
  sidebar.classList.toggle('bg-surface-overlay');
  sidebar.classList.toggle('p-3');
  sidebar.classList.toggle('rounded-2xl');
  sidebar.classList.toggle('shadow-raised');
  sidebar.classList.toggle('border');
  sidebar.classList.toggle('border-subtle');
});

window.addEventListener('hashchange', render);

if (session.current) {
  showShell(session.current);
} else {
  showLogin();
}
