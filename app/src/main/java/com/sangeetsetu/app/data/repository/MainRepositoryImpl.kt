package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.IMainRepository
import com.sangeetsetu.app.domain.repository.IUserRepository

import com.sangeetsetu.app.model.Announcement
import com.sangeetsetu.app.model.BadgeConfig
import com.sangeetsetu.app.model.Banner
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.model.Event
import com.sangeetsetu.app.model.HomeSection
import com.sangeetsetu.app.model.LiveTelecast
import com.sangeetsetu.app.model.NavigationItem
import com.sangeetsetu.app.model.Offer
import com.sangeetsetu.app.model.PopUpConfig
import com.sangeetsetu.app.model.Review
import com.sangeetsetu.app.model.Service
import com.sangeetsetu.app.model.SystemSettings
import com.sangeetsetu.app.model.Translation
import com.sangeetsetu.app.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val userRepository: IUserRepository
) : IMainRepository {

    override fun getCategoriesFlow(): Flow<List<Category>> = callbackFlow {
        val subscription = db.collection("categories")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                }?.sortedBy { it.name } ?: emptyList()
                trySend(categories)
            }
        awaitClose { subscription.remove() }
    }

    override fun getArtistsFlow(category: String, state: String?, district: String?): Flow<List<User>> = callbackFlow {
        val cleanCategoryId = category.trim()
        Log.d("FirestoreQuery", "Starting fetchArtists for Category: '$cleanCategoryId', State: $state, District: $district")
        
        var query: Query = db.collection("users")
            .whereEqualTo("userType", "Artist")
            // We use accountStatus as the primary filter to match Admin Panel's active artists
            .whereEqualTo("accountStatus", "ACTIVE")

        if (cleanCategoryId != "All" && cleanCategoryId != "trending" && cleanCategoryId.isNotEmpty()) {
            // We'll query by categoryId primarily. 
            // If the passed value is a legacy name, the local filter will catch it.
            query = query.whereEqualTo("categoryId", cleanCategoryId)
            Log.d("FirestoreQuery", "Applied categoryId filter: $cleanCategoryId")
        }

        val subscription = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirestoreQuery", "Firestore Query Error: ${error.message}", error)
                if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                    Log.w("FirestoreQuery", "Index missing, falling back to local filtering")
                    db.collection("users")
                        .whereEqualTo("userType", "Artist")
                        .get()
                        .addOnSuccessListener { s ->
                            val artists = s.toObjects(User::class.java)
                            val filtered = applyLocalFilters(artists, cleanCategoryId, state, district)
                            Log.d("FirestoreQuery", "Fallback results: ${filtered.size} artists found")
                            trySend(filtered)
                        }
                } else {
                    trySend(emptyList())
                }
                return@addSnapshotListener
            }
            
            val artists = snapshot?.toObjects(User::class.java) ?: emptyList()
            Log.d("FirestoreQuery", "Raw artists fetched from Firestore: ${artists.size}")
            
            val finalArtists = applyLocalFilters(artists, cleanCategoryId, state, district)
            Log.d("FirestoreQuery", "Final artists returned after local filters: ${finalArtists.size}")
            trySend(finalArtists)
        }
        awaitClose { subscription.remove() }
    }

    override fun getVipArtistsFlow(): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("users")
            .whereEqualTo("userType", "Artist")
            .whereEqualTo("accountStatus", "ACTIVE")
            .whereEqualTo("isVip", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MainRepository", "Error fetching VIP artists", error)
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        db.collection("users")
                            .whereEqualTo("userType", "Artist")
                            .get()
                            .addOnSuccessListener { s ->
                                val artists = s.toObjects(User::class.java).filter { it.isVip && it.accountStatus == "ACTIVE" }
                                trySend(artists)
                            }
                    } else {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    private fun applyLocalFilters(artists: List<User>, category: String, state: String?, district: String?): List<User> {
        val cleanCat = category.trim()
        return artists.filter { artist ->
            val categoryMatch = if (cleanCat != "All" && cleanCat != "trending" && cleanCat.isNotEmpty()) {
                // Support both ID and Name matching for robust discovery
                val idMatch = artist.categoryId.trim().equals(cleanCat, ignoreCase = true)
                val nameMatch = artist.category.trim().equals(cleanCat, ignoreCase = true)
                idMatch || nameMatch
            } else true

            // Location filters (optional)
            val stateMatch = if (!state.isNullOrEmpty() && cleanCat != "All") artist.state == state else true
            val districtMatch = if (!district.isNullOrEmpty() && cleanCat != "All") artist.district == district else true
            
            categoryMatch && stateMatch && districtMatch
        }
    }

    override fun getBannersFlow(): Flow<List<Banner>> = callbackFlow {
        val subscription = db.collection("banners")
            .whereEqualTo("active", true)
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        db.collection("banners").get().addOnSuccessListener { snap ->
                            val banners = snap.toObjects(Banner::class.java)
                                .filter { it.isActive }
                                .sortedBy { it.order }
                            trySend(banners)
                        }
                    } else {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Banner::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllBannersFlow(): Flow<List<Banner>> = callbackFlow {
        val subscription = db.collection("banners")
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Banner::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getOffersFlow(): Flow<List<Offer>> = callbackFlow {
        val subscription = db.collection("offers")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Offer::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getServicesFlow(): Flow<List<Service>> = callbackFlow {
        val subscription = db.collection("services")
            .whereEqualTo("active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Service::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getHomeSectionsFlow(): Flow<List<HomeSection>> = callbackFlow {
        val subscription = db.collection("home_sections")
            .whereEqualTo("isVisible", true)
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        db.collection("home_sections").get().addOnSuccessListener { snap ->
                            val sections = snap.toObjects(HomeSection::class.java)
                                .filter { it.isVisible }
                                .sortedBy { it.displayOrder }
                            trySend(sections)
                        }
                    } else {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(HomeSection::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getUpcomingEventsFlow(): Flow<List<Event>> = callbackFlow {
        val subscription = db.collection("events")
            .whereEqualTo("status", "Upcoming")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        db.collection("events").get().addOnSuccessListener { snap ->
                            val events = snap.toObjects(Event::class.java)
                                .filter { it.status == "Upcoming" }
                                .sortedByDescending { it.createdAt }
                            trySend(events)
                        }
                    } else {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Event::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getReviewsFlow(): Flow<List<Review>> = callbackFlow {
        val subscription = db.collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Review::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getBadgesFlow(): Flow<List<BadgeConfig>> = callbackFlow {
        val subscription = db.collection("badges")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(BadgeConfig::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getArtistsByIdsFlow(ids: List<String>): Flow<List<User>> = callbackFlow {
        val uniqueIds = ids.distinct().filter { it.isNotBlank() }
        if (uniqueIds.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val subscription = db.collection("users")
            .whereIn("uid", uniqueIds.take(10))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(User::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getSystemSettingsFlow(): Flow<SystemSettings> = callbackFlow {
        val subscription = db.collection("settings").document("system")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(SystemSettings())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(SystemSettings::class.java) ?: SystemSettings())
            }
        awaitClose { subscription.remove() }
    }

    override fun getNavigationItemsFlow(): Flow<List<NavigationItem>> = callbackFlow {
        val subscription = db.collection("navigation")
            .orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(NavigationItem::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getPopUpsFlow(): Flow<List<PopUpConfig>> = callbackFlow {
        val subscription = db.collection("popups")
            .whereEqualTo("isActive", true)
            .orderBy("priority", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        db.collection("popups").get().addOnSuccessListener { snap ->
                            val popups = snap.toObjects(PopUpConfig::class.java)
                                .filter { it.isActive }
                                .sortedByDescending { it.priority }
                            trySend(popups)
                        }
                    } else {
                        trySend(emptyList())
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(PopUpConfig::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getTranslationsFlow(): Flow<List<Translation>> = callbackFlow {
        val subscription = db.collection("translations")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Translation::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getLiveTelecastsFlow(): Flow<List<LiveTelecast>> = callbackFlow {
        val subscription = db.collection("live_telecasts")
            .orderBy("startTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(LiveTelecast::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }

    override fun getAnnouncementsFlow(): Flow<List<Announcement>> = callbackFlow {
        Log.d("BroadcastDebug", "User App: Initializing real-time Announcement Flow")

        val subscription = db.collection("announcements")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("BroadcastDebug", "User App: Firestore Error fetching announcements", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                
                val allAnnouncements = try {
                    snapshot?.toObjects(Announcement::class.java) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("BroadcastDebug", "User App: Error parsing Announcements", e)
                    emptyList()
                }
                
                Log.d("BroadcastDebug", "User App: Received ${allAnnouncements.size} raw announcements from Firestore")

                val currentUid = userRepository.getCurrentUserId()
                val now = System.currentTimeMillis()

                if (currentUid == null) {
                    val publicAnnouncements = allAnnouncements.filter { 
                        it.isActive && it.expiresAt > now && (it.targetType == "ALL" || it.recipientCategories.contains("All Users"))
                    }.sortedByDescending { it.createdAt }
                    trySend(publicAnnouncements)
                } else {
                    db.collection("users").document(currentUid).get().addOnSuccessListener { userDoc ->
                        val user = userDoc.toObject(User::class.java)
                        
                        val filtered = allAnnouncements.filter { ann ->
                            if (!ann.isActive || ann.expiresAt < now) return@filter false

                            val targetMatches = when (ann.targetType) {
                                "ALL" -> true
                                "USERS" -> user?.userType == "User"
                                "ARTISTS" -> user?.userType == "Artist"
                                "VIP_USERS" -> user?.isVip == true
                                "VERIFIED_ARTISTS" -> user?.userType == "Artist" && user.verificationStatus == "VERIFIED"
                                "CATEGORY" -> user?.userType == "Artist" && (
                                    ann.targetCategories.any { it.trim().equals(user.category.trim(), ignoreCase = true) } || 
                                    ann.targetCategories.any { it.trim().equals(user.categoryId.trim(), ignoreCase = true) } ||
                                    ann.recipientCategories.any { it.trim().equals(user.category.trim(), ignoreCase = true) }
                                )
                                "LOCATION" -> user?.state == ann.targetStates.firstOrNull() || user?.district == ann.targetDistricts.firstOrNull()
                                "CUSTOM" -> ann.targetUserIds.contains(currentUid)
                                else -> {
                                    // Fallback to legacy recipientCategories
                                    ann.recipientCategories.contains("All Users") ||
                                    (ann.recipientCategories.contains("All Artists") && user?.userType == "Artist") ||
                                    (user?.userType == "Artist" && ann.recipientCategories.contains(user.category))
                                }
                            }
                            targetMatches
                        }.sortedByDescending { it.createdAt }
                        
                        trySend(filtered)
                    }.addOnFailureListener { e ->
                        val publicOnly = allAnnouncements.filter { 
                            it.isActive && it.expiresAt > now && (it.targetType == "ALL" || it.recipientCategories.contains("All Users"))
                        }.sortedByDescending { it.createdAt }
                        trySend(publicOnly)
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    override fun getAllAnnouncementsFlow(): Flow<List<Announcement>> = callbackFlow {
        val subscription = db.collection("announcements")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MainRepository", "Error fetching all announcements", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Announcement::class.java) ?: emptyList())
            }
        awaitClose { subscription.remove() }
    }
}
