// src/utils.ts
import { DbUser, UserResponse, UserRole, TeamRole, TokenPair } from './types';
import { permissionsFor } from './permissions';

export function generateUuid(): string {
  return crypto.randomUUID();
}

export function toUserResponse(user: DbUser): UserResponse {
  return {
    id: user.id,
    name: user.name,
    email: user.email,
    phone: user.phone,
    token_balance: user.token_balance,
    is_active: Boolean(user.is_active),
    role: user.role as UserRole,
    team_id: user.team_id,
    team_role: (user.team_role as TeamRole) || null,
    meal_allowance: typeof user.meal_allowance === 'number' ? user.meal_allowance : 6,
    meals_balance: typeof user.meals_balance === 'number' ? user.meals_balance : 6,
  };
}

export function tokenFor(user: DbUser): TokenPair {
  return {
    access_token: `access-${user.id}`,
    refresh_token: 'refresh',
    expires_in: 3600,
    role: user.role as UserRole,
    user_id: user.id,
    name: user.name,
    team_id: user.team_id,
    team_role: (user.team_role as TeamRole) || null,
    permissions: Array.from(permissionsFor(user.role)).sort(),
  };
}
