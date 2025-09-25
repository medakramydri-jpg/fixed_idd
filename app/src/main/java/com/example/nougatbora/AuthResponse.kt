package com.example.nougatbora

data class AuthResponse(
    val token: String,      // Optional: might be null if login fails
    val user: UserResponse,
    val driver: DriverResponse? = null // in case role equal driver
)
