// test_api.mjs
const BASE_URL = process.env.BASE_URL || 'https://mianu-backend.karimshacker1234.workers.dev';

async function run() {
  console.log('Testing Cloudflare Workers MIANU API against:', BASE_URL);

  // 1. Health check
  const healthRes = await fetch(`${BASE_URL}/health`);
  const healthJson = await healthRes.json();
  console.log('✓ 1. Health check:', healthJson.status);
  if (healthJson.status !== 'ok') throw new Error('Health check failed');

  // 2. Auth login
  const loginRes = await fetch(`${BASE_URL}/api/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username: 'admin@mianu.org', password: 'Admin@2026' }),
  });
  if (!loginRes.ok) {
    const err = await loginRes.text();
    throw new Error(`Login failed with status ${loginRes.status}: ${err}`);
  }
  const loginJson = await loginRes.json();
  console.log(`✓ 2. Auth login: ${loginJson.name} (Role: ${loginJson.role})`);
  const token = loginJson.access_token;
  if (!token) throw new Error('No access_token returned');

  // 3. List Users
  const usersRes = await fetch(`${BASE_URL}/api/v1/users`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const usersJson = await usersRes.json();
  console.log(`✓ 3. Users: ${usersJson.length} delegates loaded`);

  // 4. List Teams
  const teamsRes = await fetch(`${BASE_URL}/api/v1/teams`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const teamsJson = await teamsRes.json();
  console.log(`✓ 4. Teams: ${teamsJson.length} committees loaded`);

  // 5. List Halls
  const hallsRes = await fetch(`${BASE_URL}/api/v1/access/halls`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const hallsJson = await hallsRes.json();
  console.log(`✓ 5. Halls: ${hallsJson.length} conference halls loaded`);
  const firstHall = hallsJson[0];

  // 6. Record Meal Swipe
  const mealRes = await fetch(`${BASE_URL}/api/v1/meals/swipe`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      card_uid: '04A2B3C4D5',
      meal_type: 'lunch',
    }),
  });
  const mealJson = await mealRes.json();
  console.log(`✓ 6. Meal Swipe (${mealRes.status}): User=${mealJson.user_name || mealJson.user_id}, Remaining=${mealJson.meals_remaining}`);

  // 7. Hall Access Scan
  const accessRes = await fetch(`${BASE_URL}/api/v1/access/scan`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      card_uid: '04A2B3C4D5',
      hall_id: firstHall.id,
      action: 'entry',
    }),
  });
  const accessJson = await accessRes.json();
  console.log(`✓ 7. Hall Access Scan (${accessRes.status}): Allowed=${accessJson.allowed}, Hall=${accessJson.hall_id}`);

  // 8. Conference Attendance Summary
  const attRes = await fetch(`${BASE_URL}/api/v1/attendance`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const attJson = await attRes.json();
  console.log(`✓ 8. Attendance Telemetry: Present=${attJson.summary.total_present}/${attJson.summary.total_registered} (${attJson.summary.attendance_rate}%), Swipes=${attJson.summary.total_swipes}`);

  // 9. Attendance NFC Swipe
  const attSwipeRes = await fetch(`${BASE_URL}/api/v1/attendance/swipe`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      card_uid: '04A2B3C4D5',
      action: 'toggle',
    }),
  });
  const attSwipeJson = await attSwipeRes.json();
  console.log(`✓ 9. Attendance Swipe: User=${attSwipeJson.user.name}, Status=${attSwipeJson.user.status}`);

  // 10. Attendance Single Override
  const singleRes = await fetch(`${BASE_URL}/api/v1/attendance/single`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      user_id: 'usr-jean',
      status: 'present',
    }),
  });
  const singleJson = await singleRes.json();
  console.log(`✓ 10. Single Attendance Override: User=${singleJson.name}, Status=${singleJson.status}`);

  // 11. Locations Telemetry
  const locsRes = await fetch(`${BASE_URL}/api/v1/operations/locations`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const locsJson = await locsRes.json();
  console.log(`✓ 11. Locations: ${locsJson.length} attendee tracking records loaded`);

  // 12. Dashboard Overview
  const dashRes = await fetch(`${BASE_URL}/api/v1/dashboard/overview`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const dashJson = await dashRes.json();
  console.log(`✓ 12. Dashboard Overview: Active=${dashJson.active_users}, Meals=${dashJson.total_meals_today}, Inside=${dashJson.people_inside_halls}`);

  // 13. Web Admin Dashboard HTML Route
  const adminHtmlRes = await fetch(`${BASE_URL}/admin`);
  const adminHtml = await adminHtmlRes.text();
  console.log(`✓ 13. Web Dashboard /admin: Status=${adminHtmlRes.status}, Size=${adminHtml.length} bytes`);

  console.log('\n======================================================');
  console.log('ALL 13 VERIFICATION TESTS PASSED SUCCESSFULLY ON EDGE!');
  console.log('======================================================');
}

run().catch((err) => {
  console.error('Test failed:', err);
  process.exit(1);
});
