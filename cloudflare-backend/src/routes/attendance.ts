// src/routes/attendance.ts
import { Hono } from 'hono';
import { AppEnv, getCurrentUser, requirePermission } from '../middleware/auth';
import { Permission } from '../permissions';
import { generateUuid } from '../utils';
import { DbCard } from '../types';

const attendance = new Hono<AppEnv>();

/**
 * GET /api/v1/attendance
 * Returns overall conference attendance summary and full delegate roster with attendance state.
 */
attendance.get('/', async (c) => {
  const usersResult = await c.env.DB.prepare(
    `SELECT u.id as user_id, u.name, u.email, u.phone, u.role, u.team_id, t.name as team_name,
            c.uid as card_uid,
            COALESCE(ca.status, 'absent') as status,
            ca.last_check_in,
            ca.last_check_out,
            COALESCE(ca.method, 'none') as method,
            ca.updated_at
     FROM users u
     LEFT JOIN teams t ON u.team_id = t.id
     LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
     LEFT JOIN conference_attendance ca ON u.id = ca.user_id
     WHERE u.is_active = 1
     ORDER BY u.name ASC`
  ).all<{
    user_id: string;
    name: string;
    email: string | null;
    phone: string | null;
    role: string;
    team_id: string | null;
    team_name: string | null;
    card_uid: string | null;
    status: string;
    last_check_in: string | null;
    last_check_out: string | null;
    method: string;
    updated_at: string | null;
  }>();

  const attendees = usersResult.results || [];
  const totalRegistered = attendees.length;
  const totalPresent = attendees.filter((a) => a.status === 'present').length;
  const totalAbsent = totalRegistered - totalPresent;
  const rate = totalRegistered > 0 ? Math.round((totalPresent / totalRegistered) * 100) : 0;

  const totalSwipesRow = await c.env.DB.prepare(
    `SELECT COUNT(*) as total_swipes FROM attendance_records`
  ).first<{ total_swipes: number }>();

  return c.json({
    summary: {
      total_registered: totalRegistered,
      total_present: totalPresent,
      total_absent: totalAbsent,
      attendance_rate: rate,
      total_swipes: totalSwipesRow?.total_swipes || 0,
    },
    attendees,
  });
});

/**
 * POST /api/v1/attendance/swipe
 * Register conference entrance attendance via NFC card badge swipe.
 */
attendance.post('/swipe', async (c) => {
  const actor = (await getCurrentUser(c)) || c.get('user');
  if (!actor) return c.json({ detail: 'Sign in to continue.' }, 401);

  const body = await c.req.json<{
    card_uid: string;
    action?: 'check_in' | 'check_out' | 'toggle';
  }>();

  if (!body.card_uid?.trim()) {
    return c.json({ detail: 'card_uid is required' }, 400);
  }

  const card = await c.env.DB.prepare('SELECT * FROM cards WHERE uid = ?')
    .bind(body.card_uid.trim())
    .first<DbCard>();

  if (!card) return c.json({ detail: 'Card not registered to any conference attendee' }, 404);

  const user = await c.env.DB.prepare(
    `SELECT u.id, u.name, u.email, u.phone, u.role, u.team_id, t.name as team_name
     FROM users u
     LEFT JOIN teams t ON u.team_id = t.id
     WHERE u.id = ?`
  )
    .bind(card.user_id)
    .first<{
      id: string;
      name: string;
      email: string | null;
      phone: string | null;
      role: string;
      team_id: string | null;
      team_name: string | null;
    }>();

  if (!user) return c.json({ detail: 'User not found' }, 404);

  // Check current presence state
  const currentPresence = await c.env.DB.prepare(
    'SELECT * FROM conference_attendance WHERE user_id = ?'
  )
    .bind(user.id)
    .first<{ status: string; last_check_in: string | null; last_check_out: string | null }>();

  let newStatus = 'present';
  let logAction = 'check_in';

  if (body.action === 'check_out') {
    newStatus = 'absent';
    logAction = 'check_out';
  } else if (body.action === 'toggle') {
    if (currentPresence?.status === 'present') {
      newStatus = 'absent';
      logAction = 'check_out';
    } else {
      newStatus = 'present';
      logAction = 'check_in';
    }
  } else {
    // Default action: check_in
    newStatus = 'present';
    logAction = 'check_in';
  }

  const now = new Date().toISOString();
  const logId = generateUuid();

  await c.env.DB.batch([
    c.env.DB.prepare(
      `INSERT INTO conference_attendance (user_id, status, last_check_in, last_check_out, method, updated_at)
       VALUES (?, ?, ?, ?, 'nfc_swipe', ?)
       ON CONFLICT(user_id) DO UPDATE SET
         status = excluded.status,
         last_check_in = CASE WHEN excluded.status = 'present' THEN excluded.last_check_in ELSE conference_attendance.last_check_in END,
         last_check_out = CASE WHEN excluded.status = 'absent' THEN excluded.last_check_out ELSE conference_attendance.last_check_out END,
         method = 'nfc_swipe',
         updated_at = excluded.updated_at`
    ).bind(
      user.id,
      newStatus,
      newStatus === 'present' ? now : (currentPresence?.last_check_in || null),
      newStatus === 'absent' ? now : (currentPresence?.last_check_out || null),
      now
    ),
    c.env.DB.prepare(
      `INSERT INTO attendance_records (id, user_id, card_uid, action, method, timestamp)
       VALUES (?, ?, ?, ?, 'nfc_swipe', ?)`
    ).bind(logId, user.id, body.card_uid.trim(), logAction, now),
  ]);

  // Compute updated summary
  const summaryRow = await c.env.DB.prepare(
    `SELECT 
       COUNT(*) as total_registered,
       SUM(CASE WHEN ca.status = 'present' THEN 1 ELSE 0 END) as total_present
     FROM users u
     LEFT JOIN conference_attendance ca ON u.id = ca.user_id
     WHERE u.is_active = 1`
  ).first<{ total_registered: number; total_present: number }>();

  const totalRegistered = summaryRow?.total_registered || 0;
  const totalPresent = summaryRow?.total_present || 0;
  const totalAbsent = totalRegistered - totalPresent;
  const rate = totalRegistered > 0 ? Math.round((totalPresent / totalRegistered) * 100) : 0;

  return c.json({
    success: true,
    user: {
      id: user.id,
      name: user.name,
      role: user.role,
      team_name: user.team_name,
      card_uid: body.card_uid.trim(),
      status: newStatus,
      timestamp: now,
      action: logAction,
    },
    summary: {
      total_registered: totalRegistered,
      total_present: totalPresent,
      total_absent: totalAbsent,
      attendance_rate: rate,
    },
  });
});

/**
 * POST /api/v1/attendance/single
 * Modify a single person's conference attendance status.
 */
attendance.post('/single', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const body = await c.req.json<{
    user_id: string;
    status: 'present' | 'absent';
  }>();

  if (!body.user_id || !body.status) {
    return c.json({ detail: 'user_id and status are required' }, 400);
  }

  const user = await c.env.DB.prepare('SELECT id, name FROM users WHERE id = ?')
    .bind(body.user_id)
    .first<{ id: string; name: string }>();

  if (!user) return c.json({ detail: 'User not found' }, 404);

  const now = new Date().toISOString();
  const logId = generateUuid();
  const logAction = body.status === 'present' ? 'check_in' : 'check_out';

  await c.env.DB.batch([
    c.env.DB.prepare(
      `INSERT INTO conference_attendance (user_id, status, last_check_in, last_check_out, method, updated_at)
       VALUES (?, ?, ?, ?, 'admin_manual', ?)
       ON CONFLICT(user_id) DO UPDATE SET
         status = excluded.status,
         last_check_in = CASE WHEN excluded.status = 'present' THEN excluded.last_check_in ELSE conference_attendance.last_check_in END,
         last_check_out = CASE WHEN excluded.status = 'absent' THEN excluded.last_check_out ELSE conference_attendance.last_check_out END,
         method = 'admin_manual',
         updated_at = excluded.updated_at`
    ).bind(
      user.id,
      body.status,
      body.status === 'present' ? now : null,
      body.status === 'absent' ? now : null,
      now
    ),
    c.env.DB.prepare(
      `INSERT INTO attendance_records (id, user_id, card_uid, action, method, timestamp)
       VALUES (?, ?, NULL, ?, 'admin_manual', ?)`
    ).bind(logId, user.id, logAction, now),
  ]);

  return c.json({
    success: true,
    user_id: user.id,
    name: user.name,
    status: body.status,
    timestamp: now,
  });
});

/**
 * POST /api/v1/attendance/bulk
 * Modify conference attendance for multiple persons (or all) at once.
 */
attendance.post('/bulk', requirePermission(Permission.MANAGE_USERS), async (c) => {
  const body = await c.req.json<{
    user_ids?: string[];
    all?: boolean;
    status: 'present' | 'absent';
  }>();

  if (!body.status || (body.status !== 'present' && body.status !== 'absent')) {
    return c.json({ detail: 'Valid status (present or absent) is required' }, 400);
  }

  let targetIds: string[] = [];

  if (body.all) {
    const allUsers = await c.env.DB.prepare('SELECT id FROM users WHERE is_active = 1').all<{
      id: string;
    }>();
    targetIds = (allUsers.results || []).map((u) => u.id);
  } else if (Array.isArray(body.user_ids) && body.user_ids.length > 0) {
    targetIds = body.user_ids;
  } else {
    return c.json({ detail: 'Either all: true or user_ids list is required' }, 400);
  }

  const now = new Date().toISOString();
  const logAction = body.status === 'present' ? 'check_in' : 'check_out';

  const statements: D1PreparedStatement[] = [];

  for (const uid of targetIds) {
    statements.push(
      c.env.DB.prepare(
        `INSERT INTO conference_attendance (user_id, status, last_check_in, last_check_out, method, updated_at)
         VALUES (?, ?, ?, ?, 'bulk_override', ?)
         ON CONFLICT(user_id) DO UPDATE SET
           status = excluded.status,
           last_check_in = CASE WHEN excluded.status = 'present' THEN excluded.last_check_in ELSE conference_attendance.last_check_in END,
           last_check_out = CASE WHEN excluded.status = 'absent' THEN excluded.last_check_out ELSE conference_attendance.last_check_out END,
           method = 'bulk_override',
           updated_at = excluded.updated_at`
      ).bind(
        uid,
        body.status,
        body.status === 'present' ? now : null,
        body.status === 'absent' ? now : null,
        now
      )
    );

    statements.push(
      c.env.DB.prepare(
        `INSERT INTO attendance_records (id, user_id, card_uid, action, method, timestamp)
         VALUES (?, ?, NULL, ?, 'bulk_override', ?)`
      ).bind(generateUuid(), uid, logAction, now)
    );
  }

  // Execute in batches of 100
  const BATCH_SIZE = 100;
  for (let i = 0; i < statements.length; i += BATCH_SIZE) {
    const chunk = statements.slice(i, i + BATCH_SIZE);
    await c.env.DB.batch(chunk);
  }

  return c.json({
    success: true,
    count: targetIds.length,
    status: body.status,
    timestamp: now,
  });
});

export default attendance;
