// src/routes/analytics.ts
import { Hono } from 'hono';
import { AppEnv } from '../middleware/auth';
import { AnalyticsSummary } from '../types';

const analytics = new Hono<AppEnv>();

analytics.get('/daily', async (c) => {
  const today = new Date().toISOString().split('T')[0];
  const row = await c.env.DB.prepare(
    'SELECT COUNT(*) as count FROM meal_records WHERE swipe_timestamp LIKE ?'
  )
    .bind(`${today}%`)
    .first<{ count: number }>();

  const summary: AnalyticsSummary = {
    metric: 'meals_today',
    total: row?.count || 0,
    generated_at: new Date().toISOString(),
  };

  return c.json(summary);
});

analytics.get('/trends', async (c) => {
  const row = await c.env.DB.prepare('SELECT COUNT(*) as count FROM transactions').first<{ count: number }>();

  const summary: AnalyticsSummary = {
    metric: 'transactions_total',
    total: row?.count || 0,
    generated_at: new Date().toISOString(),
  };

  return c.json(summary);
});

export default analytics;
