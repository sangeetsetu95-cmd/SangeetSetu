package com.sangeetsetu.app.model

data class LiveTelecast(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val thumbnailUrl: String = "",
    val youtubeLink: String = "",
    val isLive: Boolean = false,
    val startTime: Long = System.currentTimeMillis()
)
