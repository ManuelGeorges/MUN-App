package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.AccessAction
import com.mianu.paymentapp.data.models.AttendanceSummary
import com.mianu.paymentapp.data.models.HallResponse
import com.mianu.paymentapp.data.models.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Outcome of the most recent scan, driving the big pass/fail readout. */
sealed interface ScanOutcome {
    data object Idle : ScanOutcome
    data object Scanning : ScanOutcome
    data class Granted(val headline: String, val detail: String? = null) : ScanOutcome
    data class Denied(val headline: String, val detail: String? = null) : ScanOutcome
}

data class ScanState(
    val outcome: ScanOutcome = ScanOutcome.Idle,
    val mealType: MealType = MealType.BREAKFAST,
    val halls: List<HallResponse> = emptyList(),
    val selectedHallId: String? = null,
    val action: AccessAction = AccessAction.ENTRY,
    val attendanceAction: String = "check_in", // "check_in" | "check_out" | "toggle"
    val attendanceSummary: AttendanceSummary? = null,
    val history: List<ScanHistoryEntry> = emptyList(),
) {
    val selectedHall: HallResponse? get() = halls.firstOrNull { it.id == selectedHallId }
    val isBusy: Boolean get() = outcome is ScanOutcome.Scanning
}

data class ScanHistoryEntry(
    val label: String,
    val detail: String,
    val granted: Boolean,
    val at: Long = System.currentTimeMillis(),
)

/**
 * Drives both scanning surfaces: meal swipes and hall access.
 *
 * They share one ViewModel because they share the same physical interaction — tap a card, get a
 * pass/fail — and stewards switch between them on the same device during an event.
 */
class ScanViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    init {
        loadHalls()
    }

    fun loadHalls() {
        viewModelScope.launch {
            (repo.getHalls() as? ApiResult.Success)?.let { result ->
                _state.update { current ->
                    current.copy(
                        halls = result.data,
                        // Preselect the first hall so the scanner is usable without a config step.
                        selectedHallId = current.selectedHallId ?: result.data.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun onMealTypeChange(type: MealType) = _state.update { it.copy(mealType = type) }
    fun onHallChange(hallId: String) = _state.update { it.copy(selectedHallId = hallId) }
    fun onActionChange(action: AccessAction) = _state.update { it.copy(action = action) }
    fun onAttendanceActionChange(action: String) = _state.update { it.copy(attendanceAction = action) }
    fun reset() = _state.update { it.copy(outcome = ScanOutcome.Idle) }

    fun loadAttendanceSummary() {
        viewModelScope.launch {
            (repo.getAttendance() as? ApiResult.Success)?.let { result ->
                _state.update { it.copy(attendanceSummary = result.data.summary) }
            }
        }
    }

    fun scanAttendance(cardUid: String) {
        if (cardUid.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(outcome = ScanOutcome.Scanning) }
            val action = _state.value.attendanceAction
            when (val result = repo.recordAttendanceSwipe(cardUid, action)) {
                is ApiResult.Success -> {
                    val user = result.data.user
                    val isPresent = user.status == "present"
                    val actionVerb = if (user.action == "check_in") "Checked In" else "Checked Out"
                    val statusText = if (isPresent) "PRESENT" else "ABSENT"
                    val teamInfo = user.teamName?.let { " • $it" } ?: ""
                    finish(
                        ScanOutcome.Granted("$actionVerb: ${user.name}", "${user.role}$teamInfo • $statusText"),
                        ScanHistoryEntry(actionVerb, "${user.name} ($statusText)", granted = true),
                    )
                    _state.update { it.copy(attendanceSummary = result.data.summary) }
                }
                is ApiResult.Failure -> finish(
                    ScanOutcome.Denied("Attendance swipe failed", result.error.message),
                    ScanHistoryEntry("Denied", result.error.message, granted = false),
                )
            }
        }
    }

    fun swipeMeal(cardUid: String) {
        if (cardUid.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(outcome = ScanOutcome.Scanning) }
            val mealType = _state.value.mealType
            when (val result = repo.swipeMeal(cardUid, mealType)) {
                is ApiResult.Success -> {
                    val label = mealType.name.lowercase().replaceFirstChar { it.uppercase() }
                    val remainingText = result.data.mealsRemaining?.let { " • $it meals left" } ?: ""
                    val nameText = result.data.userName ?: "Delegate ${result.data.userId}"
                    finish(
                        ScanOutcome.Granted("$label served", "$nameText$remainingText"),
                        ScanHistoryEntry(label, "$nameText$remainingText", granted = true),
                    )
                }
                is ApiResult.Failure -> finish(
                    ScanOutcome.Denied("Not served", result.error.message),
                    ScanHistoryEntry("Denied", result.error.message, granted = false),
                )
            }
        }
    }

    fun scanAccess(cardUid: String) {
        val hallId = _state.value.selectedHallId
        if (cardUid.isBlank()) return
        if (hallId == null) {
            _state.update {
                it.copy(outcome = ScanOutcome.Denied("No hall selected", "Pick a hall before scanning."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(outcome = ScanOutcome.Scanning) }
            val action = _state.value.action
            when (val result = repo.scanAccess(cardUid, hallId, action)) {
                is ApiResult.Success -> {
                    val log = result.data
                    val verb = if (action == AccessAction.ENTRY) "Entry" else "Exit"
                    // A 200 doesn't mean admitted — the log carries its own allow/deny decision.
                    if (log.allowed) {
                        finish(
                            ScanOutcome.Granted("$verb granted", "Delegate ${log.userId}"),
                            ScanHistoryEntry(verb, "Delegate ${log.userId}", granted = true),
                        )
                    } else {
                        finish(
                            ScanOutcome.Denied("$verb denied", log.reason ?: "Not permitted in this hall."),
                            ScanHistoryEntry(verb, log.reason ?: "Denied", granted = false),
                        )
                    }
                    loadHalls() // Occupancy just changed.
                }
                is ApiResult.Failure -> finish(
                    ScanOutcome.Denied("Scan failed", result.error.message),
                    ScanHistoryEntry("Error", result.error.message, granted = false),
                )
            }
        }
    }

    private fun finish(outcome: ScanOutcome, entry: ScanHistoryEntry) {
        _state.update {
            // Cap the tape at 20 — it's a glanceable recent-activity strip, not an audit log.
            it.copy(outcome = outcome, history = (listOf(entry) + it.history).take(20))
        }
    }
}
