// src/routes/access.ts
import { Hono } from 'hono';
import { AppEnv, getCurrentUser, requirePermission } from '../middleware/auth';
import { Permission, hasPermission } from '../permissions';
import {
  AccessAction,
  AccessLogResponse,
  DbAccessLog,
  DbCard,
  DbHall,
  DbUser,
  HallAvailability,
  HallResponse,
  HallState,
  UserRole,
} from '../types';
import { generateUuid } from '../utils';

const access = new Hono<AppEnv>();

function parseRoles(rolesStr: string): UserRole[] {
  if (!rolesStr) return [UserRole.USER, UserRole.ORGANIZER, UserRole.ADMIN];
  return rolesStr
    .split(',')
    .map((r) => r.trim() as UserRole)
    .filter((r) => Object.values(UserRole).includes(r));
}

function toHallResponse(hall: DbHall): HallResponse {
  return {
    id: hall.id,
    name: hall.name,
    capacity_threshold: hall.capacity_threshold,
    allowed_roles: parseRoles(hall.allowed_roles),
    current_occupancy: hall.current_occupancy || 0,
  };
}

access.post('/halls', requirePermission(Permission.MANAGE_HALLS), async (c) => {
  const body = await c.req.json<{
    name: string;
    capacity_threshold?: number;
    allowed_roles?: string[];
  }>();

  if (!body.name?.trim()) {
    return c.json({ detail: 'Name is required' }, 400);
  }

  const id = generateUuid();
  const threshold = typeof body.capacity_threshold === 'number' ? body.capacity_threshold : 100;
  const rolesStr = (body.allowed_roles || [
    'user',
    'organizer',
    'admin',
    'chief_organizer',
    'team_leader',
    'team_member',
  ]).join(',');

  await c.env.DB.prepare(
    'INSERT INTO halls (id, name, capacity_threshold, allowed_roles, current_occupancy) VALUES (?, ?, ?, ?, 0)'
  )
    .bind(id, body.name.trim(), threshold, rolesStr)
    .run();

  const created = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(id)
    .first<DbHall>();

  return c.json(toHallResponse(created!), 201);
});

access.get('/halls', async (c) => {
  const result = await c.env.DB.prepare('SELECT * FROM halls ORDER BY name ASC').all<DbHall>();
  return c.json((result.results || []).map(toHallResponse));
});

access.put('/halls/:hall_id', requirePermission(Permission.MANAGE_HALLS), async (c) => {
  const hallId = c.req.param('hall_id');
  const existing = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(hallId)
    .first<DbHall>();

  if (!existing) {
    return c.json({ detail: 'Hall not found' }, 404);
  }

  const body = await c.req.json<{
    name?: string;
    capacity_threshold?: number;
    allowed_roles?: string[];
    current_occupancy?: number;
  }>();

  const name = body.name !== undefined ? body.name : existing.name;
  const threshold =
    body.capacity_threshold !== undefined ? body.capacity_threshold : existing.capacity_threshold;
  const rolesStr =
    body.allowed_roles !== undefined ? body.allowed_roles.join(',') : existing.allowed_roles;
  const current_occupancy =
    body.current_occupancy !== undefined ? Math.max(0, body.current_occupancy) : existing.current_occupancy;

  await c.env.DB.prepare(
    'UPDATE halls SET name = ?, capacity_threshold = ?, allowed_roles = ?, current_occupancy = ? WHERE id = ?'
  )
    .bind(name, threshold, rolesStr, current_occupancy, hallId)
    .run();

  const updated = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(hallId)
    .first<DbHall>();

  return c.json(toHallResponse(updated!));
});

access.post('/halls/:hall_id/occupancy', requirePermission(Permission.MANAGE_HALLS), async (c) => {
  const hallId = c.req.param('hall_id');
  const hall = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(hallId)
    .first<DbHall>();

  if (!hall) return c.json({ detail: 'Hall not found' }, 404);

  const body = await c.req.json<{ delta?: number; set_to?: number }>();
  let newOccupancy = hall.current_occupancy || 0;
  if (typeof body.delta === 'number') {
    newOccupancy = Math.max(0, newOccupancy + body.delta);
  } else if (typeof body.set_to === 'number') {
    newOccupancy = Math.max(0, body.set_to);
  }

  await c.env.DB.prepare('UPDATE halls SET current_occupancy = ? WHERE id = ?')
    .bind(newOccupancy, hallId)
    .run();

  const updated = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(hallId)
    .first<DbHall>();

  return c.json(toHallResponse(updated!));
});

access.delete('/halls/:hall_id', requirePermission(Permission.MANAGE_HALLS), async (c) => {
  const hallId = c.req.param('hall_id');
  await c.env.DB.batch([
    c.env.DB.prepare('DELETE FROM access_logs WHERE hall_id = ?').bind(hallId),
    c.env.DB.prepare('DELETE FROM halls WHERE id = ?').bind(hallId),
  ]);
  return c.json({ success: true, message: 'Hall and its logs deleted successfully' });
});

access.get('/halls/:hall_id/presence', async (c) => {
  const hallId = c.req.param('hall_id');
  const hall = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(hallId)
    .first<DbHall>();

  if (!hall) {
    return c.json({ detail: 'Hall not found' }, 404);
  }

  // Get users who entered but have not exited
  const logs = await c.env.DB.prepare(
    `SELECT u.name, l.action
     FROM access_logs l
     JOIN users u ON l.user_id = u.id
     WHERE l.hall_id = ? AND l.allowed = 1
     ORDER BY l.timestamp ASC`
  )
    .bind(hallId)
    .all<{ name: string; action: string }>();

  const inside = new Set<string>();
  (logs.results || []).forEach((row) => {
    if (row.action === 'entry') inside.add(row.name);
    else if (row.action === 'exit') inside.delete(row.name);
  });

  return c.json(Array.from(inside));
});

access.get('/availability', async (c) => {
  const result = await c.env.DB.prepare('SELECT * FROM halls ORDER BY name ASC').all<DbHall>();
  const availabilities: HallAvailability[] = (result.results || []).map((h) => {
    const ratio = h.capacity_threshold > 0 ? h.current_occupancy / h.capacity_threshold : 0;
    let state = HallState.AVAILABLE;
    if (ratio >= 1.0) state = HallState.FULL;
    else if (ratio >= 0.8) state = HallState.BUSY;

    return {
      id: h.id,
      name: h.name,
      current_occupancy: h.current_occupancy,
      capacity_threshold: h.capacity_threshold,
      state,
    };
  });

  return c.json(availabilities);
});

access.post('/scan', async (c) => {
  const actor = (await getCurrentUser(c)) || c.get('user');
  if (!actor) return c.json({ detail: 'Sign in to continue.' }, 401);

  if (!hasPermission(actor.role, Permission.SCAN_ACCESS)) {
    return c.json({ detail: "Your role can't scan access." }, 403);
  }

  const body = await c.req.json<{
    card_uid: string;
    hall_id: string;
    action: AccessAction;
  }>();

  if (!body.card_uid?.trim() || !body.hall_id?.trim() || !body.action) {
    return c.json({ detail: 'card_uid, hall_id and action are required' }, 400);
  }

  const card = await c.env.DB.prepare('SELECT * FROM cards WHERE uid = ?')
    .bind(body.card_uid.trim())
    .first<DbCard>();

  if (!card) return c.json({ detail: 'Card not registered' }, 404);

  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(card.user_id)
    .first<DbUser>();

  if (!user) return c.json({ detail: 'User not found' }, 404);

  const hall = await c.env.DB.prepare('SELECT * FROM halls WHERE id = ?')
    .bind(body.hall_id.trim())
    .first<DbHall>();

  if (!hall) return c.json({ detail: 'Hall not found' }, 404);

  const allowedRoles = parseRoles(hall.allowed_roles);
  let isAllowed = true;
  let reason: string | null = null;

  if (!allowedRoles.includes(user.role as UserRole)) {
    isAllowed = false;
    reason = `Role '${user.role}' not permitted in this hall.`;
  }

  if (isAllowed && body.action === AccessAction.ENTRY) {
    if (hall.capacity_threshold > 0 && hall.current_occupancy >= hall.capacity_threshold) {
      isAllowed = false;
      reason = 'Hall is at full capacity.';
    }
  }

  let newOccupancy = hall.current_occupancy;
  if (isAllowed) {
    if (body.action === AccessAction.ENTRY) {
      newOccupancy += 1;
    } else if (body.action === AccessAction.EXIT) {
      newOccupancy = Math.max(0, newOccupancy - 1);
    }
  }

  const logId = generateUuid();
  const timestamp = new Date().toISOString();

  await c.env.DB.batch([
    c.env.DB.prepare('UPDATE halls SET current_occupancy = ? WHERE id = ?').bind(
      newOccupancy,
      hall.id
    ),
    c.env.DB.prepare(
      'INSERT INTO access_logs (id, user_id, hall_id, action, timestamp, allowed, reason) VALUES (?, ?, ?, ?, ?, ?, ?)'
    ).bind(
      logId,
      user.id,
      hall.id,
      body.action,
      timestamp,
      isAllowed ? 1 : 0,
      reason
    ),
  ]);

  return c.json<AccessLogResponse>({
    id: logId,
    user_id: user.id,
    hall_id: hall.id,
    action: body.action,
    timestamp,
    allowed: isAllowed,
    reason,
  });
});

export default access;
