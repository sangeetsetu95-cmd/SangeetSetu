package com.sangeetsetu.app.repository

import com.sangeetsetu.app.model.PaymentRequest
import com.sangeetsetu.app.util.FirestoreAudit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object PaymentRepository {
    private val db by lazy { FirebaseFirestore.getInstance() }

    suspend fun createPaymentRequest(request: PaymentRequest): Result<Unit> {
        return try {
            val id = db.collection("payment_requests").document().id
            val finalRequest = request.copy(id = id)
            FirestoreAudit.verifiedWrite("payment_requests", id) {
                db.collection("payment_requests").document(id).set(finalRequest).await()
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
