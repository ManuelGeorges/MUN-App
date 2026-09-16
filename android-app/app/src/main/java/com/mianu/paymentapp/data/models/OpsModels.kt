package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

/**
 * Models for the operational endpoints — analytics, reports and settings. These had no client
 * representation before, so the corresponding backend routes were unreachable from the app.
 */

data class AnalyticsSummary(
    val metric: String,
    val total: Int,
    @SerializedName("generated_at") val generatedAt: String,
) {
    /** `meals_today` -> `Meals Today`, for direct display. */
    val metricLabel: String
        get() = metric.split("_", "-")
            .filter { it.isNotBlank() }
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
}

data class ReportSummary(
    @SerializedName("total_records") val totalRecords: Int,
    @SerializedName("generated_at") val generatedAt: String,
)

data class MealWindowRequest(
    @SerializedName("breakfast_start") val breakfastStart: String,
    @SerializedName("breakfast_end") val breakfastEnd: String,
    @SerializedName("lunch_start") val lunchStart: String,
    @SerializedName("lunch_end") val lunchEnd: String,
) {
    companion object {
        /** Matches the backend's own defaults, so the settings form opens pre-filled. */
        val Default = MealWindowRequest(
            breakfastStart = "07:00",
            breakfastEnd = "10:00",
            lunchStart = "12:00",
            lunchEnd = "15:00",
        )
    }
}

data class DelegateLocation(
    @SerializedName("user_id") val userId: String,
    val name: String,
    val role: String,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("card_uid") val cardUid: String? = null,
    @SerializedName("current_hall_id") val currentHallId: String? = null,
    @SerializedName("current_hall_name") val currentHallName: String? = null,
    val status: String, // "inside", "exited", "never_scanned"
    @SerializedName("last_action") val lastAction: String? = null,
    @SerializedName("last_seen") val lastSeen: String? = null,
    @SerializedName("attendance_status") val attendanceStatus: String? = "absent",
    @SerializedName("attendance_time") val attendanceTime: String? = null,
    @SerializedName("attendance_method") val attendanceMethod: String? = null,
) {
    val isInside: Boolean get() = status == "inside"
    val isExited: Boolean get() = status == "exited"
    val isNeverScanned: Boolean get() = status == "never_scanned"
    val isPresent: Boolean get() = attendanceStatus == "present"

    val statusDisplay: String
        get() = when (status) {
            "inside" -> currentHallName ?: "Inside Hall"
            "exited" -> "Exited"
            else -> "Not Checked In"
        }
}

data class LiveScanEvent(
    val id: String,
    @SerializedName("scan_type") val scanType: String, // "access_entry", "access_exit", "meal_swipe"
    @SerializedName("delegate_id") val delegateId: String,
    @SerializedName("delegate_name") val delegateName: String,
    @SerializedName("delegate_role") val delegateRole: String,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("location_or_service") val locationOrService: String,
    val allowed: Boolean,
    val reason: String? = null,
    val timestamp: String,
    @SerializedName("meals_remaining") val mealsRemaining: Int? = null,
)

// region Conference Attendance Models

data class AttendanceSummary(
    @SerializedName("total_registered") val totalRegistered: Int = 0,
    @SerializedName("total_present") val totalPresent: Int = 0,
    @SerializedName("total_absent") val totalAbsent: Int = 0,
    @SerializedName("attendance_rate") val attendanceRate: Int = 0,
    @SerializedName("total_swipes") val totalSwipes: Int = 0,
)

data class AttendeeRecord(
    @SerializedName("user_id") val userId: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val role: String,
    @SerializedName("team_id") val teamId: String? = null,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("card_uid") val cardUid: String? = null,
    val status: String = "absent",
    @SerializedName("last_check_in") val lastCheckIn: String? = null,
    @SerializedName("last_check_out") val lastCheckOut: String? = null,
    val method: String = "none",
    @SerializedName("updated_at") val updatedAt: String? = null,
) {
    val isPresent: Boolean get() = status == "present"
}

data class AttendanceResponse(
    val summary: AttendanceSummary,
    val attendees: List<AttendeeRecord> = emptyList(),
)

data class AttendanceSwipeRequest(
    @SerializedName("card_uid") val cardUid: String,
    val action: String = "check_in",
)

data class AttendanceSwipeUser(
    val id: String,
    val name: String,
    val role: String,
    @SerializedName("team_name") val teamName: String? = null,
    @SerializedName("card_uid") val cardUid: String,
    val status: String,
    val timestamp: String,
    val action: String,
)

data class AttendanceSwipeResponse(
    val success: Boolean,
    val user: AttendanceSwipeUser,
    val summary: AttendanceSummary,
)

data class SingleAttendanceRequest(
    @SerializedName("user_id") val userId: String,
    val status: String,
)

data class SingleAttendanceResponse(
    val success: Boolean,
    @SerializedName("user_id") val userId: String,
    val name: String,
    val status: String,
    val timestamp: String,
)

data class BulkAttendanceRequest(
    @SerializedName("user_ids") val userIds: List<String>? = null,
    val all: Boolean? = null,
    val status: String,
)

data class BulkAttendanceResponse(
    val success: Boolean,
    val count: Int,
    val status: String,
    val timestamp: String,
)

// endregion

