package com.sangeetsetu.app

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat

enum class AppTheme {
    LIGHT, DARK, SYSTEM, RED
}

object AppSettings {
    private const val PREFS_NAME = "app_settings_prefs"
    private const val KEY_THEME = "app_theme"
    private const val KEY_LANGUAGE = "app_language"
    private const val KEY_LOCATION = "app_location"

    private val _theme = mutableStateOf(AppTheme.SYSTEM)
    val theme: State<AppTheme> = _theme

    private val _language = mutableStateOf("System")
    val language: State<String> = _language

    private val _location = mutableStateOf("Select Location")
    val location: State<String> = _location

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        val themeName = prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name
        _theme.value = try { AppTheme.valueOf(themeName) } catch (e: Exception) { AppTheme.SYSTEM }
        
        _language.value = prefs.getString(KEY_LANGUAGE, "System") ?: "System"
        _location.value = prefs.getString(KEY_LOCATION, "Select Location") ?: "Select Location"
        
        // Don't call applyLocale here if it's already set by the system/previous run
        // but AppCompatDelegate.setApplicationLocales should be called once to ensure consistency
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (currentLocales.isEmpty && _language.value != "System") {
            applyLocale(_language.value)
        }
    }

    fun setTheme(context: Context, theme: AppTheme) {
        _theme.value = theme
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME, theme.name)
            .apply()
    }

    fun setLanguage(context: Context, language: String) {
        if (_language.value == language) return
        
        _language.value = language
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
        applyLocale(language)
    }
    
    private fun applyLocale(language: String) {
        val appLocale: LocaleListCompat = when (language) {
            "English" -> LocaleListCompat.forLanguageTags("en")
            "Hindi" -> LocaleListCompat.forLanguageTags("hi")
            else -> LocaleListCompat.getEmptyLocaleList()
        }
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun setLocation(context: Context, location: String) {
        _location.value = location
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LOCATION, location)
            .apply()
    }
}
