package com.mianu.paymentapp.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.os.Bundle
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicReference

/** Whether this device can scan at all, re-checked every time the app comes back to the foreground. */
enum class NfcAvailability {
    /** No NFC hardware. Admin-only devices are expected to land here. */
    UNSUPPORTED,

    /** Hardware present but switched off in system settings. */
    DISABLED,

    READY,
}

/** What came off the antenna. Carries no platform types, so screens never touch `android.nfc`. */
sealed interface NfcEvent {
    /** A badge was read. [uid] is the hardware UID the backend keys on. */
    data class Scanned(val uid: String) : NfcEvent

    data class Written(val uid: String) : NfcEvent
    data class WriteFailed(val uid: String, val reason: String) : NfcEvent
}

/**
 * The app's single NFC reader.
 *
 * Reader mode rather than foreground dispatch: it keeps tag handling inside the app instead of
 * bouncing through intents, stops the system launching some other NDEF handler mid-service, and
 * lets us silence the platform chime (see [READER_FLAGS]).
 *
 * Bound for the whole foreground lifetime of the activity rather than per screen, so a badge tapped
 * on the wrong tab is still swallowed by us instead of waking another app. Screens opt in by
 * collecting [events].
 */
object NfcHub {

    /**
     * Platform sounds are off deliberately. The chime fires the instant a tag is read, which is
     * before the server has decided anything — a steward hearing it would read "served" off a scan
     * that is about to be refused. Feedback belongs after the verdict, not on contact.
     */
    private const val READER_FLAGS =
        NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

    /**
     * Ignore a repeat of the same UID inside this window.
     *
     * A badge held against the antenna can re-enumerate, and on a payment surface a duplicate is a
     * double charge. Long enough to absorb bounce, short enough that a steward deliberately
     * re-tapping the same badge isn't left wondering why nothing happened.
     */
    private const val DUPLICATE_WINDOW_MS = 2_500L

    private val _availability = MutableStateFlow(NfcAvailability.UNSUPPORTED)
    val availability: StateFlow<NfcAvailability> = _availability.asStateFlow()

    // Replay 0: a scan is a moment, not state. A screen opening later must not inherit the last tap
    // and act on it — that would re-charge a wallet on navigation.
    private val _events = MutableSharedFlow<NfcEvent>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<NfcEvent> = _events.asSharedFlow()

    private val pendingWrite = AtomicReference<PendingWrite?>(null)

    private val _writeArmed = MutableStateFlow(false)
    val writeArmed: StateFlow<Boolean> = _writeArmed.asStateFlow()

    private var adapter: NfcAdapter? = null

    @Volatile private var lastUid: String? = null
    @Volatile private var lastUidAt = 0L

    private class PendingWrite(val cardUid: String, val delegateName: String)

    /** Call from the activity's `onResume`. */
    fun bind(activity: Activity) {
        val nfc = NfcAdapter.getDefaultAdapter(activity)
        adapter = nfc

        _availability.value = when {
            nfc == null -> NfcAvailability.UNSUPPORTED
            !nfc.isEnabled -> NfcAvailability.DISABLED
            else -> NfcAvailability.READY
        }

        if (_availability.value != NfcAvailability.READY) return

        // Shorter presence check than the default makes badge-removal detection snappy, which is
        // what lets a queue move at one delegate per tap.
        val extras = Bundle().apply {
            putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        }

        nfc.enableReaderMode(activity, { tag ->
            val uid = tag.id.toUidHex()

            // Runs on a binder thread — safe to block, and required to, since the tag is only
            // addressable while it is physically in the field.
            val write = pendingWrite.getAndSet(null)

            if (write == null) {
                if (isDuplicate(uid)) return@enableReaderMode
                markSeen(uid)
                _events.tryEmit(NfcEvent.Scanned(uid))
            } else {
                // A write bypasses the duplicate window: it's an explicit operator action, and the
                // badge being encoded is very often the one just read to fill in the UID.
                markSeen(uid)
                _writeArmed.value = false
                val message = badgeMessage(write.cardUid.ifBlank { uid }, write.delegateName)
                _events.tryEmit(
                    when (val result = writeNdef(tag, message)) {
                        is WriteResult.Success -> NfcEvent.Written(uid)
                        is WriteResult.Failure -> NfcEvent.WriteFailed(uid, result.reason)
                    },
                )
            }
        }, READER_FLAGS, extras)
    }

    /** Call from the activity's `onPause`. */
    fun unbind(activity: Activity) {
        runCatching { adapter?.disableReaderMode(activity) }
        // Don't carry an armed write across a trip to the background; the operator who armed it may
        // not be the one holding the device when it comes back.
        cancelWrite()
    }

    /**
     * Arms the next tap to be written rather than just read.
     *
     * Single-shot: it disarms on the first badge presented, on failure, and on backgrounding, so a
     * forgotten arm can't silently overwrite the next delegate's badge.
     */
    fun armWrite(cardUid: String, delegateName: String) {
        pendingWrite.set(PendingWrite(cardUid, delegateName))
        _writeArmed.value = true
    }

    fun cancelWrite() {
        pendingWrite.set(null)
        _writeArmed.value = false
    }

    /**
     * Injects a scan event into the hub.
     * Works seamlessly on emulator or testing environments while honoring normal event flows.
     */
    fun simulateScan(uid: String) {
        val clean = uid.trim().uppercase()
        if (clean.isNotBlank()) {
            markSeen(clean)
            _events.tryEmit(NfcEvent.Scanned(clean))
        }
    }

    private fun isDuplicate(uid: String): Boolean =
        uid == lastUid && System.currentTimeMillis() - lastUidAt < DUPLICATE_WINDOW_MS

    private fun markSeen(uid: String) {
        lastUid = uid
        lastUidAt = System.currentTimeMillis()
    }
}
