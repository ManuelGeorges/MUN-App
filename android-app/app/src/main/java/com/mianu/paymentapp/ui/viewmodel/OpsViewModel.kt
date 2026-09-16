package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.AnalyticsSummary
import com.mianu.paymentapp.data.models.MealWindowRequest
import com.mianu.paymentapp.data.models.ReportSummary
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpsState(
    val daily: AnalyticsSummary? = null,
    val trends: AnalyticsSummary? = null,
    val mealsReport: ReportSummary? = null,
    val transactionsReport: ReportSummary? = null,
    val usersReport: ReportSummary? = null,
    val isLoading: Boolean = true,
    val isExporting: Boolean = false,
    val exportPath: String? = null,
    val mealWindows: MealWindowRequest = MealWindowRequest.Default,
    val isSavingWindows: Boolean = false,
    val message: UiMessage? = null,
)

/** Backs the analytics, reports and settings screens — the operational tail of the admin app. */
class OpsViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(OpsState())
    val state: StateFlow<OpsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            // Five independent read-only endpoints; fan them out rather than chaining.
            val daily = async { repo.getDailyAnalytics() }
            val trends = async { repo.getTrendAnalytics() }
            val meals = async { repo.getMealsReport() }
            val txns = async { repo.getTransactionsReport() }
            val users = async { repo.getUsersReport() }

            _state.update {
                it.copy(
                    isLoading = false,
                    daily = (daily.await() as? ApiResult.Success)?.data,
                    trends = (trends.await() as? ApiResult.Success)?.data,
                    mealsReport = (meals.await() as? ApiResult.Success)?.data,
                    transactionsReport = (txns.await() as? ApiResult.Success)?.data,
                    usersReport = (users.await() as? ApiResult.Success)?.data,
                )
            }
        }
    }

    fun export() {
        viewModelScope.launch {
            _state.update { it.copy(isExporting = true) }
            val result = repo.exportReport()
            _state.update { it.copy(isExporting = false) }
            when (result) {
                is ApiResult.Success -> _state.update {
                    // The endpoint answers with a location/status map rather than a file body.
                    val path = result.data["path"] ?: result.data.values.firstOrNull()
                    it.copy(
                        exportPath = path,
                        message = UiMessage("Export ready", UiMessage.Tone.Success),
                    )
                }
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun onWindowsChange(windows: MealWindowRequest) = _state.update { it.copy(mealWindows = windows) }

    fun saveMealWindows() {
        viewModelScope.launch {
            _state.update { it.copy(isSavingWindows = true) }
            val result = repo.updateMealWindows(_state.value.mealWindows)
            _state.update { it.copy(isSavingWindows = false) }
            when (result) {
                is ApiResult.Success -> notify("Meal windows saved", UiMessage.Tone.Success)
                is ApiResult.Failure -> notify(result.error.message, UiMessage.Tone.Error)
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }

    private fun notify(text: String, tone: UiMessage.Tone) =
        _state.update { it.copy(message = UiMessage(text, tone)) }
}
