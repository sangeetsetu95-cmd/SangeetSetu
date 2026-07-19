package com.sangeetsetu.app.util

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object NotificationSettings {
    private const val PREFS_NAME = "notification_settings_prefs"
    private const val KEY_PUSH_ENABLED = "push_enabled"
    private const val KEY_BOOKING_ENABLED = "booking_enabled"
    private const val KEY_PAYMENT_ENABLED = "payment_enabled"
    private const val KEY_OFFER_ENABLED = "offer_enabled"
    private const val KEY_EVENT_ENABLED = "event_enabled"

    private val _pushEnabled = mutableStateOf(true)
    val pushEnabled: State<Boolean> = _pushEnabled

    private val _bookingEnabled = mutableStateOf(true)
    val bookingEnabled: State<Boolean> = _bookingEnabled

    private val _paymentEnabled = mutableStateOf(true)
    val paymentEnabled: State<Boolean> = _paymentEnabled

    private val _offerEnabled = mutableStateOf(true)
    val offerEnabled: State<Boolean> = _offerEnabled

    private val _eventEnabled = mutableStateOf(true)
    val eventEnabled: State<Boolean> = _eventEnabled

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        _pushEnabled.value = prefs.getBoolean(KEY_PUSH_ENABLED, true)
        _bookingEnabled.value = prefs.getBoolean(KEY_BOOKING_ENABLED, true)
        _paymentEnabled.value = prefs.getBoolean(KEY_PAYMENT_ENABLED, true)
        _offerEnabled.value = prefs.getBoolean(KEY_OFFER_ENABLED, true)
        _eventEnabled.value = prefs.getBoolean(KEY_EVENT_ENABLED, true)
    }

    fun setPushEnabled(context: Context, enabled: Boolean) {
        _pushEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_PUSH_ENABLED, enabled).apply()
    }

    fun setBookingEnabled(context: Context, enabled: Boolean) {
        _bookingEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_BOOKING_ENABLED, enabled).apply()
    }

    fun setPaymentEnabled(context: Context, enabled: Boolean) {
        _paymentEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_PAYMENT_ENABLED, enabled).apply()
    }

    fun setOfferEnabled(context: Context, enabled: Boolean) {
        _offerEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_OFFER_ENABLED, enabled).apply()
    }

    fun setEventEnabled(context: Context, enabled: Boolean) {
        _eventEnabled.value = enabled
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_EVENT_ENABLED, enabled).apply()
    }
}
