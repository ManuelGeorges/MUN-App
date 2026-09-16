package com.mianu.paymentapp.ui.viewmodel

import com.mianu.paymentapp.data.ApiError

/**
 * Load state for a screen section.
 *
 * [Loading] is separate from an empty [Success] so the UI can show a shimmer instead of an
 * "add your first item" empty state while a request is still in flight.
 */
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: ApiError) : UiState<Nothing>
}

val <T> UiState<T>.dataOrNull: T?
    get() = (this as? UiState.Success)?.data

val UiState<*>.isLoading: Boolean get() = this is UiState.Loading

/** One-shot user feedback (snackbars), as opposed to persistent screen state. */
data class UiMessage(
    val text: String,
    val tone: Tone = Tone.Neutral,
    val id: Long = System.currentTimeMillis(),
) {
    enum class Tone { Neutral, Success, Error, Warning }
}
