package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.Role
import com.sangeetsetu.app.model.User
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    fun getCurrentUserId(): String?
    suspend fun getUserProfile(uid: String): Result<User?>
    suspend fun saveUserProfile(user: User): Result<Unit>
    suspend fun updateProfileFields(uid: String, updates: Map<String, Any>): Result<Unit>
    fun listenToUserProfile(uid: String, onUpdate: (User?) -> Unit): ListenerRegistration?
    fun getAllUsersFlow(): Flow<List<User>>
    suspend fun getRole(roleName: String): Result<Role?>
    suspend fun getNextArtistId(): String
    suspend fun updateArtistStatusTransaction(uid: String, statusUpdates: Map<String, Any>): Result<Unit>
    suspend fun runArtistStatusMigration(): Result<Int>
    suspend fun runCategoryMigration(): Result<Int>
    fun logout()
}
