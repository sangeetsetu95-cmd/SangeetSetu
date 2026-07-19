package com.sangeetsetu.app.repository

import android.net.Uri
import android.util.Log
import com.sangeetsetu.app.util.CloudinaryManager

data class CloudinaryResource(val url: String, val publicId: String)

/**
 * StorageRepository - Proxy for CloudinaryManager to maintain backward compatibility
 * and handle specific business logic like deletions.
 */
object StorageRepository {
    private const val TAG = "StorageRepo"

    /**
     * Uploads a media file (image, video, audio) using the centralized CloudinaryManager.
     */
    suspend fun uploadMedia(uri: Uri, folder: String, fileName: String, resourceType: String = "image"): Result<CloudinaryResource> {
        Log.d(TAG, "StorageRepository.uploadMedia called for $folder/$fileName type: $resourceType")
        return CloudinaryManager.uploadMedia(uri, folder, resourceType, fileName)
    }

    /**
     * Uploads an image using the centralized CloudinaryManager.
     */
    suspend fun uploadImage(uri: Uri, folder: String, fileName: String): Result<CloudinaryResource> {
        return uploadMedia(uri, folder, fileName, "image")
    }

    /**
     * Delete image - Note: Unsigned uploads don't support client-side deletion for security.
     * This usually requires a signed request or a backend implementation.
     */
    suspend fun deleteImage(publicId: String): Result<Unit> {
        Log.d(TAG, "Deletion requested for publicId: $publicId. (Client-side deletion limited in unsigned setups)")
        // Implement signed deletion here if API Key/Secret are available safely, 
        // or trigger a Cloud Function.
        return Result.success(Unit)
    }
}
