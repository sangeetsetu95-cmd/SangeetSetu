package com.sangeetsetu.app.repository

import com.sangeetsetu.app.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object DynamicFormRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val configCollection = db.collection("app_config")
    private val fieldsCollection = db.collection("registration_fields")
    private val categoriesCollection = db.collection("categories")
    private val instrumentsCollection = db.collection("instruments")
    private val statesCollection = db.collection("states")
    private val districtsCollection = db.collection("districts")

    fun getFormFields(): Flow<List<FormField>> = callbackFlow {
        val subscription = fieldsCollection.orderBy("displayOrder", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val fields = snapshot?.toObjects(FormField::class.java) ?: emptyList()
                trySend(fields)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveFormField(field: FormField): Result<Unit> = runCatching {
        val docRef = if (field.id.isEmpty()) fieldsCollection.document() else fieldsCollection.document(field.id)
        val finalField = if (field.id.isEmpty()) field.copy(id = docRef.id) else field
        docRef.set(finalField).await()
    }

    suspend fun deleteFormField(fieldId: String): Result<Unit> = runCatching {
        fieldsCollection.document(fieldId).delete().await()
    }

    fun getRegistrationSettings(): Flow<RegistrationSettings> = callbackFlow {
        val subscription = configCollection.document("registration_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val settings = snapshot?.toObject(RegistrationSettings::class.java) ?: RegistrationSettings()
                trySend(settings)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveRegistrationSettings(settings: RegistrationSettings): Result<Unit> = runCatching {
        configCollection.document("registration_settings").set(settings).await()
    }

    fun getUISettings(): Flow<AppUISettings> = callbackFlow {
        val subscription = configCollection.document("ui_settings")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val settings = snapshot?.toObject(AppUISettings::class.java) ?: AppUISettings()
                trySend(settings)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveUISettings(settings: AppUISettings): Result<Unit> = runCatching {
        configCollection.document("ui_settings").set(settings).await()
    }

    fun getInstruments(): Flow<List<Instrument>> = callbackFlow {
        val subscription = instrumentsCollection.orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val instruments = snapshot?.toObjects(Instrument::class.java) ?: emptyList()
                trySend(instruments)
            }
        awaitClose { subscription.remove() }
    }

    fun getCategories(): Flow<List<Category>> = callbackFlow {
        val subscription = categoriesCollection.whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.toObjects(Category::class.java) ?: emptyList()
                trySend(categories)
            }
        awaitClose { subscription.remove() }
    }

    fun getStates(): Flow<List<State>> = callbackFlow {
        val subscription = statesCollection.whereEqualTo("isActive", true).orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val states = snapshot?.toObjects(State::class.java) ?: emptyList()
                trySend(states)
            }
        awaitClose { subscription.remove() }
    }

    fun getDistricts(stateId: String): Flow<List<District>> = callbackFlow {
        val query = if (stateId.isEmpty()) {
            districtsCollection.whereEqualTo("isActive", true)
        } else {
            districtsCollection.whereEqualTo("isActive", true).whereEqualTo("stateId", stateId)
        }
        val subscription = query.orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val districts = snapshot?.toObjects(District::class.java) ?: emptyList()
                trySend(districts)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveInstrument(instrument: Instrument): Result<Unit> = runCatching {
        val docRef = if (instrument.id.isEmpty()) instrumentsCollection.document() else instrumentsCollection.document(instrument.id)
        val finalInstrument = if (instrument.id.isEmpty()) instrument.copy(id = docRef.id) else instrument
        docRef.set(finalInstrument).await()
    }
}
