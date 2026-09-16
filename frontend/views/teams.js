import { api, can, esc, emptyState, initials, P, toast } from '../app.js';

/**
 * Team roster management.
 *
 * Leaders get a read-only view of their own team; organizers and admins get the full roster with
 * create, assign and promote.
 */
export async function mount(root, { session }) {
  const manage = can(session, P.MANAGE_TEAMS);

  async function refresh() {
    const teams = await api('/teams');
    const scoped = manage ? teams : teams.filter((t) => t.id === session.team_id);

    const rosters = await Promise.all(
      scoped.map((team) =>
        api(`/teams/${team.id}/members`)
          .then((members) => ({ team, members }))
          .catch(() => ({ team, members: [] })),
      ),
    );

    root.innerHTML = `
      ${
        manage
          ? `<section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-5 mb-6">
              <form id="new-team" class="flex flex-wrap items-end gap-3">
                <div class="flex-1 min-w-[180px]">
                  <label for="team-name" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">New team</label>
                  <input id="team-name" required placeholder="Hospitality" class="field" />
                </div>
                <div class="w-28">
                  <label for="team-capacity" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Capacity</label>
                  <input id="team-capacity" type="number" min="1" value="10" class="field" />
                </div>
                <button class="rounded-xl bg-accent text-white font-semibold px-5 py-2.5 hover:brightness-110 transition">Create</button>
              </form>
            </section>`
          : ''
      }

      ${
        rosters.length
          ? `<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">${rosters.map((r) => teamCard(r, manage)).join('')}</div>`
          : emptyState(
              manage ? 'No teams yet' : 'You are not on a team',
              manage ? 'Create the first one above.' : 'An organizer needs to add you to a team.',
            )
      }`;

    if (manage) {
      root.querySelector('#new-team')?.addEventListener('submit', async (event) => {
        event.preventDefault();
        try {
          await api('/teams', {
            method: 'POST',
            body: {
              name: root.querySelector('#team-name').value.trim(),
              capacity: Number(root.querySelector('#team-capacity').value) || 1,
            },
          });
          toast('Team created.');
          refresh();
        } catch (error) {
          toast(error.message, 'error');
        }
      });

      root.querySelectorAll('[data-promote]').forEach((button) =>
        button.addEventListener('click', async () => {
          const { promote: userId, team, role } = button.dataset;
          try {
            await api(`/teams/${team}/members/${userId}`, { method: 'POST', params: { role } });
            toast(role === 'leader' ? 'Promoted to leader.' : 'Set as member.');
            refresh();
          } catch (error) {
            toast(error.message, 'error');
          }
        }),
      );

      root.querySelectorAll('[data-remove]').forEach((button) =>
        button.addEventListener('click', async () => {
          try {
            await api(`/teams/${button.dataset.team}/members/${button.dataset.remove}`, { method: 'DELETE' });
            toast('Removed from team.');
            refresh();
          } catch (error) {
            toast(error.message, 'error');
          }
        }),
      );
    }
  }

  await refresh();
}

function teamCard({ team, members }, manage) {
  const full = team.capacity && team.current_size >= team.capacity;
  return `
    <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
      <div class="flex items-start justify-between mb-4">
        <div>
          <h2 class="font-display font-bold text-lg">${esc(team.name)}</h2>
          <p class="text-sm ${full ? 'text-danger' : 'text-fg-muted'}">
            ${team.current_size} of ${team.capacity} ${full ? '· full' : ''}
          </p>
        </div>
      </div>
      ${
        members.length
          ? `<div class="space-y-2">${members.map((m) => memberRow(m, team, manage)).join('')}</div>`
          : `<p class="text-sm text-fg-muted py-4 text-center">No members yet.</p>`
      }
    </section>`;
}

function memberRow(member, team, manage) {
  const isLeader = member.team_role === 'leader';
  return `
    <div class="flex items-center gap-3 rounded-xl border border-subtle bg-surface p-3">
      <div class="w-8 h-8 rounded-full bg-accent/15 text-accent grid place-items-center text-xs font-bold shrink-0">
        ${esc(initials(member.name))}
      </div>
      <div class="min-w-0 flex-1">
        <p class="text-sm font-medium truncate">${esc(member.name)}</p>
        <p class="text-xs text-fg-muted">${isLeader ? 'Leader' : 'Member'}</p>
      </div>
      ${
        manage
          ? `<div class="flex items-center gap-1 shrink-0">
              <button data-promote="${member.id}" data-team="${team.id}" data-role="${isLeader ? 'member' : 'leader'}"
                class="text-xs rounded-lg border border-subtle px-2.5 py-1.5 hover:bg-surface-hover transition">
                ${isLeader ? 'Demote' : 'Make leader'}
              </button>
              <button data-remove="${member.id}" data-team="${team.id}"
                class="text-xs rounded-lg border border-subtle px-2.5 py-1.5 text-danger hover:bg-danger/10 transition">
                Remove
              </button>
            </div>`
          : ''
      }
    </div>`;
}
