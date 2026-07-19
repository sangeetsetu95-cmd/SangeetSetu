package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.AdminLog
import kotlinx.coroutines.flow.Flow

interface ISystemLogRepository {
    fun getRecentLogsFlow(limit: Int = 20): Flow<List<AdminLog>>
    suspend fun logAction(action: String): Result<Unit>
}
