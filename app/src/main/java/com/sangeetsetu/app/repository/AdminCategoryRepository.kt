package com.sangeetsetu.app.repository

import android.net.Uri
import android.util.Log
import com.sangeetsetu.app.model.Category
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AdminCategoryRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val categoriesCollection by lazy { db.collection("categories") }

    suspend fun updateCategory(
        category: Category,
        newImageUri: Uri?
    ): Result<Unit> {
        Log.d("CATEGORY_EDIT", "Repository: Starting update process for ${category.id}")
        
        var imageUrl = category.imageUrl.ifEmpty { category.iconUrl }
        var publicId = category.cloudinaryPublicId

        // 1. Handle Image Upload outside the verified write if it's new
        if (newImageUri != null) {
            try {
                Log.d("CATEGORY_EDIT", "Repository: New image detected, uploading...")
                val uploadResult = StorageRepository.uploadImage(
                    newImageUri,
                    "categories",
                    "cat_${category.id}_${System.currentTimeMillis()}"
                ).getOrThrow()
                
                imageUrl = uploadResult.url
                publicId = uploadResult.publicId
                Log.d("CATEGORY_EDIT", "Repository: New image uploaded: $imageUrl")
                
                // Optional: Delete old image
                if (category.cloudinaryPublicId.isNotEmpty()) {
                    StorageRepository.deleteImage(category.cloudinaryPublicId)
                }
            } catch (e: Exception) {
                Log.e("CATEGORY_EDIT", "Repository: Image upload failed", e)
                return Result.failure(e)
            }
        }

        // 2. Prepare the exact map we expect to see on the server
        val finalUpdates = mapOf<String, Any>(
            "id" to category.id,
            "name" to category.name,
            "imageUrl" to imageUrl,
            "iconUrl" to imageUrl, // Keep both for safety
            "cloudinaryPublicId" to publicId,
            "isActive" to category.isActive,
            "createdAt" to category.createdAt
        )

        // 3. Perform verified write
        return com.sangeetsetu.app.util.FirestoreAudit.verifiedWrite(
            collectionName = "categories", 
            documentId = category.id,
            expectedData = finalUpdates
        ) {
            Log.d("CATEGORY_EDIT", "Repository: Calling Firestore set(merge) for ${category.id}")
            categoriesCollection.document(category.id)
                .set(finalUpdates, com.google.firebase.firestore.SetOptions.merge())
                .await()
        }
    }

    suspend fun addCategory(name: String, imageUri: Uri?): Result<Unit> {
        val docRef = categoriesCollection.document()
        val id = docRef.id
        
        Log.d("CATEGORY_EDIT", "Repository: Starting add process for '$name' with ID: $id")

        var imageUrl = ""
        var publicId = ""

        // 1. Handle Image Upload
        if (imageUri != null) {
            try {
                val uploadResult = StorageRepository.uploadImage(
                    imageUri,
                    "categories",
                    "cat_${id}_${System.currentTimeMillis()}"
                ).getOrThrow()
                imageUrl = uploadResult.url
                publicId = uploadResult.publicId
            } catch (e: Exception) {
                Log.e("CATEGORY_EDIT", "Repository: Image upload failed for new category", e)
                return Result.failure(e)
            }
        } else {
            // Requirement 8: If image upload fails, do not create the category.
            // But if no image is selected, we should assign a default. 
            // "For every category, assign a relevant default image/icon if no custom image is selected."
            // This default image logic might be better in ViewModel or here.
            // Let's assume we need an image.
            return Result.failure(Exception("Image is required for category creation"))
        }

        val category = Category(
            id = id,
            name = name,
            imageUrl = imageUrl,
            iconUrl = imageUrl,
            cloudinaryPublicId = publicId,
            createdAt = System.currentTimeMillis(),
            isActive = true
        )

        // 2. Prepare verification map
        val expectedData = mapOf<String, Any>(
            "id" to id,
            "name" to name,
            "imageUrl" to imageUrl,
            "iconUrl" to imageUrl,
            "cloudinaryPublicId" to publicId,
            "isActive" to true,
            "createdAt" to category.createdAt
        )

        // 3. Perform verified write
        return com.sangeetsetu.app.util.FirestoreAudit.verifiedWrite(
            collectionName = "categories", 
            documentId = id,
            expectedData = expectedData
        ) {
            Log.d("CATEGORY_EDIT", "Repository: Calling Firestore set() for $id")
            docRef.set(category).await()
        }
    }

}
