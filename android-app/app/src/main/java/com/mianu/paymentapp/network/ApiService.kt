package com.mianu.paymentapp.network

import com.mianu.paymentapp.data.models.*
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit surface for the FastAPI backend.
 *
 * Paths here are written to match the server's routes exactly, with no trailing slashes. FastAPI
 * would answer `users/` with a 307 to `users`, and while OkHttp does preserve the method across a
 * 307, relying on a redirect for every write is a needless round trip.
 */
interface ApiService {

    /** Leading slash: health sits at the host root, outside the `/api/v1` base path. */
    @GET("/health")
    suspend fun checkHealth(): Map<String, String>

    // region Auth

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): TokenPair

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): TokenPair

    // endregion

    // region Dashboard

    @GET("dashboard/overview")
    suspend fun getDashboardOverview(): DashboardOverview

    // endregion

    // region Users

    @GET("users")
    suspend fun getUsers(): List<UserResponse>

    @POST("users")
    suspend fun createUser(@Body user: UserCreate): UserResponse

    @GET("users/{user_id}")
    suspend fun getUser(@Path("user_id") userId: String): UserResponse

    @PUT("users/{user_id}")
    suspend fun updateUser(
        @Path("user_id") userId: String,
        @Body update: UserUpdate,
    ): UserResponse

    @DELETE("users/{user_id}")
    suspend fun deleteUser(@Path("user_id") userId: String): Map<String, Any>

    @POST("users/{user_id}/meals/adjust")
    suspend fun adjustUserMeals(
        @Path("user_id") userId: String,
        @Body body: MealAdjustRequest,
    ): UserResponse

    /** Server route is `users/cards/link` — an earlier client used `users/link-card`, which 404s. */
    @POST("users/cards/link")
    suspend fun linkCard(@Body request: CardLinkRequest): CardResponse

    @GET("users/cards/{uid}")
    suspend fun getCard(@Path("uid") uid: String): CardResponse

    // endregion

    // region Meals

    @POST("meals/swipe")
    suspend fun swipeMeal(@Body request: MealSwipeRequest): MealSwipeResponse

    // endregion

    // region Transactions

    @POST("transactions")
    suspend fun createTransaction(@Body request: TransactionRequest): TransactionResponse

    // endregion

    // region Teams

    @GET("teams")
    suspend fun getTeams(): List<TeamResponse>

    @POST("teams")
    suspend fun createTeam(@Body team: TeamCreate): TeamResponse

    @PUT("teams/{team_id}")
    suspend fun updateTeam(
        @Path("team_id") teamId: String,
        @Body update: TeamUpdate,
    ): TeamResponse

    @DELETE("teams/{team_id}")
    suspend fun deleteTeam(@Path("team_id") teamId: String): Map<String, Any>

    @POST("teams/{team_id}/members/{user_id}")
    suspend fun assignMember(
        @Path("team_id") teamId: String,
        @Path("user_id") userId: String,
        @Query("role") role: TeamRole,
    ): Map<String, String>

    @DELETE("teams/{team_id}/members/{user_id}")
    suspend fun removeMemberFromTeam(
        @Path("team_id") teamId: String,
        @Path("user_id") userId: String,
    ): Map<String, Any>

    /** `sender_id` is a required query param server-side; omitting it fails validation with a 422. */
    @POST("teams/{team_id}/notifications")
    suspend fun createTeamNotification(
        @Path("team_id") teamId: String,
        @Query("sender_id") senderId: String,
        @Body notification: NotificationCreate,
    ): NotificationResponse

    @GET("teams/{team_id}/notifications")
    suspend fun getTeamNotifications(
        @Path("team_id") teamId: String,
    ): List<NotificationResponse>

    @GET("teams/{team_id}/members")
    suspend fun getTeamMembers(
        @Path("team_id") teamId: String,
    ): List<UserResponse>

    /** Targeted broadcast. Supersedes [createTeamNotification], which only ever hit one team. */
    @POST("notifications")
    suspend fun broadcast(
        @Query("sender_id") senderId: String,
        @Body body: BroadcastCreate,
    ): NotificationResponse

    @GET("notifications")
    suspend fun getNotifications(
        @Query("sender_id") senderId: String? = null,
        @Query("team_id") teamId: String? = null,
        @Query("limit") limit: Int = 50,
    ): List<NotificationResponse>

    @GET("notifications/inbox/{user_id}")
    suspend fun getInbox(
        @Path("user_id") userId: String,
    ): List<NotificationResponse>

    // endregion

    // region Access control / halls

    @GET("access/halls")
    suspend fun getHalls(): List<HallResponse>

    /** Identity-free room-finding view, for non-staff roles. */
    @GET("access/halls/availability")
    suspend fun getHallAvailability(): List<HallAvailability>

    @POST("access/halls")
    suspend fun createHall(@Body hall: HallCreate): HallResponse

    @PUT("access/halls/{hall_id}")
    suspend fun updateHall(
        @Path("hall_id") hallId: String,
        @Body update: HallUpdate,
    ): HallResponse

    /** Returns the user ids currently inside the hall. */
    @GET("access/halls/{hall_id}/presence")
    suspend fun getHallPresence(@Path("hall_id") hallId: String): List<String>

    /** Body, not query params — the server binds an `AccessScanRequest`. */
    @POST("access/scan")
    suspend fun scanAccess(@Body request: AccessScanRequest): AccessLogResponse

    // endregion

    // region Analytics & reports

    @GET("analytics/daily")
    suspend fun getDailyAnalytics(): AnalyticsSummary

    @GET("analytics/trends")
    suspend fun getTrendAnalytics(): AnalyticsSummary

    @GET("reports/meals")
    suspend fun getMealsReport(): ReportSummary

    @GET("reports/transactions")
    suspend fun getTransactionsReport(): ReportSummary

    @GET("reports/users")
    suspend fun getUsersReport(): ReportSummary

    @GET("reports/export")
    suspend fun exportReport(): Map<String, String>

    // endregion

    // region Settings

    @PUT("settings/meal-windows")
    suspend fun updateMealWindows(
        @Body request: MealWindowRequest,
    ): Map<String, MealWindowRequest>

    // endregion

    // region Operations

    @GET("operations/locations")
    suspend fun getLocations(): List<DelegateLocation>

    @GET("operations/scans")
    suspend fun getLiveScans(
        @Query("type") type: String? = null,
    ): List<LiveScanEvent>

    // endregion

    // region Conference Attendance

    @GET("attendance")
    suspend fun getAttendance(): AttendanceResponse

    @POST("attendance/swipe")
    suspend fun recordAttendanceSwipe(
        @Body request: AttendanceSwipeRequest,
    ): AttendanceSwipeResponse

    @POST("attendance/single")
    suspend fun updateSingleAttendance(
        @Body request: SingleAttendanceRequest,
    ): SingleAttendanceResponse

    @POST("attendance/bulk")
    suspend fun updateBulkAttendance(
        @Body request: BulkAttendanceRequest,
    ): BulkAttendanceResponse

    // endregion
}
