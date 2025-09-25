package com.example.nougatbora

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
        // token is passed here as raw token, but Api requires "Bearer ..." so:
        return api.getDriverById(id, "Bearer $token")

}
}
