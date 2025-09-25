package com.example.nougatbora

import com.google.gson.annotations.SerializedName

data class DriverResponse(
    @SerializedName("id") val UserId: String,
    val name: String,
    val email: String,
    val products: List<ProductResponse>
)

