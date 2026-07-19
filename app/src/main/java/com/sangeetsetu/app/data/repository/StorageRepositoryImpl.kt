package com.sangeetsetu.app.data.repository

import android.net.Uri
import android.util.Log
import com.sangeetsetu.app.domain.model.CloudinaryResource
import com.sangeetsetu.app.domain.repository.IStorageRepository
import com.sangeetsetu.app.util.CloudinaryManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor() : IStorageRepository {
    companion object {
        private const val TAG = "StorageRepo"
    }

    override suspend fun uploadMedia(uri: Uri, folder: String, fileName: String, resourceType: String): Result<CloudinaryResource> {
        Log.d(TAG, "StorageRepository.uploadMedia called for $folder/$fileName type: $resourceType")
        return CloudinaryManager.uploadMedia(uri, folder, resourceType, fileName).map { 
            CloudinaryResource(it.url, it.publicId)
        }
    }

    override suspend fun uploadImage(uri: Uri, folder: String, fileName: String): Result<CloudinaryResource> {
        return uploadMedia(uri, folder, fileName, "image")
    }

    override suspend fun deleteImage(publicId: String): Result<Unit> {
        Log.d(TAG, "Deletion requested for publicId: $publicId. (Client-side deletion limited in unsigned setups)")
        return Result.success(Unit)
    }
}
