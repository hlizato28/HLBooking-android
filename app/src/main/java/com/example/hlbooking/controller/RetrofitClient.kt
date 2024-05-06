package com.example.hlbooking.controller

import com.example.hlbooking.util.GsonUtil
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Membuat instance HttpLoggingInterceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Konfigurasi OkHttpClient
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()


    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(GsonUtil.gson))
        .client(httpClient)
        .build()

    val authController: AuthController by lazy {
        retrofit.create(AuthController::class.java)
    }

    val userController: UserController by lazy {
        retrofit.create(UserController::class.java)
    }

    val bookingController: BookingController by lazy {
        retrofit.create(BookingController::class.java)
    }
}
