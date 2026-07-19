package com.sangeetsetu.app.util

import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.math.pow

object FirestoreAudit {
    private const val TAG = "BackendAudit"
    
    private fun getDb(): FirebaseFirestore? = try {
        if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
            FirebaseFirestore.getInstance()
        } else null
    } catch (_: Exception) { null }

    private fun getAuth(): FirebaseAuth? = try {
        if (FirebaseApp.getApps(FirebaseApp.getInstance().applicationContext).isNotEmpty()) {
            FirebaseAuth.getInstance()
        } else null
    } catch (_: Exception) { null }

    private const val MAX_RETRIES = 3
    private const val INITIAL_BACKOFF = 1000L // 1 second
    private const val TIMEOUT_MS = 30000L // 30 seconds

    fun logSystemInfo() {
        try {
            val app = FirebaseApp.getInstance()
            val options = app.options
            Log.d(TAG, "==== FIREBASE SYSTEM AUDIT ====")
            Log.d(TAG, "Project ID: ${options.projectId}")
            Log.d(TAG, "Application ID: ${options.applicationId}")
            Log.d(TAG, "Package Name: ${app.applicationContext.packageName}")
            Log.d(TAG, "Storage Bucket: ${options.storageBucket}")
            Log.d(TAG, "Database URL: ${options.databaseUrl}")
            Log.d(TAG, "Google App ID: ${options.applicationId}")
            Log.d(TAG, "================================")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log Firebase info", e)
        }
    }

    suspend fun verifiedWrite(
        collectionName: String,
        documentId: String,
        expectedData: Map<String, Any>? = null,
        operation: suspend () -> Unit,
    ): Result<Unit> {
        val auth = getAuth() ?: return Result.failure(Exception("Firebase not initialized"))
        val db = getDb() ?: return Result.failure(Exception("Firebase not initialized"))
        
        val user = auth.currentUser
        if (user == null) {
            logCriticalFailure("CREATE/UPDATE", collectionName, documentId, "UNAUTHENTICATED", "AUTH_REQUIRED", "No authenticated user found")
            return Result.failure(Exception("Authentication required"))
        }

        val uid = user.uid
        var currentAttempt = 0
        var lastException: Exception? = null

        while (currentAttempt < MAX_RETRIES) {
            currentAttempt++
            Log.d(TAG, "STEP 01: Starting write attempt $currentAttempt for $collectionName/$documentId")
            
            try {
                withTimeout(TIMEOUT_MS) {
                    operation()
                }
                Log.d(TAG, "STEP 02: Operation completed. Awaiting server propagation...")
                
                // Increased delay to ensure Firestore propagation on server side
                delay(2000L)

                Log.d(TAG, "STEP 03: Verifying from Source.SERVER...")
                val snapshot = try {
                    db.collection(collectionName).document(documentId).get(Source.SERVER).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Verification get(Source.SERVER) failed for $collectionName/$documentId: ${e.message}. Retrying with default source...")
                    delay(1000L)
                    db.collection(collectionName).document(documentId).get().await()
                }

                if (!snapshot.exists()) {
                    throw Exception("Verification Failed: Document does not exist on server after write. Path: $collectionName/$documentId")
                }

                if (expectedData != null) {
                    Log.d(TAG, "STEP 04: Verifying data fields...")
                    val serverData = snapshot.data ?: emptyMap()
                    val mismatchFields = mutableListOf<String>()
                    expectedData.forEach { (key, value) ->
                        if (serverData[key] != value) {
                            mismatchFields.add("$key: expected=$value, actual=${serverData[key]}")
                        }
                    }
                    if (mismatchFields.isNotEmpty()) {
                        throw Exception("Data Mismatch: ${mismatchFields.joinToString(", ")}")
                    }
                }

                Log.d(TAG, "STEP FINAL VERIFIED: Write confirmed on Google Servers for $collectionName/$documentId")
                return Result.success(Unit)

            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Attempt $currentAttempt failed: ${e.message}")
                
                if (shouldRetry(e) && (currentAttempt < MAX_RETRIES)) {
                    val backoff = INITIAL_BACKOFF * 2.0.pow(currentAttempt.toDouble() - 1).toLong()
                    Log.d(TAG, "Retrying in ${backoff}ms...")
                    delay(backoff)
                } else {
                    break
                }
            }
        }

        val finalEx = lastException ?: Exception("Unknown error during verified write")
        logCriticalFailure(
            operationType = "CREATE/UPDATE",
            collection = collectionName,
            docPath = "$collectionName/$documentId",
            uid = uid,
            errorCode = (finalEx as? FirebaseFirestoreException)?.code?.name ?: "UNKNOWN_ERROR",
            message = finalEx.message ?: "No message",
            exception = finalEx
        )
        return Result.failure(finalEx)
    }

    suspend fun verifiedDelete(
        collectionName: String,
        documentId: String,
        operation: suspend () -> Unit
    ): Result<Unit> {
        val auth = getAuth() ?: return Result.failure(Exception("Firebase not initialized"))
        val db = getDb() ?: return Result.failure(Exception("Firebase not initialized"))

        val user = auth.currentUser
        if (user == null) {
            logCriticalFailure("DELETE", collectionName, documentId, "UNAUTHENTICATED", "AUTH_REQUIRED", "No authenticated user found")
            return Result.failure(Exception("Authentication required"))
        }

        val uid = user.uid
        var currentAttempt = 0
        var lastException: Exception? = null

        while (currentAttempt < MAX_RETRIES) {
            currentAttempt++
            Log.d(TAG, "STEP 01: Starting delete attempt $currentAttempt for $collectionName/$documentId")

            try {
                withTimeout(TIMEOUT_MS) {
                    operation()
                }
                Log.d(TAG, "STEP 02: Delete operation completed. Verifying removal...")
                
                delay(500L)

                val snapshot = db.collection(collectionName).document(documentId).get(Source.SERVER).await()
                if (snapshot.exists()) {
                    throw Exception("Verification Failed: Document still exists on server after delete.")
                }

                Log.d(TAG, "STEP FINAL VERIFIED: Deletion confirmed on Google Servers for $collectionName/$documentId")
                return Result.success(Unit)

            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Delete attempt $currentAttempt failed: ${e.message}")
                
                if (shouldRetry(e) && currentAttempt < MAX_RETRIES) {
                    val backoff = INITIAL_BACKOFF * 2.0.pow(currentAttempt.toDouble() - 1).toLong()
                    delay(backoff)
                } else {
                    break
                }
            }
        }

        val finalEx = lastException ?: Exception("Unknown error during verified delete")
        logCriticalFailure(
            operationType = "DELETE",
            collection = collectionName,
            docPath = "$collectionName/$documentId",
            uid = uid,
            errorCode = (finalEx as? FirebaseFirestoreException)?.code?.name ?: "UNKNOWN_ERROR",
            message = finalEx.message ?: "No message",
            exception = finalEx
        )
        return Result.failure(finalEx)
    }

    private fun shouldRetry(e: Exception): Boolean {
        if (e is FirebaseFirestoreException) {
            return when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.DEADLINE_EXCEEDED,
                FirebaseFirestoreException.Code.ABORTED -> true
                else -> false
            }
        }
        return e is java.util.concurrent.TimeoutException || e is kotlinx.coroutines.TimeoutCancellationException
    }

    private fun logCriticalFailure(
        operationType: String,
        collection: String,
        docPath: String,
        uid: String,
        errorCode: String,
        message: String,
        exception: Exception? = null
    ) {
        val stacktrace = exception?.let { Log.getStackTraceString(it) } ?: "No stacktrace"
        Log.e(TAG, """
            |
            |CRITICAL FIRESTORE FAILURE
            |Operation: $operationType
            |Collection: $collection
            |Document: $docPath
            |UID: $uid
            |ErrorCode: $errorCode
            |Message: $message
            |Exception Type: ${exception?.javaClass?.simpleName ?: "N/A"}
            |Stacktrace: $stacktrace
            |
        """.trimMargin())
    }
}
