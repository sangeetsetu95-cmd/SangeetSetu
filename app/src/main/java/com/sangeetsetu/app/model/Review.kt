package com.sangeetsetu.app.model

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val artistId: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
