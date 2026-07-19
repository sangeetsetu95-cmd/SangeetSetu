package com.sangeetsetu.app.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangeetsetu.app.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AISupportViewModel : ViewModel() {
    private val repository = AiRepository()
    
    private val _messages = MutableStateFlow<List<Pair<String, Boolean>>>(emptyList())
    val messages: StateFlow<List<Pair<String, Boolean>>> = _messages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _isAIAvailable = MutableStateFlow(true) // Set based on API Key presence
    val isAIAvailable: StateFlow<Boolean> = _isAIAvailable.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = repository.fetchChatHistory()
            if (history.isEmpty()) {
                _messages.value = listOf(
                    "Namaste! I am Setu AI, your premium assistant. How can I help you discover Indian art today?" to false
                )
            } else {
                _messages.value = history
            }
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(userMessage to true)
        _messages.value = currentMessages
        
        viewModelScope.launch {
            repository.saveChatMessage(userMessage, true)
            _isGenerating.value = true
            
            val aiResponseBuilder = StringBuilder()
            var firstChunk = true

            try {
                repository.getAiResponse(_messages.value.dropLast(1), userMessage).collect { chunk ->
                    if (firstChunk) {
                        currentMessages.add("" to false)
                        firstChunk = false
                    }
                    aiResponseBuilder.append(chunk)
                    currentMessages[currentMessages.lastIndex] = aiResponseBuilder.toString() to false
                    _messages.value = currentMessages.toList()
                }
                repository.saveChatMessage(aiResponseBuilder.toString(), false)
            } catch (e: Exception) {
                currentMessages.add("I'm sorry, I'm having trouble connecting to my creative circuits. Please try again or contact our support team." to false)
                _messages.value = currentMessages.toList()
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun clearChat() {
        // Option to clear UI, but maybe keep in DB? 
        // For now just reload
        _messages.value = listOf("Namaste! How can I help you?" to false)
    }
}
