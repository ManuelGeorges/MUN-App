// src/permissions.ts
import { UserRole } from './types';

export enum Permission {
  MANAGE_USERS = 'manage_users',
  MANAGE_TEAMS = 'manage_teams',
  MANAGE_HALLS = 'manage_halls',
  MANAGE_SETTINGS = 'manage_settings',
  SCAN_ACCESS = 'scan_access',
  CHARGE_TOKENS = 'charge_tokens',
  CHARGE_MEALS = 'charge_meals',
  TOP_UP_WALLET = 'top_up_wallet',
  BROADCAST_WIDE = 'broadcast_wide',
  BROADCAST_OWN_TEAM = 'broadcast_own_team',
  VIEW_HALL_AVAILABILITY = 'view_hall_availability',
  VIEW_REPORTS = 'view_reports',
  VIEW_INBOX = 'view_inbox',
}

const ALL_PERMISSIONS = new Set(Object.values(Permission));
const BASELINE_PERMISSIONS = new Set([Permission.VIEW_INBOX, Permission.VIEW_HALL_AVAILABILITY]);

const CHIEF_PERMISSIONS = new Set(ALL_PERMISSIONS);
CHIEF_PERMISSIONS.delete(Permission.MANAGE_SETTINGS);

const ORGANIZER_PERMISSIONS = new Set([
  Permission.VIEW_INBOX,
  Permission.SCAN_ACCESS,
  Permission.CHARGE_TOKENS,
  Permission.CHARGE_MEALS,
]);

const LEADER_PERMISSIONS = new Set([
  ...BASELINE_PERMISSIONS,
  Permission.BROADCAST_OWN_TEAM,
]);

export const ROLE_PERMISSIONS: Record<string, Set<Permission>> = {
  [UserRole.ADMIN]: ALL_PERMISSIONS,
  [UserRole.CHIEF_ORGANIZER]: CHIEF_PERMISSIONS,
  [UserRole.ORGANIZER]: ORGANIZER_PERMISSIONS,
  [UserRole.TEAM_LEADER]: LEADER_PERMISSIONS,
  [UserRole.TEAM_MEMBER]: BASELINE_PERMISSIONS,
  [UserRole.USER]: new Set([Permission.VIEW_INBOX]),
};

export function permissionsFor(role: string): Set<Permission> {
  return ROLE_PERMISSIONS[role] || new Set();
}

export function hasPermission(role: string, permission: Permission): boolean {
  return permissionsFor(role).has(permission);
}
