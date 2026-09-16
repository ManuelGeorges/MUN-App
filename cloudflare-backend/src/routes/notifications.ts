// src/routes/notifications.ts
import { Context, Hono } from 'hono';
import { AppEnv, getCurrentUser } from '../middleware/auth';
import { Permission, hasPermission } from '../permissions';
import {
  BroadcastAudience,
  DbNotification,
  DbTeam,
  DbUser,
  NotificationResponse,
  TeamRole,
  UserRole,
} from '../types';
import { generateUuid } from '../utils';

const notifications = new Hono<AppEnv>();

const STAFF_ROLES = [UserRole.ADMIN, UserRole.CHIEF_ORGANIZER, UserRole.ORGANIZER];

const handleBroadcast = async (c: Context<AppEnv>) => {
  const actor = (await getCurrentUser(c)) || c.get('user');
  if (!actor) return c.json({ detail: 'Sign in to continue.' }, 401);

  const body = await c.req.json<{
    message: string;
    audience: BroadcastAudience;
    team_id?: string | null;
    recipient_id?: string | null;
  }>();

  if (!body.message?.trim()) {
    return c.json({ detail: 'Message is required' }, 400);
  }

  // Check authorization
  if (!hasPermission(actor.role, Permission.BROADCAST_WIDE)) {
    if (!hasPermission(actor.role, Permission.BROADCAST_OWN_TEAM)) {
      return c.json({ detail: 'This role cannot send broadcasts.' }, 403);
    }
    if (actor.team_role !== TeamRole.LEADER) {
      return c.json({ detail: 'Only the leader of a team can message it.' }, 403);
    }
    if (!actor.team_id) {
      return c.json({ detail: 'You lead no team, so there is nobody to broadcast to.' }, 403);
    }
    if (body.audience === BroadcastAudience.ALL) {
      return c.json(
        { detail: 'Team leaders can only message their own team. Ask a chief organizer to reach everyone.' },
        403
      );
    }
    if (body.audience === BroadcastAudience.TEAM && body.team_id !== actor.team_id) {
      return c.json({ detail: 'Team leaders can only message their own team.' }, 403);
    }
    if (body.audience === BroadcastAudience.PARTICIPANT) {
      const target = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
        .bind(body.recipient_id)
        .first<DbUser>();
      if (!target) return c.json({ detail: 'Recipient not found' }, 404);
      if (target.team_id !== actor.team_id) {
        return c.json({ detail: 'That participant is not on your team.' }, 403);
      }
    }
  }

  // Count recipients
  let recipientCount = 0;
  if (body.audience === BroadcastAudience.ALL) {
    const staffPlaceholders = STAFF_ROLES.map(() => '?').join(',');
    const countRow = await c.env.DB.prepare(
      `SELECT COUNT(*) as count FROM users WHERE is_active = 1 AND role NOT IN (${staffPlaceholders})`
    )
      .bind(...STAFF_ROLES)
      .first<{ count: number }>();
    recipientCount = countRow?.count || 0;
  } else if (body.audience === BroadcastAudience.TEAM) {
    const countRow = await c.env.DB.prepare(
      'SELECT COUNT(*) as count FROM users WHERE is_active = 1 AND team_id = ?'
    )
      .bind(body.team_id)
      .first<{ count: number }>();
    recipientCount = countRow?.count || 0;
  } else {
    recipientCount = 1;
  }

  const id = generateUuid();
  const timestamp = new Date().toISOString();

  await c.env.DB.prepare(
    `INSERT INTO notifications (id, sender_id, audience, team_id, recipient_id, message, recipient_count, timestamp)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?)`
  )
    .bind(
      id,
      actor.id,
      body.audience,
      body.team_id || null,
      body.recipient_id || null,
      body.message.trim(),
      recipientCount,
      timestamp
    )
    .run();

  let teamName: string | null = null;
  if (body.team_id) {
    const t = await c.env.DB.prepare('SELECT name FROM teams WHERE id = ?')
      .bind(body.team_id)
      .first<DbTeam>();
    teamName = t?.name || null;
  }

  let recipientName: string | null = null;
  if (body.recipient_id) {
    const r = await c.env.DB.prepare('SELECT name FROM users WHERE id = ?')
      .bind(body.recipient_id)
      .first<DbUser>();
    recipientName = r?.name || null;
  }

  const response: NotificationResponse = {
    id,
    sender_id: actor.id,
    sender_name: actor.name,
    audience: body.audience,
    team_id: body.team_id || null,
    team_name: teamName,
    recipient_id: body.recipient_id || null,
    recipient_name: recipientName,
    message: body.message.trim(),
    recipient_count: recipientCount,
    timestamp,
  };

  return c.json(response, 201);
};

notifications.post('/broadcast', handleBroadcast);
notifications.post('/', handleBroadcast);

notifications.delete('/:id', async (c) => {
  const actor = (await getCurrentUser(c)) || c.get('user');
  if (!actor || (actor.role !== UserRole.ADMIN && actor.role !== UserRole.CHIEF_ORGANIZER)) {
    return c.json({ detail: 'Only administrators can delete broadcast announcements.' }, 403);
  }
  const id = c.req.param('id');
  await c.env.DB.prepare('DELETE FROM notifications WHERE id = ?').bind(id).run();
  return c.json({ success: true, message: 'Broadcast deleted' });
});

notifications.get('/', async (c) => {
  const senderId = c.req.query('sender_id');
  const limit = parseInt(c.req.query('limit') || '50', 10);

  let query = 'SELECT * FROM notifications';
  const params: any[] = [];

  if (senderId) {
    query += ' WHERE sender_id = ?';
    params.push(senderId);
  }

  query += ' ORDER BY timestamp DESC LIMIT ?';
  params.push(limit);

  const result = await c.env.DB.prepare(query)
    .bind(...params)
    .all<DbNotification>();

  const list: NotificationResponse[] = [];
  for (const n of result.results || []) {
    const sender = await c.env.DB.prepare('SELECT name FROM users WHERE id = ?')
      .bind(n.sender_id)
      .first<DbUser>();
    const team = n.team_id
      ? await c.env.DB.prepare('SELECT name FROM teams WHERE id = ?').bind(n.team_id).first<DbTeam>()
      : null;
    const recipient = n.recipient_id
      ? await c.env.DB.prepare('SELECT name FROM users WHERE id = ?')
          .bind(n.recipient_id)
          .first<DbUser>()
      : null;

    list.push({
      id: n.id,
      sender_id: n.sender_id,
      sender_name: sender?.name || null,
      audience: n.audience as BroadcastAudience,
      team_id: n.team_id,
      team_name: team?.name || null,
      recipient_id: n.recipient_id,
      recipient_name: recipient?.name || null,
      message: n.message,
      recipient_count: n.recipient_count,
      timestamp: n.timestamp,
    });
  }

  return c.json(list);
});

notifications.get('/inbox/:user_id', async (c) => {
  const userId = c.req.param('user_id');
  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(userId)
    .first<DbUser>();

  if (!user) return c.json({ detail: 'User not found' }, 404);

  const result = await c.env.DB.prepare(
    `SELECT * FROM notifications
     WHERE audience = 'all'
        OR recipient_id = ?
        OR (team_id IS NOT NULL AND team_id = ?)
     ORDER BY timestamp DESC LIMIT 50`
  )
    .bind(userId, user.team_id || '')
    .all<DbNotification>();

  const list: NotificationResponse[] = [];
  for (const n of result.results || []) {
    const sender = await c.env.DB.prepare('SELECT name FROM users WHERE id = ?')
      .bind(n.sender_id)
      .first<DbUser>();
    const team = n.team_id
      ? await c.env.DB.prepare('SELECT name FROM teams WHERE id = ?').bind(n.team_id).first<DbTeam>()
      : null;

    list.push({
      id: n.id,
      sender_id: n.sender_id,
      sender_name: sender?.name || null,
      audience: n.audience as BroadcastAudience,
      team_id: n.team_id,
      team_name: team?.name || null,
      recipient_id: n.recipient_id,
      message: n.message,
      recipient_count: n.recipient_count,
      timestamp: n.timestamp,
    });
  }

  return c.json(list);
});

export default notifications;
