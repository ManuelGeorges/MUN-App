-- 0001_initial_schema.sql
-- MIANU Conference SQLite schema for Cloudflare D1

CREATE TABLE IF NOT EXISTS teams (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    capacity INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE,
    phone TEXT,
    token_balance REAL DEFAULT 0.0,
    is_active INTEGER DEFAULT 1,
    role TEXT DEFAULT 'user',
    team_id TEXT REFERENCES teams(id) ON DELETE SET NULL,
    team_role TEXT,
    password_hash TEXT,
    salt TEXT
);

CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_team ON users(team_id);

CREATE TABLE IF NOT EXISTS cards (
    uid TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    is_active INTEGER DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_cards_user ON cards(user_id);

CREATE TABLE IF NOT EXISTS meal_records (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    meal_type TEXT NOT NULL,
    swipe_timestamp TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_meal_records_user ON meal_records(user_id);
CREATE INDEX IF NOT EXISTS idx_meal_records_timestamp ON meal_records(swipe_timestamp);

CREATE TABLE IF NOT EXISTS transactions (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount REAL NOT NULL,
    transaction_type TEXT NOT NULL,
    description TEXT,
    created_at TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_user ON transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_transactions_created ON transactions(created_at);

CREATE TABLE IF NOT EXISTS halls (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    capacity_threshold INTEGER DEFAULT 100,
    allowed_roles TEXT DEFAULT 'user,organizer,admin,chief_organizer,team_leader,team_member',
    current_occupancy INTEGER DEFAULT 0
);

CREATE TABLE IF NOT EXISTS access_logs (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    hall_id TEXT NOT NULL REFERENCES halls(id) ON DELETE CASCADE,
    action TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    allowed INTEGER DEFAULT 1,
    reason TEXT
);

CREATE INDEX IF NOT EXISTS idx_access_logs_hall ON access_logs(hall_id);
CREATE INDEX IF NOT EXISTS idx_access_logs_user ON access_logs(user_id);

CREATE TABLE IF NOT EXISTS notifications (
    id TEXT PRIMARY KEY,
    sender_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    audience TEXT DEFAULT 'team',
    team_id TEXT REFERENCES teams(id) ON DELETE SET NULL,
    recipient_id TEXT REFERENCES users(id) ON DELETE SET NULL,
    message TEXT NOT NULL,
    recipient_count INTEGER DEFAULT 0,
    timestamp TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient ON notifications(recipient_id);
CREATE INDEX IF NOT EXISTS idx_notifications_team ON notifications(team_id);

CREATE TABLE IF NOT EXISTS meal_windows (
    meal_type TEXT PRIMARY KEY,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL
);
