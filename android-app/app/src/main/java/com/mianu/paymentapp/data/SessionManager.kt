package com.mianu.paymentapp.data

import com.mianu.paymentapp.data.models.TeamRole
import com.mianu.paymentapp.data.models.TokenPair
import com.mianu.paymentapp.ui.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.mianu.paymentapp.data.models.UserRole as ApiUserRole

/**
 * Who is signed in, for the lifetime of the process.
 *
 * In-memory only — the session does not survive a process death, so the app returns to the login
 * screen on cold start. That's deliberate for a shared kiosk device handed between stewards;
 * persisting it would need an encrypted store, not plain DataStore, since it holds a bearer token.
 */
object SessionManager {

    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    val currentRole: UserRole get() = _state.value.role
    val currentUserId: String? get() = _state.value.userId
    val isSignedIn: Boolean get() = _state.value.role != UserRole.NONE

    /** True only when this account both *is* a team leader and actually leads a team. */
    val canBroadcastToTeam: Boolean
        get() = _state.value.let {
            it.apiRole == ApiUserRole.TEAM_LEADER && it.teamRole == TeamRole.LEADER && it.teamId != null
        }

    /**
     * Whether the signed-in account holds [permission].
     *
     * The permission set is resolved server-side and travels with the token, so this gates the UI
     * from the same source the API enforces on — the two can't disagree. It is a UI convenience
     * only; the server still checks every mutating request.
     */
    fun has(permission: String): Boolean = permission in _state.value.permissions

    fun signIn(token: TokenPair) {
        _state.value = SessionState(
            userId = token.userId,
            username = token.name ?: "",
            displayName = token.name ?: "",
            role = token.role.toUiRole(),
            apiRole = token.role,
            teamId = token.teamId,
            teamRole = token.teamRole,
            // Coerce the Gson-nullable field; absent permissions mean "grant nothing", which is the
            // safe default — the UI simply hides gated features rather than crashing.
            permissions = token.permissions ?: emptyList(),
        )
    }

    fun signOut() {
        MianuRepository.instance.logout()
        _state.value = SessionState()
    }
}

data class SessionState(
    val userId: String? = null,
    val username: String = "",
    val displayName: String = "",
    val role: UserRole = UserRole.NONE,
    val apiRole: ApiUserRole? = null,
    val teamId: String? = null,
    val teamRole: TeamRole? = null,
    val permissions: List<String> = emptyList(),
) {
    val label: String get() = displayName.ifBlank { username }.ifBlank { "Guest" }
}

/** Permission names, mirroring the backend `app/permissions.py`. */
object Permissions {
    const val MANAGE_USERS = "manage_users"
    const val MANAGE_TEAMS = "manage_teams"
    const val MANAGE_HALLS = "manage_halls"
    const val CHARGE_TOKENS = "charge_tokens"
    const val CHARGE_MEALS = "charge_meals"
    const val TOP_UP_WALLET = "top_up_wallet"
    const val SCAN_ACCESS = "scan_access"
    const val BROADCAST_WIDE = "broadcast_wide"
    const val BROADCAST_OWN_TEAM = "broadcast_own_team"
    const val VIEW_HALL_AVAILABILITY = "view_hall_availability"
    const val VIEW_INBOX = "view_inbox"
}

/**
 * The app has two role enums: the API's three-value role and the UI's navigation role (which adds
 * NONE for the signed-out state). Delegates map onto ORGANIZER because the operational app is
 * staff-facing — a delegate signing in gets the logistics surface, not an admin console.
 */
fun ApiUserRole.toUiRole(): UserRole = when (this) {
    ApiUserRole.ADMIN -> UserRole.ADMIN
    ApiUserRole.CHIEF_ORGANIZER -> UserRole.CHIEF_ORGANIZER
    ApiUserRole.ORGANIZER -> UserRole.ORGANIZER
    ApiUserRole.TEAM_LEADER -> UserRole.TEAM_LEADER
    ApiUserRole.TEAM_MEMBER -> UserRole.TEAM_MEMBER
    // A plain delegate has no operational surface, but the app is staff-facing; default to the
    // most limited staff view rather than crashing the nav on an unmapped role.
    ApiUserRole.USER -> UserRole.TEAM_MEMBER
}
