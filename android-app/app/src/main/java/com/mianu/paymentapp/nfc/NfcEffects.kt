package com.mianu.paymentapp.nfc

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * Runs [onEvent] for every badge tapped while this screen is on top.
 *
 * Collection is scoped to STARTED against the nav back stack entry's lifecycle, so a screen the user
 * has navigated away from stops receiving taps — otherwise one badge would register a meal *and* a
 * door entry. Note this does not cover dialogs: a dialog leaves the screen behind it STARTED, so a
 * screen that scans must not also host a scanning dialog.
 *
 * Set [enabled] to false while a scan is already in flight; taps during that window are dropped
 * rather than queued, which is the right call for a payment surface.
 */
@Composable
fun OnNfcEvent(enabled: Boolean = true, onEvent: (NfcEvent) -> Unit) {
    val handler by rememberUpdatedState(onEvent)
    val owner = LocalLifecycleOwner.current

    LaunchedEffect(owner, enabled) {
        if (!enabled) return@LaunchedEffect
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            NfcHub.events.collect { handler(it) }
        }
    }
}

/** Convenience for the common case: only care about a successful read. */
@Composable
fun OnNfcScan(enabled: Boolean = true, onScan: (uid: String) -> Unit) {
    OnNfcEvent(enabled = enabled) { event ->
        if (event is NfcEvent.Scanned) onScan(event.uid)
    }
}

/** Current reader availability, for screens that explain why nothing is happening. */
@Composable
fun nfcAvailability(): State<NfcAvailability> =
    NfcHub.availability.collectAsStateWithLifecycle()
