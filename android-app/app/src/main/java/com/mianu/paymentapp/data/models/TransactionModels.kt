package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

enum class TransactionType {
    @SerializedName("deduction") DEDUCTION,
    @SerializedName("recharge") RECHARGE
}

data class TransactionRequest(
    @SerializedName("card_uid") val cardUid: String,
    val amount: Double,
    val description: String? = null,
    @SerializedName("transaction_type") val transactionType: TransactionType
)

data class TransactionResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    val amount: Double,
    @SerializedName("transaction_type") val transactionType: TransactionType,
    val description: String? = null,
    @SerializedName("created_at") val createdAt: String
)

data class DashboardOverview(
    @SerializedName("total_meals_today") val totalMealsToday: Int,
    @SerializedName("total_access_scans_today") val totalAccessScansToday: Int = 0,
    @SerializedName("active_users") val activeUsers: Int,
    @SerializedName("people_inside_halls") val peopleInsideHalls: Int = 0,
    @SerializedName("total_transactions_today") val totalTransactionsToday: Int = 0,
    @SerializedName("total_token_balance") val totalTokenBalance: Double = 0.0
)
