package com.mianu.paymentapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mianu.paymentapp.data.ApiResult
import com.mianu.paymentapp.data.MianuRepository
import com.mianu.paymentapp.data.models.AnalyticsSummary
import com.mianu.paymentapp.data.models.DashboardOverview
import com.mianu.paymentapp.data.models.HallResponse
import com.mianu.paymentapp.data.models.LiveScanEvent
import com.mianu.paymentapp.data.models.TeamResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val overview: UiState<DashboardOverview> = UiState.Loading,
    val halls: List<HallResponse> = emptyList(),
    val teams: List<TeamResponse> = emptyList(),
    val daily: AnalyticsSummary? = null,
    val liveScans: List<LiveScanEvent> = emptyList(),
    val isRefreshing: Boolean = false,
) {
    /** Halls needing steward attention, surfaced at the top of the dashboard. */
    val hallsNeedingAttention: List<HallResponse>
        get() = halls.filter { it.isAtCapacity || it.isNearCapacity }

    val teamsAtCapacity: Int get() = teams.count { it.isFull }
}

class DashboardViewModel(
    private val repo: MianuRepository = MianuRepository.instance,
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    init {
        refresh()
    }

    /**
     * Loads every dashboard panel concurrently.
     *
     * The four calls are independent, so they run in parallel — sequentially this would be four
     * round trips of latency before the screen settles. Secondary panels degrade to empty on
     * failure rather than taking the whole dashboard down; only the overview drives the error state.
     */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }

            val overviewJob = async { repo.getDashboardOverview() }
            val hallsJob = async { repo.getHalls() }
            val teamsJob = async { repo.getTeams() }
            val dailyJob = async { repo.getDailyAnalytics() }
            val scansJob = async { repo.getLiveScans() }

            val overview = when (val r = overviewJob.await()) {
                is ApiResult.Success -> UiState.Success(r.data)
                is ApiResult.Failure -> UiState.Error(r.error)
            }

            _state.update {
                it.copy(
                    overview = overview,
                    halls = (hallsJob.await() as? ApiResult.Success)?.data ?: emptyList(),
                    teams = (teamsJob.await() as? ApiResult.Success)?.data ?: emptyList(),
                    daily = (dailyJob.await() as? ApiResult.Success)?.data,
                    liveScans = (scansJob.await() as? ApiResult.Success)?.data ?: emptyList(),
                    isRefreshing = false,
                )
            }
        }
    }
}
