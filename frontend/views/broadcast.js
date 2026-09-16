import { api, can, esc, emptyState, formatWhen, P, skeleton, toast } from '../app.js';

/**
 * Broadcast console.
 *
 * The audience picker is the whole screen. A team leader sees only the targets they may actually
 * use, so the wider options aren't dangled and then refused — but the server still rules on every
 * send, and its 403 text is surfaced verbatim rather than reworded.
 */
export async function mount(root, { session }) {
  const wide = can(session, P.BROADCAST_WIDE);

  const [teams, users] = await Promise.all([
    api('/teams'),
    api('/users').catch(() => []), // leaders may not list all users; team members are fetched below
  ]);

  const ownTeam = teams.find((t) => t.id === session.team_id) || null;

  if (!wide && !ownTeam) {
    root.innerHTML = emptyState(
      'You lead no team',
      'An organizer needs to assign you as leader of a team before you can send anything.',
    );
    return;
  }

  // Leaders may only address their own team, so the recipient list is scoped to it.
  const scopedUsers = wide
    ? users
    : await api(`/teams/${ownTeam.id}/members`).catch(() => []);

  const audiences = wide
    ? [
        { value: 'all', label: 'All participants', hint: 'Every active delegate at the event' },
        { value: 'team', label: 'A team', hint: 'Everyone on one team' },
        { value: 'participant', label: 'One participant', hint: 'A single person' },
      ]
    : [
        { value: 'team', label: 'My team', hint: esc(ownTeam.name) },
        { value: 'participant', label: 'One team member', hint: 'Someone on your team' },
      ];

  root.innerHTML = `
    <div class="grid grid-cols-1 xl:grid-cols-[minmax(0,1fr)_380px] gap-6">
      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
        <h2 class="font-display font-bold text-lg">Send a message</h2>
        <p class="text-sm text-fg-muted mt-0.5 mb-5">
          ${wide
            ? 'You can reach the whole event, a single team, or one person.'
            : `You can reach <strong class="text-fg">${esc(ownTeam.name)}</strong> or one of its members.`}
        </p>

        <form id="broadcast-form" class="space-y-5">
          <fieldset>
            <legend class="text-xs font-semibold uppercase tracking-wider text-fg-muted mb-2">Audience</legend>
            <div class="grid sm:grid-cols-3 gap-2">
              ${audiences
                .map(
                  (a, i) => `
                <label class="audience-option ${i === 0 ? 'is-selected' : ''}">
                  <input type="radio" name="audience" value="${a.value}" ${i === 0 ? 'checked' : ''} class="sr-only" />
                  <span class="block font-medium text-sm">${a.label}</span>
                  <span class="block text-xs text-fg-muted mt-0.5">${a.hint}</span>
                </label>`,
                )
                .join('')}
            </div>
          </fieldset>

          <div id="target-team" class="hidden">
            <label for="team-select" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Team</label>
            <select id="team-select" class="field">
              ${teams
                .map(
                  (t) =>
                    `<option value="${t.id}" ${t.id === session.team_id ? 'selected' : ''}>${esc(t.name)} — ${t.current_size} member${t.current_size === 1 ? '' : 's'}</option>`,
                )
                .join('')}
            </select>
          </div>

          <div id="target-participant" class="hidden">
            <label for="participant-select" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Participant</label>
            <select id="participant-select" class="field">
              ${scopedUsers.length
                ? scopedUsers.map((u) => `<option value="${u.id}">${esc(u.name)}</option>`).join('')
                : '<option value="">No participants available</option>'}
            </select>
          </div>

          <div>
            <label for="message" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Message</label>
            <textarea id="message" rows="4" maxlength="1000" required
              class="field resize-y" placeholder="Opening ceremony starts in 15 minutes — please head to the main hall."></textarea>
            <div class="flex justify-between mt-1.5">
              <p id="reach" class="text-xs text-fg-muted"></p>
              <p id="counter" class="text-xs text-fg-muted tabular-nums">0 / 1000</p>
            </div>
          </div>

          <button type="submit" id="send"
            class="w-full sm:w-auto rounded-xl bg-accent text-white font-semibold px-6 py-2.5 shadow-glow hover:brightness-110 transition disabled:opacity-50">
            Send broadcast
          </button>
        </form>
      </section>

      <aside class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6 min-w-0">
        <h2 class="font-display font-bold">Recently sent</h2>
        <p class="text-sm text-fg-muted mt-0.5 mb-4">Your last messages</p>
        <div id="history">${skeleton(3)}</div>
      </aside>
    </div>`;

  const form = root.querySelector('#broadcast-form');
  const teamBox = root.querySelector('#target-team');
  const participantBox = root.querySelector('#target-participant');
  const message = root.querySelector('#message');
  const counter = root.querySelector('#counter');
  const reach = root.querySelector('#reach');
  const sendButton = root.querySelector('#send');

  const selectedAudience = () => form.querySelector('input[name="audience"]:checked').value;

  function syncTargets() {
    const audience = selectedAudience();
    teamBox.classList.toggle('hidden', audience !== 'team');
    participantBox.classList.toggle('hidden', audience !== 'participant');

    root.querySelectorAll('.audience-option').forEach((option) => {
      option.classList.toggle('is-selected', option.querySelector('input').checked);
    });

    if (audience === 'all') {
      reach.textContent = 'Goes to every active delegate.';
    } else if (audience === 'team') {
      const team = teams.find((t) => t.id === root.querySelector('#team-select').value);
      reach.textContent = team ? `Goes to ${team.current_size} member${team.current_size === 1 ? '' : 's'}.` : '';
    } else {
      reach.textContent = 'Goes to one person.';
    }
  }

  form.addEventListener('change', syncTargets);
  message.addEventListener('input', () => {
    counter.textContent = `${message.value.length} / 1000`;
  });
  syncTargets();

  async function loadHistory() {
    const box = root.querySelector('#history');
    try {
      const sent = await api('/notifications', { params: { sender_id: session.user_id, limit: 8 } });
      box.innerHTML = sent.length
        ? sent.map(historyRow).join('')
        : emptyState('Nothing sent yet', 'Messages you send will be listed here.');
    } catch (error) {
      box.innerHTML = `<p class="text-sm text-danger">${esc(error.message)}</p>`;
    }
  }

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const audience = selectedAudience();
    const body = { message: message.value.trim(), audience };
    if (audience === 'team') body.team_id = root.querySelector('#team-select').value;
    if (audience === 'participant') body.recipient_id = root.querySelector('#participant-select').value;

    if (!body.message) return;
    if (audience === 'participant' && !body.recipient_id) {
      toast('Pick a participant first.', 'error');
      return;
    }

    sendButton.disabled = true;
    sendButton.textContent = 'Sending…';
    try {
      const sent = await api('/notifications', {
        method: 'POST',
        params: { sender_id: session.user_id },
        body,
      });
      toast(`Sent to ${sent.recipient_count} recipient${sent.recipient_count === 1 ? '' : 's'}.`);
      message.value = '';
      counter.textContent = '0 / 1000';
      loadHistory();
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      sendButton.disabled = false;
      sendButton.textContent = 'Send broadcast';
    }
  });

  loadHistory();
}

function audienceBadge(notification) {
  const map = {
    all: ['All participants', 'bg-danger/12 text-danger'],
    team: [notification.team_name || 'Team', 'bg-accent/12 text-accent'],
    participant: [notification.recipient_name || 'Direct', 'bg-warning/15 text-warning'],
  };
  const [label, style] = map[notification.audience] || map.team;
  return `<span class="text-[10px] font-semibold uppercase tracking-wider px-2 py-0.5 rounded-full ${style}">${esc(label)}</span>`;
}

function historyRow(notification) {
  return `
    <div class="border-l-2 border-l-accent/40 pl-3 py-2 mb-3 last:mb-0">
      <div class="flex items-center gap-2 mb-1 flex-wrap">
        ${audienceBadge(notification)}
        <span class="text-[11px] text-fg-muted">${formatWhen(notification.timestamp)}</span>
        <span class="text-[11px] text-fg-muted">· ${notification.recipient_count} reached</span>
      </div>
      <p class="text-sm leading-snug break-words">${esc(notification.message)}</p>
    </div>`;
}
