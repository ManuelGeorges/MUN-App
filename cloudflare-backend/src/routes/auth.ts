// src/routes/auth.ts
import { Hono } from 'hono';
import { AppEnv, requirePermission } from '../middleware/auth';
import { Permission } from '../permissions';
import { DbUser, UserRole } from '../types';
import { generateUuid, tokenFor } from '../utils';
import { hashPassword, verifyPassword } from '../crypto';

const auth = new Hono<AppEnv>();

/**
 * POST /api/v1/auth/login
 * Full cryptographic authentication checking username and hashed password.
 */
auth.post('/login', async (c) => {
  const body = await c.req.json<{ username?: string; password?: string }>();
  const username = body.username?.trim();
  const password = body.password;

  if (!username || !password) {
    return c.json({ detail: 'Username and password are required' }, 400);
  }

  const user = await c.env.DB.prepare(
    `SELECT * FROM users 
     WHERE LOWER(name) = LOWER(?) 
        OR LOWER(email) = LOWER(?) 
        OR LOWER(id) = LOWER(?)
        OR (LOWER(?) = 'admin' AND role = 'admin')
        OR LOWER(REPLACE(name, ' ', '.')) = LOWER(?)
        OR LOWER(SUBSTR(name, 1, INSTR(name, ' ') - 1)) = LOWER(?)`
  )
    .bind(username, username, username, username, username, username)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'Invalid username or password' }, 401);
  }

  if (!user.is_active) {
    return c.json({ detail: 'Account is deactivated or suspended' }, 403);
  }

  if (!user.password_hash || !user.salt) {
    return c.json({ detail: 'Account has no password configured. Contact conference admin.' }, 401);
  }

  const isValid = await verifyPassword(password, user.password_hash, user.salt);
  if (!isValid) {
    return c.json({ detail: 'Invalid username or password' }, 401);
  }

  return c.json(tokenFor(user), 200);
});

/**
 * POST /api/v1/auth/register
 * Admin-only delegate onboarding endpoint. Public registration is prohibited.
 */
auth.post('/register', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const body = await c.req.json<{
    username?: string;
    email?: string;
    password?: string;
    role?: string;
  }>();
  const username = body.username?.trim();

  if (!username) {
    return c.json({ detail: 'Username is required' }, 400);
  }

  const existing = await c.env.DB.prepare('SELECT id FROM users WHERE name = ?')
    .bind(username)
    .first();

  if (existing) {
    return c.json({ detail: 'User already exists' }, 400);
  }

  const rawPassword = body.password?.trim() || 'Welcome@2026';
  const { hash, salt } = await hashPassword(rawPassword);
  const role = body.role || UserRole.USER;
  const id = generateUuid();

  await c.env.DB.prepare(
    'INSERT INTO users (id, name, email, token_balance, is_active, role, password_hash, salt) VALUES (?, ?, ?, 0.0, 1, ?, ?, ?)'
  )
    .bind(id, username, body.email || null, role, hash, salt)
    .run();

  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(id)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'Registration failed' }, 500);
  }

  return c.json(tokenFor(user), 201);
});

export default auth;
