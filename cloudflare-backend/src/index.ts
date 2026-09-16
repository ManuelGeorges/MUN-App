// src/index.ts
import { Hono } from 'hono';
import { cors } from 'hono/cors';
import { AppEnv } from './middleware/auth';
import auth from './routes/auth';
import users from './routes/users';
import meals from './routes/meals';
import teams from './routes/teams';
import access from './routes/access';
import operations from './routes/operations';
import notifications from './routes/notifications';
import dashboard from './routes/dashboard';
import reports from './routes/reports';
import analytics from './routes/analytics';
import settings from './routes/settings';
import attendance from './routes/attendance';
import { ADMIN_DASHBOARD_HTML } from './admin/dashboardHtml';

const app = new Hono<AppEnv>();

// CORS middleware allowing requests from any origin
app.use(
  '*',
  cors({
    origin: '*',
    allowMethods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowHeaders: ['Content-Type', 'Authorization'],
  })
);

// Health check endpoint
app.get('/health', (c) => {
  return c.json({ status: 'ok' });
});

// Admin Command Center Dashboard UI
app.get('/', (c) => c.html(ADMIN_DASHBOARD_HTML));
app.get('/admin', (c) => c.html(ADMIN_DASHBOARD_HTML));
app.get('/dashboard', (c) => c.html(ADMIN_DASHBOARD_HTML));

// Mount API v1 sub-routes
const api = new Hono<AppEnv>();
api.get('/health', (c) => c.json({ status: 'ok' }));
api.route('/auth', auth);
api.route('/users', users);
api.route('/meals', meals);
api.route('/operations', operations);
api.route('/teams', teams);
api.route('/access', access);
api.route('/notifications', notifications);
api.route('/dashboard', dashboard);
api.route('/reports', reports);
api.route('/analytics', analytics);
api.route('/settings', settings);
api.route('/attendance', attendance);

app.route('/api/v1', api);

export default app;
