package com.mianu.paymentapp.data.models

import com.google.gson.annotations.SerializedName

enum class MealType {
    @SerializedName("breakfast") BREAKFAST,
    @SerializedName("lunch") LUNCH
}

data class MealSwipeRequest(
    @SerializedName("card_uid") val cardUid: String,
    @SerializedName("meal_type") val mealType: MealType
)

data class MealSwipeResponse(
    val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("meal_type") val mealType: MealType,
    @SerializedName("swipe_timestamp") val swipeTimestamp: String,
    @SerializedName("meals_remaining") val mealsRemaining: Int? = null,
    @SerializedName("user_name") val userName: String? = null,
)
