import { api, esc, emptyState, initials, toast, ROLE_LABELS } from '../app.js';

/** Delegate roster: register people, link badges, and assign them to teams. */
export async function mount(root) {
  let query = '';

  async function refresh() {
    const [users, teams] = await Promise.all([api('/users'), api('/teams').catch(() => [])]);
    const teamsById = Object.fromEntries(teams.map((t) => [t.id, t.name]));
    const filtered = users.filter((u) =>
      !query || `${u.name} ${u.email || ''} ${u.phone || ''}`.toLowerCase().includes(query.toLowerCase()),
    );

    root.innerHTML = `
      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-5 mb-6">
        <form id="new-user" class="flex flex-wrap items-end gap-3">
          <div class="flex-1 min-w-[160px]">
            <label for="u-name" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Register delegate</label>
            <input id="u-name" required placeholder="Full name" class="field" />
          </div>
          <div class="flex-1 min-w-[160px]">
            <label for="u-email" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Email</label>
            <input id="u-email" type="email" placeholder="optional" class="field" />
          </div>
          <div class="w-40">
            <label for="u-role" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Role</label>
            <select id="u-role" class="field">
              ${['user', 'team_member', 'team_leader', 'organizer', 'chief_organizer', 'admin']
                .map((r) => `<option value="${r}">${ROLE_LABELS[r]}</option>`)
                .join('')}
            </select>
          </div>
          <div class="w-32">
            <label for="u-tokens" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Tokens</label>
            <input id="u-tokens" type="number" min="0" step="0.01" value="0" class="field" />
          </div>
          <button class="rounded-xl bg-accent text-white font-semibold px-5 py-2.5 hover:brightness-110 transition">Add</button>
        </form>
      </section>

      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
        <div class="flex flex-wrap items-center justify-between gap-3 mb-4">
          <h2 class="font-display font-bold">Delegates <span class="text-fg-muted font-sans font-normal text-sm">(${filtered.length})</span></h2>
          <input id="search" value="${esc(query)}" placeholder="Search name, email or phone"
                 class="field max-w-xs" />
        </div>
        ${
          filtered.length
            ? `<div class="space-y-2">${filtered.map((u) => userRow(u, teamsById, teams)).join('')}</div>`
            : emptyState('Nobody here', query ? 'No delegate matches that search.' : 'Register the first delegate above.')
        }
      </section>`;

    const search = root.querySelector('#search');
    search.addEventListener('input', () => {
      query = search.value;
      const at = search.selectionStart;
      refresh().then(() => {
        const next = root.querySelector('#search');
        next.focus();
        next.setSelectionRange(at, at);
      });
    });

    root.querySelector('#new-user').addEventListener('submit', async (event) => {
      event.preventDefault();
      try {
        await api('/users', {
          method: 'POST',
          body: {
            name: root.querySelector('#u-name').value.trim(),
            email: root.querySelector('#u-email').value.trim() || null,
            role: root.querySelector('#u-role').value,
            token_balance: Number(root.querySelector('#u-tokens').value) || 0,
          },
        });
        toast('Delegate registered.');
        refresh();
      } catch (error) {
        toast(error.message, 'error');
      }
    });

    root.querySelectorAll('[data-assign]').forEach((select) =>
      select.addEventListener('change', async () => {
        const userId = select.dataset.assign;
        const teamId = select.value;
        try {
          if (teamId) {
            await api(`/teams/${teamId}/members/${userId}`, { method: 'POST', params: { role: 'member' } });
            toast('Assigned to team.');
          } else if (select.dataset.current) {
            await api(`/teams/${select.dataset.current}/members/${userId}`, { method: 'DELETE' });
            toast('Removed from team.');
          }
          refresh();
        } catch (error) {
          toast(error.message, 'error');
          refresh();
        }
      }),
    );

    root.querySelectorAll('[data-link-card]').forEach((button) =>
      button.addEventListener('click', async () => {
        const uid = prompt('Card UID to link (tap the badge on the Android app to read it):');
        if (!uid) return;
        try {
          await api('/users/cards/link', {
            method: 'POST',
            body: { user_id: button.dataset.linkCard, card_uid: uid.trim().toUpperCase() },
          });
          toast('Card linked.');
        } catch (error) {
          toast(error.message, 'error');
        }
      }),
    );

    root.querySelectorAll('[data-delete]').forEach((button) =>
      button.addEventListener('click', async () => {
        if (!confirm(`Remove ${button.dataset.name}? This cannot be undone.`)) return;
        try {
          await api(`/users/${button.dataset.delete}`, { method: 'DELETE' });
          toast('Delegate removed.');
          refresh();
        } catch (error) {
          toast(error.message, 'error');
        }
      }),
    );
  }

  await refresh();
}

function userRow(user, teamsById, teams) {
  return `
    <div class="flex flex-wrap items-center gap-3 rounded-xl border border-subtle bg-surface p-3">
      <div class="w-9 h-9 rounded-full bg-accent/15 text-accent grid place-items-center text-xs font-bold shrink-0">
        ${esc(initials(user.name))}
      </div>
      <div class="min-w-0 flex-1">
        <p class="text-sm font-medium truncate">${esc(user.name)}</p>
        <p class="text-xs text-fg-muted truncate">
          ${esc(ROLE_LABELS[user.role] || user.role)}${user.email ? ` · ${esc(user.email)}` : ''}
          ${user.team_role === 'leader' ? ' · leads ' + esc(teamsById[user.team_id] || 'a team') : ''}
        </p>
      </div>
      <div class="text-right shrink-0">
        <p class="text-sm font-semibold tabular-nums">${user.token_balance.toFixed(2)}</p>
        <p class="text-[11px] text-fg-muted">tokens</p>
      </div>
      <select data-assign="${user.id}" data-current="${user.team_id || ''}" class="field w-36 text-xs py-1.5">
        <option value="">No team</option>
        ${teams
          .map((t) => `<option value="${t.id}" ${t.id === user.team_id ? 'selected' : ''}>${esc(t.name)}</option>`)
          .join('')}
      </select>
      <button data-link-card="${user.id}" class="text-xs rounded-lg border border-subtle px-2.5 py-1.5 hover:bg-surface-hover transition">Link card</button>
      <button data-delete="${user.id}" data-name="${esc(user.name)}" class="text-xs rounded-lg border border-subtle px-2.5 py-1.5 text-danger hover:bg-danger/10 transition">Remove</button>
    </div>`;
}
