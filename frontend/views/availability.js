import { api, esc, emptyState } from '../app.js';

/**
 * Where there is room to work right now.
 *
 * Built for team members — press especially — who need to find a hall with space, not to know who
 * is in it. The endpoint returns no occupant identities, so this view can't leak them.
 */
const STATES = {
  empty: { label: 'Empty', tone: 'success', blurb: 'Nobody inside' },
  available: { label: 'Available', tone: 'success', blurb: 'Plenty of room' },
  filling: { label: 'Filling up', tone: 'warning', blurb: 'Getting busy' },
  full: { label: 'Full', tone: 'danger', blurb: 'At capacity' },
};

export async function mount(root) {
  async function refresh() {
    const halls = await api('/access/halls/availability');
    const free = halls.filter((h) => h.state === 'empty' || h.state === 'available').length;

    root.innerHTML = `
      <div class="flex flex-wrap items-center justify-between gap-3 mb-5">
        <div>
          <h2 class="font-display font-bold text-lg">Hall availability</h2>
          <p class="text-sm text-fg-muted">
            ${halls.length ? `${free} of ${halls.length} with room right now` : 'No halls configured'}
          </p>
        </div>
        <button id="refresh" class="rounded-xl border border-subtle bg-surface-raised px-4 py-2 text-sm font-medium hover:bg-surface-hover transition">
          Refresh
        </button>
      </div>

      ${
        halls.length
          ? `<div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-4">${halls.map(card).join('')}</div>`
          : emptyState('No halls yet', 'An organizer needs to add halls before availability can be shown.')
      }`;

    root.querySelector('#refresh')?.addEventListener('click', refresh);
  }

  await refresh();
}

function card(hall) {
  const state = STATES[hall.state] || STATES.available;
  const ratio = hall.capacity ? Math.min(100, Math.round((hall.occupancy / hall.capacity) * 100)) : 0;

  return `
    <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-5">
      <div class="flex items-start justify-between gap-3 mb-3">
        <h3 class="font-display font-bold leading-tight">${esc(hall.name)}</h3>
        <span class="shrink-0 text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-${state.tone}/12 text-${state.tone}">
          ${state.label}
        </span>
      </div>

      <p class="text-3xl font-display font-bold tabular-nums text-${state.tone}">
        ${hall.seats_free}
        <span class="text-sm font-sans font-normal text-fg-muted">seat${hall.seats_free === 1 ? '' : 's'} free</span>
      </p>
      <p class="text-xs text-fg-muted mt-0.5">${hall.occupancy} of ${hall.capacity} · ${state.blurb}</p>

      <div class="h-1.5 rounded-full bg-surface-sunken overflow-hidden mt-3">
        <div class="h-full rounded-full bg-${state.tone}" style="width:${ratio}%"></div>
      </div>
    </section>`;
}
