// src/routes/operations.ts
import { Hono } from 'hono';
import { AppEnv, getCurrentUser } from '../middleware/auth';
import { DelegateLocation, LiveScanEvent, UserRole } from '../types';

const operations = new Hono<AppEnv>();

/**
 * GET /api/v1/operations/locations
 * Real-time tracking: where every delegate and staff member is right now.
 * Accessible only by administrators. Organizers are restricted to scanning only.
 */
operations.get('/locations', async (c) => {
  const actor = (await getCurrentUser(c)) || c.get('user');
  if (!actor || (actor.role !== UserRole.ADMIN && actor.role !== UserRole.CHIEF_ORGANIZER)) {
    return c.json({ detail: 'Forbidden. Organizers cannot oversee venue locations.' }, 403);
  }

  // Query all users, their teams, their linked card, and conference attendance
  const usersResult = await c.env.DB.prepare(
    `SELECT u.id, u.name, u.role, u.team_id, t.name as team_name, c.uid as card_uid,
            COALESCE(ca.status, 'absent') as attendance_status,
            ca.last_check_in as attendance_time,
            COALESCE(ca.method, 'none') as attendance_method
     FROM users u
     LEFT JOIN teams t ON u.team_id = t.id
     LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
     LEFT JOIN conference_attendance ca ON u.id = ca.user_id
     WHERE u.is_active = 1
     ORDER BY u.name ASC`
  ).all<{
    id: string;
    name: string;
    role: string;
    team_id: string | null;
    team_name: string | null;
    card_uid: string | null;
    attendance_status: string;
    attendance_time: string | null;
    attendance_method: string | null;
  }>();

  // For each user, get their latest allowed access log
  const logsResult = await c.env.DB.prepare(
    `SELECT al.user_id, al.hall_id, al.action, al.timestamp, h.name as hall_name
     FROM access_logs al
     JOIN (
       SELECT user_id, MAX(timestamp) as max_time
       FROM access_logs
       WHERE allowed = 1
       GROUP BY user_id
     ) latest ON al.user_id = latest.user_id AND al.timestamp = latest.max_time
     LEFT JOIN halls h ON al.hall_id = h.id
     WHERE al.allowed = 1`
  ).all<{
    user_id: string;
    hall_id: string;
    action: string;
    timestamp: string;
    hall_name: string | null;
  }>();

  const latestLogByUser = new Map<string, {
    hall_id: string;
    hall_name: string | null;
    action: string;
    timestamp: string;
  }>();

  (logsResult.results || []).forEach((row) => {
    latestLogByUser.set(row.user_id, {
      hall_id: row.hall_id,
      hall_name: row.hall_name,
      action: row.action,
      timestamp: row.timestamp,
    });
  });

  const locations: DelegateLocation[] = (usersResult.results || []).map((u) => {
    const lastLog = latestLogByUser.get(u.id);
    let status: 'inside' | 'exited' | 'never_scanned' = 'never_scanned';
    let currentHallId: string | null = null;
    let currentHallName: string | null = null;
    let lastAction: string | null = null;
    let lastSeen: string | null = null;

    if (lastLog) {
      lastAction = lastLog.action;
      lastSeen = lastLog.timestamp;
      if (lastLog.action === 'entry') {
        status = 'inside';
        currentHallId = lastLog.hall_id;
        currentHallName = lastLog.hall_name;
      } else {
        status = 'exited';
      }
    }

    return {
      user_id: u.id,
      name: u.name,
      role: u.role,
      team_id: u.team_id,
      team_name: u.team_name,
      card_uid: u.card_uid,
      current_hall_id: currentHallId,
      current_hall_name: currentHallName,
      status,
      last_action: lastAction,
      last_seen: lastSeen,
      attendance_status: u.attendance_status as 'present' | 'absent',
      attendance_time: u.attendance_time,
      attendance_method: u.attendance_method,
    };
  });

  return c.json(locations);
});

/**
 * GET /api/v1/operations/scans
 * Unified chronological stream of all venue scans (hall door entries/exits & meal swipes).
 */
operations.get('/scans', async (c) => {
  const filterType = c.req.query('type') || 'all';

  const events: LiveScanEvent[] = [];

  if (filterType === 'all' || filterType === 'access') {
    const accessScans = await c.env.DB.prepare(
      `SELECT al.id, al.action, al.timestamp, al.allowed, al.reason,
              u.id as user_id, u.name as user_name, u.role as user_role,
              t.name as team_name,
              h.name as hall_name,
              c.uid as card_uid
       FROM access_logs al
       JOIN users u ON al.user_id = u.id
       LEFT JOIN teams t ON u.team_id = t.id
       LEFT JOIN halls h ON al.hall_id = h.id
       LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
       ORDER BY al.timestamp DESC
       LIMIT 50`
    ).all<{
      id: string;
      action: string;
      timestamp: string;
      allowed: number;
      reason: string | null;
      user_id: string;
      user_name: string;
      user_role: string;
      team_name: string | null;
      hall_name: string | null;
      card_uid: string | null;
    }>();

    (accessScans.results || []).forEach((row) => {
      events.push({
        id: row.id,
        scan_type: row.action === 'entry' ? 'access_entry' : 'access_exit',
        delegate_id: row.user_id,
        delegate_name: row.user_name,
        delegate_role: row.user_role,
        team_name: row.team_name,
        location_or_service: row.hall_name || 'Conference Hall',
        allowed: Boolean(row.allowed),
        reason: row.reason,
        timestamp: row.timestamp,
        meals_remaining: null,
      });
    });
  }

  if (filterType === 'all' || filterType === 'meals') {
    const mealScans = await c.env.DB.prepare(
      `SELECT mr.id, mr.meal_type, mr.swipe_timestamp as timestamp,
              u.id as user_id, u.name as user_name, u.role as user_role, u.meals_balance as meals_remaining,
              t.name as team_name,
              c.uid as card_uid
       FROM meal_records mr
       JOIN users u ON mr.user_id = u.id
       LEFT JOIN teams t ON u.team_id = t.id
       LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
       ORDER BY mr.swipe_timestamp DESC
       LIMIT 50`
    ).all<{
      id: string;
      meal_type: string;
      timestamp: string;
      user_id: string;
      user_name: string;
      user_role: string;
      meals_remaining: number | null;
      team_name: string | null;
      card_uid: string | null;
    }>();

    (mealScans.results || []).forEach((row) => {
      const mealLabel = row.meal_type === 'breakfast' ? 'Breakfast Service' : 'Lunch Service';
      events.push({
        id: row.id,
        scan_type: 'meal_swipe',
        delegate_id: row.user_id,
        delegate_name: row.user_name,
        delegate_role: row.user_role,
        team_name: row.team_name,
        location_or_service: mealLabel,
        allowed: true,
        reason: null,
        timestamp: row.timestamp,
        meals_remaining: typeof row.meals_remaining === 'number' ? row.meals_remaining : null,
      });
    });
  }

  if (filterType === 'all' || filterType === 'attendance' || filterType === 'access') {
    const attScans = await c.env.DB.prepare(
      `SELECT ar.id, ar.action, ar.method, ar.timestamp,
              u.id as user_id, u.name as user_name, u.role as user_role,
              t.name as team_name,
              c.uid as card_uid
       FROM attendance_records ar
       JOIN users u ON ar.user_id = u.id
       LEFT JOIN teams t ON u.team_id = t.id
       LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
       ORDER BY ar.timestamp DESC
       LIMIT 50`
    ).all<{
      id: string;
      action: string;
      method: string;
      timestamp: string;
      user_id: string;
      user_name: string;
      user_role: string;
      team_name: string | null;
      card_uid: string | null;
    }>();

    (attScans.results || []).forEach((row) => {
      const isCheckIn = row.action === 'check_in';
      events.push({
        id: row.id,
        scan_type: isCheckIn ? ('attendance_checkin' as any) : ('attendance_checkout' as any),
        delegate_id: row.user_id,
        delegate_name: row.user_name,
        delegate_role: row.user_role,
        team_name: row.team_name,
        location_or_service: 'Conference Main Entrance',
        allowed: true,
        reason: row.method === 'nfc_swipe' ? 'NFC Badge Check-in' : 'Admin Status Update',
        timestamp: row.timestamp,
        meals_remaining: null,
      });
    });
  }

  // Sort descending by timestamp
  events.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

  return c.json(events.slice(0, 50));
});

/**
 * GET /api/v1/operations/activity
 * Formatted scan event list for dashboard telemetry
 */
operations.get('/activity', async (c) => {
  const filterType = c.req.query('type') || 'all';

  const events: LiveScanEvent[] = [];

  if (filterType === 'all' || filterType === 'access') {
    const accessScans = await c.env.DB.prepare(
      `SELECT al.id, al.action, al.timestamp, al.allowed, al.reason,
              u.id as user_id, u.name as user_name, u.role as user_role,
              t.name as team_name,
              h.name as hall_name,
              c.uid as card_uid
       FROM access_logs al
       JOIN users u ON al.user_id = u.id
       LEFT JOIN teams t ON u.team_id = t.id
       LEFT JOIN halls h ON al.hall_id = h.id
       LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
       ORDER BY al.timestamp DESC
       LIMIT 50`
    ).all<{
      id: string;
      action: string;
      timestamp: string;
      allowed: number;
      reason: string | null;
      user_id: string;
      user_name: string;
      user_role: string;
      team_name: string | null;
      hall_name: string | null;
      card_uid: string | null;
    }>();

    (accessScans.results || []).forEach((row) => {
      events.push({
        id: row.id,
        scan_type: row.action === 'entry' ? 'access_entry' : 'access_exit',
        delegate_id: row.user_id,
        delegate_name: row.user_name,
        delegate_role: row.user_role,
        team_name: row.team_name,
        location_or_service: row.hall_name || 'Conference Hall',
        allowed: Boolean(row.allowed),
        reason: row.reason,
        timestamp: row.timestamp,
        meals_remaining: null,
      });
    });
  }

  if (filterType === 'all' || filterType === 'meals') {
    const mealScans = await c.env.DB.prepare(
      `SELECT mr.id, mr.meal_type, mr.swipe_timestamp as timestamp,
              u.id as user_id, u.name as user_name, u.role as user_role, u.meals_balance as meals_remaining,
              t.name as team_name,
              c.uid as card_uid
       FROM meal_records mr
       JOIN users u ON mr.user_id = u.id
       LEFT JOIN teams t ON u.team_id = t.id
       LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
       ORDER BY mr.swipe_timestamp DESC
       LIMIT 50`
    ).all<{
      id: string;
      meal_type: string;
      timestamp: string;
      user_id: string;
      user_name: string;
      user_role: string;
      meals_remaining: number | null;
      team_name: string | null;
      card_uid: string | null;
    }>();

    (mealScans.results || []).forEach((row) => {
      const mealLabel = row.meal_type === 'breakfast' ? 'Breakfast Service' : 'Lunch Service';
      events.push({
        id: row.id,
        scan_type: 'meal_swipe',
        delegate_id: row.user_id,
        delegate_name: row.user_name,
        delegate_role: row.user_role,
        team_name: row.team_name,
        location_or_service: mealLabel,
        allowed: true,
        reason: null,
        timestamp: row.timestamp,
        meals_remaining: typeof row.meals_remaining === 'number' ? row.meals_remaining : null,
      });
    });
  }

  if (filterType === 'all' || filterType === 'attendance' || filterType === 'access') {
    const attScans = await c.env.DB.prepare(
      `SELECT ar.id, ar.action, ar.method, ar.timestamp,
              u.id as user_id, u.name as user_name, u.role as user_role,
              t.name as team_name,
              c.uid as card_uid
       FROM attendance_records ar
       JOIN users u ON ar.user_id = u.id
       LEFT JOIN teams t ON u.team_id = t.id
       LEFT JOIN cards c ON u.id = c.user_id AND c.is_active = 1
       ORDER BY ar.timestamp DESC
       LIMIT 50`
    ).all<{
      id: string;
      action: string;
      method: string;
      timestamp: string;
      user_id: string;
      user_name: string;
      user_role: string;
      team_name: string | null;
      card_uid: string | null;
    }>();

    (attScans.results || []).forEach((row) => {
      const isCheckIn = row.action === 'check_in';
      events.push({
        id: row.id,
        scan_type: isCheckIn ? ('attendance_checkin' as any) : ('attendance_checkout' as any),
        delegate_id: row.user_id,
        delegate_name: row.user_name,
        delegate_role: row.user_role,
        team_name: row.team_name,
        location_or_service: 'Conference Main Entrance',
        allowed: true,
        reason: row.method === 'nfc_swipe' ? 'NFC Badge Check-in' : 'Admin Status Update',
        timestamp: row.timestamp,
        meals_remaining: null,
      });
    });
  }

  events.sort((a, b) => new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime());

  return c.json({
    events: events.slice(0, 50),
    total_events: events.length,
  });
});

export default operations;
