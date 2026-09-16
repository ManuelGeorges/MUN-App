-- 0002_seed_data.sql
-- Seed data for MIANU-SM IV Conference

-- Teams (Committees)
INSERT OR IGNORE INTO teams (id, name, capacity) VALUES
('team-csh', 'CSH (Comité Social et Humanitaire)', 30),
('team-cij', 'CIJ (Cour Internationale de Justice)', 20),
('team-ecosoc', 'ECOSOC (Conseil Économique et Social)', 35),
('team-hrc', 'HRC (Conseil des Droits de l''Homme)', 25),
('team-logistics', 'Logistique & Opérations', 15),
('team-hospitality', 'Accueil & Protocole', 15);

-- Halls
INSERT OR IGNORE INTO halls (id, name, capacity_threshold, allowed_roles, current_occupancy) VALUES
('hall-ga', 'Salle Plénière (General Assembly)', 300, 'user,organizer,admin,chief_organizer,team_leader,team_member', 42),
('hall-csh', 'CSH Council Chamber', 35, 'user,organizer,admin,chief_organizer,team_leader,team_member', 8),
('hall-cij', 'CIJ Courtroom', 25, 'user,organizer,admin,chief_organizer,team_leader,team_member', 4),
('hall-ecosoc', 'ECOSOC Hall', 40, 'user,organizer,admin,chief_organizer,team_leader,team_member', 12),
('hall-hrc', 'HRC Assembly', 30, 'user,organizer,admin,chief_organizer,team_leader,team_member', 6),
('hall-cafe', 'Cafétéria Principale', 150, 'user,organizer,admin,chief_organizer,team_leader,team_member', 28);

-- Users
INSERT OR IGNORE INTO users (id, name, email, phone, token_balance, is_active, role, team_id, team_role) VALUES
('usr-admin', 'admin', 'admin@mianu.org', '+20 100 000 0001', 500.0, 1, 'admin', NULL, NULL),
('usr-org', 'organizer', 'ops@mianu.org', '+20 100 000 0002', 250.0, 1, 'organizer', 'team-logistics', 'leader'),
('usr-csh-lead', 'Alexandre Dubois', 'alexandre.d@mianu.org', '+20 100 000 0003', 120.0, 1, 'team_leader', 'team-csh', 'leader'),
('usr-press', 'Camille Laurent', 'camille.l@mianu.org', '+20 100 000 0004', 80.0, 1, 'team_member', 'team-logistics', 'member'),
('usr-jean', 'Jean Dupont', 'jean.dupont@delegate.org', '+20 100 111 2233', 150.0, 1, 'user', 'team-csh', 'member'),
('usr-marie', 'Marie Curie', 'marie.curie@delegate.org', '+20 100 222 3344', 200.0, 1, 'user', 'team-ecosoc', 'member'),
('usr-paul', 'Paul Valéry', 'paul.valery@delegate.org', '+20 100 333 4455', 90.0, 1, 'user', 'team-hrc', 'member');

-- Cards
INSERT OR IGNORE INTO cards (uid, user_id, is_active) VALUES
('04A2B3C4D5', 'usr-jean', 1),
('04B3C4D5E6', 'usr-marie', 1),
('04C4D5E6F7', 'usr-paul', 1);

-- Meal Windows
INSERT OR IGNORE INTO meal_windows (meal_type, start_time, end_time) VALUES
('breakfast', '07:00', '10:00'),
('lunch', '12:00', '15:00');

-- Sample Broadcast Announcement
INSERT OR IGNORE INTO notifications (id, sender_id, audience, team_id, recipient_id, message, recipient_count, timestamp) VALUES
('notif-welcome', 'usr-admin', 'all', NULL, NULL, 'Bienvenue à MIANU-SM IV ! Le service de restauration et les badges NFC sont actifs.', 7, '2026-10-02T08:00:00Z');
