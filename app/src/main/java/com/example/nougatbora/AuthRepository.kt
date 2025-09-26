package com.example.nougatbora

import android.util.Log

class AuthRepository(private val api: AuthApi) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        return api.register(request)
    }

    suspend fun login(request: LoginRequest): AuthResponse {
        return api.login(request)
    }

    suspend fun logout(token: String) {
        api.logout("Bearer $token")
    }

    suspend fun getDriverById(id: String, token: String): DriverResponse {
        Log.d("AuthRepository", "=== MAKING API CALL ===")
        Log.d("AuthRepository", "Driver ID: '$id'")
        Log.d("AuthRepository", "Token: Bearer ${token.take(20)}...")

        return try {
            val response = api.getDriverById(id, "Bearer $token")
            Log.d("AuthRepository", "✅ API call successful")
            response
        } catch (e: Exception) {
            Log.e("AuthRepository", "❌ API call failed", e)
            throw e
        }
    }
}