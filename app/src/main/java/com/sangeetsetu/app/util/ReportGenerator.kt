package com.sangeetsetu.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.sangeetsetu.app.model.Booking
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportGenerator {

    fun generateBookingPDF(context: Context, bookings: List<Booking>) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint().apply {
            textSize = 24f
            isFakeBoldText = true
        }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        canvas.drawText("Sangeet Setu - Booking Report", 50f, 50f, titlePaint)
        canvas.drawText("Generated on: ${SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())}", 50f, 80f, paint)

        var y = 120f
        paint.textSize = 12f
        
        // Header
        canvas.drawText("Booking ID", 50f, y, paint)
        canvas.drawText("Artist", 200f, y, paint)
        canvas.drawText("User", 350f, y, paint)
        canvas.drawText("Status", 500f, y, paint)
        
        y += 20f
        canvas.drawLine(50f, y, 550f, y, paint)
        y += 20f

        bookings.take(20).forEach { booking ->
            canvas.drawText(booking.id.take(8), 50f, y, paint)
            canvas.drawText(booking.artistName, 200f, y, paint)
            canvas.drawText(booking.userName, 350f, y, paint)
            canvas.drawText(booking.status.name, 500f, y, paint)
            y += 25f
        }

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "Booking_Report_${System.currentTimeMillis()}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
            shareFile(context, file, "application/pdf")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            pdfDocument.close()
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open Report"))
    }
}
