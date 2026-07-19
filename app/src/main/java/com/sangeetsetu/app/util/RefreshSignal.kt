package com.sangeetsetu.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object RefreshSignal {
    private val _refreshEvent = MutableSharedFlow<Unit>(replay = 0)
    val refreshEvent = _refreshEvent.asSharedFlow()

    suspend fun onDataUpdated() {
        _refreshEvent.emit(Unit)
    }
}
