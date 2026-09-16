// Which sections exist and what each one requires.
//
// Gating is by permission, never by role: the permission set arrives with the login token, so this
// table can't drift out of step with the server's. Every one of these is enforced again server-side
// — a hand-typed hash gets a view whose API calls come back 403.

import { P } from '../permissions.js';

const icon = (path) =>
  `<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round">${path}</svg>`;

/**
 * `requires` — every listed permission is needed.
 * `requiresAny` — at least one is needed.
 * Neither — visible to any signed-in account.
 */
export const ROUTES = [
  {
    name: 'overview',
    label: 'Overview',
    icon: icon('<rect x="3" y="3" width="7" height="9"/><rect x="14" y="3" width="7" height="5"/><rect x="14" y="12" width="7" height="9"/><rect x="3" y="16" width="7" height="5"/>'),
  },
  {
    name: 'broadcast',
    label: 'Broadcast',
    requiresAny: [P.BROADCAST_WIDE, P.BROADCAST_OWN_TEAM],
    icon: icon('<path d="m3 11 18-5v12L3 14v-3z"/><path d="M11.6 16.8a3 3 0 1 1-5.8-1.6"/>'),
  },
  {
    name: 'availability',
    label: 'Hall availability',
    requires: [P.VIEW_HALL_AVAILABILITY],
    icon: icon('<path d="M3 21h18"/><path d="M6 21V8l6-4 6 4v13"/><circle cx="12" cy="12" r="1.5"/>'),
  },
  {
    name: 'delegates',
    label: 'Delegates',
    requires: [P.MANAGE_USERS],
    icon: icon('<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/>'),
  },
  {
    name: 'teams',
    label: 'Teams',
    requiresAny: [P.MANAGE_TEAMS, P.BROADCAST_OWN_TEAM],
    icon: icon('<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>'),
  },
  {
    name: 'halls',
    label: 'Manage halls',
    requires: [P.MANAGE_HALLS],
    icon: icon('<path d="M3 21h18"/><path d="M5 21V7l7-4 7 4v14"/><path d="M9 21v-6h6v6"/>'),
  },
  {
    name: 'wallet',
    label: 'Wallet',
    requires: [P.CHARGE_TOKENS],
    icon: icon('<rect x="2" y="6" width="20" height="13" rx="2"/><path d="M2 10h20"/><circle cx="17" cy="14.5" r="1.2"/>'),
  },
  {
    name: 'inbox',
    label: 'Messages',
    requires: [P.VIEW_INBOX],
    icon: icon('<path d="M4 4h16v12H7l-3 3V4z"/>'),
  },
];

export function routesFor(permissions) {
  const held = permissions || [];
  return ROUTES.filter(
    (route) =>
      (route.requires || []).every((p) => held.includes(p)) &&
      (!route.requiresAny || route.requiresAny.some((p) => held.includes(p))),
  );
}

const loaders = {
  overview: () => import('./overview.js'),
  broadcast: () => import('./broadcast.js'),
  availability: () => import('./availability.js'),
  delegates: () => import('./delegates.js'),
  teams: () => import('./teams.js'),
  halls: () => import('./halls.js'),
  wallet: () => import('./wallet.js'),
  inbox: () => import('./inbox.js'),
};

export function routeFor(name) {
  return loaders[name]();
}
