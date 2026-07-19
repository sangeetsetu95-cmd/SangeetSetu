package com.sangeetsetu.app.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class AiRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Place for API Key - Ideally fetched from secure storage or BuildConfig
    private val GEMINI_API_KEY = "GEMINI_API_KEY" 

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = GEMINI_API_KEY
    )

    private val systemPrompt = """
        You are 'Setu AI', the official premium assistant for Sangeet Setu.
        Sangeet Setu is Bharat's leading platform for booking traditional and contemporary artists.
        Mission: 'Kala se Sanskar, Sanskriti se Sansar'.
        Tagline: 'Jode Kalakar, Banaye Yaadgar Pal'.
        
        Guidelines:
        1. Always be polite, respectful, and professional.
        2. Help users find artists like Bhajan Singers, Tabla players, Dholak players, Kathavachaks, etc.
        3. Provide support contact: WhatsApp +91 9119119119, Email: support@sangeetsetu.com.
        4. If you don't know something, suggest contacting a human expert.
        5. Use Hindi and English as per user preference.
    """.trimIndent()

    suspend fun getAiResponse(history: List<Pair<String, Boolean>>, prompt: String): Flow<String> = flow {
        if (GEMINI_API_KEY == "GEMINI_API_KEY" || GEMINI_API_KEY.isBlank()) {
            emit("Namaste! I am Setu AI. Currently, my creative circuits are being updated. Please contact our support at +91 9119119119 for immediate assistance.")
            return@flow
        }

        val chat = generativeModel.startChat(
            history = listOf(
                content(role = "user") { text(systemPrompt) },
                content(role = "model") { text("I am ready to assist as Setu AI.") }
            ) + history.map { (msg, isMe) ->
                content(role = if (isMe) "user" else "model") { text(msg) }
            }
        )

        val response = chat.sendMessageStream(prompt)
        response.collect { chunk ->
            chunk.text?.let { emit(it) }
        }
    }

    suspend fun saveChatMessage(text: String, isMe: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(userId)
            .collection("ai_chats").document()
        val chatMessage = mapOf(
            "text" to text,
            "isMe" to isMe,
            "timestamp" to System.currentTimeMillis()
        )
        
        com.sangeetsetu.app.util.FirestoreAudit.verifiedWrite("users/$userId/ai_chats", docRef.id) {
            docRef.set(chatMessage).await()
        }
    }

    suspend fun fetchChatHistory(): List<Pair<String, Boolean>> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        return try {
            val snapshot = db.collection("users").document(userId)
                .collection("ai_chats")
                .orderBy("timestamp")
                .get(com.google.firebase.firestore.Source.SERVER)
                .await()
            
            snapshot.documents.map { 
                val text = it.getString("text") ?: ""
                val isMe = it.getBoolean("isMe") ?: false
                text to isMe
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
