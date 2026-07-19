package com.sangeetsetu.app.util

import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.sangeetsetu.app.repository.CloudinaryResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * CloudinaryManager - Centralized utility for all media uploads in Sangeet Setu.
 * Handles retries, logging, and returns CloudinaryResource (URL + Public ID).
 */
object CloudinaryManager {
    private const val TAG = "CloudinaryManager"
    private const val MAX_RETRIES = 3

    /**
     * Uploads an image to Cloudinary with automatic retries.
     * @param uri The local Uri of the image.
     * @param folder The destination folder in Cloudinary (e.g., "profiles", "banners").
     * @param fileName Optional custom filename.
     * @return Result containing CloudinaryResource on success.
     */
    suspend fun uploadMedia(
        uri: Uri,
        folder: String,
        resourceType: String = "image",
        fileName: String = "media_${System.currentTimeMillis()}"
    ): Result<CloudinaryResource> {
        var lastException: Exception? = null

        repeat(MAX_RETRIES) { attempt ->
            try {
                Log.d(TAG, "Starting Upload Attempt ${attempt + 1} for folder: $folder, type: $resourceType")
                
                val result = performUpload(uri, folder, fileName, resourceType)
                if (result.isSuccess) {
                    Log.i(TAG, "Upload SUCCESS on attempt ${attempt + 1}. URL: ${result.getOrNull()?.url}")
                    return result
                } else {
                    lastException = result.exceptionOrNull() as? Exception
                }
            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Exception on attempt ${attempt + 1}: ${e.message}")
            }

            if (attempt < MAX_RETRIES - 1) {
                val waitTime = (attempt + 1) * 2000L
                Log.d(TAG, "Retrying in ${waitTime}ms...")
                delay(waitTime)
            }
        }

        Log.e(TAG, "All $MAX_RETRIES upload attempts failed.")
        return Result.failure(lastException ?: Exception("Cloudinary upload failed after $MAX_RETRIES attempts"))
    }

    private suspend fun performUpload(
        uri: Uri,
        folder: String,
        fileName: String,
        resourceType: String = "image"
    ): Result<CloudinaryResource> = suspendCancellableCoroutine { continuation ->
        
        Log.d(TAG, "Executing MediaManager.upload for $uri as $resourceType")
        
        MediaManager.get().upload(uri)
            .option("folder", folder)
            .option("public_id", fileName.substringBeforeLast("."))
            .option("resource_type", resourceType)
            .unsigned("sangeetsetu_upload") // Ensure this preset is in Cloudinary Settings
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d(TAG, "Cloudinary callback: onStart - $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // Optional: Track progress
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    val publicId = resultData["public_id"] as? String

                    if (secureUrl != null && publicId != null) {
                        if (continuation.isActive) {
                            continuation.resume(Result.success(CloudinaryResource(secureUrl, publicId)))
                        }
                    } else {
                        if (continuation.isActive) {
                            continuation.resume(Result.failure(Exception("Cloudinary response missing data")))
                        }
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e(TAG, "Cloudinary callback: onError - ${error.description}")
                    if (continuation.isActive) {
                        continuation.resume(Result.failure(Exception(error.description)))
                    }
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w(TAG, "Cloudinary callback: onReschedule")
                }
            })
            .dispatch()
    }
}
