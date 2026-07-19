package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.*
import kotlinx.coroutines.flow.Flow

interface IConfigRepository {
    fun getItems(collection: String): Flow<List<ConfigItem>>
    suspend fun saveItem(collection: String, item: ConfigItem): Result<Unit>
    suspend fun deleteItem(collection: String, id: String): Result<Unit>
    fun getSubscriptionPlans(): Flow<List<SubscriptionPlan>>
    fun getHomeSections(): Flow<List<HomeSection>>
    fun getAppInfo(): Flow<AppInformation>
    suspend fun updateAppInfo(info: AppInformation): Result<Unit>
}
