package com.example.nougatbora

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest ): AuthResponse

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/auth/logout")
    suspend fun logout(@Header("Authorization") token: String ): Void

    @GET("api/drivers/{id}")
    suspend fun getDriverById(
        @Path("id") id: String,
        @Header("Authorization") token: String
    ): DriverResponse

}