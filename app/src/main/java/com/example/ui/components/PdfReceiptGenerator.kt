package com.example.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Donation
import com.example.data.model.SocietyInfo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReceiptGenerator {

    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    /**
     * Generates a high-quality PDF receipt file using Android's native PdfDocument.
     */
    fun generatePdfReceipt(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo
    ): File? {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points (72 dpi)
        val pageHeight = 842 // Standard A4 height in points

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        drawReceiptContent(canvas, pageWidth.toFloat(), pageHeight.toFloat(), donation, societyInfo)

        pdfDocument.finishPage(page)

        val cleanReceiptNo = donation.receiptNo.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val fileName = "Receipt_${cleanReceiptNo}.pdf"
        val file = File(context.cacheDir, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    /**
     * Generates an image (Bitmap) of the receipt that can be shared or saved.
     */
    fun generateReceiptImage(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo
    ): File? {
        val width = 1200
        val height = 1600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawReceiptContent(canvas, width.toFloat(), height.toFloat(), donation, societyInfo)

        val cleanReceiptNo = donation.receiptNo.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val fileName = "Receipt_${cleanReceiptNo}.png"
        val file = File(context.cacheDir, fileName)

        return try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Opens or shares the generated PDF receipt using standard Android share sheet.
     */
    fun sharePdfReceipt(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo
    ) {
        val file = generatePdfReceipt(context, donation, societyInfo)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Failed to generate PDF receipt", Toast.LENGTH_SHORT).show()
            return
        }

        val contentUri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_SUBJECT, "Donation Receipt ${donation.receiptNo} - ${societyInfo.name}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Dear ${donation.donorName},\n\nPlease find attached the official PDF donation receipt for ₹ ${String.format("%,.2f", donation.amount)} towards ${societyInfo.name}.\n\nReceipt No: ${donation.receiptNo}\nPayment Mode: ${donation.paymentMode}\nStatus: ${donation.status.uppercase()}"
            )
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share PDF Receipt"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No app available to share PDF", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * View/Open the generated PDF receipt with an external PDF viewer.
     */
    fun viewPdfReceipt(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo
    ) {
        val file = generatePdfReceipt(context, donation, societyInfo)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Failed to generate PDF receipt", Toast.LENGTH_SHORT).show()
            return
        }

        val contentUri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(contentUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(viewIntent)
        } catch (e: ActivityNotFoundException) {
            // Fallback to chooser or share
            sharePdfReceipt(context, donation, societyInfo)
        }
    }

    /**
     * Shares image version of receipt.
     */
    fun shareImageReceipt(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo
    ) {
        val file = generateReceiptImage(context, donation, societyInfo)
        if (file == null || !file.exists()) {
            Toast.makeText(context, "Failed to generate receipt image", Toast.LENGTH_SHORT).show()
            return
        }

        val contentUri: Uri = try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Uri.fromFile(file)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_SUBJECT, "Payment Receipt ${donation.receiptNo}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Official Payment Receipt from ${societyInfo.name} for ₹ ${String.format("%,.2f", donation.amount)}."
            )
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            context.startActivity(Intent.createChooser(shareIntent, "Share Receipt Image"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing receipt image", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * HTML Template representation for preview or web-sharing
     */
    fun generateHtmlReceipt(donation: Donation, societyInfo: SocietyInfo): String {
        val formattedDate = dateFormat.format(Date(donation.paymentDate))
        val amountInWords = "${NumberToWords.convert(donation.amount.toLong())} Rupees Only"
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Receipt ${donation.receiptNo}</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; padding: 24px; color: #263238; max-width: 600px; margin: auto; }
                    .card { border: 2px solid #00695C; border-radius: 12px; padding: 24px; box-shadow: 0 4px 12px rgba(0,0,0,0.08); background: #ffffff; }
                    .header { text-align: center; border-bottom: 2px solid #ECEFF1; padding-bottom: 16px; margin-bottom: 16px; }
                    .society-name { color: #1A237E; font-size: 20px; font-weight: bold; margin: 0; }
                    .reg-no { color: #546E7A; font-size: 12px; margin-top: 4px; }
                    .title-badge { background: #E0F2F1; color: #00695C; font-weight: bold; font-size: 13px; display: inline-block; padding: 4px 12px; border-radius: 20px; margin-top: 8px; }
                    .grid-row { display: flex; justify-content: space-between; margin-bottom: 12px; }
                    .donor-box { background: #E8F5E9; border: 1px solid #C8E6C9; border-radius: 8px; padding: 12px; margin: 16px 0; }
                    .amount-box { background: #F1F8E9; border-radius: 8px; padding: 14px; text-align: center; margin: 16px 0; }
                    .amount { font-size: 28px; font-weight: bold; color: #2E7D32; }
                    .footer { display: flex; justify-content: space-between; align-items: flex-end; margin-top: 24px; border-top: 1px solid #ECEFF1; padding-top: 16px; font-size: 12px; }
                    .seal { color: #2E7D32; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="header">
                        <div class="society-name">${societyInfo.name.uppercase()}</div>
                        <div class="reg-no">Reg. No: ${societyInfo.registrationNo} | ${societyInfo.address}</div>
                        <div class="title-badge">OFFICIAL DONATION RECEIPT</div>
                    </div>
                    <div class="grid-row">
                        <div><strong>Receipt No:</strong> <span style="font-family: monospace; color: #00695C;">${donation.receiptNo}</span></div>
                        <div><strong>Date:</strong> $formattedDate</div>
                    </div>
                    <div class="donor-box">
                        <div style="font-size: 11px; color: #2E7D32; text-transform: uppercase; font-weight: bold;">Received with thanks from:</div>
                        <div style="font-size: 16px; font-weight: bold; color: #1B5E20; margin: 4px 0;">${donation.donorName}</div>
                        <div style="font-size: 12px; color: #388E3C;">Phone: +91 ${donation.phone}</div>
                        ${if (donation.notes.isNotBlank()) "<div style='font-size: 12px; font-style: italic; margin-top: 4px;'>Towards: " + donation.notes + "</div>" else ""}
                    </div>
                    <div class="amount-box">
                        <div style="font-size: 11px; color: #546E7A;">AMOUNT RECEIVED</div>
                        <div class="amount">₹ ${String.format("%,.2f", donation.amount)}</div>
                        <div style="font-size: 12px; color: #37474F; margin-top: 4px;">$amountInWords</div>
                        <div style="font-size: 11px; color: #00695C; margin-top: 6px;">Mode: <strong>${donation.paymentMode.uppercase()}</strong></div>
                    </div>
                    <div class="footer">
                        <div class="seal">✓ Digitally Verified & Issued</div>
                        <div style="text-align: right;">
                            <div style="color: #78909C; font-size: 10px;">Authorized Signature</div>
                            <div style="font-weight: bold;">${donation.recordedByName}</div>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Canvas drawing engine for both PDF document pages and Bitmap images.
     */
    private fun drawReceiptContent(
        canvas: Canvas,
        width: Float,
        height: Float,
        donation: Donation,
        societyInfo: SocietyInfo
    ) {
        val scale = width / 595f // relative to A4 595pt

        // Background canvas fill
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Outer Decorative Margin Border
        val margin = 24f * scale
        val cardRect = RectF(margin, margin, width - margin, height - margin)

        // Card background fill
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(253, 254, 255)
        canvas.drawRoundRect(cardRect, 16f * scale, 16f * scale, paint)

        // Outer Stroke
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f * scale
        paint.color = Color.rgb(0, 105, 92) // SocietyTealPrimary #00695C
        canvas.drawRoundRect(cardRect, 16f * scale, 16f * scale, paint)

        // Inner subtle border
        paint.strokeWidth = 0.8f * scale
        paint.color = Color.rgb(207, 216, 220)
        val innerMargin = margin + (6f * scale)
        canvas.drawRoundRect(
            RectF(innerMargin, innerMargin, width - innerMargin, height - innerMargin),
            12f * scale,
            12f * scale,
            paint
        )

        // Header Banner Background
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(26, 35, 126) // SapphireNavy #1A237E
        val headerRect = RectF(margin + 8f * scale, margin + 8f * scale, width - margin - 8f * scale, margin + (85f * scale))
        canvas.drawRoundRect(headerRect, 10f * scale, 10f * scale, paint)

        // Society Name (White on Navy Banner)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 18f * scale
        canvas.drawText(societyInfo.name.uppercase(), width / 2f, margin + (38f * scale), paint)

        // Registration & Subtitle
        paint.textSize = 10f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.rgb(224, 224, 224)
        canvas.drawText("Reg. No: ${societyInfo.registrationNo} • ${societyInfo.financialYear}", width / 2f, margin + (54f * scale), paint)
        paint.textSize = 9f * scale
        canvas.drawText(societyInfo.address, width / 2f, margin + (68f * scale), paint)

        var y = margin + (110f * scale)

        // Title Tag Pill
        val tagWidth = 260f * scale
        val tagHeight = 24f * scale
        val tagRect = RectF((width - tagWidth) / 2f, y - (16f * scale), (width + tagWidth) / 2f, y + (8f * scale))
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(224, 242, 241) // Light teal
        canvas.drawRoundRect(tagRect, 12f * scale, 12f * scale, paint)

        paint.color = Color.rgb(0, 105, 92)
        paint.textSize = 11f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("OFFICIAL DONATION RECEIPT", width / 2f, y - (1f * scale), paint)

        y += 28f * scale

        // Meta info line: Receipt No & Date
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(90, 90, 90)
        paint.textSize = 10f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("RECEIPT NO:", margin + 20f * scale, y, paint)

        paint.color = Color.rgb(0, 105, 92)
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        paint.textSize = 12f * scale
        canvas.drawText(donation.receiptNo, margin + 88f * scale, y, paint)

        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.rgb(90, 90, 90)
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f * scale
        val formattedDate = dateFormat.format(Date(donation.paymentDate))
        canvas.drawText("DATE: $formattedDate", width - margin - 20f * scale, y, paint)

        y += 18f * scale

        // Divider
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(224, 224, 224)
        paint.strokeWidth = 1f * scale
        canvas.drawLine(margin + 16f * scale, y, width - margin - 16f * scale, y, paint)

        y += 20f * scale

        // Received From Donor Box
        val donorBoxHeight = 85f * scale
        val donorBoxRect = RectF(margin + 16f * scale, y, width - margin - 16f * scale, y + donorBoxHeight)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(232, 245, 233) // Light Green
        canvas.drawRoundRect(donorBoxRect, 8f * scale, 8f * scale, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(200, 230, 201)
        paint.strokeWidth = 1f * scale
        canvas.drawRoundRect(donorBoxRect, 8f * scale, 8f * scale, paint)

        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 10f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("RECEIVED WITH THANKS FROM:", margin + 28f * scale, y + (20f * scale), paint)

        paint.color = Color.rgb(27, 94, 32)
        paint.textSize = 15f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(donation.donorName, margin + 28f * scale, y + (40f * scale), paint)

        paint.color = Color.rgb(56, 142, 60)
        paint.textSize = 11f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Contact: +91 ${donation.phone}", margin + 28f * scale, y + (58f * scale), paint)

        if (donation.notes.isNotBlank()) {
            paint.color = Color.rgb(46, 125, 50)
            paint.textSize = 10f * scale
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Towards: ${donation.notes}", margin + 28f * scale, y + (74f * scale), paint)
        }

        y += donorBoxHeight + (20f * scale)

        // Amount Box
        val amountBoxHeight = 100f * scale
        val amountBoxRect = RectF(margin + 16f * scale, y, width - margin - 16f * scale, y + amountBoxHeight)
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(241, 248, 233)
        canvas.drawRoundRect(amountBoxRect, 8f * scale, 8f * scale, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(220, 237, 200)
        paint.strokeWidth = 1f * scale
        canvas.drawRoundRect(amountBoxRect, 8f * scale, 8f * scale, paint)

        // Amount Left
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 10f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("AMOUNT RECEIVED (INR)", margin + 28f * scale, y + (22f * scale), paint)

        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 24f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("₹ ${String.format("%,.2f", donation.amount)}", margin + 28f * scale, y + (52f * scale), paint)

        val amountInWords = "${NumberToWords.convert(donation.amount.toLong())} Rupees Only"
        paint.color = Color.rgb(60, 60, 60)
        paint.textSize = 10.5f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("In Words: $amountInWords", margin + 28f * scale, y + (72f * scale), paint)

        // Payment Mode Badge Right
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9.5f * scale
        canvas.drawText("PAYMENT MODE", width - margin - 28f * scale, y + (22f * scale), paint)

        val modePillWidth = 110f * scale
        val modePillRect = RectF(width - margin - 28f * scale - modePillWidth, y + (32f * scale), width - margin - 28f * scale, y + (56f * scale))
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(224, 242, 241)
        canvas.drawRoundRect(modePillRect, 6f * scale, 6f * scale, paint)

        paint.color = Color.rgb(0, 105, 92)
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(donation.paymentMode.uppercase(), modePillRect.centerX(), y + (49f * scale), paint)

        // Status Badge
        paint.textAlign = Paint.Align.RIGHT
        paint.color = if (donation.status == "received") Color.rgb(46, 125, 50) else Color.rgb(230, 81, 0)
        paint.textSize = 10f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Status: ${donation.status.uppercase()}", width - margin - 28f * scale, y + (76f * scale), paint)

        y += amountBoxHeight + (24f * scale)

        // Society Bank / UPI Info Strip
        val infoStripRect = RectF(margin + 16f * scale, y, width - margin - 16f * scale, y + (36f * scale))
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(245, 247, 250)
        canvas.drawRoundRect(infoStripRect, 6f * scale, 6f * scale, paint)

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(84, 110, 122)
        paint.textSize = 9.5f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("UPI ID: ${societyInfo.upiId}  •  Bank: ${societyInfo.bankAccount}", margin + 24f * scale, y + (22f * scale), paint)

        y += 48f * scale

        // Divider
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(224, 224, 224)
        paint.strokeWidth = 1f * scale
        canvas.drawLine(margin + 16f * scale, y, width - margin - 16f * scale, y, paint)

        y += 26f * scale

        // Footer Section: Verified Seal Left, QR Code Middle, Authorized Signatory Right
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT

        // Verified Badge Icon
        paint.color = Color.rgb(46, 125, 50)
        canvas.drawCircle(margin + 24f * scale, y + (12f * scale), 10f * scale, paint)
        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 11f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("✓", margin + 24f * scale, y + (16f * scale), paint)

        paint.textAlign = Paint.Align.LEFT
        paint.color = Color.rgb(46, 125, 50)
        paint.textSize = 10f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("VERIFIED DIGITAL RECEIPT", margin + 40f * scale, y + (8f * scale), paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Public Transparency Ledger", margin + 40f * scale, y + (20f * scale), paint)
        canvas.drawText("Authentic Transaction", margin + 40f * scale, y + (30f * scale), paint)

        // Draw Google Play Store App Download QR Code in center
        try {
            val playStorePayload = "https://play.google.com/store/apps/details?id=com.societyledger.app"
            val qrBitmap = QrCodeGenerator.generateQrBitmap(
                content = playStorePayload,
                size = (70 * scale).toInt().coerceAtLeast(64),
                foregroundColor = Color.rgb(26, 35, 126),
                backgroundColor = Color.WHITE
            )
            val qrSize = 58f * scale
            val qrLeft = (width - qrSize) / 2f
            val qrTop = y - 8f * scale
            canvas.drawBitmap(qrBitmap, null, RectF(qrLeft, qrTop, qrLeft + qrSize, qrTop + qrSize), paint)

            paint.textAlign = Paint.Align.CENTER
            paint.color = Color.rgb(26, 35, 126)
            paint.textSize = 7.5f * scale
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("SCAN FOR PLAY STORE APP", width / 2f, qrTop + qrSize + (10f * scale), paint)
            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 6.5f * scale
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("(Download on Google Play)", width / 2f, qrTop + qrSize + (18f * scale), paint)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Signatory Right
        paint.textAlign = Paint.Align.RIGHT
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 9f * scale
        canvas.drawText("AUTHORIZED ISSUER", width - margin - 24f * scale, y + (6f * scale), paint)

        paint.color = Color.rgb(26, 35, 126)
        paint.textSize = 12f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(donation.recordedByName, width - margin - 24f * scale, y + (22f * scale), paint)

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f * scale
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Society Executive Committee", width - margin - 24f * scale, y + (34f * scale), paint)

        // Bottom Disclaimer
        val bottomY = height - margin - (14f * scale)
        paint.textAlign = Paint.Align.CENTER
        paint.color = Color.rgb(144, 164, 174)
        paint.textSize = 8f * scale
        canvas.drawText(
            "This digital acknowledgement is generated electronically by the Society Ledger App. Timestamp: ${timeFormat.format(Date())}",
            width / 2f,
            bottomY,
            paint
        )
    }
}
