package com.sangeetsetu.app.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ProfileSetup : Screen("profile_setup")
    object ArtistRegistration : Screen("artist_registration")
    object ArtistDashboard : Screen("artist_dashboard")
    object OrganizerDashboard : Screen("organizer_dashboard")

    // Main
    object Home : Screen("home")
    object AllServices : Screen("all_services")
    object Categories : Screen("categories")
    object ArtistList : Screen("artist_list/{categoryId}") {
        fun createRoute(categoryId: String) = "artist_list/$categoryId"
    }
    object ArtistDetails : Screen("artist_details/{artistId}") {
        fun createRoute(artistId: String) = "artist_details/$artistId"
        val deepLinkUri = "sangeetsetu://artist/{artistId}"
    }
    object Booking : Screen("booking/{artistId}") {
        fun createRoute(artistId: String) = if (artistId.isEmpty()) "booking/all" else "booking/$artistId"
    }
    object MyBookings : Screen("my_bookings")
    object Chat : Screen("chat")
    object MessageDetail : Screen("message_detail/{chatId}/{receiverId}") {
        fun createRoute(chatId: String, receiverId: String) = "message_detail/$chatId/$receiverId"
    }
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Settings : Screen("settings")
    object Language : Screen("language")
    object Theme : Screen("theme")
    object Notifications : Screen("notifications")
    object Announcements : Screen("announcements")
    object Search : Screen("search?query={query}") {
        fun createRoute(query: String = "") = "search?query=$query"
    }
    object AISupport : Screen("ai_support")
    object Subscription : Screen("subscription")
    object MyPayments : Screen("my_payments")
    object SavedArtists : Screen("saved_artists")
    object Addresses : Screen("addresses")
    object About : Screen("about")
    object ContactUs : Screen("contact_us")
    object ChangePassword : Screen("change_password")
    object Donation : Screen("donation")
    object AllEvents : Screen("all_events")
    object EventDetails : Screen("event_details/{eventId}") {
        fun createRoute(eventId: String) = "event_details/$eventId"
    }

    // Admin
    object AdminDashboard : Screen("admin_dashboard")
    object AdminUserList : Screen("admin_user_list")
    object AdminArtistList : Screen("admin_artist_list")
    object AdminArtistDetails : Screen("admin_artist_details/{artistId}") {
        fun createRoute(artistId: String) = "admin_artist_details/$artistId"
    }
    object AdminBroadcast : Screen("admin_broadcast")
    object ManageLive : Screen("manage_live")
    object AdminCategories : Screen("admin_categories")
    object AdminInstruments : Screen("admin_instruments")
    object AdminCities : Screen("admin_cities")
    object AdminBookings : Screen("admin_bookings")
    object AdminPayments : Screen("admin_payments")
    object AdminUnlockPayments : Screen("admin_unlock_payments")
    object AdminSubscriptions : Screen("admin_subscriptions")
    object AdminOffers : Screen("admin_offers")
    object AdminReviews : Screen("admin_reviews")
    object AdminNotifications : Screen("admin_notifications")
    object ManageReports : Screen("manage_reports")
    object Banners : Screen("banners")
    object AdminLogs : Screen("admin_logs")
    object AdminAnalytics : Screen("admin_analytics")
    object AdminProfile : Screen("admin_profile")
    object AdminBhajans : Screen("admin_bhajans")
    object AdminRequests : Screen("admin_requests")
    object AdminRBAC : Screen("admin_rbac")
    object AdminMore : Screen("admin_more")
    object AdminServices : Screen("admin_services")
    object AdminHomeSections : Screen("admin_home_sections")
    object AdminBadgeManagement : Screen("admin_badge_management")
    object AdminCategoryArtists : Screen("admin_category_artists/{categoryId}") {
        fun createRoute(categoryId: String) = "admin_category_artists/$categoryId"
    }
    object AdminFormBuilder : Screen("admin_form_builder")
    object DynamicRegistration : Screen("dynamic_registration")
    object AdminConfig : Screen("admin_config/{collection}/{title}") {
        fun createRoute(collection: String, title: String) = "admin_config/$collection/$title"
    }
    object AdminAppSettings : Screen("admin_app_settings")
    object AdminChatControl : Screen("admin_chat_control")
}
