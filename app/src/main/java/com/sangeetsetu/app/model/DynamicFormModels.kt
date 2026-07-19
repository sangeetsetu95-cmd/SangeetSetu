package com.sangeetsetu.app.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

enum class FormFieldType {
    TEXT, NUMBER, MOBILE, EMAIL, DROPDOWN, MULTI_SELECT, 
    CHECKBOX, RADIO_BUTTON, DATE_PICKER, STATE, DISTRICT, 
    PHOTO_UPLOAD, MULTIPLE_PHOTOS, VIDEO_UPLOAD, FILE_UPLOAD, 
    MULTILINE_TEXT, AUDIO_UPLOAD
}

data class FormField(
    val id: String = "",
    val label: String = "",
    val placeholder: String = "",
    val type: FormFieldType = FormFieldType.TEXT,
    val options: List<String> = emptyList(), // For dropdowns, radio, etc.
    @get:PropertyName("isRequired") @set:PropertyName("isRequired") var isRequired: Boolean = true,
    @get:PropertyName("isEnabled") @set:PropertyName("isEnabled") var isEnabled: Boolean = true,
    val displayOrder: Int = 0,
    val validationRegex: String? = null,
    val errorMessage: String? = null,
    val categoryFilter: List<String> = emptyList(), // For conditional fields
    val defaultValue: String = "",
    val maxLength: Int = 0 // 0 means no limit
)

data class RegistrationSettings(
    @get:PropertyName("otpEnabled") @set:PropertyName("otpEnabled") var otpEnabled: Boolean = true,
    @get:PropertyName("autoApproval") @set:PropertyName("autoApproval") var autoApproval: Boolean = false,
    val termsAndConditions: String = "",
    val maxImages: Int = 5,
    val maxVideos: Int = 2,
    val maxFileSizeMB: Int = 10
)

data class AppUISettings(
    val appLogo: String = "",
    val bannerImage: String = "",
    val headerTitle: String = "Sangeet Setu",
    val headerSubtitle: String = "Jode Kalakar, Banaye Yaadgar Pal",
    val primaryColor: String = "#D4AF37", // SangeetGold
    val secondaryColor: String = "#0B0B0F", // PremiumDark
    val backgroundColor: String = "#0B0B0F",
    val buttonColor: String = "#D4AF37",
    val buttonTextColor: String = "#000000",
    val cardStyle: String = "ROUNDED", // ROUNDED, ELEVATED, FLAT
    val showHeroBanner: Boolean = true,
    val showIcons: Boolean = true,
    val typography: String = "DEFAULT",
    val gradientStart: String = "#D4AF37",
    val gradientEnd: String = "#0B0B0F",
    val iconTint: String = "#D4AF37"
)

data class Instrument(
    val id: String = "",
    val name: String = "",
    @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
)

data class DynamicConfig(
    val formFields: List<FormField> = emptyList(),
    val registrationSettings: RegistrationSettings = RegistrationSettings(),
    val uiSettings: AppUISettings = AppUISettings()
)
