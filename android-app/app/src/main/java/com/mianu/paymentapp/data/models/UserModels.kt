package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

enum class TeamRole {
    @SerializedName("leader") LEADER,
    @SerializedName("member") MEMBER,
}

data class UserCreate(
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val password: String? = null,
    @SerializedName("token_balance") val tokenBalance: Double = 0.0,
    @SerializedName("meal_allowance") val mealAllowance: Int = 6,
    @SerializedName("meals_balance") val mealsBalance: Int = 6,
    val role: UserRole = UserRole.USER,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_role") val teamRole: TeamRole? = null,
)

/**
 * Every field is nullable because the backend treats an omitted field as "leave unchanged" —
 * a non-null default would silently overwrite server state on a partial edit.
 */
data class UserUpdate(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("is_active") val isActive: Boolean? = null,
    @SerializedName("meal_allowance") val mealAllowance: Int? = null,
    @SerializedName("meals_balance") val mealsBalance: Int? = null,
    val role: UserRole? = null,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_role") val teamRole: TeamRole? = null,
)

data class UserResponse(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    @SerializedName("token_balance") val tokenBalance: Double = 0.0,
    @SerializedName("meal_allowance") val mealAllowance: Int = 6,
    @SerializedName("meals_balance") val mealsBalance: Int = 6,
    @SerializedName("is_active") val isActive: Boolean,
    val role: String,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_role") val teamRole: TeamRole? = null,
) {
    /** `role` arrives as a raw string, so map it onto the enum here rather than at each call site. */
    val roleEnum: UserRole
        get() = when (role.lowercase()) {
            "admin" -> UserRole.ADMIN
            "chief_organizer" -> UserRole.CHIEF_ORGANIZER
            "organizer" -> UserRole.ORGANIZER
            "team_leader" -> UserRole.TEAM_LEADER
            "team_member" -> UserRole.TEAM_MEMBER
            else -> UserRole.USER
        }

    val initials: String
        get() = name.trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { "?" }
}

data class MealAdjustRequest(
    val delta: Int? = null,
    @SerializedName("meals_balance") val mealsBalance: Int? = null,
)

data class CardLinkRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("card_uid") val cardUid: String,
)

data class CardResponse(
    @SerializedName("card_uid") val cardUid: String,
    val user: UserResponse,
)
