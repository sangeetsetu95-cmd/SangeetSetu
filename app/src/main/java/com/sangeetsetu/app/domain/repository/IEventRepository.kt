package com.sangeetsetu.app.domain.repository

import com.sangeetsetu.app.model.Event
import kotlinx.coroutines.flow.Flow

interface IEventRepository {
    suspend fun getAllEvents(fromServer: Boolean = false): List<Event>
    suspend fun getEventById(eventId: String): Event?
}
