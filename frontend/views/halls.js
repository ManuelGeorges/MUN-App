import { api, esc, emptyState, toast } from '../app.js';

/** Halls and live occupancy. */
export async function mount(root) {
  async function refresh() {
    const halls = await api('/access/halls');
    const presence = await Promise.all(
      halls.map((hall) =>
        api(`/access/halls/${hall.id}/presence`)
          .then((ids) => ids.length)
          .catch(() => 0),
      ),
    );

    root.innerHTML = `
      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-5 mb-6">
        <form id="new-hall" class="flex flex-wrap items-end gap-3">
          <div class="flex-1 min-w-[180px]">
            <label for="h-name" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">New hall</label>
            <input id="h-name" required placeholder="General Assembly" class="field" />
          </div>
          <div class="w-32">
            <label for="h-capacity" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Capacity</label>
            <input id="h-capacity" type="number" min="1" value="100" class="field" />
          </div>
          <button class="rounded-xl bg-accent text-white font-semibold px-5 py-2.5 hover:brightness-110 transition">Create</button>
        </form>
      </section>

      ${
        halls.length
          ? `<div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
              ${halls.map((hall, i) => hallCard(hall, presence[i])).join('')}
            </div>`
          : emptyState('No halls', 'Create a hall so door scanning has somewhere to admit people.')
      }`;

    root.querySelector('#new-hall').addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        await api('/access/halls', {
          method: 'POST',
          body: {
            name: root.querySelector('#h-name').value.trim(),
            capacity: Number(root.querySelector('#h-capacity').value) || 1,
          },
        });
        toast('Hall created.');
        refresh();
      } catch (error) {
        toast(error.message, 'error');
      }
    });
  }

  await refresh();
}

function hallCard(hall, occupancy) {
  const capacity = hall.capacity || 0;
  const ratio = capacity ? Math.min(100, Math.round((occupancy / capacity) * 100)) : 0;
  const tone = ratio >= 100 ? 'danger' : ratio >= 80 ? 'warning' : 'success';
  return `
    <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-5">
      <div class="flex items-start justify-between mb-3">
        <h2 class="font-display font-bold">${esc(hall.name)}</h2>
        <span class="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full bg-${tone}/12 text-${tone}">
          ${ratio}%
        </span>
      </div>
      <p class="text-2xl font-display font-bold tabular-nums">${occupancy} <span class="text-fg-muted text-base font-sans font-normal">/ ${capacity}</span></p>
      <div class="h-1.5 rounded-full bg-surface-sunken overflow-hidden mt-3">
        <div class="h-full rounded-full bg-${tone}" style="width:${ratio}%"></div>
      </div>
    </section>`;
}
