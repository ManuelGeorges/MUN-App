// seed_users.js
import crypto from 'crypto';
import fs from 'fs';

function hashPassword(password) {
  const salt = crypto.randomBytes(16);
  const hash = crypto.pbkdf2Sync(password, salt, 100000, 32, 'sha256');
  return {
    salt: salt.toString('hex'),
    hash: hash.toString('hex')
  };
}

const ACCOUNTS = [
  // 1. Administration (Executive Command)
  {
    id: 'usr-admin',
    name: 'Secrétariat Général',
    email: 'admin@mianu.org',
    password: 'Admin@2026',
    role: 'admin',
    team_id: null,
    team_role: null,
    phone: '+20 100 888 9999',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04F0E1D2C3'
  },
  {
    id: 'usr-pres',
    name: 'Présidence MIANU',
    email: 'president@mianu.org',
    password: 'President@2026',
    role: 'admin',
    team_id: null,
    team_role: null,
    phone: '+20 100 888 1111',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04E1D2C3B4'
  },

  // 2. Floor Staff & Organizers (Scan & Logistics)
  {
    id: 'usr-org',
    name: 'Équipe Logistique',
    email: 'ops@mianu.org',
    password: 'Organizer@2026',
    role: 'organizer',
    team_id: 'team-logistics',
    team_role: 'member',
    phone: '+20 100 000 0002',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04A1122334'
  },
  {
    id: 'usr-org-hospitality',
    name: 'Équipe Accueil & Protocole',
    email: 'hospitality@mianu.org',
    password: 'Hospitality@2026',
    role: 'organizer',
    team_id: 'team-hospitality',
    team_role: 'member',
    phone: '+20 100 000 0006',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04A2233445'
  },
  {
    id: 'usr-chief',
    name: 'Chef des Opérations',
    email: 'chief@mianu.org',
    password: 'Chief@2026',
    role: 'chief_organizer',
    team_id: null,
    team_role: null,
    phone: '+20 100 000 0005',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04A3344556'
  },

  // 3. Committee Chairs & Leaders
  {
    id: 'usr-csh-lead',
    name: 'Alexandre Dubois',
    email: 'alexandre.d@mianu.org',
    password: 'LeadCSH@2026',
    role: 'team_leader',
    team_id: 'team-csh',
    team_role: 'leader',
    phone: '+20 100 000 0003',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04B1122334'
  },
  {
    id: 'usr-cij-lead',
    name: 'Youssef Mansour',
    email: 'youssef.m@mianu.org',
    password: 'LeadCIJ@2026',
    role: 'team_leader',
    team_id: 'team-cij',
    team_role: 'leader',
    phone: '+20 100 000 0007',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04B2233445'
  },
  {
    id: 'usr-ecosoc-lead',
    name: 'Salma El-Sayed',
    email: 'salma.e@mianu.org',
    password: 'LeadECOSOC@2026',
    role: 'team_leader',
    team_id: 'team-ecosoc',
    team_role: 'leader',
    phone: '+20 100 000 0008',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04B3344556'
  },
  {
    id: 'usr-hrc-lead',
    name: 'Marc Antoine',
    email: 'marc.a@mianu.org',
    password: 'LeadHRC@2026',
    role: 'team_leader',
    team_id: 'team-hrc',
    team_role: 'leader',
    phone: '+20 100 000 0009',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04B4455667'
  },

  // 4. Conference Delegates (2 meals/day x 3 days = 6 meals)
  {
    id: 'usr-jean',
    name: 'Jean Dupont',
    email: 'jean.dupont@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-csh',
    team_role: null,
    phone: '+20 100 111 2233',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04A2B3C4D5'
  },
  {
    id: 'usr-marie',
    name: 'Marie Curie',
    email: 'marie.curie@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-ecosoc',
    team_role: null,
    phone: '+20 100 222 3344',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04B3C4D5E6'
  },
  {
    id: 'usr-paul',
    name: 'Paul Valéry',
    email: 'paul.valery@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-hrc',
    team_role: null,
    phone: '+20 100 333 4455',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04C4D5E6F7'
  },
  {
    id: '68c3e3a9-e839-4f9f-be01-117b8faa5dc7',
    name: 'Mohamed Hossam',
    email: 'mohamed.h@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-cij',
    team_role: null,
    phone: '+20 100 444 5566',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04034FB5410289' // Matches previously scanned NFC card
  },
  {
    id: 'usr-leila',
    name: 'Leïla Benali',
    email: 'leila.benali@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-csh',
    team_role: null,
    phone: '+20 100 555 6677',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04D5E6F7A8'
  },
  {
    id: 'usr-omar',
    name: 'Omar Khalil',
    email: 'omar.khalil@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-cij',
    team_role: null,
    phone: '+20 100 666 7788',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04E6F7A8B9'
  },
  {
    id: 'usr-nour',
    name: 'Nour Mansour',
    email: 'nour.mansour@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-ecosoc',
    team_role: null,
    phone: '+20 100 777 8899',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '04F7A8B9C0'
  },
  {
    id: 'usr-tarek',
    name: 'Tarek Hassan',
    email: 'tarek.hassan@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-hrc',
    team_role: null,
    phone: '+20 100 888 0011',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '0401A2B3C4'
  },
  {
    id: 'usr-chloe',
    name: 'Chloé Martin',
    email: 'chloe.martin@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-csh',
    team_role: null,
    phone: '+20 100 999 1122',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '0412B3C4D5'
  },
  {
    id: 'usr-kareem',
    name: 'Kareem Fahmy',
    email: 'kareem.fahmy@delegate.org',
    password: 'Delegate@2026',
    role: 'user',
    team_id: 'team-ecosoc',
    team_role: null,
    phone: '+20 100 112 2334',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '0423C4D5E6'
  },
  {
    id: 'usr-press',
    name: 'Camille Laurent',
    email: 'camille.l@mianu.org',
    password: 'Press@2026',
    role: 'team_member',
    team_id: 'team-logistics',
    team_role: 'member',
    phone: '+20 100 000 0004',
    meal_allowance: 6,
    meals_balance: 6,
    card_uid: '0434D5E6F7'
  }
];

let sqlStatements = [];

// Clean up old temporary test users first
sqlStatements.push("DELETE FROM users WHERE id LIKE 'test_%' OR id LIKE 'usr-adm-%' OR id = 'e4c3fe26-692f-4227-b5b1-a9a8a28439ef';");
sqlStatements.push("DELETE FROM cards WHERE user_id LIKE 'test_%' OR user_id LIKE 'usr-adm-%' OR user_id = 'e4c3fe26-692f-4227-b5b1-a9a8a28439ef';");

for (const acc of ACCOUNTS) {
  const { salt, hash } = hashPassword(acc.password);
  
  // Upsert user
  sqlStatements.push(`
INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('${acc.id}', '${acc.name.replace(/'/g, "''")}', '${acc.email}', '${acc.phone}', '${acc.role}', ${acc.team_id ? `'${acc.team_id}'` : 'NULL'}, ${acc.team_role ? `'${acc.team_role}'` : 'NULL'}, '${hash}', '${salt}', ${acc.meal_allowance}, ${acc.meals_balance}, 1)
ON CONFLICT(id) DO UPDATE SET
  name = excluded.name,
  email = excluded.email,
  phone = excluded.phone,
  role = excluded.role,
  team_id = excluded.team_id,
  team_role = excluded.team_role,
  password_hash = excluded.password_hash,
  salt = excluded.salt,
  meal_allowance = excluded.meal_allowance,
  meals_balance = excluded.meals_balance,
  is_active = 1;
`);

  if (acc.card_uid) {
    sqlStatements.push(`
INSERT INTO cards (uid, user_id, is_active)
VALUES ('${acc.card_uid}', '${acc.id}', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;
`);
  }
}

const fullSql = sqlStatements.join('\n');
fs.writeFileSync('seed_accounts.sql', fullSql, 'utf8');
console.log(`Generated SQL for ${ACCOUNTS.length} accounts with PBKDF2 hashing in seed_accounts.sql`);
