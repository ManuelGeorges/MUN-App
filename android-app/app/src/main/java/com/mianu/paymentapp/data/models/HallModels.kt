package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

enum class AccessAction {
    @SerializedName("entry") ENTRY,
    @SerializedName("exit") EXIT,
}

data class HallCreate(
    val name: String,
    @SerializedName("capacity_threshold") val capacityThreshold: Int,
    @SerializedName("allowed_roles") val allowedRoles: List<UserRole> =
        listOf(UserRole.USER, UserRole.ORGANIZER, UserRole.ADMIN),
)

data class HallUpdate(
    val name: String? = null,
    @SerializedName("capacity_threshold") val capacityThreshold: Int? = null,
    @SerializedName("allowed_roles") val allowedRoles: List<UserRole>? = null,
)

data class HallResponse(
    val id: String,
    val name: String,
    @SerializedName("capacity_threshold") val capacityThreshold: Int,
    @SerializedName("current_occupancy") val currentOccupancy: Int,
    @SerializedName("allowed_roles") val allowedRoles: List<UserRole> = emptyList(),
) {
    val occupancyRatio: Float
        get() = if (capacityThreshold <= 0) 0f
        else (currentOccupancy.toFloat() / capacityThreshold).coerceIn(0f, 1f)

    val isAtCapacity: Boolean get() = currentOccupancy >= capacityThreshold

    /** Crossing 80% is the point where stewards should start managing the door. */
    val isNearCapacity: Boolean get() = !isAtCapacity && occupancyRatio >= 0.8f
}

/** Room-finding state of a hall, from the identity-free availability endpoint. */
enum class HallState {
    @SerializedName("empty") EMPTY,
    @SerializedName("available") AVAILABLE,
    @SerializedName("filling") FILLING,
    @SerializedName("full") FULL,
}

/**
 * What a team member (press) sees: where there is room, with no occupant identities. Distinct from
 * [HallResponse], which carries the operational fields staff need.
 */
data class HallAvailability(
    val id: String,
    val name: String,
    val capacity: Int,
    val occupancy: Int,
    @SerializedName("seats_free") val seatsFree: Int,
    val state: HallState,
) {
    val occupancyRatio: Float
        get() = if (capacity <= 0) 0f else (occupancy.toFloat() / capacity).coerceIn(0f, 1f)
}

/** Scan payload — the backend takes this as a JSON body, not query params. */
data class AccessScanRequest(
    @SerializedName("card_uid") val cardUid: String,
    @SerializedName("hall_id") val hallId: String,
    val action: AccessAction,
)

data class AccessLogResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("hall_id") val hallId: String,
    val action: AccessAction,
    val timestamp: String,
    val allowed: Boolean,
    val reason: String? = null,
)
