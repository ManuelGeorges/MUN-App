// src/routes/settings.ts
import { Hono } from 'hono';
import { AppEnv, requirePermission } from '../middleware/auth';
import { Permission } from '../permissions';

const settings = new Hono<AppEnv>();

settings.get('/meal-windows', async (c) => {
  const result = await c.env.DB.prepare(
    'SELECT meal_type, start_time, end_time FROM meal_windows'
  ).all();

  const windows: Record<string, { start: string; end: string }> = {};
  for (const row of (result.results || []) as any[]) {
    windows[row.meal_type] = { start: row.start_time, end: row.end_time };
  }

  return c.json({
    breakfast_start: windows['breakfast']?.start || '08:00',
    breakfast_end: windows['breakfast']?.end || '10:30',
    lunch_start: windows['lunch']?.start || '13:00',
    lunch_end: windows['lunch']?.end || '15:30',
  });
});

settings.put('/meal-windows', requirePermission(Permission.MANAGE_SETTINGS), async (c) => {
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

export default settings;
