package com.sangeetsetu.app.data.repository

import com.sangeetsetu.app.domain.repository.IEventRepository
import com.sangeetsetu.app.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : IEventRepository {

    override suspend fun getAllEvents(fromServer: Boolean): List<Event> {
        return try {
            val source = if (fromServer) com.google.firebase.firestore.Source.SERVER else com.google.firebase.firestore.Source.DEFAULT
            val snap = db.collection("events")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get(source)
                .await()
            snap.toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = db.collection("events").document(eventId).get().await()
            doc.toObject(Event::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
