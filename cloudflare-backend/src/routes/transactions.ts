// src/routes/transactions.ts
import { Hono } from 'hono';
import { AppEnv, getCurrentUser } from '../middleware/auth';
import { Permission, hasPermission } from '../permissions';
import { DbCard, DbTransaction, DbUser, TransactionType } from '../types';
import { generateUuid } from '../utils';

const transactions = new Hono<AppEnv>();

transactions.post('/', async (c) => {
  const actor = (await getCurrentUser(c)) || c.get('user');
  if (!actor) {
    return c.json({ detail: 'Sign in to continue.' }, 401);
  }

  const body = await c.req.json<{
    card_uid: string;
    amount: number;
    transaction_type: TransactionType;
    description?: string | null;
  }>();

  if (!body.card_uid?.trim() || body.amount === undefined || !body.transaction_type) {
    return c.json({ detail: 'card_uid, amount and transaction_type are required' }, 400);
  }

  // Authorize direction
  const neededPermission =
    body.transaction_type === TransactionType.RECHARGE
      ? Permission.TOP_UP_WALLET
      : Permission.CHARGE_TOKENS;

  if (!hasPermission(actor.role, neededPermission)) {
    const detail =
      body.transaction_type === TransactionType.RECHARGE
        ? 'Only an admin can top up a wallet.'
        : "Your role can't take payment.";
    return c.json({ detail }, 403);
  }

  // Resolve user
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
    return c.json({ detail: 'User not found' }, 404);
  }

  let newBalance = user.token_balance;
  if (body.transaction_type === TransactionType.RECHARGE) {
    newBalance += body.amount;
  } else if (body.transaction_type === TransactionType.DEDUCTION) {
    if (user.token_balance < body.amount) {
      return c.json({ detail: 'Insufficient balance' }, 400);
    }
    newBalance -= body.amount;
  }

  const txId = generateUuid();
  const createdAt = new Date().toISOString();

  // Execute update balance and record transaction atomically in D1
  await c.env.DB.batch([
    c.env.DB.prepare('UPDATE users SET token_balance = ? WHERE id = ?').bind(newBalance, user.id),
    c.env.DB.prepare(
      'INSERT INTO transactions (id, user_id, amount, transaction_type, description, created_at) VALUES (?, ?, ?, ?, ?, ?)'
    ).bind(
      txId,
      user.id,
      body.amount,
      body.transaction_type,
      body.description || null,
      createdAt
    ),
  ]);

  const createdTx = await c.env.DB.prepare('SELECT * FROM transactions WHERE id = ?')
    .bind(txId)
    .first<DbTransaction>();

  if (!createdTx) {
    return c.json({ detail: 'Failed to record transaction' }, 500);
  }

  return c.json(
    {
      id: createdTx.id,
      user_id: createdTx.user_id,
      amount: createdTx.amount,
      transaction_type: createdTx.transaction_type as TransactionType,
      description: createdTx.description,
      created_at: createdTx.created_at,
    },
    201
  );
});

export default transactions;
