package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppComplaint
import com.example.data.model.SocietyInfo
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

/**
 * User-friendly modal dialog allowing members and residents to send feedback,
 * feature requests, or support inquiries directly to the Master Admin email (nammaengineer23@gmail.com).
 */
@Composable
fun ContactSupportDialog(
    societyInfo: SocietyInfo,
    onDismiss: () -> Unit,
    onSubmitTicket: ((AppComplaint) -> Unit)? = null,
    masterAdminEmail: String = "nammaengineer23@gmail.com",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val categories = listOf(
        "General Feedback",
        "Feature Request",
        "Bug / Technical Issue",
        "Subscription & Pro",
        "Data / Backup Help",
        "Account & Access"
    )

    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var senderName by remember { mutableStateOf("") }
    var senderContact by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("contact_support_dialog"),
        shape = RoundedCornerShape(20.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SapphireNavy.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = "Contact Support",
                    tint = SapphireNavy,
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Contact Support",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SapphireNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Direct Line to Master Admin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isSubmitted) {
                    // Success View
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Request Dispatched!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your message has been routed to the Master Admin email ($masterAdminEmail) and logged in the App Support Desk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF2E7D32),
                                lineHeight = 18.sp
                            )
                        }
                    }
                } else {
                    // Recipient Banner
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = IndianSaffron,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "To: Master Admin",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = SapphireNavy
                                    )
                                    Text(
                                        text = masterAdminEmail,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Master Admin Email", masterAdminEmail))
                                    Toast.makeText(context, "Email copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Email",
                                    tint = SapphireNavy,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Category Selection Chips
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SapphireNavy,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Member Name
                    OutlinedTextField(
                        value = senderName,
                        onValueChange = {
                            senderName = it
                            errorMessage = null
                        },
                        label = { Text("Your Name / Flat Number") },
                        placeholder = { Text("e.g. Rahul Sharma (Flat 402)") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SapphireNavy)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("support_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireNavy,
                            focusedLabelColor = SapphireNavy
                        )
                    )

                    // Contact info
                    OutlinedTextField(
                        value = senderContact,
                        onValueChange = {
                            senderContact = it
                            errorMessage = null
                        },
                        label = { Text("Your Email or Phone Number") },
                        placeholder = { Text("e.g. member@gmail.com / 9876543210") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = SapphireNavy)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("support_contact_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireNavy,
                            focusedLabelColor = SapphireNavy
                        )
                    )

                    // Subject
                    OutlinedTextField(
                        value = subject,
                        onValueChange = {
                            subject = it
                            errorMessage = null
                        },
                        label = { Text("Subject / Brief Summary") },
                        placeholder = { Text("e.g. Inquiring about Pro multi-society renewal") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("support_subject_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireNavy,
                            focusedLabelColor = SapphireNavy
                        )
                    )

                    // Message
                    OutlinedTextField(
                        value = message,
                        onValueChange = {
                            message = it
                            errorMessage = null
                        },
                        label = { Text("Detailed Feedback or Request") },
                        placeholder = { Text("Please describe your feedback, questions, or issue in detail...") },
                        minLines = 4,
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("support_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SapphireNavy,
                            focusedLabelColor = SapphireNavy
                        )
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isSubmitted) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close")
                }
            } else {
                Button(
                    onClick = {
                        if (senderName.isBlank()) {
                            errorMessage = "Please enter your name or flat number"
                            return@Button
                        }
                        if (message.isBlank()) {
                            errorMessage = "Please write your feedback or request"
                            return@Button
                        }

                        val cleanSubject = subject.ifBlank { "$selectedCategory from ${societyInfo.name}" }
                        val fullBody = buildString {
                            appendLine("Society: ${societyInfo.name} (${societyInfo.registrationNo.ifBlank { "Unregistered" }})")
                            appendLine("Sender: ${senderName.trim()}")
                            if (senderContact.isNotBlank()) {
                                appendLine("Contact: ${senderContact.trim()}")
                            }
                            appendLine("Category: $selectedCategory")
                            appendLine()
                            appendLine("--- Message / Feedback ---")
                            appendLine(message.trim())
                            appendLine()
                            appendLine("--- Device Info ---")
                            appendLine("Sent from Society Ledger App • Android")
                        }

                        // 1. Log ticket in App Desk
                        val complaint = AppComplaint(
                            id = "SUP-${System.currentTimeMillis() % 100000}",
                            societyName = societyInfo.name,
                            societyId = societyInfo.registrationNo.ifBlank { "default_society" },
                            submittedByName = senderName.trim(),
                            contactPhone = senderContact.trim().ifBlank { societyInfo.contactPhone },
                            contactEmail = masterAdminEmail,
                            category = selectedCategory,
                            subject = cleanSubject,
                            description = message.trim(),
                            status = "OPEN",
                            priority = if (selectedCategory.contains("Bug", ignoreCase = true)) "HIGH" else "MEDIUM",
                            createdAt = System.currentTimeMillis()
                        )
                        onSubmitTicket?.invoke(complaint)

                        // 2. Launch Email client targeting master admin email
                        try {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$masterAdminEmail")
                                putExtra(Intent.EXTRA_SUBJECT, "[Society Support] $cleanSubject")
                                putExtra(Intent.EXTRA_TEXT, fullBody)
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "Send Email via..."))
                        } catch (e: Exception) {
                            // Fallback if no email client is installed
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Support Message", "To: $masterAdminEmail\nSubject: $cleanSubject\n\n$fullBody"))
                            Toast.makeText(context, "Feedback logged & message copied to clipboard!", Toast.LENGTH_LONG).show()
                        }

                        isSubmitted = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("send_support_email_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = IndianSaffron
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send Request", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        },
        dismissButton = {
            if (!isSubmitted) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("cancel_support_btn")
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
