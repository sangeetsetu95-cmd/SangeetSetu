package com.sangeetsetu.app.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object PaymentUtils {

    fun startUPIPayment(activity: Activity, upiId: String, name: String, note: String, requestCode: Int) {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri

        val chooser = Intent.createChooser(intent, "Pay with...")
        if (intent.resolveActivity(activity.packageManager) != null) {
            activity.startActivityForResult(chooser, requestCode)
        } else {
            Toast.makeText(activity, "No UPI app found, please install one to continue", Toast.LENGTH_SHORT).show()
        }
    }
}
