package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.*
import kotlinx.coroutines.flow.Flow

interface IDynamicFormRepository {
    fun getFormFields(): Flow<List<FormField>>
    fun getUISettings(): Flow<AppUISettings>
    fun getRegistrationSettings(): Flow<RegistrationSettings>
    fun getCategories(): Flow<List<Category>>
    fun getInstruments(): Flow<List<Instrument>>
    fun getStates(): Flow<List<State>>
    fun getDistricts(stateId: String): Flow<List<District>>
    suspend fun saveFormFields(fields: List<FormField>): Result<Unit>
}
