package com.example.hlbooking.model

import java.time.LocalDate
import java.time.LocalTime

data class BookingDTO(
    val idBooking: Long?,
    val namaLapangan: String,
    val tanggal: LocalDate,
    val jamMulaiBooking: LocalTime,
    val jamSelesaiBooking: LocalTime,
    val member: Long?,
    val guest: Long?
)
