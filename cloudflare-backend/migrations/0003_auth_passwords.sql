-- 0003_auth_passwords.sql
-- Add password authentication fields and seed password hashes

ALTER TABLE users ADD COLUMN password_hash TEXT;
ALTER TABLE users ADD COLUMN salt TEXT;

-- Update existing seeded users
UPDATE users SET 
  password_hash = '096c25e42e287159ed1110a9d4437b108bc2ec8faa88dbc3c4552ff450a2f146',
  salt = '31d1ba8ca29121813d63cdc7eb0ed05b'
WHERE name = 'admin';

UPDATE users SET 
  password_hash = '1dc55b8e5a192684302634e514ca10a8284d24419e4f5539cb5a08b6f0db9d34',
  salt = '50b722791aea8b11721e04ce98c704f2'
WHERE name = 'organizer';

UPDATE users SET 
  password_hash = 'c3afe291e18655bbf2fa864cbb9542795b93983f136dd43bb10d280a72ea1cb5',
  salt = '5856c55b72d0458fe2bd86633b1e9596'
WHERE name = 'Alexandre Dubois';

UPDATE users SET 
  password_hash = '97f11cfce1c12405f238c8307ab410cc8d6812a824bd7b14b8f70644570e10db',
  salt = '3352eee5375c12d8c5460324bd81a27c'
WHERE name = 'Camille Laurent';

UPDATE users SET 
  password_hash = 'ade4eeeacc26d7411e9cf4efebd127af6541c288d218e20641a6e059bab8d712',
  salt = '641c54027ca364c889471666ab7dea2a'
WHERE name = 'Jean Dupont';

UPDATE users SET 
  password_hash = '0b89b72164ba793f25df75343145b21117853262711b05d171dc4b4e7210ea37',
  salt = '03f049ae89db99fcf5cea9207ebe1e78'
WHERE name = 'Marie Curie';

UPDATE users SET 
  password_hash = 'ea494ccb4aed7bb0576ff11995646664ebbd0b1697a6b246a8efd224f44b6d0f',
  salt = 'bbd5b19cc61548473067e86a7ddf9b2e'
WHERE name = 'Paul Valéry';

-- Also insert lowercase handle variants if not present for streamlined mobile entry
INSERT OR IGNORE INTO users (id, name, email, phone, token_balance, is_active, role, team_id, team_role, password_hash, salt) VALUES
('usr-chief', 'chief', 'chief@mianu.org', '+20 100 000 0005', 350.0, 1, 'chief_organizer', NULL, NULL,
 'b5fb68fc22d8437ed16346bf2656ede2a01adefdcd4c7ab97d048cffda31330e', '9611ae327c7603522b6542f3171f0ade'),
('usr-alexandre', 'alexandre', 'alexandre.d@mianu.org', '+20 100 000 0003', 120.0, 1, 'team_leader', 'team-csh', 'leader',
 'c3afe291e18655bbf2fa864cbb9542795b93983f136dd43bb10d280a72ea1cb5', '5856c55b72d0458fe2bd86633b1e9596'),
('usr-camille', 'camille', 'camille.l@mianu.org', '+20 100 000 0004', 80.0, 1, 'team_member', 'team-logistics', 'member',
 '97f11cfce1c12405f238c8307ab410cc8d6812a824bd7b14b8f70644570e10db', '3352eee5375c12d8c5460324bd81a27c'),
('usr-jean-dupont', 'jean.dupont', 'jean.dupont@delegate.org', '+20 100 111 2233', 150.0, 1, 'user', 'team-csh', 'member',
 'ade4eeeacc26d7411e9cf4efebd127af6541c288d218e20641a6e059bab8d712', '641c54027ca364c889471666ab7dea2a'),
('usr-marie-curie', 'marie.curie', 'marie.curie@delegate.org', '+20 100 222 3344', 200.0, 1, 'user', 'team-ecosoc', 'member',
 '0b89b72164ba793f25df75343145b21117853262711b05d171dc4b4e7210ea37', '03f049ae89db99fcf5cea9207ebe1e78'),
('usr-paul-valery', 'paul.valery', 'paul.valery@delegate.org', '+20 100 333 4455', 90.0, 1, 'user', 'team-hrc', 'member',
 'ea494ccb4aed7bb0576ff11995646664ebbd0b1697a6b246a8efd224f44b6d0f', 'bbd5b19cc61548473067e86a7ddf9b2e');
