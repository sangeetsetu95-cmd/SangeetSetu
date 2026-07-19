package com.sangeetsetu.app.model

import com.google.firebase.firestore.PropertyName

data class State(
    val id: String = "",
    val name: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
)

data class District(
    val id: String = "",
    val stateId: String = "",
    val name: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
)
