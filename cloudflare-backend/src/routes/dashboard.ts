// src/routes/dashboard.ts
import { Hono } from 'hono';
import { AppEnv } from '../middleware/auth';
import { DashboardOverview } from '../types';

const dashboard = new Hono<AppEnv>();

dashboard.get('/overview', async (c) => {
  const today = new Date().toISOString().split('T')[0];

  const mealsRow = await c.env.DB.prepare(
    'SELECT COUNT(*) as count FROM meal_records WHERE swipe_timestamp LIKE ?'
  )
    .bind(`${today}%`)
    .first<{ count: number }>();

  const accessRow = await c.env.DB.prepare(
    'SELECT COUNT(*) as count FROM access_logs WHERE timestamp LIKE ?'
  )
    .bind(`${today}%`)
    .first<{ count: number }>();

  const activeUsersRow = await c.env.DB.prepare(
    'SELECT COUNT(*) as count FROM users WHERE is_active = 1'
  ).first<{ count: number }>();

  const insideRow = await c.env.DB.prepare(
    'SELECT COALESCE(SUM(current_occupancy), 0) as total FROM halls'
  ).first<{ total: number }>();

  const overview: DashboardOverview = {
    total_meals_today: mealsRow?.count || 0,
    total_access_scans_today: accessRow?.count || 0,
    active_users: activeUsersRow?.count || 0,
    people_inside_halls: insideRow?.total || 0,
  };

  return c.json(overview);
});

dashboard.put('/settings/meal-windows', async (c) => {
  const body = await c.req.json<{
    breakfast_start: string;
    breakfast_end: string;
    lunch_start: string;
    lunch_end: string;
  }>();

  await c.env.DB.batch([
    c.env.DB.prepare(
      'INSERT OR REPLACE INTO meal_windows (meal_type, start_time, end_time) VALUES (?, ?, ?)'
    ).bind('breakfast', body.breakfast_start, body.breakfast_end),
    c.env.DB.prepare(
      'INSERT OR REPLACE INTO meal_windows (meal_type, start_time, end_time) VALUES (?, ?, ?)'
    ).bind('lunch', body.lunch_start, body.lunch_end),
  ]);

  return c.json({ settings: body });
});

export default dashboard;
