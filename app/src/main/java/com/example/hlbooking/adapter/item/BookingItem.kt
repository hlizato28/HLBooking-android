package com.example.hlbooking.adapter.item

data class BookingItem(
    @Transient
    val bookingId: Long?,

    val namaLapangan: String,
    val tanggal: String,
    val jamBooking: String
)
