package com.example.nougatbora

import com.google.gson.annotations.SerializedName

data class UserResponse(
        @SerializedName("id") val UserId: String,
        val name: String,
        val email: String,
        val role: String       // e.g. "driver" or "store"
    )