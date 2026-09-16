// src/routes/users.ts
import { Hono } from 'hono';
import { AppEnv, getCurrentUser, requirePermission } from '../middleware/auth';
import { Permission } from '../permissions';
import { DbCard, DbUser } from '../types';
import { generateUuid, toUserResponse } from '../utils';
import { hashPassword } from '../crypto';

const users = new Hono<AppEnv>();

users.get('/', async (c) => {
  const result = await c.env.DB.prepare(
    `SELECT u.*, c.uid as card_uid 
     FROM users u 
     LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
     ORDER BY u.name ASC`
  ).all<DbUser & { card_uid?: string }>();
  const list = (result.results || []).map((u) => ({
    ...toUserResponse(u),
    card_uid: u.card_uid || null,
  }));
  return c.json(list);
});

users.post('/', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const body = await c.req.json<{
    name: string;
    email?: string | null;
    phone?: string | null;
    password?: string | null;
    token_balance?: number;
    role?: string;
    team_id?: string | null;
    team_role?: string | null;
    meal_allowance?: number;
    meals_balance?: number;
    card_uid?: string | null;
  }>();

  if (!body.name?.trim()) {
    return c.json({ detail: 'Name is required' }, 400);
  }

  const id = generateUuid();
  const balance = typeof body.token_balance === 'number' ? body.token_balance : 0.0;
  const role = body.role || 'user';
  const teamId = body.team_id || null;
  const teamRole = body.team_role || null;
  const mealAllowance = typeof body.meal_allowance === 'number' ? body.meal_allowance : 6;
  const mealsBalance = typeof body.meals_balance === 'number' ? body.meals_balance : mealAllowance;
  const rawPassword = body.password?.trim() || 'Welcome@2026';
  const { hash, salt } = await hashPassword(rawPassword);

  await c.env.DB.prepare(
    `INSERT INTO users (id, name, email, phone, token_balance, is_active, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance) 
     VALUES (?, ?, ?, ?, ?, 1, ?, ?, ?, ?, ?, ?, ?)`
  )
    .bind(id, body.name.trim(), body.email || null, body.phone || null, balance, role, teamId, teamRole, hash, salt, mealAllowance, mealsBalance)
    .run();

  if (body.card_uid?.trim()) {
    await c.env.DB.prepare(
      'INSERT OR REPLACE INTO cards (uid, user_id, is_active) VALUES (?, ?, 1)'
    ).bind(body.card_uid.trim(), id).run();
  }

  const created = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(id)
    .first<DbUser>();

  return c.json({
    ...toUserResponse(created!),
    card_uid: body.card_uid?.trim() || null,
  }, 201);
});

users.get('/:user_id', async (c) => {
  let userId = c.req.param('user_id');
  if (userId === 'me') {
    const current = await getCurrentUser(c);
    if (!current) {
      return c.json({ detail: 'Sign in to continue.' }, 401);
    }
    userId = current.id;
  }

  const user = await c.env.DB.prepare(
    `SELECT u.*, c.uid as card_uid 
     FROM users u 
     LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
     WHERE u.id = ?`
  )
    .bind(userId)
    .first<DbUser & { card_uid?: string }>();

  if (!user) {
    return c.json({ detail: 'User not found' }, 404);
  }

  return c.json({
    ...toUserResponse(user),
    card_uid: user.card_uid || null,
  });
});

users.put('/:user_id', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const userId = c.req.param('user_id');
  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(userId)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'User not found' }, 404);
  }

  const body = await c.req.json<Partial<DbUser> & { password?: string; card_uid?: string }>();

  const name = body.name !== undefined ? body.name : user.name;
  const email = body.email !== undefined ? body.email : user.email;
  const phone = body.phone !== undefined ? body.phone : user.phone;
  const token_balance =
    body.token_balance !== undefined ? body.token_balance : user.token_balance;
  const is_active =
    body.is_active !== undefined ? (body.is_active ? 1 : 0) : user.is_active;
  const role = body.role !== undefined ? body.role : user.role;
  const team_id = body.team_id !== undefined ? body.team_id : user.team_id;
  const team_role = body.team_role !== undefined ? body.team_role : user.team_role;
  const meal_allowance =
    body.meal_allowance !== undefined ? body.meal_allowance : (user.meal_allowance ?? 6);
  const meals_balance =
    body.meals_balance !== undefined ? body.meals_balance : (user.meals_balance ?? 6);

  let passwordHash = user.password_hash;
  let salt = user.salt;
  if (body.password && body.password.trim()) {
    const hashed = await hashPassword(body.password.trim());
    passwordHash = hashed.hash;
    salt = hashed.salt;
  }

  await c.env.DB.prepare(
    `UPDATE users SET name = ?, email = ?, phone = ?, token_balance = ?, is_active = ?, role = ?, team_id = ?, team_role = ?, meal_allowance = ?, meals_balance = ?, password_hash = ?, salt = ? WHERE id = ?`
  )
    .bind(name, email, phone, token_balance, is_active, role, team_id, team_role, meal_allowance, meals_balance, passwordHash, salt, userId)
    .run();

  if (body.card_uid !== undefined) {
    const cardUid = body.card_uid.trim();
    if (cardUid) {
      await c.env.DB.prepare(
        'INSERT OR REPLACE INTO cards (uid, user_id, is_active) VALUES (?, ?, 1)'
      ).bind(cardUid, userId).run();
    } else {
      await c.env.DB.prepare('DELETE FROM cards WHERE user_id = ?').bind(userId).run();
    }
  }

  const updated = await c.env.DB.prepare(
    `SELECT u.*, c.uid as card_uid 
     FROM users u 
     LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
     WHERE u.id = ?`
  )
    .bind(userId)
    .first<DbUser & { card_uid?: string }>();

  return c.json({
    ...toUserResponse(updated!),
    card_uid: updated?.card_uid || null,
  });
});

users.post('/:user_id/meals/adjust', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const userId = c.req.param('user_id');
  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(userId)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'User not found' }, 404);
  }

  const body = await c.req.json<{ delta?: number; meals_balance?: number }>();
  let newBalance = typeof user.meals_balance === 'number' ? user.meals_balance : 6;

  if (typeof body.delta === 'number') {
    newBalance = Math.max(0, newBalance + body.delta);
  } else if (typeof body.meals_balance === 'number') {
    newBalance = Math.max(0, body.meals_balance);
  }

  await c.env.DB.prepare('UPDATE users SET meals_balance = ? WHERE id = ?')
    .bind(newBalance, userId)
    .run();

  const updated = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(userId)
    .first<DbUser>();

  return c.json(toUserResponse(updated!));
});

users.delete('/:user_id', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const userId = c.req.param('user_id');
  await c.env.DB.prepare('DELETE FROM users WHERE id = ?').bind(userId).run();
  return c.json({ success: true, message: 'User deleted' });
});

users.post('/cards/link', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const body = await c.req.json<{ card_uid: string; user_id: string }>();
  if (!body.card_uid?.trim() || !body.user_id?.trim()) {
    return c.json({ detail: 'card_uid and user_id are required' }, 400);
  }

  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(body.user_id)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'User not found' }, 404);
  }

  const existing = await c.env.DB.prepare('SELECT uid FROM cards WHERE uid = ?')
    .bind(body.card_uid.trim())
    .first();

  if (existing) {
    return c.json({ detail: 'Card already linked' }, 400);
  }

  await c.env.DB.prepare('INSERT INTO cards (uid, user_id, is_active) VALUES (?, ?, 1)')
    .bind(body.card_uid.trim(), user.id)
    .run();

  return c.json(
    {
      card_uid: body.card_uid.trim(),
      user: toUserResponse(user),
    },
    201
  );
});

users.get('/cards/:uid', async (c) => {
  const uid = c.req.param('uid');
  const card = await c.env.DB.prepare('SELECT * FROM cards WHERE uid = ?')
    .bind(uid)
    .first<DbCard>();

  if (!card) {
    return c.json({ detail: 'Card not found' }, 404);
  }

  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(card.user_id)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'Associated user not found' }, 404);
  }

  return c.json({
    card_uid: card.uid,
    user: toUserResponse(user),
  });
});

export default users;
