// src/routes/reports.ts
import { Hono } from 'hono';
import { AppEnv } from '../middleware/auth';
import { ReportSummary } from '../types';

const reports = new Hono<AppEnv>();

reports.get('/meals', async (c) => {
  const row = await c.env.DB.prepare('SELECT COUNT(*) as count FROM meal_records').first<{ count: number }>();
  const summary: ReportSummary = {
    total_records: row?.count || 0,
    generated_at: new Date().toISOString(),
  };
  return c.json(summary);
});

reports.get('/transactions', async (c) => {
  const row = await c.env.DB.prepare('SELECT COUNT(*) as count FROM transactions').first<{ count: number }>();
  const summary: ReportSummary = {
    total_records: row?.count || 0,
    generated_at: new Date().toISOString(),
  };
  return c.json(summary);
});

reports.get('/users', async (c) => {
  const row = await c.env.DB.prepare('SELECT COUNT(*) as count FROM users').first<{ count: number }>();
  const summary: ReportSummary = {
    total_records: row?.count || 0,
    generated_at: new Date().toISOString(),
  };
  return c.json(summary);
});

reports.get('/export', async (c) => {
  const meals = await c.env.DB.prepare('SELECT COUNT(*) as count FROM meal_records').first<{ count: number }>();
  const txns = await c.env.DB.prepare('SELECT COUNT(*) as count FROM transactions').first<{ count: number }>();
  const users = await c.env.DB.prepare('SELECT COUNT(*) as count FROM users').first<{ count: number }>();

  const dateStr = new Date().toISOString().replace(/[:.]/g, '-').slice(0, 16);
  const filename = `mianu_export_${dateStr}.csv`;

  return c.json({
    status: 'ready',
    path: `/downloads/${filename} (${users?.count || 0} delegates, ${meals?.count || 0} meals, ${txns?.count || 0} txns)`,
  });
});

export default reports;
