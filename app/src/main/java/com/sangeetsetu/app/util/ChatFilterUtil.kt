package com.sangeetsetu.app.util

import java.util.regex.Pattern

object ChatFilterUtil {
    private val PHONE_PATTERN = Pattern.compile("(\\+?\\d{1,4}[\\s-]?)?(\\d{10}|\\d{3}[\\s-]?\\d{3}[\\s-]?\\d{4})")
    private val UPI_PATTERN = Pattern.compile("[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}")
    private val EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
    private val SOCIAL_AND_URL_PATTERN = Pattern.compile(
        "(instagram\\.com/[a-zA-Z0-9._]+|ig\\.me/[a-zA-Z0-9._]+|" +
        "facebook\\.com/[a-zA-Z0-9._]+|fb\\.com/[a-zA-Z0-9._]+|" +
        "t\\.me/[a-zA-Z0-9._]+|telegram\\.me/[a-zA-Z0-9._]+|" +
        "(https?://)?(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&//=]*))",
        Pattern.CASE_INSENSITIVE
    )

    fun containsRestrictedContent(text: String): Boolean {
        if (PHONE_PATTERN.matcher(text).find()) return true
        if (UPI_PATTERN.matcher(text).find()) return true
        if (EMAIL_PATTERN.matcher(text).find()) return true
        if (SOCIAL_AND_URL_PATTERN.matcher(text).find()) return true
        return false
    }

    fun getRestrictedMessage(): String {
        return "सुरक्षा कारणों से Contact Information साझा करना अनुमति नहीं है। मोबाइल नंबर देखने के लिए 'Unlock Mobile Number' (₹11) विकल्प का उपयोग करें।"
    }
}
