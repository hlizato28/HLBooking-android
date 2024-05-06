package com.example.hlbooking.response.error


data class RegisterError(
    val serverResponse: String,
    val status: Int,
    val message: String,
    val path: String,
    val registerSubErrors: List<RegisterSubError>,
    val errorCode: String
)
