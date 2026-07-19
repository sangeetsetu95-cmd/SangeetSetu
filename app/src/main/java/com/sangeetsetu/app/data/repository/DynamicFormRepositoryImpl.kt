package com.sangeetsetu.app.data.repository

import android.util.Log
import com.sangeetsetu.app.domain.repository.IDynamicFormRepository
import com.sangeetsetu.app.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicFormRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : IDynamicFormRepository {
    
    private val configCollection = db.collection("app_config")
    private val fieldsCollection = db.collection("registration_fields")
    private val categoriesCollection = db.collection("categories")
    private val instrumentsCollection = db.collection("instruments")
    private val statesCollection = db.collection("states")
    private val districtsCollection = db.collection("districts")

    override fun getFormFields(): Flow<List<FormField>> = callbackFlow {
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

    override fun getRegistrationSettings(): Flow<RegistrationSettings> = callbackFlow {
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

    override fun getUISettings(): Flow<AppUISettings> = callbackFlow {
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

    override fun getInstruments(): Flow<List<Instrument>> = callbackFlow {
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

    override fun getCategories(): Flow<List<Category>> = callbackFlow {
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

    override fun getStates(): Flow<List<State>> = callbackFlow {
        val subscription = statesCollection.whereEqualTo("isActive", true).orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        Log.e("DynamicFormRepo", "Index required for states. Data is being prepared.", error)
                        statesCollection.get().addOnSuccessListener { s ->
                            trySend(s.toObjects(State::class.java).filter { it.isActive }.sortedBy { it.name })
                        }
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                val states = snapshot?.toObjects(State::class.java) ?: emptyList()
                trySend(states)
            }
        awaitClose { subscription.remove() }
    }

    override fun getDistricts(stateId: String): Flow<List<District>> = callbackFlow {
        val query = if (stateId.isEmpty()) {
            districtsCollection.whereEqualTo("isActive", true)
        } else {
            districtsCollection.whereEqualTo("isActive", true).whereEqualTo("stateId", stateId)
        }
        val subscription = query.orderBy("name")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION) {
                        Log.e("DynamicFormRepo", "Index required for districts. Data is being prepared.", error)
                        districtsCollection.get().addOnSuccessListener { s ->
                            val list = s.toObjects(District::class.java).filter { d ->
                                d.isActive && (stateId.isEmpty() || d.stateId == stateId)
                            }.sortedBy { it.name }
                            trySend(list)
                        }
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                val districts = snapshot?.toObjects(District::class.java) ?: emptyList()
                trySend(districts)
            }
        awaitClose { subscription.remove() }
    }

    override suspend fun saveFormFields(fields: List<FormField>): Result<Unit> = runCatching {
        db.runTransaction { transaction ->
            fields.forEach { field ->
                val docRef = if (field.id.isEmpty()) fieldsCollection.document() else fieldsCollection.document(field.id)
                val finalField = if (field.id.isEmpty()) field.copy(id = docRef.id) else field
                transaction.set(docRef, finalField)
            }
        }.await()
    }
}
