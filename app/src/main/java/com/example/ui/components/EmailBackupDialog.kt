package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachEmail
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun EmailBackupDialog(
    societyInfo: SocietyInfo,
    donations: List<Donation>,
    expenses: List<Expense>,
    members: List<Member>,
    currentMember: Member?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    // Default recipient email preference
    val defaultEmail = remember {
        currentMember?.email?.takeIf { it.isNotBlank() }
            ?: societyInfo.contactEmail.takeIf { it.isNotBlank() }
            ?: "committee@society.org"
    }

    var recipientEmail by remember { mutableStateOf(defaultEmail) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val totalReceived = donations.filter { it.status == "received" }.sumOf { it.amount }
    val totalPending = donations.filter { it.status == "pending" }.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netBalance = societyInfo.openingBalance + totalReceived - totalExpenses

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .testTag("email_backup_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachEmail,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = strings.backupToEmail,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Export & Email Ledger Records",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Database Ledger Snapshot Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📦 Data Package Contents",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = ColorReceivedGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Full Audit Backup",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = ColorReceivedGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Donations & Pledges:", style = MaterialTheme.typography.bodySmall)
                            Text("${donations.size} records (₹ ${String.format("%,.0f", totalReceived)})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Expense Entries:", style = MaterialTheme.typography.bodySmall)
                            Text("${expenses.size} records (₹ ${String.format("%,.0f", totalExpenses)})", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Committee Directory:", style = MaterialTheme.typography.bodySmall)
                            Text("${members.size} members", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Net Balance Reserve:", style = MaterialTheme.typography.bodySmall)
                            Text("₹ ${String.format("%,.0f", netBalance)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = SapphireNavy)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("• Data Persistence:", style = MaterialTheme.typography.bodySmall)
                            Text("Secure Offline Storage Engine", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = ColorReceivedGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Email Input Field
                OutlinedTextField(
                    value = recipientEmail,
                    onValueChange = {
                        recipientEmail = it
                        emailError = if (it.isNotBlank() && !it.contains("@")) "Please enter a valid email address" else null
                    },
                    label = { Text(strings.emailRecipientLabel) },
                    placeholder = { Text("e.g. auditor@society.org") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = SapphireNavy)
                    },
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                            ?: Text("The backup CSV and formatted summary will be sent here.", fontSize = 11.sp)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("backup_recipient_email_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Quick Email Suggestions Chips
                val emailSuggestions = remember(members, currentMember, societyInfo) {
                    val list = mutableListOf<Pair<String, String>>()
                    currentMember?.email?.takeIf { it.isNotBlank() }?.let { list.add("My Email" to it) }
                    societyInfo.contactEmail.takeIf { it.isNotBlank() }?.let { list.add("Society Helpline" to it) }
                    members.filter { it.isSuperAdmin && it.email.isNotBlank() }.forEach {
                        list.add("${it.name.split(" ")[0]} (Admin)" to it.email)
                    }
                    list.distinctBy { it.second }
                }

                if (emailSuggestions.isNotEmpty()) {
                    Text(
                        text = "Quick Suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        emailSuggestions.forEach { (label, email) ->
                            val isSelected = recipientEmail.trim().equals(email.trim(), ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        recipientEmail = email
                                        emailError = null
                                    }
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(strings.cancelBtn)
                    }

                    Button(
                        onClick = {
                            val cleanEmail = recipientEmail.trim()
                            if (cleanEmail.isNotEmpty() && !cleanEmail.contains("@")) {
                                emailError = "Please enter a valid email address"
                                return@Button
                            }
                            ExportHelper.sendBackupEmail(
                                context = context,
                                societyInfo = societyInfo,
                                donations = donations,
                                expenses = expenses,
                                members = members,
                                recipientEmail = cleanEmail
                            )
                            Toast.makeText(context, "Opening email app with ledger backup...", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("send_email_backup_action_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(strings.sendBackupBtn, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
