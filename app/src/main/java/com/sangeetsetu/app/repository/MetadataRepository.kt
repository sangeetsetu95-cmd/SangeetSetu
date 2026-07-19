package com.sangeetsetu.app.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object MetadataRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun getCategories(): List<String> = getListFromFirestore("metadata", "categories", "values")
    suspend fun getSpecialities(): List<String> = getListFromFirestore("metadata", "specialities", "values")
    suspend fun getLanguages(): List<String> = getListFromFirestore("metadata", "languages", "values")
    suspend fun getExperienceLevels(): List<String> = getListFromFirestore("metadata", "experienceLevels", "values")

    suspend fun getStates(): List<String> = getListFromFirestore("metadata", "locations", "states")
    suspend fun getDistricts(state: String): List<String> {
        return try {
            val doc = db.collection("metadata").document("districts").get().await()
            val districtsMap = doc.get(state) as? List<String>
            districtsMap ?: emptyList()
        } catch (e: Exception) {
            Log.e("MetadataRepository", "Error fetching districts for $state", e)
            emptyList()
        }
    }

    private suspend fun getListFromFirestore(collection: String, document: String, field: String): List<String> {
        return try {
            val doc = db.collection(collection).document(document).get().await()
            val list = doc.get(field) as? List<String>
            list ?: emptyList()
        } catch (e: Exception) {
            Log.e("MetadataRepository", "Error fetching $field from $collection/$document", e)
            emptyList()
        }
    }
}
