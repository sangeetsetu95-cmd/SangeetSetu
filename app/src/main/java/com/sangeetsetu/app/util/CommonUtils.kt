package com.sangeetsetu.app.util

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.NumberFormat
import java.util.Locale

fun formatAmount(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return formatter.format(amount.toLong())
}

fun maskPhoneNumber(phone: String): String {
    val clean = phone.replace("+", "").replace(" ", "").removePrefix("91")
    if (clean.length < 10) return phone.take(2) + "XXXXXX" + phone.takeLast(2)
    return clean.take(2) + "XXXXXX" + clean.takeLast(2)
}

fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        minutes > 0 -> "$minutes mins ago"
        else -> "Just now"
    }
}

fun formatLastSeen(timestamp: Long): String {
    if (timestamp <= 0) return "Offline"
    val now = java.util.Calendar.getInstance()
    val time = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
    
    val sdf = java.text.SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = sdf.format(time.time)

    return when {
        now.get(java.util.Calendar.DATE) == time.get(java.util.Calendar.DATE) &&
        now.get(java.util.Calendar.MONTH) == time.get(java.util.Calendar.MONTH) &&
        now.get(java.util.Calendar.YEAR) == time.get(java.util.Calendar.YEAR) -> {
            "Last seen today at $formattedTime"
        }
        now.apply { add(java.util.Calendar.DATE, -1) }.get(java.util.Calendar.DATE) == time.get(java.util.Calendar.DATE) -> {
            "Last seen yesterday at $formattedTime"
        }
        else -> {
            val dateSdf = java.text.SimpleDateFormat("dd MMM, h:mm a", Locale.getDefault())
            "Last seen ${dateSdf.format(time.time)}"
        }
    }
}

fun downloadFile(context: Context, url: String, fileName: String) {
    try {
        Log.d("DownloadManager", "Starting download from: $url")
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(fileName)
            .setDescription("Downloading $fileName...")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Log.d("DownloadManager", "Download enqueued with ID: $downloadId")
        Toast.makeText(context, "डाउनलोड शुरू हो गया है...", Toast.LENGTH_LONG).show()

        // Register receiver to open file when download is complete
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context, intent: Intent) {
                val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)

                    // Android Studio Logcat के लिए खास मैसेज
                    Log.i("APP_DOWNLOAD_STATUS", "################################################")
                    Log.i("APP_DOWNLOAD_STATUS", "SUCCESS: APK DOWNLOADED SUCCESSFULLY!")
                    Log.i("APP_DOWNLOAD_STATUS", "FILE LOCATION: ${file.absolutePath}")
                    Log.i("APP_DOWNLOAD_STATUS", "################################################")

                    Toast.makeText(context, "APK डाउनलोड हो गया है!", Toast.LENGTH_LONG).show()

                    installApk(context, fileName)
                    context.unregisterReceiver(this)
                }
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

    } catch (e: Exception) {
        Log.e("DownloadManager", "Download failed: ${e.message}")
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun installApk(context: Context, fileName: String) {
    try {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
        Log.d("DownloadManager", "Attempting to open file at: ${file.absolutePath}")
        
        if (file.exists()) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)
        } else {
            Log.e("DownloadManager", "File not found at: ${file.absolutePath}")
        }
    } catch (e: Exception) {
        Log.e("DownloadManager", "Error opening file: ${e.message}")
        Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_LONG).show()
    }
}
