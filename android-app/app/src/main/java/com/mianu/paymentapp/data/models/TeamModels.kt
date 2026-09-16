package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

data class TeamCreate(
    val name: String,
    val capacity: Int,
)

data class TeamUpdate(
    val name: String? = null,
    val capacity: Int? = null,
)

data class TeamResponse(
    val id: String,
    val name: String,
    val capacity: Int,
    @SerializedName("current_size") val currentSize: Int = 0,
) {
    val isFull: Boolean get() = currentSize >= capacity

    /** Clamped so an over-capacity team can't overflow its progress bar. */
    val occupancyRatio: Float
        get() = if (capacity <= 0) 0f else (currentSize.toFloat() / capacity).coerceIn(0f, 1f)
}

data class NotificationCreate(
    val message: String,
)

/** Who a broadcast is aimed at. Mirrors the server's `BroadcastAudience`. */
enum class BroadcastAudience {
    @SerializedName("all") ALL,
    @SerializedName("team") TEAM,
    @SerializedName("participant") PARTICIPANT;

    val label: String
        get() = when (this) {
            ALL -> "Everyone"
            TEAM -> "A team"
            PARTICIPANT -> "One person"
        }
}

data class BroadcastCreate(
    val message: String,
    val audience: BroadcastAudience,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("recipient_id") val recipientId: String? = null,
)

data class NotificationResponse(
    val id: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("sender_name") val senderName: String? = null,
    val audience: BroadcastAudience = BroadcastAudience.TEAM,
    // Nullable since targeting was added: an event-wide message belongs to no team.
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("recipient_id") val recipientId: String? = null,
    @SerializedName("recipient_name") val recipientName: String? = null,
    val message: String,
    val timestamp: String,
    @SerializedName("recipient_count") val recipientCount: Int = 0,
) {
    /** Who this went to, for the sent-log row. */
    val audienceLabel: String
        get() = when (audience) {
            BroadcastAudience.ALL -> "Everyone"
            BroadcastAudience.TEAM -> teamName ?: "Team"
            BroadcastAudience.PARTICIPANT -> recipientName ?: "Direct"
        }
}
