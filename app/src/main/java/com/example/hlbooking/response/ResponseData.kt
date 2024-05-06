package com.example.hlbooking.response

data class ResponseData<T>(
    val data: T,
    val success: Boolean,
    val message: String,
    val status: Int,
    val timestamp: Long
)
