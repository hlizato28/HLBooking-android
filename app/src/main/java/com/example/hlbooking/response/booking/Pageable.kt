package com.example.hlbooking.response.booking

data class Pageable(
    val sort: Sort,
    val offset: Int,
    val pageSize: Int,
    val pageNumber: Int,
    val paged: Boolean,
    val unpaged: Boolean
)
