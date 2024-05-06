package com.example.hlbooking.response.error

data class LoginError(
    val path: String?,
    val data: String?,
    val success: Boolean,
    val errorCode: String,
    val message: String,
    val status: Int
)
