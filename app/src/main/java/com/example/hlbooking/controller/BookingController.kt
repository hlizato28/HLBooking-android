package com.example.hlbooking.controller

import com.example.hlbooking.model.BookingDTO
import com.example.hlbooking.response.ResponseData
import com.example.hlbooking.response.booking.BookingResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BookingController {
    @POST("api/b/find/lapangan")
    fun findAvailableLapangan(
        @Header("Authorization") token: String,
        @Body bookingDTO: BookingDTO,
        @Query("page") page: Int,
        @Query("size") size: Int = 6
    ): Call<BookingResponse<BookingDTO>>

    @POST("api/b/create")
    fun createBooking(
        @Header("Authorization") token: String,
        @Body bookingDTO: BookingDTO
    ): Call<ResponseData<String>>

    @GET("/api/b/valid/{id}")
    fun getValidBookingById(
        @Header("Authorization") authToken: String,
        @Path("id") id: Long,
        @Query("page") page: Int,
        @Query("size") size: Int = 6
    ): Call<BookingResponse<BookingDTO>>

    @DELETE("api/b/delete/{user}/{book}")
    fun deleteBooking(
        @Header("Authorization") token: String,
        @Path("user") userId: Long,
        @Path("book") bookId: Long
    ): Call<ResponseData<String>>

}