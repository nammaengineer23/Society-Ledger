package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportHelper {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("dd-MM-yyyy_HH-mm", Locale.getDefault())

    fun exportToCsv(
        context: Context,
        societyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>
    ) {
        try {
            val fileName = "Society_Ledger_Report_${timeFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)

            writer.append("=== SOCIETY FINANCIAL AUDIT REPORT ===\n")
            writer.append("Society Name,${societyInfo.name}\n")
            writer.append("Registration No,${societyInfo.registrationNo}\n")
            writer.append("UPI ID,${societyInfo.upiId}\n")
            writer.append("Report Date,${dateFormat.format(Date())}\n")
            writer.append("Financial Year,${societyInfo.financialYear}\n\n")

            // KPI Summary
            val totalReceived = donations.filter { it.status == "received" }.sumOf { it.amount }
            val totalPending = donations.filter { it.status == "pending" }.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }
            val netBalance = totalReceived - totalExpenses

            writer.append("=== FINANCIAL SUMMARY (INR) ===\n")
            writer.append("Total Collected (Received),Rs. $totalReceived\n")
            writer.append("Total Pending Pledges,Rs. $totalPending\n")
            writer.append("Total Expenses Incurred,Rs. $totalExpenses\n")
            writer.append("Net Available Balance,Rs. $netBalance\n\n")

            // Donations Section
            writer.append("=== DONATIONS & PLEDGES REGISTER ===\n")
            writer.append("Receipt No,Donor Name,Phone,Amount (Rs.),Status,Payment Mode,Date,Recorded By,Notes\n")
            donations.forEach { d ->
                val dateStr = dateFormat.format(Date(d.paymentDate))
                val cleanNotes = d.notes.replace(",", " ")
                val cleanDonor = d.donorName.replace(",", " ")
                val cleanRecordedBy = d.recordedByName.replace(",", " ")
                writer.append("${d.receiptNo},${cleanDonor},${d.phone},${d.amount},${d.status.uppercase()},${d.paymentMode},$dateStr,$cleanRecordedBy,$cleanNotes\n")
            }
            writer.append("\n")

            // Expenses Section
            writer.append("=== EXPENSES REGISTER ===\n")
            writer.append("ID,Expense Title,Category,Amount (Rs.),Expense Date,Paid To,Payment Mode,Receipt/Bill Ref,Approved By\n")
            expenses.forEach { e ->
                val dateStr = dateFormat.format(Date(e.expenseDate))
                val cleanTitle = e.title.replace(",", " ")
                val cleanPaidTo = e.paidTo.replace(",", " ")
                val cleanReceipt = e.receiptUrl.replace(",", " ")
                val cleanRecordedBy = e.recordedByName.replace(",", " ")
                writer.append("${e.id},${cleanTitle},${e.category},${e.amount},$dateStr,$cleanPaidTo,${e.paymentMode},$cleanReceipt,$cleanRecordedBy\n")
            }

            writer.flush()
            writer.close()

            // Open Share Intent
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
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "${societyInfo.name} - Financial Audit Report")
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share / Export Society CSV Report"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun shareAuditSummaryText(
        context: Context,
        societyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>
    ) {
        val totalReceived = donations.filter { it.status == "received" }.sumOf { it.amount }
        val totalPending = donations.filter { it.status == "pending" }.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val netBalance = totalReceived - totalExpenses

        val text = buildString {
            append("🏛️ *${societyInfo.name.uppercase()}*\n")
            append("📋 *Financial Transparency & Audit Statement*\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("📅 *Date:* ${dateFormat.format(Date())} (${societyInfo.financialYear})\n")
            append("🆔 *Reg No:* ${societyInfo.registrationNo}\n\n")

            append("💰 *FINANCIAL HIGHLIGHTS:*\n")
            append("✅ *Total Donations Received:* ₹ ${String.format("%,.2f", totalReceived)}\n")
            append("⏳ *Total Pending Pledges:* ₹ ${String.format("%,.2f", totalPending)}\n")
            append("📉 *Total Expenses Incurred:* ₹ ${String.format("%,.2f", totalExpenses)}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("🏦 *NET AVAILABLE BALANCE:* ₹ ${String.format("%,.2f", netBalance)}\n\n")

            append("📊 *DONATIONS BREAKDOWN:*\n")
            val receivedList = donations.filter { it.status == "received" }
            append("• Total Donors: ${receivedList.size}\n")
            val byMode = receivedList.groupBy { it.paymentMode }
            byMode.forEach { (mode, list) ->
                val sum = list.sumOf { it.amount }
                append("  - $mode: ₹ ${String.format("%,.0f", sum)} (${list.size} txns)\n")
            }
            append("\n")

            append("🏷️ *EXPENSES BREAKDOWN:*\n")
            val byCat = expenses.groupBy { it.category }
            byCat.forEach { (cat, list) ->
                val sum = list.sumOf { it.amount }
                append("  - $cat: ₹ ${String.format("%,.0f", sum)}\n")
            }
            append("\n")

            append("📲 *Society UPI ID:* `${societyInfo.upiId}`\n")
            append("🏦 *Bank Details:* ${societyInfo.bankAccount}\n")
            append("━━━━━━━━━━━━━━━━━━━━\n")
            append("_Generated digitally via Society Ledger App_")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "${societyInfo.name} Financial Summary")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Financial Statement"))
    }

    fun sendBackupEmail(
        context: Context,
        societyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>,
        members: List<com.example.data.model.Member>,
        recipientEmail: String = ""
    ) {
        try {
            val fileName = "Society_Full_Ledger_Backup_${timeFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)
            val writer = FileWriter(file)

            writer.append("=== SOCIETY ACCOUNTS LEDGER BACKUP ===\n")
            writer.append("Society Name,${societyInfo.name}\n")
            writer.append("Registration Number,${societyInfo.registrationNo}\n")
            writer.append("Financial Year,${societyInfo.financialYear}\n")
            writer.append("UPI ID,${societyInfo.upiId}\n")
            writer.append("Bank Account Details,${societyInfo.bankAccount.replace(",", " ")}\n")
            writer.append("Opening Bank Reserve (Rs.),${societyInfo.openingBalance}\n")
            writer.append("Backup Generated Timestamp,${dateFormat.format(Date())}\n\n")

            // Financial Summary
            val totalReceived = donations.filter { it.status == "received" }.sumOf { it.amount }
            val totalPending = donations.filter { it.status == "pending" }.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }
            val netBalance = societyInfo.openingBalance + totalReceived - totalExpenses

            writer.append("=== EXECUTIVE AUDIT SUMMARY (INR) ===\n")
            writer.append("Starting Reserve,Rs. ${societyInfo.openingBalance}\n")
            writer.append("Total Received Collections,Rs. $totalReceived\n")
            writer.append("Total Pending Pledges,Rs. $totalPending\n")
            writer.append("Total Expenses Incurred,Rs. $totalExpenses\n")
            writer.append("Net Available Balance,Rs. $netBalance\n\n")

            // Committee Members Section
            writer.append("=== COMMITTEE MEMBERS DIRECTORY ===\n")
            writer.append("ID,Name,Role,Phone,Email,Is Admin\n")
            members.forEach { m ->
                val cleanName = m.name.replace(",", " ")
                val cleanRole = m.role.replace(",", " ")
                writer.append("${m.id},$cleanName,$cleanRole,${m.phone},${m.email},${m.isSuperAdmin}\n")
            }
            writer.append("\n")

            // Donations Register
            writer.append("=== DONATIONS & PLEDGES REGISTER ===\n")
            writer.append("Receipt No,Donor Name,Phone,Amount (Rs.),Status,Payment Mode,Payment Date,Recorded By Member,Notes,User Email\n")
            donations.forEach { d ->
                val dateStr = dateFormat.format(Date(d.paymentDate))
                val cleanNotes = d.notes.replace(",", " ")
                val cleanDonor = d.donorName.replace(",", " ")
                val cleanRecordedBy = d.recordedByName.replace(",", " ")
                writer.append("${d.receiptNo},${cleanDonor},${d.phone},${d.amount},${d.status.uppercase()},${d.paymentMode},$dateStr,$cleanRecordedBy,$cleanNotes,${d.userEmail}\n")
            }
            writer.append("\n")

            // Expenses Register
            writer.append("=== EXPENSES REGISTER ===\n")
            writer.append("ID,Expense Title,Category,Amount (Rs.),Expense Date,Paid To,Payment Mode,Receipt/Bill Ref,Approved By Member,User Email\n")
            expenses.forEach { e ->
                val dateStr = dateFormat.format(Date(e.expenseDate))
                val cleanTitle = e.title.replace(",", " ")
                val cleanPaidTo = e.paidTo.replace(",", " ")
                val cleanReceipt = e.receiptUrl.replace(",", " ")
                val cleanRecordedBy = e.recordedByName.replace(",", " ")
                writer.append("${e.id},${cleanTitle},${e.category},${e.amount},$dateStr,$cleanPaidTo,${e.paymentMode},$cleanReceipt,$cleanRecordedBy,${e.userEmail}\n")
            }

            writer.flush()
            writer.close()

            // Build rich email body
            val emailBody = buildString {
                append("Dear Committee Members & Auditor,\n\n")
                append("Please find attached the official Accounts Backup and Financial Audit Ledger for ${societyInfo.name}.\n\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("📊 SOCIETY EXECUTIVE AUDIT STATEMENT\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("• Society: ${societyInfo.name}\n")
                append("• Reg No: ${societyInfo.registrationNo}\n")
                append("• Financial Year: ${societyInfo.financialYear}\n")
                append("• Date of Export: ${dateFormat.format(Date())}\n\n")

                append("💰 FINANCIAL HIGHLIGHTS:\n")
                append("  Starting Reserve / Opening Balance: ₹ ${String.format("%,.2f", societyInfo.openingBalance)}\n")
                append("  + Total Received Collections:        ₹ ${String.format("%,.2f", totalReceived)} (${donations.filter { it.status == "received" }.size} records)\n")
                append("  - Total Expenses Incurred:           ₹ ${String.format("%,.2f", totalExpenses)} (${expenses.size} records)\n")
                append("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("  🏦 NET AVAILABLE BALANCE:            ₹ ${String.format("%,.2f", netBalance)}\n")
                append("  ⏳ Total Pending Pledges:            ₹ ${String.format("%,.2f", totalPending)}\n\n")

                append("👥 COLLECTIONS BY COMMITTEE MEMBER:\n")
                val byMember = donations.filter { it.status == "received" }.groupBy { it.recordedByName }
                if (byMember.isNotEmpty()) {
                    byMember.forEach { (memberName, list) ->
                        val sum = list.sumOf { it.amount }
                        append("  • $memberName: ₹ ${String.format("%,.0f", sum)} (${list.size} receipts)\n")
                    }
                } else {
                    append("  • None recorded yet\n")
                }
                append("\n")

                append("🏷️ EXPENSES BY CATEGORY:\n")
                val byCat = expenses.groupBy { it.category }
                if (byCat.isNotEmpty()) {
                    byCat.forEach { (category, list) ->
                        val sum = list.sumOf { it.amount }
                        append("  • $category: ₹ ${String.format("%,.0f", sum)} (${list.size} bills)\n")
                    }
                } else {
                    append("  • No expenses recorded\n")
                }
                append("\n")

                append("🏦 OFFICIAL BANK ACCOUNT & UPI:\n")
                append("  • UPI ID: ${societyInfo.upiId}\n")
                append("  • Bank Details: ${societyInfo.bankAccount}\n\n")

                append("📎 Attachment: Full ledger spreadsheet ($fileName) containing all donations, expenses, and committee records.\n\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("Sent automatically via Society Ledger Management System")
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

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                if (recipientEmail.isNotBlank()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail.trim()))
                }
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "📊 [Accounts Backup] ${societyInfo.name} - Full Financial Ledger (${dateFormat.format(Date())})"
                )
                putExtra(Intent.EXTRA_TEXT, emailBody)
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(emailIntent, "Send Ledger Backup to Email"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error creating email backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun openWhatsAppChat(context: Context, rawPhone: String, message: String) {
        try {
            val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
            val formattedPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
            val encodedMessage = Uri.encode(message)
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback without package restriction (e.g. WhatsApp Business or browser)
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(fallbackIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareWhatsAppBroadcast(context: Context, broadcastMessage: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, broadcastMessage)
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, broadcastMessage)
                }
                context.startActivity(Intent.createChooser(genericIntent, "Share Broadcast Notice"))
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error sending broadcast: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyPhoneNumbersToClipboard(context: Context, numbers: List<String>, label: String = "Donor Phone Numbers") {
        try {
            val cleanNumbers = numbers.map { it.replace(Regex("[^0-9]"), "") }.filter { it.isNotBlank() }
            val text = cleanNumbers.joinToString(", ")
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied ${cleanNumbers.size} donor numbers to clipboard for WhatsApp Broadcast!", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error copying to clipboard: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    const val UPTODOWN_APPSTORE_URL = "https://society-ledger.en.uptodown.com/android"
    const val OFFICIAL_WEB_APP_URL = "https://ais-pre-x2q5265yk7suo6hhyewgn4-96719227087.asia-east1.run.app"

    /**
     * Shares the present installed APK file directly (via FileProvider or direct intent) along with the Uptodown App Store & Web links.
     */
    fun shareAppApk(
        context: Context,
        appShareUrl: String = OFFICIAL_WEB_APP_URL,
        uptodownUrl: String = UPTODOWN_APPSTORE_URL
    ) {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val appFile = File(context.applicationInfo.sourceDir)

            val shareMessage = """
                🏛️ *Society Ledger & Transparency Portal App*
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Official society accounting, audited balance sheets, donor directory & digital receipts management system.
                
                📱 *App Version:* v${packageInfo.versionName ?: "1.0.0"}
                
                📲 *Download from Uptodown App Store:*
                $uptodownUrl
                
                🌐 *Online Web Access & Society Portal:*
                $appShareUrl
                
                Share this APK file or link with your society committee members and residents for 100% financial transparency!
            """.trimIndent()

            if (appFile.exists()) {
                val tempApk = File(context.cacheDir, "SocietyLedger_v${packageInfo.versionName ?: "1.0.0"}.apk")
                try {
                    appFile.copyTo(tempApk, overwrite = true)
                } catch (e: Exception) {
                    // if copy fails, fallback to direct share
                }

                val targetFile = if (tempApk.exists()) tempApk else appFile
                val apkUri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    targetFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    putExtra(Intent.EXTRA_SUBJECT, "Download Society Ledger App (Uptodown & APK)")
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Share Society Ledger App (APK & Uptodown Link)"))
            } else {
                // If direct APK sourceDir is inaccessible, share link & details
                shareAppLinkOnly(context, appShareUrl, uptodownUrl)
            }
        } catch (e: Exception) {
            // Fallback gracefully to text & link share if APK binary extraction is restricted
            shareAppLinkOnly(context, appShareUrl, uptodownUrl)
        }
    }

    /**
     * Shares the app download link (Uptodown App Store and Web link) via standard Android share chooser.
     */
    fun shareAppLinkOnly(
        context: Context,
        appShareUrl: String = OFFICIAL_WEB_APP_URL,
        uptodownUrl: String = UPTODOWN_APPSTORE_URL
    ) {
        try {
            val shareMessage = """
                🏛️ *Society Ledger & Transparency Portal App*
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                Official society accounting, audited balance sheets, donor directory & digital receipts management system.
                
                📲 *Download on Uptodown App Store:*
                $uptodownUrl
                
                🌐 *Open Web App & Society Portal:*
                $appShareUrl
                
                Share with committee members, treasurers, and residents for 100% financial transparency.
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Society Ledger App - Uptodown App Store & Web Link")
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            context.startActivity(Intent.createChooser(intent, "Share Society Ledger App Link"))
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Opens the official Uptodown App Store page in browser or app store client.
     */
    fun openUptodownStore(context: Context, uptodownUrl: String = UPTODOWN_APPSTORE_URL) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uptodownUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open browser: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copies the app download link directly to clipboard.
     */
    fun copyAppLinkToClipboard(context: Context, appShareUrl: String = "https://ais-pre-x2q5265yk7suo6hhyewgn4-96719227087.asia-east1.run.app") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Society Ledger App Link", appShareUrl)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "App Download Link copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error copying link: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Generates a complete, structured JSON string representing the entire society's transaction history,
     * committee directory, and accounting profile.
     */
    fun generateJsonBackupString(
        societyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>,
        members: List<Member> = emptyList()
    ): String {
        val root = JSONObject()

        val totalReceived = donations.filter { it.status == "received" }.sumOf { it.amount }
        val totalPending = donations.filter { it.status == "pending" }.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val netBalance = societyInfo.openingBalance + totalReceived - totalExpenses

        // 1. Export Metadata & Audit KPIs
        val metadata = JSONObject().apply {
            put("app_name", "Society Ledger & Transparency Portal")
            put("schema_version", "1.0")
            put("export_timestamp", System.currentTimeMillis())
            put("export_date", dateFormat.format(Date()))
            put("financial_year", societyInfo.financialYear)
            put("total_donations_count", donations.size)
            put("total_donations_received_amount", totalReceived)
            put("total_donations_pending_amount", totalPending)
            put("total_expenses_count", expenses.size)
            put("total_expenses_amount", totalExpenses)
            put("opening_balance", societyInfo.openingBalance)
            put("net_available_balance", netBalance)
        }
        root.put("export_metadata", metadata)

        // 2. Society Profile
        val societyJson = JSONObject().apply {
            put("name", societyInfo.name)
            put("registration_no", societyInfo.registrationNo)
            put("financial_year", societyInfo.financialYear)
            put("opening_balance", societyInfo.openingBalance)
            put("upi_id", societyInfo.upiId)
            put("bank_name", societyInfo.bankName)
            put("account_number", societyInfo.accountNumber)
            put("ifsc_code", societyInfo.ifscCode)
            put("bank_account", societyInfo.bankAccount)
            put("address", societyInfo.address)
            put("city", societyInfo.city)
            put("state", societyInfo.state)
            put("pin_code", societyInfo.pinCode)
            put("contact_phone", societyInfo.contactPhone)
            put("contact_email", societyInfo.contactEmail)
            put("admin_email", societyInfo.adminEmail)
            put("president_name", societyInfo.presidentName)
            put("secretary_name", societyInfo.secretaryName)
            put("treasurer_name", societyInfo.treasurerName)
        }
        root.put("society_profile", societyJson)

        // 3. Committee Members
        val membersArray = JSONArray()
        members.forEach { m ->
            val memberJson = JSONObject().apply {
                put("id", m.id)
                put("name", m.name)
                put("email", m.email)
                put("phone", m.phone)
                put("role", m.role)
                put("flat_no", m.flatNo)
                put("is_super_admin", m.isSuperAdmin)
                put("created_at", m.createdAt)
            }
            membersArray.put(memberJson)
        }
        root.put("committee_members", membersArray)

        // 4. Complete Donations & Pledges History
        val donationsArray = JSONArray()
        donations.forEach { d ->
            val donationJson = JSONObject().apply {
                put("id", d.id)
                put("receipt_no", d.receiptNo)
                put("donor_name", d.donorName)
                put("phone", d.phone)
                put("amount", d.amount)
                put("status", d.status)
                put("payment_mode", d.paymentMode)
                put("payment_date_timestamp", d.paymentDate)
                put("payment_date_formatted", dateFormat.format(Date(d.paymentDate)))
                put("recorded_by_name", d.recordedByName)
                put("notes", d.notes)
                put("created_at", d.createdAt)
            }
            donationsArray.put(donationJson)
        }
        root.put("donations_history", donationsArray)

        // 5. Complete Expenses Register
        val expensesArray = JSONArray()
        expenses.forEach { e ->
            val expenseJson = JSONObject().apply {
                put("id", e.id)
                put("voucher_no", "EXP-${e.id}")
                put("title", e.title)
                put("category", e.category)
                put("amount", e.amount)
                put("expense_date_timestamp", e.expenseDate)
                put("expense_date_formatted", dateFormat.format(Date(e.expenseDate)))
                put("paid_to", e.paidTo)
                put("payment_mode", e.paymentMode)
                put("receipt_reference", e.receiptUrl)
                put("recorded_by_name", e.recordedByName)
                put("created_at", e.createdAt)
            }
            expensesArray.put(expenseJson)
        }
        root.put("expenses_history", expensesArray)

        return root.toString(2)
    }

    /**
     * Exports the entire society's transaction history to a local JSON file and launches the Android save/share intent.
     */
    fun exportToJsonBackup(
        context: Context,
        societyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>,
        members: List<Member> = emptyList()
    ): File? {
        return try {
            val jsonString = generateJsonBackupString(societyInfo, donations, expenses, members)
            val cleanSocietyName = societyInfo.name.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "Society_${cleanSocietyName}_Backup_${timeFormat.format(Date())}.json"

            // Save to cache dir for sharing via FileProvider
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(jsonString, Charsets.UTF_8)

            // Also save persistent copy in app internal backups directory
            try {
                val persistentDir = File(context.filesDir, "backups")
                if (!persistentDir.exists()) persistentDir.mkdirs()
                val persistentFile = File(persistentDir, fileName)
                persistentFile.writeText(jsonString, Charsets.UTF_8)
            } catch (e: Exception) {
                // non-fatal
            }

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "${societyInfo.name} - Complete JSON Records Backup")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "🏛️ ${societyInfo.name} JSON Backup\nFinancial Year: ${societyInfo.financialYear}\nDonations: ${donations.size} | Expenses: ${expenses.size}\nGenerated: ${dateFormat.format(Date())}"
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Save / Export Society JSON Backup"))
            Toast.makeText(
                context,
                "JSON Backup generated: $fileName (${donations.size + expenses.size} transactions)",
                Toast.LENGTH_LONG
            ).show()

            cacheFile
        } catch (e: Exception) {
            Toast.makeText(context, "Error exporting JSON backup: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * Copies the full JSON backup string directly to the device clipboard.
     */
    fun copyJsonBackupToClipboard(
        context: Context,
        societyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>,
        members: List<Member> = emptyList()
    ) {
        try {
            val jsonString = generateJsonBackupString(societyInfo, donations, expenses, members)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Society JSON Backup", jsonString)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Entire Society JSON Backup copied to clipboard!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Error copying JSON: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    data class RestoreBackupData(
        val success: Boolean,
        val errorMessage: String? = null,
        val societyInfo: SocietyInfo? = null,
        val donations: List<Donation> = emptyList(),
        val expenses: List<Expense> = emptyList(),
        val members: List<Member> = emptyList(),
        val totalReceived: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val backupDate: String = ""
    )

    /**
     * Parses an exported JSON backup string back into strongly-typed models for database restoration.
     */
    fun parseJsonBackup(jsonString: String): RestoreBackupData {
        return try {
            val root = JSONObject(jsonString)

            val metadata = root.optJSONObject("export_metadata")
            val backupDate = metadata?.optString("backup_date_formatted") ?: ""

            // 1. Society Profile
            val socObj = root.optJSONObject("society_profile")
            val restoredSociety = if (socObj != null) {
                SocietyInfo(
                    name = socObj.optString("name", "Restored Society"),
                    registrationNo = socObj.optString("registration_no", ""),
                    financialYear = socObj.optString("financial_year", "FY 2026-2027"),
                    openingBalance = socObj.optDouble("opening_balance", 0.0),
                    upiId = socObj.optString("upi_id", ""),
                    bankName = socObj.optString("bank_name", ""),
                    accountNumber = socObj.optString("account_number", ""),
                    ifscCode = socObj.optString("ifsc_code", ""),
                    branchName = socObj.optString("branch_name", ""),
                    bankAccount = socObj.optString("bank_account", ""),
                    address = socObj.optString("address", ""),
                    city = socObj.optString("city", ""),
                    state = socObj.optString("state", ""),
                    pinCode = socObj.optString("pin_code", ""),
                    contactPhone = socObj.optString("contact_phone", ""),
                    contactEmail = socObj.optString("contact_email", ""),
                    adminEmail = socObj.optString("admin_email", ""),
                    presidentName = socObj.optString("president_name", ""),
                    secretaryName = socObj.optString("secretary_name", ""),
                    treasurerName = socObj.optString("treasurer_name", "")
                )
            } else null

            // 2. Members
            val membersList = mutableListOf<Member>()
            val membersArray = root.optJSONArray("committee_members")
            if (membersArray != null) {
                for (i in 0 until membersArray.length()) {
                    val m = membersArray.getJSONObject(i)
                    membersList.add(
                        Member(
                            name = m.optString("name", "Committee Member"),
                            email = m.optString("email", ""),
                            phone = m.optString("phone", ""),
                            role = m.optString("role", "Member"),
                            pin = m.optString("pin", "1234"),
                            isSuperAdmin = m.optBoolean("is_super_admin", false)
                        )
                    )
                }
            }

            // 3. Donations
            val donationsList = mutableListOf<Donation>()
            val donationsArray = root.optJSONArray("donations_history")
            if (donationsArray != null) {
                for (i in 0 until donationsArray.length()) {
                    val d = donationsArray.getJSONObject(i)
                    donationsList.add(
                        Donation(
                            donorName = d.optString("donor_name", "Anonymous"),
                            phone = d.optString("phone", ""),
                            amount = d.optDouble("amount", 0.0),
                            status = d.optString("status", "received"),
                            paymentMode = d.optString("payment_mode", "UPI"),
                            paymentDate = d.optLong("payment_date", System.currentTimeMillis()),
                            receiptNo = d.optString("receipt_no", "REC-${System.currentTimeMillis()}"),
                            recordedByName = d.optString("recorded_by_name", "Admin"),
                            notes = d.optString("notes", ""),
                            createdAt = d.optLong("created_at", System.currentTimeMillis()),
                            isModified = d.optBoolean("is_modified", false),
                            modificationReason = d.optString("modification_reason", ""),
                            isDeleted = d.optBoolean("is_deleted", false),
                            deletionReason = d.optString("deletion_reason", "")
                        )
                    )
                }
            }

            // 4. Expenses
            val expensesList = mutableListOf<Expense>()
            val expensesArray = root.optJSONArray("expenses_history")
            if (expensesArray != null) {
                for (i in 0 until expensesArray.length()) {
                    val e = expensesArray.getJSONObject(i)
                    expensesList.add(
                        Expense(
                            title = e.optString("title", "Expense Item"),
                            category = e.optString("category", "Maintenance"),
                            amount = e.optDouble("amount", 0.0),
                            expenseDate = e.optLong("expense_date", System.currentTimeMillis()),
                            paidTo = e.optString("paid_to", "Vendor"),
                            paymentMode = e.optString("payment_mode", "Online"),
                            receiptUrl = e.optString("receipt_reference", e.optString("receipt_url", "")),
                            recordedByName = e.optString("recorded_by_name", "Admin"),
                            createdAt = e.optLong("created_at", System.currentTimeMillis()),
                            isModified = e.optBoolean("is_modified", false),
                            modificationReason = e.optString("modification_reason", ""),
                            isDeleted = e.optBoolean("is_deleted", false),
                            deletionReason = e.optString("deletion_reason", "")
                        )
                    )
                }
            }

            val totalRec = donationsList.filter { it.status == "received" }.sumOf { it.amount }
            val totalExp = expensesList.sumOf { it.amount }

            RestoreBackupData(
                success = true,
                societyInfo = restoredSociety,
                donations = donationsList,
                expenses = expensesList,
                members = membersList,
                totalReceived = totalRec,
                totalExpenses = totalExp,
                backupDate = backupDate
            )
        } catch (e: Exception) {
            RestoreBackupData(
                success = false,
                errorMessage = "Invalid or corrupted JSON backup format: ${e.localizedMessage}"
            )
        }
    }
}
