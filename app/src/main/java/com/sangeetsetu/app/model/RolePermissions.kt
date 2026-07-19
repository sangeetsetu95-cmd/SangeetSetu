package com.sangeetsetu.app.model

data class Role(
    val name: String = "",
    val permissions: List<String> = emptyList()
)

object Permissions {
    const val ACCESS_ADMIN_PANEL = "access_admin_panel"
    const val MANAGE_USERS = "manage_users"
    const val MANAGE_ARTISTS = "manage_artists"
    const val MANAGE_ORGANIZERS = "manage_organizers"
    const val VIEW_ANALYTICS = "view_analytics"
    const val MANAGE_BOOKINGS = "manage_bookings"
    const val CREATE_BOOKING = "create_booking"
    const val ACCESS_ARTIST_DASHBOARD = "access_artist_dashboard"
    const val ACCESS_ORGANIZER_DASHBOARD = "access_organizer_dashboard"
    const val MANAGE_PROFILE = "manage_profile"
}
