package com.example.hlbooking.response.booking

data class BookingResponse<T>(
    val content: List<T>,
    val pageable: Pageable,
    val last: Boolean,
    val totalPages: Int,
    val totalElements: Int,
    val size: Int,
    val number: Int,
    val numberOfElements: Int
)








