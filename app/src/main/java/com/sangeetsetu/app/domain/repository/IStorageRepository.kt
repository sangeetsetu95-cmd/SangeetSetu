package com.sangeetsetu.app.domain.repository

import android.net.Uri
import com.sangeetsetu.app.domain.model.CloudinaryResource

interface IStorageRepository {
    suspend fun uploadImage(uri: Uri, folder: String, fileName: String): Result<CloudinaryResource>
    suspend fun uploadMedia(uri: Uri, folder: String, fileName: String, resourceType: String = "image"): Result<CloudinaryResource>
    suspend fun deleteImage(publicId: String): Result<Unit>
}
