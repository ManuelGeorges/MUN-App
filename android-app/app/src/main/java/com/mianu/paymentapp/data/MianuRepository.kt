package com.mianu.paymentapp.data

import com.mianu.paymentapp.data.models.*
import com.mianu.paymentapp.network.ApiService
import com.mianu.paymentapp.network.RetrofitClient

/**
 * Single entry point to the backend for the whole app.
 *
 * Everything returns [ApiResult] rather than throwing, so ViewModels handle success and failure on
 * the same path instead of wrapping each call in try/catch.
 */
class MianuRepository(
    private val api: ApiService = RetrofitClient.apiService,
) {

    // region Auth

    suspend fun login(username: String, password: String): ApiResult<TokenPair> =
        apiCall { api.login(LoginRequest(username, password)) }
            .onSuccess { RetrofitClient.setToken(it.accessToken) }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        role: UserRole = UserRole.USER,
    ): ApiResult<TokenPair> =
        apiCall { api.register(RegisterRequest(username, email, password, role)) }
            .onSuccess { RetrofitClient.setToken(it.accessToken) }

    fun logout() = RetrofitClient.clearToken()

    suspend fun checkHealth(): ApiResult<Map<String, String>> = apiCall { api.checkHealth() }

    // endregion

    // region Dashboard

    suspend fun getDashboardOverview(): ApiResult<DashboardOverview> =
        apiCall { api.getDashboardOverview() }

    // endregion

    // region Users

    suspend fun getUsers(): ApiResult<List<UserResponse>> = apiCall { api.getUsers() }

    suspend fun getUser(userId: String): ApiResult<UserResponse> = apiCall { api.getUser(userId) }

    suspend fun createUser(user: UserCreate): ApiResult<UserResponse> = apiCall { api.createUser(user) }

    suspend fun updateUser(userId: String, update: UserUpdate): ApiResult<UserResponse> =
        apiCall { api.updateUser(userId, update) }

    suspend fun deleteUser(userId: String): ApiResult<Unit> = apiCall {
        api.deleteUser(userId)
        Unit
    }

    suspend fun adjustUserMeals(userId: String, delta: Int? = null, mealsBalance: Int? = null): ApiResult<UserResponse> =
        apiCall { api.adjustUserMeals(userId, MealAdjustRequest(delta = delta, mealsBalance = mealsBalance)) }

    suspend fun setUserActive(userId: String, active: Boolean): ApiResult<UserResponse> =
        apiCall { api.updateUser(userId, UserUpdate(isActive = active)) }

    suspend fun linkCard(userId: String, cardUid: String): ApiResult<CardResponse> =
        apiCall { api.linkCard(CardLinkRequest(userId, cardUid)) }

    suspend fun getCard(uid: String): ApiResult<CardResponse> = apiCall { api.getCard(uid) }

    // endregion

    // region Meals

    suspend fun swipeMeal(cardUid: String, mealType: MealType): ApiResult<MealSwipeResponse> =
        apiCall { api.swipeMeal(MealSwipeRequest(cardUid, mealType)) }

    // endregion

    // region Transactions

    suspend fun charge(
        cardUid: String,
        amount: Double,
        description: String? = null,
    ): ApiResult<TransactionResponse> = apiCall {
        api.createTransaction(
            TransactionRequest(
                cardUid = cardUid,
                amount = amount,
                description = description,
                transactionType = TransactionType.DEDUCTION,
            ),
        )
    }

    suspend fun recharge(
        cardUid: String,
        amount: Double,
        description: String? = null,
    ): ApiResult<TransactionResponse> = apiCall {
        api.createTransaction(
            TransactionRequest(
                cardUid = cardUid,
                amount = amount,
                description = description,
                transactionType = TransactionType.RECHARGE,
            ),
        )
    }

    // endregion

    // region Teams

    suspend fun getTeams(): ApiResult<List<TeamResponse>> = apiCall { api.getTeams() }

    suspend fun createTeam(name: String, capacity: Int): ApiResult<TeamResponse> =
        apiCall { api.createTeam(TeamCreate(name, capacity)) }

    suspend fun updateTeam(teamId: String, update: TeamUpdate): ApiResult<TeamResponse> =
        apiCall { api.updateTeam(teamId, update) }

    suspend fun deleteTeam(teamId: String): ApiResult<Unit> = apiCall {
        api.deleteTeam(teamId)
        Unit
    }

    suspend fun assignMember(
        teamId: String,
        userId: String,
        role: TeamRole,
    ): ApiResult<Map<String, String>> = apiCall { api.assignMember(teamId, userId, role) }

    suspend fun removeMemberFromTeam(
        teamId: String,
        userId: String,
    ): ApiResult<Unit> = apiCall {
        api.removeMemberFromTeam(teamId, userId)
        Unit
    }

    suspend fun getTeamNotifications(teamId: String): ApiResult<List<NotificationResponse>> =
        apiCall { api.getTeamNotifications(teamId) }

    /**
     * Sends a targeted broadcast.
     *
     * Permission is decided server-side — a team leader aiming wider than their own team gets a 403
     * with an explanation, which is surfaced as-is rather than pre-empted here.
     */
    suspend fun broadcast(
        senderId: String,
        message: String,
        audience: BroadcastAudience,
        teamId: String? = null,
        recipientId: String? = null,
    ): ApiResult<NotificationResponse> =
        apiCall {
            api.broadcast(
                senderId = senderId,
                body = BroadcastCreate(
                    message = message,
                    audience = audience,
                    teamId = teamId,
                    recipientId = recipientId,
                ),
            )
        }

    suspend fun getSentNotifications(senderId: String): ApiResult<List<NotificationResponse>> =
        apiCall { api.getNotifications(senderId = senderId, limit = 20) }

    suspend fun getTeamMembers(teamId: String): ApiResult<List<UserResponse>> =
        apiCall { api.getTeamMembers(teamId) }

    suspend fun getInbox(userId: String): ApiResult<List<NotificationResponse>> =
        apiCall { api.getInbox(userId) }

    suspend fun getHallAvailability(): ApiResult<List<HallAvailability>> =
        apiCall { api.getHallAvailability() }

    // endregion

    // region Access control

    suspend fun getHalls(): ApiResult<List<HallResponse>> = apiCall { api.getHalls() }

    suspend fun createHall(
        name: String,
        capacityThreshold: Int,
        allowedRoles: List<UserRole>,
    ): ApiResult<HallResponse> =
        apiCall { api.createHall(HallCreate(name, capacityThreshold, allowedRoles)) }

    suspend fun updateHall(hallId: String, update: HallUpdate): ApiResult<HallResponse> =
        apiCall { api.updateHall(hallId, update) }

    suspend fun getHallPresence(hallId: String): ApiResult<List<String>> =
        apiCall { api.getHallPresence(hallId) }

    suspend fun scanAccess(
        cardUid: String,
        hallId: String,
        action: AccessAction,
    ): ApiResult<AccessLogResponse> =
        apiCall { api.scanAccess(AccessScanRequest(cardUid, hallId, action)) }

    // endregion

    // region Analytics & reports

    suspend fun getDailyAnalytics(): ApiResult<AnalyticsSummary> = apiCall { api.getDailyAnalytics() }

    suspend fun getTrendAnalytics(): ApiResult<AnalyticsSummary> = apiCall { api.getTrendAnalytics() }

    suspend fun getMealsReport(): ApiResult<ReportSummary> = apiCall { api.getMealsReport() }

    suspend fun getTransactionsReport(): ApiResult<ReportSummary> = apiCall { api.getTransactionsReport() }

    suspend fun getUsersReport(): ApiResult<ReportSummary> = apiCall { api.getUsersReport() }

    suspend fun exportReport(): ApiResult<Map<String, String>> = apiCall { api.exportReport() }

    // endregion

    // region Settings

    suspend fun updateMealWindows(
        windows: MealWindowRequest,
    ): ApiResult<Map<String, MealWindowRequest>> = apiCall { api.updateMealWindows(windows) }

    // endregion

    // region Operations

    suspend fun getLocations(): ApiResult<List<DelegateLocation>> =
        apiCall { api.getLocations() }

    suspend fun getLiveScans(type: String? = null): ApiResult<List<LiveScanEvent>> =
        apiCall { api.getLiveScans(type) }

    // endregion

    // region Conference Attendance

    suspend fun getAttendance(): ApiResult<AttendanceResponse> =
        apiCall { api.getAttendance() }

    suspend fun recordAttendanceSwipe(
        cardUid: String,
        action: String = "check_in",
    ): ApiResult<AttendanceSwipeResponse> =
        apiCall { api.recordAttendanceSwipe(AttendanceSwipeRequest(cardUid, action)) }

    suspend fun updateSingleAttendance(
        userId: String,
        status: String,
    ): ApiResult<SingleAttendanceResponse> =
        apiCall { api.updateSingleAttendance(SingleAttendanceRequest(userId, status)) }

    suspend fun updateBulkAttendance(
        userIds: List<String>? = null,
        all: Boolean? = null,
        status: String,
    ): ApiResult<BulkAttendanceResponse> =
        apiCall { api.updateBulkAttendance(BulkAttendanceRequest(userIds, all, status)) }

    // endregion

    companion object {
        /**
         * Shared instance. The app has no DI container yet; when one lands, inject
         * [MianuRepository] instead and drop this.
         */
        val instance: MianuRepository by lazy { MianuRepository() }
    }
}
