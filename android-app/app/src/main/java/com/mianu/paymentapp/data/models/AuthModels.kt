package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

enum class UserRole {
    @SerializedName("admin") ADMIN,
    @SerializedName("chief_organizer") CHIEF_ORGANIZER,
    @SerializedName("organizer") ORGANIZER,
    @SerializedName("team_leader") TEAM_LEADER,
    @SerializedName("team_member") TEAM_MEMBER,
    @SerializedName("user") USER;

    val displayName: String
        get() = when (this) {
            ADMIN -> "Admin"
            CHIEF_ORGANIZER -> "Chief organizer"
            ORGANIZER -> "Organizer"
            TEAM_LEADER -> "Team leader"
            TEAM_MEMBER -> "Team member"
            USER -> "Delegate"
        }
}
// TeamRole (leader/member) lives in UserModels.kt — it is orthogonal to UserRole, and broadcasting
// needs both.

data class LoginRequest(
    val username: String,
    val password: String,
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.USER,
)

data class TokenPair(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val role: UserRole,
    // Identity now travels with the token, so the session no longer has to fetch the whole user
    // list and match on username just to learn its own id.
    @SerializedName("user_id") val userId: String? = null,
    val name: String? = null,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_role") val teamRole: TeamRole? = null,
    // What this account may do, resolved server-side. The app gates its UI on these so it can't
    // offer an action the API will refuse.
    //
    // Nullable on purpose: Gson populates fields by reflection and ignores Kotlin default values, so
    // an absent JSON field arrives as null, not `emptyList()`. Declaring it non-null would let a
    // response from an older backend crash the sign-in. Callers coerce with `?: emptyList()`.
    val permissions: List<String>? = null,
)
