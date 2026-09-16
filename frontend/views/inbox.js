import { api, esc, emptyState, formatWhen } from '../app.js';

/** What this account has received: event-wide, its team's, and anything addressed to it. */
export async function mount(root, { session }) {
  const messages = await api(`/notifications/inbox/${session.user_id}`);

  root.innerHTML = `
    <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6 max-w-3xl">
      <h2 class="font-display font-bold text-lg">Messages</h2>
      <p class="text-sm text-fg-muted mt-0.5 mb-5">Announcements addressed to you, your team, or everyone.</p>
      ${
        messages.length
          ? `<div class="space-y-3">${messages.map(row).join('')}</div>`
          : emptyState('No messages', 'Announcements sent to you will appear here.')
      }
    </section>`;
}

function row(notification) {
  const tone = {
    all: ['Everyone', 'border-l-danger/50'],
    team: [notification.team_name || 'Your team', 'border-l-accent/50'],
    participant: ['Just you', 'border-l-warning/60'],
  }[notification.audience] || ['Message', 'border-l-accent/50'];

  return `
    <article class="rounded-xl border border-subtle border-l-2 ${tone[1]} bg-surface p-4">
      <div class="flex items-center justify-between gap-3 mb-1.5 text-xs">
        <span class="font-semibold uppercase tracking-wider text-fg-muted">${esc(tone[0])}</span>
        <span class="text-fg-muted">${formatWhen(notification.timestamp)}</span>
      </div>
      <p class="text-sm leading-relaxed break-words">${esc(notification.message)}</p>
      <p class="text-xs text-fg-muted mt-2">from ${esc(notification.sender_name || 'Unknown')}</p>
    </article>`;
}
