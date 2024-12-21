package com.rrr.apprrre.models

data class Comment(
    val id: String = "",
    val text: String = "",
    val author: String = "",
    val userId: String = "",
    val rating: Double = 0.0,
    val totalRatings: Int = 0,
    val ratedBy: List<String> = emptyList()
)