package com.example.data.model

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data model for App Service Complaints, Helpdesk inquiries, and Support Tickets.
 */
data class AppComplaint(
    val id: String = "TKT-${System.currentTimeMillis() % 100000}",
    val societyName: String = "",
    val societyId: String = "",
    val submittedByName: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val category: String = "General Inquiry", // "Payment & Razorpay", "Cloud Sync Issue", "PDF & Receipts", "Account Access / PIN", "Feature Request", "Service General"
    val subject: String = "",
    val description: String = "",
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "RESOLVED", "REJECTED"
    val priority: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "CRITICAL"
    val adminNotes: String = "",
    val resolutionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
) {
    val formattedDate: String
        get() = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(createdAt))

    val statusColor: Color
        get() = when (status.uppercase()) {
            "OPEN" -> Color(0xFFE65100) // Deep Orange
            "IN_PROGRESS" -> Color(0xFF1976D2) // Blue
            "RESOLVED" -> Color(0xFF2E7D32) // Green
            "REJECTED" -> Color(0xFFD32F2F) // Red
            else -> Color(0xFF757575)
        }

    val priorityColor: Color
        get() = when (priority.uppercase()) {
            "CRITICAL" -> Color(0xFFD32F2F)
            "HIGH" -> Color(0xFFE65100)
            "MEDIUM" -> Color(0xFFF57C00)
            "LOW" -> Color(0xFF388E3C)
            else -> Color(0xFF757575)
        }
}
