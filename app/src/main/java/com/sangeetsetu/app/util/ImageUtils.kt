package com.sangeetsetu.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUtils {
    fun compressImage(context: Context, uri: Uri, quality: Int = 70): Uri? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            val compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(compressedFile)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            
            Uri.fromFile(compressedFile)
        } catch (e: Exception) {
            null
        }
    }
}
