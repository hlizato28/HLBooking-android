package com.example.hlbooking.response.error

data class RegisterSubError(
    val field: String,
    val message: String,
    val rejectedValue: String
)
