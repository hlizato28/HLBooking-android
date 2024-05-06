package com.example.hlbooking.controller

import com.example.hlbooking.model.UserDTO
import com.example.hlbooking.response.ResponseData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface UserController {
    @GET("/api/u/{id}")
    fun getUserById(
        @Header("Authorization") authToken: String,
        @Path("id") userId: Long
    ): Call<ResponseData<UserDTO>>
}