package com.example.nougatbora

data class RegisterRequest(
    val name : String,
    val email : String,
    val password : String,
    val role : String,
    val phoneNumber: String
)
