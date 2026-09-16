// src/routes/teams.ts
import { Context, Hono } from 'hono';
import { AppEnv, requirePermission } from '../middleware/auth';
import { Permission } from '../permissions';
import { BroadcastAudience, DbNotification, DbTeam, DbUser, TeamResponse, TeamRole } from '../types';
import { generateUuid, toUserResponse } from '../utils';

const teams = new Hono<AppEnv>();

async function getTeamWithCount(c: Context<AppEnv>, teamId: string): Promise<TeamResponse | null> {
  const team = await c.env.DB.prepare('SELECT * FROM teams WHERE id = ?')
    .bind(teamId)
    .first<DbTeam>();

  if (!team) return null;

  const countRow = await c.env.DB.prepare('SELECT COUNT(*) as count FROM users WHERE team_id = ?')
    .bind(teamId)
    .first<{ count: number }>();

  return {
    id: team.id,
    name: team.name,
    capacity: team.capacity,
    current_size: countRow?.count || 0,
  };
}

teams.post('/', requirePermission(Permission.MANAGE_TEAMS), async (c) => {
  const body = await c.req.json<{ name: string; capacity?: number }>();
  if (!body.name?.trim()) {
    return c.json({ detail: 'Name is required' }, 400);
  }

  const id = generateUuid();
  const capacity = typeof body.capacity === 'number' ? body.capacity : 0;

  await c.env.DB.prepare('INSERT INTO teams (id, name, capacity) VALUES (?, ?, ?)')
    .bind(id, body.name.trim(), capacity)
    .run();

  const created = await getTeamWithCount(c, id);
  return c.json(created, 201);
});

teams.get('/', async (c) => {
  const teamsList = await c.env.DB.prepare('SELECT * FROM teams ORDER BY name ASC').all<DbTeam>();
  const counts = await c.env.DB.prepare(
    'SELECT team_id, COUNT(*) as count FROM users WHERE team_id IS NOT NULL GROUP BY team_id'
  ).all<{ team_id: string; count: number }>();

  const countMap = new Map<string, number>();
  (counts.results || []).forEach((r) => countMap.set(r.team_id, r.count));

  const result: TeamResponse[] = (teamsList.results || []).map((t) => ({
    id: t.id,
    name: t.name,
    capacity: t.capacity,
    current_size: countMap.get(t.id) || 0,
  }));

  return c.json(result);
});

teams.get('/:team_id', async (c) => {
  const team = await getTeamWithCount(c, c.req.param('team_id'));
  if (!team) {
    return c.json({ detail: 'Team not found' }, 404);
  }
  return c.json(team);
});

teams.put('/:team_id', requirePermission(Permission.MANAGE_TEAMS), async (c) => {
  const teamId = c.req.param('team_id');
  const existing = await getTeamWithCount(c, teamId);
  if (!existing) {
    return c.json({ detail: 'Team not found' }, 404);
  }

  const body = await c.req.json<{ name?: string; capacity?: number }>();
  const name = body.name !== undefined ? body.name : existing.name;
  let capacity = body.capacity !== undefined ? body.capacity : existing.capacity;

  if (body.capacity !== undefined && body.capacity < existing.current_size) {
    return c.json(
      { detail: `Team already has ${existing.current_size} members.` },
      400
    );
  }

  await c.env.DB.prepare('UPDATE teams SET name = ?, capacity = ? WHERE id = ?')
    .bind(name, capacity, teamId)
    .run();

  const updated = await getTeamWithCount(c, teamId);
  return c.json(updated);
});

teams.delete('/:team_id', requirePermission(Permission.MANAGE_TEAMS), async (c) => {
  const teamId = c.req.param('team_id');
  await c.env.DB.batch([
    c.env.DB.prepare('UPDATE users SET team_id = NULL, team_role = NULL WHERE team_id = ?').bind(teamId),
    c.env.DB.prepare('DELETE FROM teams WHERE id = ?').bind(teamId),
  ]);
  return c.json({ success: true, message: 'Team deleted' });
});

teams.post('/:team_id/members/:user_id', requirePermission(Permission.MANAGE_TEAMS), async (c) => {
  const teamId = c.req.param('team_id');
  const userId = c.req.param('user_id');
  let role: TeamRole = TeamRole.MEMBER;
  try {
    const body = await c.req.json<{ role?: TeamRole }>();
    if (body?.role) role = body.role;
  } catch {
    // default to MEMBER if no body or invalid json
  }

  const team = await getTeamWithCount(c, teamId);
  if (!team) {
    return c.json({ detail: 'Team not found' }, 404);
  }

  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(userId)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'User not found' }, 404);
  }

  // If user is not already in this team, check capacity
  if (user.team_id !== teamId && team.capacity > 0 && team.current_size >= team.capacity) {
    return c.json({ detail: 'Team is at full capacity' }, 400);
  }

  await c.env.DB.prepare('UPDATE users SET team_id = ?, team_role = ? WHERE id = ?')
    .bind(teamId, role, userId)
    .run();

  return c.json({ status: 'assigned', team_id: teamId, user_id: userId, role });
});

teams.delete('/:team_id/members/:user_id', requirePermission(Permission.MANAGE_TEAMS), async (c) => {
  const teamId = c.req.param('team_id');
  const userId = c.req.param('user_id');

  await c.env.DB.prepare(
    'UPDATE users SET team_id = NULL, team_role = NULL WHERE id = ? AND team_id = ?'
  )
    .bind(userId, teamId)
    .run();

  return c.json({ success: true, message: 'Member removed from team' });
});

teams.get('/:team_id/members', async (c) => {
  const teamId = c.req.param('team_id');
  const result = await c.env.DB.prepare('SELECT * FROM users WHERE team_id = ? ORDER BY name ASC')
    .bind(teamId)
    .all<DbUser>();

  return c.json((result.results || []).map(toUserResponse));
});

teams.get('/:team_id/notifications', async (c) => {
  const teamId = c.req.param('team_id');
  const result = await c.env.DB.prepare(
    'SELECT * FROM notifications WHERE team_id = ? ORDER BY timestamp DESC LIMIT 50'
  )
    .bind(teamId)
    .all<DbNotification>();

  return c.json(
    (result.results || []).map((n) => ({
      id: n.id,
      sender_id: n.sender_id,
      audience: n.audience as BroadcastAudience,
      team_id: n.team_id,
      recipient_id: n.recipient_id,
      message: n.message,
      timestamp: n.timestamp,
      recipient_count: n.recipient_count,
    }))
  );
});

export default teams;
