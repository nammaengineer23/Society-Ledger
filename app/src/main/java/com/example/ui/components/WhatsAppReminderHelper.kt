package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.data.model.AppLanguage
import com.example.data.model.Donation
import com.example.data.model.SocietyInfo
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WhatsAppReminderHelper {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun sendPendingReminder(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo,
        language: AppLanguage = AppLanguage.ENGLISH
    ) {
        val cleanPhone = formatIndianPhoneNumber(donation.phone)
        val formattedAmount = String.format("%,.0f", donation.amount)

        val message = if (language == AppLanguage.KANNADA) {
            buildString {
                append("ನಮಸ್ಕಾರ *${donation.donorName}* ರವರಿಗೆ,\n\n")
                append("*${societyInfo.name}* ವತಿಯಿಂದ ಗೌರವಪೂರ್ವಕ ನಮಸ್ಕಾರಗಳು.\n\n")
                append("ತಾವು ಸೊಸೈಟಿಗೆ ನೀಡಲು ಒಪ್ಪಿಕೊಂಡಿರುವ ₹*$formattedAmount* ಮೊತ್ತದ ದೇಣಿಗೆ ಬಗ್ಗೆ ಈ ಸೌಹಾರ್ದಯುತ ಜ್ಞಾಪನೆ ಸಂದೇಶ")
                if (donation.notes.isNotBlank()) {
                    append(" (${donation.notes})")
                }
                append(".\n\n")
                append("ಸೊಸೈಟಿಯ ಬ್ಯಾಂಕ್ / UPI ವಿವರಗಳನ್ನು ಕೆಳಗೆ ನೀಡಲಾಗಿದೆ. ದಯವಿಟ್ಟು ಅನುಕೂಲವಾದಾಗ ಪಾವತಿಸಬೇಕಾಗಿ ಕೋರಿಕೆ:\n\n")
                append("📲 *ಸೊಸೈಟಿ UPI ID:* `${societyInfo.upiId}`\n")
                append("🏦 *ಬ್ಯಾಂಕ್ ವಿವರ:* ${societyInfo.bankAccount}\n\n")
                append("ಹಣ ವರ್ಗಾವಣೆ ಮಾಡಿದ ನಂತರ, ದಯವಿಟ್ಟು ಸ್ಕ್ರೀನ್‌ಶಾಟ್ ಅಥವಾ UTR ಸಂಖ್ಯೆಯನ್ನು ಕಳುಹಿಸಿ ತಕ್ಷಣವೇ ಡಿಜಿಟಲ್ ರಶೀದಿಯನ್ನು ಪಡೆದುಕೊಳ್ಳಿ.\n\n")
                append("ಸೊಸೈಟಿಯ ಅಭಿವೃದ್ಧಿಗೆ ತಮ್ಮ ಅಮೂಲ್ಯ ಕೊಡುಗೆಗೆ ಅನಂತ ಧನ್ಯವಾದಗಳು!\n")
                append("ಇಂತಿ ನಿಮ್ಮ,\n*ಆಡಳಿತ ಸಮಿತಿ*\n${societyInfo.name}")
            }
        } else {
            buildString {
                append("Namaste *${donation.donorName}* ji,\n\n")
                append("Greetings from *${societyInfo.name}*.\n\n")
                append("This is a gentle reminder regarding your pledged contribution of *₹$formattedAmount*")
                if (donation.notes.isNotBlank()) {
                    append(" towards _${donation.notes}_")
                }
                append(".\n\n")
                append("We request you to kindly complete the contribution at your earliest convenience using our society payment details below:\n\n")
                append("📲 *Society UPI ID:* `${societyInfo.upiId}`\n")
                append("🏦 *Bank A/C Details:* ${societyInfo.bankAccount}\n\n")
                append("Once transferred, kindly reply with the transaction screenshot / UTR number for your instant digital receipt.\n\n")
                append("Thank you for your valuable support to our community!\n")
                append("Warm regards,\n*Managing Committee*\n${societyInfo.name}")
            }
        }

        launchWhatsAppOrShare(context, cleanPhone, message)
    }

    fun sendPaymentConfirmationReceipt(
        context: Context,
        donation: Donation,
        societyInfo: SocietyInfo,
        language: AppLanguage = AppLanguage.ENGLISH
    ) {
        val cleanPhone = formatIndianPhoneNumber(donation.phone)
        val formattedAmount = String.format("%,.0f", donation.amount)
        val dateStr = dateFormat.format(Date(donation.paymentDate))

        val message = if (language == AppLanguage.KANNADA) {
            buildString {
                append("🧾 *ಅಧಿಕೃತ ದೇಣಿಗೆ ರಶೀದಿ*\n")
                append("🏛️ *${societyInfo.name.uppercase()}*\n")
                append("ನೋಂದಣಿ ಸಂಖ್ಯೆ: ${societyInfo.registrationNo}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("✅ *ರಶೀದಿ ಸಂಖ್ಯೆ:* ${donation.receiptNo}\n")
                append("📅 *ದಿನಾಂಕ:* $dateStr\n")
                append("👤 *ದಾನಿಗಳ ಹೆಸರು:* ${donation.donorName}\n")
                append("💰 *ಸ್ವೀಕರಿಸಿದ ಮೊತ್ತ:* ₹$formattedAmount\n")
                append("💳 *ಪಾವತಿ ವಿಧಾನ:* ${donation.paymentMode}\n")
                if (donation.notes.isNotBlank()) {
                    append("📝 *ಉದ್ದೇಶ / ಟಿಪ್ಪಣಿ:* ${donation.notes}\n")
                }
                append("✍️ *ದಾಖಲಿಸಿದವರು:* ${donation.recordedByName}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("🙏 _ಸೊಸೈಟಿಯ ಕ್ಷೇಮಾಭಿವೃದ್ಧಿಗೆ ತಮ್ಮ ಅಮೂಲ್ಯ ಕೊಡುಗೆಗಾಗಿ ಹಾರ್ದಿಕ ಧನ್ಯವಾದಗಳು!_\n\n")
                append("ಸೊಸೈಟಿ ಕಚೇರಿ: ${societyInfo.address}")
            }
        } else {
            buildString {
                append("🧾 *OFFICIAL DONATION RECEIPT*\n")
                append("🏛️ *${societyInfo.name.uppercase()}*\n")
                append("Reg No: ${societyInfo.registrationNo}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("✅ *Receipt No:* ${donation.receiptNo}\n")
                append("📅 *Date:* $dateStr\n")
                append("👤 *Received From:* ${donation.donorName}\n")
                append("💰 *Amount Received:* ₹$formattedAmount (${NumberToWords.convert(donation.amount.toLong())} Rupees Only)\n")
                append("💳 *Payment Mode:* ${donation.paymentMode}\n")
                if (donation.notes.isNotBlank()) {
                    append("📝 *Purpose/Notes:* ${donation.notes}\n")
                }
                append("✍️ *Recorded By:* ${donation.recordedByName}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("🙏 _Thank you for your generous contribution towards the development and welfare of our society!_\n\n")
                append("Society Office: ${societyInfo.address}")
            }
        }

        launchWhatsAppOrShare(context, cleanPhone, message)
    }

    /**
     * Send Society Monthly Financial Summary to Committee/Residents Group
     */
    fun sendMonthlyFinancialSummary(
        context: Context,
        totalReceived: Double,
        totalExpenses: Double,
        balance: Double,
        donationsCount: Int,
        expensesCount: Int,
        societyInfo: SocietyInfo,
        language: AppLanguage = AppLanguage.ENGLISH
    ) {
        val message = if (language == AppLanguage.KANNADA) {
            buildString {
                append("📊 *ಮಾಸಿಕ ಲೆಕ್ಕಪತ್ರ ಸಾರಾಂಶ (Monthly Financial Summary)*\n")
                append("🏛️ *${societyInfo.name.uppercase()}*\n")
                append("ಆರ್ಥಿಕ ವರ್ಷ: ${societyInfo.financialYear}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("📥 *ಒಟ್ಟು ದೇಣಿಗೆ ಸ್ವೀಕೃತಿ ($donationsCount):* ₹${String.format("%,.0f", totalReceived)}\n")
                append("📤 *ಒಟ್ಟು ಖರ್ಚುಗಳು ($expensesCount):* ₹${String.format("%,.0f", totalExpenses)}\n")
                append("💰 *ನಿವ್ವಳ ಉಳಿತಾಯ ಶಿಲ್ಕು:* ₹${String.format("%,.0f", balance)}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("📲 *ಸೊಸೈಟಿ UPI:* `${societyInfo.upiId}`\n\n")
                append("ಪಾರದರ್ಶಕ ಸಮಾಜ ನಿರ್ಮಾಣಕ್ಕಾಗಿ ನಮ್ಮೊಂದಿಗೆ ಸಹಕರಿಸುತ್ತಿರುವ ಪ್ರತಿಯೊಬ್ಬ ಸದಸ್ಯರಿಗೂ ಧನ್ಯವಾದಗಳು.\n")
                append("— *ಆಡಳಿತ ಸಮಿತಿ*")
            }
        } else {
            buildString {
                append("📊 *MONTHLY FINANCIAL STATEMENT*\n")
                append("🏛️ *${societyInfo.name.uppercase()}*\n")
                append("Financial Year: ${societyInfo.financialYear}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("📥 *Total Collections ($donationsCount):* ₹${String.format("%,.0f", totalReceived)}\n")
                append("📤 *Total Expenditure ($expensesCount):* ₹${String.format("%,.0f", totalExpenses)}\n")
                append("💰 *Net Available Balance:* ₹${String.format("%,.0f", balance)}\n")
                append("━━━━━━━━━━━━━━━━━━━━\n")
                append("📲 *Society UPI:* `${societyInfo.upiId}`\n\n")
                append("Thank you to all donors and residents for supporting transparent community governance.\n")
                append("— *Managing Committee*")
            }
        }

        launchWhatsAppBroadcast(context, message)
    }

    /**
     * Broadcast Global Announcement via WhatsApp
     */
    fun sendGlobalAnnouncement(
        context: Context,
        announcementText: String,
        societyInfo: SocietyInfo
    ) {
        val message = buildString {
            append("📢 *IMPORTANT SOCIETY ANNOUNCEMENT*\n")
            append("🏛️ *${societyInfo.name.uppercase()}*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n\n")
            append("$announcementText\n\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("Issued by: *Managing Committee*\n${societyInfo.name}")
        }

        launchWhatsAppBroadcast(context, message)
    }

    private fun launchWhatsAppBroadcast(context: Context, message: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            try {
                context.startActivity(Intent.createChooser(genericIntent, "Share via WhatsApp"))
            } catch (ex: Exception) {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Society Message", message))
                Toast.makeText(context, "Message copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchWhatsAppOrShare(context: Context, phone: String, message: String) {
        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val targetPhone = if (phone.startsWith("+")) phone else "+91$phone"
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$targetPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            
            // Check if WhatsApp is installed or fall back to general intent
            val pm = context.packageManager
            val matches = pm.queryIntentActivities(intent, 0)
            if (matches.isNotEmpty()) {
                context.startActivity(intent)
            } else {
                // Fallback to browser or generic share
                val browserIntent = Intent(Intent.ACTION_VIEW, uri)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                }
                try {
                    context.startActivity(browserIntent)
                } catch (e: Exception) {
                    context.startActivity(Intent.createChooser(shareIntent, "Send Message"))
                }
            }
        } catch (e: Exception) {
            // Fallback: copy to clipboard
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Society Message", message))
            Toast.makeText(context, "Message copied to clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatIndianPhoneNumber(phone: String): String {
        return phone.replace(Regex("[^0-9]"), "").takeLast(10)
    }
}

object NumberToWords {
    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    )
    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convert(n: Long): String {
        if (n == 0L) return "Zero"
        if (n < 0) return "Minus " + convert(-n)

        val sb = StringBuilder()
        var num = n

        if (num / 10000000 > 0) {
            sb.append(convert(num / 10000000)).append(" Crore ")
            num %= 10000000
        }
        if (num / 100000 > 0) {
            sb.append(convert(num / 100000)).append(" Lakh ")
            num %= 100000
        }
        if (num / 1000 > 0) {
            sb.append(convert(num / 1000)).append(" Thousand ")
            num %= 1000
        }
        if (num / 100 > 0) {
            sb.append(convert(num / 100)).append(" Hundred ")
            num %= 100
        }
        if (num > 0) {
            if (sb.isNotEmpty()) sb.append("and ")
            if (num < 20) {
                sb.append(units[num.toInt()])
            } else {
                sb.append(tens[(num / 10).toInt()])
                if (num % 10 > 0) {
                    sb.append(" ").append(units[(num % 10).toInt()])
                }
            }
        }
        return sb.toString().trim()
    }
}
