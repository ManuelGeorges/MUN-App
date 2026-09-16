import { api, can, esc, initials, P, toast } from '../app.js';

/**
 * Wallet operations: top up a balance or take payment against a badge.
 *
 * Card lookup is a separate, explicit step from the transaction — the operator confirms who they
 * are about to charge before any money moves.
 *
 * Crediting a wallet (top up) is an admin action; an organizer at the till can only take payment.
 * The top-up direction is hidden unless the session holds TOP_UP_WALLET, and the server enforces it.
 */
export async function mount(root, { session }) {
  const canTopUp = can(session, P.TOP_UP_WALLET);

  root.innerHTML = `
    <div class="grid grid-cols-1 lg:grid-cols-[minmax(0,1fr)_320px] gap-6">
      <section class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
        <h2 class="font-display font-bold text-lg mb-5">Wallet</h2>

        <form id="wallet-form" class="space-y-5">
          <div>
            <label for="uid" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Card UID</label>
            <div class="flex gap-2">
              <input id="uid" required placeholder="04A2B3C4D5" class="field flex-1 uppercase" />
              <button type="button" id="lookup" class="rounded-xl border border-subtle px-4 py-2.5 text-sm font-medium hover:bg-surface-hover transition whitespace-nowrap">Look up</button>
            </div>
          </div>

          <fieldset>
            <legend class="text-xs font-semibold uppercase tracking-wider text-fg-muted mb-2">Direction</legend>
            <div class="grid ${canTopUp ? 'grid-cols-2' : 'grid-cols-1'} gap-2">
              ${
                canTopUp
                  ? `<label class="audience-option is-selected">
                       <input type="radio" name="kind" value="recharge" checked class="sr-only" />
                       <span class="block font-medium text-sm">Top up</span>
                       <span class="block text-xs text-fg-muted mt-0.5">Add tokens</span>
                     </label>`
                  : ''
              }
              <label class="audience-option ${canTopUp ? '' : 'is-selected'}">
                <input type="radio" name="kind" value="deduction" ${canTopUp ? '' : 'checked'} class="sr-only" />
                <span class="block font-medium text-sm">Charge</span>
                <span class="block text-xs text-fg-muted mt-0.5">Take payment</span>
              </label>
            </div>
          </fieldset>

          <div class="grid sm:grid-cols-2 gap-4">
            <div>
              <label for="amount" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Amount</label>
              <input id="amount" type="number" min="0.01" step="0.01" required placeholder="0.00" class="field tabular-nums" />
            </div>
            <div>
              <label for="description" class="block text-xs font-semibold uppercase tracking-wider text-fg-muted mb-1.5">Description</label>
              <input id="description" placeholder="Cash top-up" class="field" />
            </div>
          </div>

          <button type="submit" id="submit"
            class="w-full sm:w-auto rounded-xl bg-accent text-white font-semibold px-6 py-2.5 shadow-glow hover:brightness-110 transition disabled:opacity-50">
            Record transaction
          </button>
        </form>
      </section>

      <aside id="holder" class="rounded-2xl border border-subtle bg-surface-raised shadow-card p-6">
        <p class="text-sm text-fg-muted">Look up a card to see who holds it.</p>
      </aside>
    </div>`;

  const uid = root.querySelector('#uid');
  const holder = root.querySelector('#holder');
  const form = root.querySelector('#wallet-form');

  form.addEventListener('change', () => {
    root.querySelectorAll('.audience-option').forEach((option) =>
      option.classList.toggle('is-selected', option.querySelector('input').checked),
    );
  });

  async function lookup() {
    const value = uid.value.trim().toUpperCase();
    if (!value) return null;
    holder.innerHTML = '<p class="text-sm text-fg-muted">Looking up…</p>';
    try {
      const card = await api(`/users/cards/${encodeURIComponent(value)}`);
      holder.innerHTML = `
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-full bg-accent/15 text-accent grid place-items-center text-sm font-bold">${esc(initials(card.user.name))}</div>
          <div class="min-w-0">
            <p class="font-medium truncate">${esc(card.user.name)}</p>
            <p class="text-xs text-fg-muted">${esc(card.card_uid)}</p>
          </div>
        </div>
        <div class="rounded-xl bg-surface-sunken border border-subtle p-4 text-center">
          <p class="text-xs uppercase tracking-wider text-fg-muted font-semibold">Balance</p>
          <p class="font-display font-bold text-3xl tabular-nums mt-1">${card.user.token_balance.toFixed(2)}</p>
        </div>`;
      return card;
    } catch (error) {
      holder.innerHTML = `<p class="text-sm text-danger">${esc(error.message)}</p>`;
      return null;
    }
  }

  root.querySelector('#lookup').addEventListener('click', lookup);
  uid.addEventListener('blur', () => uid.value.trim() && lookup());

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const button = root.querySelector('#submit');
    button.disabled = true;
    try {
      await api('/transactions', {
        method: 'POST',
        body: {
          card_uid: uid.value.trim().toUpperCase(),
          amount: Number(root.querySelector('#amount').value),
          description: root.querySelector('#description').value.trim() || null,
          transaction_type: form.querySelector('input[name="kind"]:checked').value,
        },
      });
      toast('Transaction recorded.');
      root.querySelector('#amount').value = '';
      lookup();
    } catch (error) {
      toast(error.message, 'error');
    } finally {
      button.disabled = false;
    }
  });
}
