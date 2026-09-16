import { api, can, esc, emptyState, formatWhen, P, skeleton } from '../app.js';

export async function mount(root, { session }) {
  const wide = can(session, P.MANAGE_USERS);

  const [overview, teams, recent] = await Promise.all([
    api('/dashboard/overview'),
    api('/teams').catch(() => []),
    api('/notifications', { params: { limit: 6 } }).catch(() => []),
  ]);

  const stats = [
    { label: 'Meals today', value: overview.total_meals_today, tone: 'accent' },
    { label: 'Transactions today', value: overview.total_transactions_today, tone: 'info' },
    { label: 'Active users', value: overview.active_users, tone: 'success' },
    ...(can(session, P.CHARGE_TOKENS)
      ? [{ label: 'Tokens in circulation', value: overview.total_token_balance.toFixed(2), tone: 'warning' }]
      : []),
  ];

  root.innerHTML = `
    <section class="grid grid-cols-2 xl:grid-cols-4 gap-4 mb-6">
      ${stats.map(statCard).join('')}
    </section>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
        <h2 class="font-display font-bold mb-4">Teams</h2>
        ${
          teams.length
            ? `<div class="space-y-3">${teams.map(teamRow).join('')}</div>`
            : emptyState('No teams yet', wide ? 'Create one from the Teams section.' : 'An organizer has not set up teams.')
        }
      </section>

      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
        <h2 class="font-display font-bold mb-4">Latest announcements</h2>
        ${
          recent.length
            ? `<div class="space-y-3">${recent.map(announcementRow).join('')}</div>`
            : emptyState('Nothing announced', 'Broadcasts will appear here as they go out.')
        }
      </section>
    </div>`;
}

function statCard({ label, value, tone }) {
  return `
    <div class="glass rounded-2xl p-5 shadow-card">
      <p class="text-xs font-semibold uppercase tracking-wider text-fg-muted">${esc(label)}</p>
      <p class="font-display font-bold text-3xl mt-2 tabular-nums text-${tone}">${esc(value)}</p>
    </div>`;
}

function teamRow(team) {
  const ratio = team.capacity ? Math.min(100, Math.round((team.current_size / team.capacity) * 100)) : 0;
  const full = team.capacity && team.current_size >= team.capacity;
  return `
    <div class="rounded-xl border border-subtle bg-surface p-4">
      <div class="flex items-center justify-between mb-2">
        <p class="font-medium">${esc(team.name)}</p>
        <p class="text-sm tabular-nums ${full ? 'text-danger font-semibold' : 'text-fg-muted'}">
          ${team.current_size} / ${team.capacity}
        </p>
      </div>
      <div class="h-1.5 rounded-full bg-surface-sunken overflow-hidden">
        <div class="h-full rounded-full ${full ? 'bg-danger' : 'bg-accent'}" style="width:${ratio}%"></div>
      </div>
    </div>`;
}

function announcementRow(notification) {
  const scope =
    notification.audience === 'all'
      ? 'Everyone'
      : notification.audience === 'team'
        ? notification.team_name || 'Team'
        : notification.recipient_name || 'Direct';
  return `
    <div class="rounded-xl border border-subtle bg-surface p-4">
      <div class="flex items-center justify-between gap-3 mb-1 text-xs text-fg-muted">
        <span class="font-medium text-fg">${esc(notification.sender_name || 'Unknown')} → ${esc(scope)}</span>
        <span>${formatWhen(notification.timestamp)}</span>
      </div>
      <p class="text-sm leading-snug break-words">${esc(notification.message)}</p>
    </div>`;
}
