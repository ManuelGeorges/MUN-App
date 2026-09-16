package com.mianu.paymentapp.nfc

import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException

/**
 * Low-level tag I/O. Everything here is blocking and must run off the main thread — the reader-mode
 * callback already delivers on a binder thread, which is where these are called from.
 */

/**
 * Hardware UID as uppercase hex.
 *
 * This is the identifier the whole backend keys on (`Card.uid`), so the formatting has to stay
 * stable: a badge enrolled as `04A2B3C4D5` must scan back byte-for-byte identical at the meal line.
 */
fun ByteArray.toUidHex(): String = joinToString("") { "%02X".format(it.toInt() and 0xFF) }

sealed interface WriteResult {
    data object Success : WriteResult
    data class Failure(val reason: String) : WriteResult
}

/** NDEF external type. Namespaced to us so writing a badge can't hijack another app's handler. */
private const val BADGE_DOMAIN = "mianu.com"
private const val BADGE_TYPE = "badge"

/**
 * The payload written to a delegate badge during enrolment.
 *
 * Two records, for two different readers:
 *  - an external record carrying the UID, so a badge is self-describing and a re-encoded or cloned
 *    tag can be spotted by comparing the written UID against the hardware one;
 *  - a plain text record, so a badge found on the floor and tapped with any phone says what it is.
 *
 * Deliberately no Android Application Record: an AAR would push a delegate who taps their own badge
 * to install the staff app.
 */
fun badgeMessage(cardUid: String, delegateName: String): NdefMessage = NdefMessage(
    arrayOf(
        NdefRecord.createExternal(BADGE_DOMAIN, BADGE_TYPE, cardUid.toByteArray(Charsets.UTF_8)),
        NdefRecord.createTextRecord("en", "MIANU delegate badge — $delegateName"),
    ),
)

/**
 * Writes [message] to [tag], formatting the tag first if it is blank.
 *
 * The tag is only in the RF field for as long as someone holds it there, so this runs inline on
 * discovery rather than being handed off — a round trip through a coroutine dispatcher is often
 * enough to lose the connection mid-write.
 */
fun writeNdef(tag: Tag, message: NdefMessage): WriteResult {
    val size = message.byteArrayLength

    Ndef.get(tag)?.let { ndef ->
        return try {
            ndef.connect()
            when {
                !ndef.isWritable -> WriteResult.Failure("This badge is locked read-only.")
                ndef.maxSize < size ->
                    WriteResult.Failure("Badge holds ${ndef.maxSize} bytes, payload needs $size.")
                else -> {
                    ndef.writeNdefMessage(message)
                    WriteResult.Success
                }
            }
        } catch (e: IOException) {
            WriteResult.Failure("Badge moved away before the write finished.")
        } catch (e: FormatException) {
            WriteResult.Failure("Badge rejected the payload format.")
        } finally {
            runCatching { ndef.close() }
        }
    }

    // Blank tag straight out of the packet: no NDEF structure yet, so format and write in one step.
    val formatable = NdefFormatable.get(tag)
        ?: return WriteResult.Failure("This badge doesn't support NDEF and can't be written.")

    return try {
        formatable.connect()
        formatable.format(message)
        WriteResult.Success
    } catch (e: IOException) {
        WriteResult.Failure("Badge moved away before formatting finished.")
    } catch (e: FormatException) {
        WriteResult.Failure("Badge couldn't be formatted for NDEF.")
    } finally {
        runCatching { formatable.close() }
    }
}
