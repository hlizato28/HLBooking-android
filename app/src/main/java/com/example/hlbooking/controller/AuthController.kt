package com.example.hlbooking.controller

import com.example.hlbooking.model.LoginDTO
import com.example.hlbooking.model.RegisDTO
import com.example.hlbooking.model.VerifyDTO
import com.example.hlbooking.response.AuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthController {
    @POST("api/auth/login")
    fun loginUser(
        @Body loginDTO: LoginDTO
    ): Call<AuthResponse>

    @POST("api/auth/registration")
    fun registerUser(
        @Body regisDTO: RegisDTO
    ): Call<AuthResponse>

    @POST("api/auth/registration/verify")
    fun verifyAccount(
        @Body verifyDTO: VerifyDTO
    ): Call<AuthResponse>
}