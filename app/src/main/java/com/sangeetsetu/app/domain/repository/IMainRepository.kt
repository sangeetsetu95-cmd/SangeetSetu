package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.*
import kotlinx.coroutines.flow.Flow

interface IMainRepository {
    fun getCategoriesFlow(): Flow<List<Category>>
    fun getArtistsFlow(category: String = "All", state: String? = null, district: String? = null): Flow<List<User>>
    fun getVipArtistsFlow(): Flow<List<User>>
    fun getBannersFlow(): Flow<List<Banner>>
    fun getAllBannersFlow(): Flow<List<Banner>>
    fun getOffersFlow(): Flow<List<Offer>>
    fun getServicesFlow(): Flow<List<Service>>
    fun getHomeSectionsFlow(): Flow<List<HomeSection>>
    fun getUpcomingEventsFlow(): Flow<List<Event>>
    fun getReviewsFlow(): Flow<List<Review>>
    fun getBadgesFlow(): Flow<List<BadgeConfig>>
    fun getArtistsByIdsFlow(ids: List<String>): Flow<List<User>>
    fun getSystemSettingsFlow(): Flow<SystemSettings>
    fun getNavigationItemsFlow(): Flow<List<NavigationItem>>
    fun getPopUpsFlow(): Flow<List<PopUpConfig>>
    fun getTranslationsFlow(): Flow<List<Translation>>
    fun getLiveTelecastsFlow(): Flow<List<LiveTelecast>>
    fun getAnnouncementsFlow(): Flow<List<Announcement>>
    fun getAllAnnouncementsFlow(): Flow<List<Announcement>>
}
