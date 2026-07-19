package com.sangeetsetu.app.data.repository

import android.net.Uri
import android.util.Log
import com.sangeetsetu.app.domain.repository.IAdminCategoryRepository
import com.sangeetsetu.app.domain.repository.IStorageRepository
import com.sangeetsetu.app.model.Category
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminCategoryRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore,
    private val storageRepository: IStorageRepository
) : IAdminCategoryRepository {

    override suspend fun addCategory(name: String, imageUri: Uri): Result<Unit> {
        return try {
            val fileName = "cat_${System.currentTimeMillis()}"
            val uploadResult = storageRepository.uploadImage(imageUri, "categories", fileName)
            
            uploadResult.fold(
                onSuccess = { resource ->
                    val docRef = db.collection("categories").document()
                    val category = Category(
                        id = docRef.id,
                        name = name,
                        imageUrl = resource.url,
                        iconUrl = resource.url,
                        cloudinaryPublicId = resource.publicId,
                        isActive = true,
                        createdAt = System.currentTimeMillis()
                    )
                    
                    Log.d("AdminCategoryRepo", "Writing category to Firestore: ${docRef.id}")
                    FirestoreAudit.verifiedWrite("categories", docRef.id) {
                        docRef.set(category).await()
                    }
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(category: Category, newImageUri: Uri?): Result<Unit> {
        return try {
            var updatedCategory = category
            
            if (newImageUri != null) {
                // Delete old image if it exists
                if (category.cloudinaryPublicId.isNotEmpty()) {
                    storageRepository.deleteImage(category.cloudinaryPublicId)
                }
                
                val fileName = "cat_${System.currentTimeMillis()}"
                val uploadResult = storageRepository.uploadImage(newImageUri, "categories", fileName)
                
                uploadResult.onSuccess { resource ->
                    updatedCategory = category.copy(
                        imageUrl = resource.url,
                        iconUrl = resource.url,
                        cloudinaryPublicId = resource.publicId
                    )
                }.onFailure { return Result.failure(it) }
            }
            
            FirestoreAudit.verifiedWrite("categories", category.id) {
                db.collection("categories").document(category.id).set(updatedCategory).await()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            if (category.cloudinaryPublicId.isNotEmpty()) {
                storageRepository.deleteImage(category.cloudinaryPublicId)
            }
            
            FirestoreAudit.verifiedDelete("categories", category.id) {
                db.collection("categories").document(category.id).delete().await()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
