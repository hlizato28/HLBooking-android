package com.example.hlbooking.response

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
)