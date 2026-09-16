-- 0005_conference_attendance.sql
-- Conference-wide general attendance tracking (not hall-specific, but conference in sum)

CREATE TABLE IF NOT EXISTS conference_attendance (
    user_id TEXT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'present',
    last_check_in TEXT,
    last_check_out TEXT,
    method TEXT DEFAULT 'nfc_swipe',
    updated_at TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS attendance_records (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_uid TEXT,
    action TEXT NOT NULL,
    method TEXT DEFAULT 'nfc_swipe',
    timestamp TEXT NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_att_records_user ON attendance_records(user_id);
CREATE INDEX IF NOT EXISTS idx_att_records_time ON attendance_records(timestamp);

-- Seed initial attendance status for all existing users
INSERT OR REPLACE INTO conference_attendance (user_id, status, last_check_in, method, updated_at)
SELECT 
    id as user_id,
    CASE WHEN id IN ('usr-admin', 'usr-pres', 'usr-org', 'usr-csh-lead', 'usr-jean', '68c3e3a9-e839-4f9f-be01-117b8faa5dc7', 'usr-marie', 'usr-paul', 'usr-chief', 'usr-ecosoc-lead', 'usr-cij-lead', 'usr-hrc-lead', 'usr-org-hospitality', 'usr-press') 
         THEN 'present' 
         ELSE 'absent' 
    END as status,
    CASE WHEN id IN ('usr-admin', 'usr-pres', 'usr-org', 'usr-csh-lead', 'usr-jean', '68c3e3a9-e839-4f9f-be01-117b8faa5dc7', 'usr-marie', 'usr-paul', 'usr-chief', 'usr-ecosoc-lead', 'usr-cij-lead', 'usr-hrc-lead', 'usr-org-hospitality', 'usr-press') 
         THEN datetime('now', '-2 hours')
         ELSE NULL 
    END as last_check_in,
    'nfc_swipe' as method,
    datetime('now') as updated_at
FROM users;
