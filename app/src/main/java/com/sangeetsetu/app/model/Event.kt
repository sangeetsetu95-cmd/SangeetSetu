package com.sangeetsetu.app.model

data class Event(
    val id: String = "",
    val title: String = "",
    val artistId: String = "",
    val artistName: String = "",
    val location: String = "",
    val date: String = "", // e.g., "25 May 2024"
    val day: String = "", // e.g., "25"
    val month: String = "", // e.g., "मई"
    val imageUrl: String = "",
    val description: String = "",
    val status: String = "Upcoming",
    val createdAt: Long = System.currentTimeMillis()
)
