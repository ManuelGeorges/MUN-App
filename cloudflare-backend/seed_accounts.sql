DELETE FROM users WHERE id LIKE 'test_%' OR id LIKE 'usr-adm-%' OR id = 'e4c3fe26-692f-4227-b5b1-a9a8a28439ef';
DELETE FROM cards WHERE user_id LIKE 'test_%' OR user_id LIKE 'usr-adm-%' OR user_id = 'e4c3fe26-692f-4227-b5b1-a9a8a28439ef';

INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-admin', 'Secrétariat Général', 'admin@mianu.org', '+20 100 888 9999', 'admin', NULL, NULL, '430d94536eb45c42e00ea2afbe4fc5cbe8a4a6b9e999ba9ad27763f4b43ae652', 'ab95a893fa4f51ec6da93c5e56b4d4b1', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04F0E1D2C3', 'usr-admin', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-pres', 'Présidence MIANU', 'president@mianu.org', '+20 100 888 1111', 'admin', NULL, NULL, '9e8e5423cbe3acbb9d18157e1f61d2a45f1a4795835906cffe755401a6e66fe9', '42b6895e500bd1482bfb5ca838e8bdf3', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04E1D2C3B4', 'usr-pres', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-org', 'Équipe Logistique', 'ops@mianu.org', '+20 100 000 0002', 'organizer', 'team-logistics', 'member', 'b9148cab1280ac4b8c5c41af07a4b0e011e8c6c3ce78af08f9202211209bf4dc', 'b507cc1a62a9726fd270d61510d49d8e', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04A1122334', 'usr-org', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-org-hospitality', 'Équipe Accueil & Protocole', 'hospitality@mianu.org', '+20 100 000 0006', 'organizer', 'team-hospitality', 'member', '1dca5858dee4dae40f4e1c5f00156060853c6537881826dc8a71abc068466548', '57f924f1f79c416db7a9e9deebaddf08', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04A2233445', 'usr-org-hospitality', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-chief', 'Chef des Opérations', 'chief@mianu.org', '+20 100 000 0005', 'chief_organizer', NULL, NULL, 'ea0669b6095f12366572448b7d6399044c95e0e50e529df32e0f5f545a7a6a18', '4d20cfc63bdfa950b1d6b2758f636e53', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04A3344556', 'usr-chief', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-csh-lead', 'Alexandre Dubois', 'alexandre.d@mianu.org', '+20 100 000 0003', 'team_leader', 'team-csh', 'leader', '4e66f76e4f06effb9e9e39c733e6cc244ea1ada9581cbd95303fbd8968a94a49', '0710132a12c598b17b7229aac2b47cd7', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04B1122334', 'usr-csh-lead', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-cij-lead', 'Youssef Mansour', 'youssef.m@mianu.org', '+20 100 000 0007', 'team_leader', 'team-cij', 'leader', '2a21938971a573422b39d3d26b3ffeac7558d6eaad2bc9dce02087ad1b160948', '9ee5787f404a81f8083ac481fe75fad2', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04B2233445', 'usr-cij-lead', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-ecosoc-lead', 'Salma El-Sayed', 'salma.e@mianu.org', '+20 100 000 0008', 'team_leader', 'team-ecosoc', 'leader', '66a3b289b6b987908141100881f2969e3182a9821c1d635ac5460dde2e6febab', 'f3e65e93ac6d8f423c74a80c4b67a330', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04B3344556', 'usr-ecosoc-lead', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-hrc-lead', 'Marc Antoine', 'marc.a@mianu.org', '+20 100 000 0009', 'team_leader', 'team-hrc', 'leader', 'f00caedfda80690c744e43c9b5fb1f2d25f7c0662d4bf95dc0088e14f7cf222c', 'c1a3e33fc5fb9b0e1c1c2197a468645d', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04B4455667', 'usr-hrc-lead', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-jean', 'Jean Dupont', 'jean.dupont@delegate.org', '+20 100 111 2233', 'user', 'team-csh', NULL, '637989049e048362e4ecbf26445e9d6b62e2d32ec74ff79219d09516ba523915', '5c557f7e3051f96e9b9584ed1951cef1', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04A2B3C4D5', 'usr-jean', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-marie', 'Marie Curie', 'marie.curie@delegate.org', '+20 100 222 3344', 'user', 'team-ecosoc', NULL, 'da6ae67d621d024e3db524e1c93f31174e75e7100b11b4209079c1f9161debb6', 'e8a74027c641485e18a5a7be89a43105', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04B3C4D5E6', 'usr-marie', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-paul', 'Paul Valéry', 'paul.valery@delegate.org', '+20 100 333 4455', 'user', 'team-hrc', NULL, 'bc92cba3c686c25498e7eefd11efde1fc1bfae764e2ba7b031658cd203d26fa8', 'e896a0c85fd4f6b2dfa218ef010a2b7f', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04C4D5E6F7', 'usr-paul', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('68c3e3a9-e839-4f9f-be01-117b8faa5dc7', 'Mohamed Hossam', 'mohamed.h@delegate.org', '+20 100 444 5566', 'user', 'team-cij', NULL, '4fb6e22f815aef273620bfb3694541a8fae631221d6b9c4489087b4daa7ef325', 'ab9bb9299be4e7a503f27687cc97c456', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04034FB5410289', '68c3e3a9-e839-4f9f-be01-117b8faa5dc7', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-leila', 'Leïla Benali', 'leila.benali@delegate.org', '+20 100 555 6677', 'user', 'team-csh', NULL, '12b740cde45c550d1e3a6d998b1c7bc066948abbeb863e1f0501142f9b32030c', '3923d390008d86719722dd3cdf2ec793', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04D5E6F7A8', 'usr-leila', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-omar', 'Omar Khalil', 'omar.khalil@delegate.org', '+20 100 666 7788', 'user', 'team-cij', NULL, 'd2ac62fc6542e4fe1e4f9220b18177de7b490d8d4df62c7d75fb5df166807605', '91c34e8f8c1736b8be0531cb230a90ce', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04E6F7A8B9', 'usr-omar', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-nour', 'Nour Mansour', 'nour.mansour@delegate.org', '+20 100 777 8899', 'user', 'team-ecosoc', NULL, 'd3280e1fff8652547c6b9a949f295b218c0063d7d415904bed6c788ac8996b9b', 'e7f6a534854aa761db06c1e95da71873', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('04F7A8B9C0', 'usr-nour', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-tarek', 'Tarek Hassan', 'tarek.hassan@delegate.org', '+20 100 888 0011', 'user', 'team-hrc', NULL, '7100c1cb83e235662ac6a56afafeded97d402dc70264f1bff853931cf4563eef', 'ccb65bac1669f08259bc04d71f99963f', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('0401A2B3C4', 'usr-tarek', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-chloe', 'Chloé Martin', 'chloe.martin@delegate.org', '+20 100 999 1122', 'user', 'team-csh', NULL, 'ed2707c8c4abf4dda7380ab64e040e257c516eeef5b90e1bbb9778bd0ea52b2e', 'be03714b4c6352564804d32c4d9cdae4', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('0412B3C4D5', 'usr-chloe', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-kareem', 'Kareem Fahmy', 'kareem.fahmy@delegate.org', '+20 100 112 2334', 'user', 'team-ecosoc', NULL, 'ecc901fae1e9ba1e41ef7cbbeebb0817b113b9d759dcd0f511b2e88bed31e6f9', '458d0c48e6853a19afbfce0bef72809c', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('0423C4D5E6', 'usr-kareem', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;


INSERT INTO users (id, name, email, phone, role, team_id, team_role, password_hash, salt, meal_allowance, meals_balance, is_active)
VALUES ('usr-press', 'Camille Laurent', 'camille.l@mianu.org', '+20 100 000 0004', 'team_member', 'team-logistics', 'member', 'a3e6f6f4b487306f53c6f11fd3892b300f63a81fc1950812bc81bc583fa31100', 'd582a6d6da77df7445f85bf550c5176e', 6, 6, 1)
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


INSERT INTO cards (uid, user_id, is_active)
VALUES ('0434D5E6F7', 'usr-press', 1)
ON CONFLICT(uid) DO UPDATE SET
  user_id = excluded.user_id,
  is_active = 1;
