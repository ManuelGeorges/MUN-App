// src/routes/meals.ts
import { Hono } from 'hono';
import { AppEnv } from '../middleware/auth';
import { DbCard, DbMealRecord, DbUser, MealType } from '../types';
import { generateUuid } from '../utils';

const meals = new Hono<AppEnv>();

meals.post('/swipe', async (c) => {
  const body = await c.req.json<{
    card_uid: string;
    meal_type: string;
    timestamp?: string;
  }>();

  if (!body.card_uid?.trim() || !body.meal_type?.trim()) {
    return c.json({ detail: 'card_uid and meal_type are required' }, 400);
  }

  // 1. Resolve User from Card
  const card = await c.env.DB.prepare('SELECT * FROM cards WHERE uid = ?')
    .bind(body.card_uid.trim())
    .first<DbCard>();

  if (!card) {
    return c.json({ detail: 'Card not registered' }, 404);
  }

  const user = await c.env.DB.prepare('SELECT * FROM users WHERE id = ?')
    .bind(card.user_id)
    .first<DbUser>();

  if (!user) {
    return c.json({ detail: 'Card not registered' }, 404);
  }

  if (!user.is_active) {
    return c.json({ detail: 'User account inactive' }, 403);
  }

  const currentMeals = typeof user.meals_balance === 'number' ? user.meals_balance : 6;
  if (currentMeals <= 0) {
    return c.json({ detail: 'No meal credits remaining (0 left)' }, 403);
  }

  const swipeTimestamp = body.timestamp || new Date().toISOString();
  const todayDate = swipeTimestamp.split('T')[0];

  // 2. Check for duplicate meal today
  const existing = await c.env.DB.prepare(
    'SELECT id FROM meal_records WHERE user_id = ? AND meal_type = ? AND swipe_timestamp LIKE ?'
  )
    .bind(user.id, body.meal_type, `${todayDate}%`)
    .first();

  if (existing) {
    return c.json({ detail: `Already had ${body.meal_type} today` }, 409);
  }

  // 3. Record Meal & Decrement Balance
  const id = generateUuid();
  const newBalance = Math.max(0, currentMeals - 1);

  await c.env.DB.batch([
    c.env.DB.prepare(
      'INSERT INTO meal_records (id, user_id, meal_type, swipe_timestamp) VALUES (?, ?, ?, ?)'
    ).bind(id, user.id, body.meal_type, swipeTimestamp),
    c.env.DB.prepare('UPDATE users SET meals_balance = ? WHERE id = ?').bind(newBalance, user.id),
  ]);

  const record = await c.env.DB.prepare('SELECT * FROM meal_records WHERE id = ?')
    .bind(id)
    .first<DbMealRecord>();

  if (!record) {
    return c.json({ detail: 'Failed to record meal' }, 500);
  }

  return c.json(
    {
      id: record.id,
      user_id: record.user_id,
      meal_type: record.meal_type as MealType,
      swipe_timestamp: record.swipe_timestamp,
      meals_remaining: newBalance,
    },
    201
  );
});

export default meals;
