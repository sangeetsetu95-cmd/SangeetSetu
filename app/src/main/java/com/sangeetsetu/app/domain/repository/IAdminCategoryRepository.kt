package com.sangeetsetu.app.domain.repository

import android.net.Uri
import com.sangeetsetu.app.model.Category

interface IAdminCategoryRepository {
    suspend fun addCategory(name: String, imageUri: Uri): Result<Unit>
    suspend fun updateCategory(category: Category, newImageUri: Uri?): Result<Unit>
    suspend fun deleteCategory(category: Category): Result<Unit>
}
