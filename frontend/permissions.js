// Permission names, mirroring backend `app/permissions.py`.
//
// Its own module to break a cycle: both app.js and views/registry.js need these at load time, and
// if either owned them the other would read `P` before initialisation (temporal dead zone).
//
// This file lists the names but never decides which role holds which — that arrives with the login
// token, so the two can't drift into disagreeing.

export const P = {
  MANAGE_USERS: 'manage_users',
  MANAGE_TEAMS: 'manage_teams',
  MANAGE_HALLS: 'manage_halls',
  MANAGE_SETTINGS: 'manage_settings',
  SCAN_ACCESS: 'scan_access',
  CHARGE_TOKENS: 'charge_tokens',
  CHARGE_MEALS: 'charge_meals',
  TOP_UP_WALLET: 'top_up_wallet',
  BROADCAST_WIDE: 'broadcast_wide',
  BROADCAST_OWN_TEAM: 'broadcast_own_team',
  VIEW_HALL_AVAILABILITY: 'view_hall_availability',
  VIEW_REPORTS: 'view_reports',
  VIEW_INBOX: 'view_inbox',
};

/** Does this session (or bare permission array) hold every one of `required`? */
export function can(sessionOrPerms, ...required) {
  const held = Array.isArray(sessionOrPerms) ? sessionOrPerms : sessionOrPerms?.permissions || [];
  return required.every((permission) => held.includes(permission));
}

/** Does it hold at least one of `options`? */
export function canAny(sessionOrPerms, ...options) {
  const held = Array.isArray(sessionOrPerms) ? sessionOrPerms : sessionOrPerms?.permissions || [];
  return options.some((permission) => held.includes(permission));
}
