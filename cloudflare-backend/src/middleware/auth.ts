// src/middleware/auth.ts
import { Context, MiddlewareHandler } from 'hono';
import { Bindings, DbUser } from '../types';
import { Permission, hasPermission } from '../permissions';

export type AppEnv = {
  Bindings: Bindings;
  Variables: {
    user: DbUser;
  };
};

const TOKEN_PREFIX = 'access-';

export async function getCurrentUser(c: Context<AppEnv>): Promise<DbUser | null> {
  const authHeader = c.req.header('Authorization');
  if (!authHeader || !authHeader.toLowerCase().startsWith('bearer ')) {
    return null;
  }

  const token = authHeader.substring(7).trim();
  if (!token.startsWith(TOKEN_PREFIX)) {
    return null;
  }

  const rawIdentifier = token.substring(TOKEN_PREFIX.length);
  const identifier = decodeURIComponent(rawIdentifier);
  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ? OR name = ? OR email = ?')
    .bind(identifier, identifier, identifier)
    .first<DbUser>();

  return user || null;
}

export const authMiddleware: MiddlewareHandler<AppEnv> = async (c, next) => {
  const user = await getCurrentUser(c);
  if (!user) {
    return c.json({ detail: 'Sign in to continue.' }, 401);
  }
  c.set('user', user);
  await next();
};

export function requirePermission(permission: Permission): MiddlewareHandler<AppEnv> {
  return async (c, next) => {
    const user = c.get('user') || (await getCurrentUser(c));
    if (!user) {
      return c.json({ detail: 'Sign in to continue.' }, 401);
    }
    c.set('user', user);

    if (!hasPermission(user.role, permission)) {
      return c.json({ detail: `Your role can't ${permission.replace(/_/g, ' ')}.` }, 403);
    }
    await next();
  };
}
