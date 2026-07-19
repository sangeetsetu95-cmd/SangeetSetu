package com.sangeetsetu.app.ai

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

object AISettings {
    private const val PREFS_NAME = "ai_support_prefs"
    private const val KEY_AI_ENABLED = "ai_enabled"

    private val _isAIEnabled = mutableStateOf(true)
    val isAIEnabled: State<Boolean> = _isAIEnabled

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _isAIEnabled.value = prefs.getBoolean(KEY_AI_ENABLED, true)
    }

    fun setAIEnabled(context: Context, enabled: Boolean) {
        _isAIEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AI_ENABLED, enabled)
            .apply()
    }
}
