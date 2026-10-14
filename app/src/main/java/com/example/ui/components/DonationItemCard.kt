package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.data.model.SocietyInfo
import com.example.ui.theme.ColorBank
import com.example.ui.theme.ColorBankBg
import com.example.ui.theme.ColorCash
import com.example.ui.theme.ColorCashBg
import com.example.ui.theme.ColorCheque
import com.example.ui.theme.ColorChequeBg
import com.example.ui.theme.ColorPendingBg
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorReceivedBg
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.ColorUPI
import com.example.ui.theme.ColorUPIBg
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DonationItemCard(
    donation: Donation,
    societyInfo: SocietyInfo,
    isAdmin: Boolean,
    onViewReceipt: (Donation) -> Unit,
    onMarkAsPaid: (Donation) -> Unit,
    onEdit: (Donation) -> Unit,
    onDelete: (Donation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appLanguage = LocalAppLanguage.current
    val strings = LocalAppStrings.current
    val isReceived = donation.status == "received"
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var menuExpanded by remember { mutableStateOf(false) }

    val isDeleted = donation.isDeleted
    val isModified = donation.isModified

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("donation_card_${donation.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDeleted) {
                Color(0xFFFFEBEE) // Soft Red container for deleted
            } else if (isModified) {
                Color(0xFFFFF8E1) // Soft Amber container for modified
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isDeleted) {
            androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD32F2F))
        } else if (isModified) {
            androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFA000))
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDeleted) 0.dp else 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Status badge, Receipt No & Overflow Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDeleted) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFD32F2F), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "VOIDED / EXCLUDED",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isReceived) ColorReceivedBg else ColorPendingBg,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isReceived) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = if (isReceived) ColorReceivedGreen else ColorPendingOrange,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isReceived) strings.filterReceived.uppercase() else strings.filterPending.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isReceived) ColorReceivedGreen else ColorPendingOrange
                                )
                            }
                        }
                    }

                    if (isModified && !isDeleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF8F00), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "MODIFIED",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = donation.receiptNo,
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Amount & Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDeleted) "₹ ${String.format("%,.0f", donation.amount)} (VOID)" else "₹ ${String.format("%,.0f", donation.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDeleted) Color(0xFFD32F2F) else if (isReceived) ColorReceivedGreen else ColorPendingOrange,
                        textDecoration = if (isDeleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    )

                    if (!isDeleted) {
                        Box {
                            IconButton(
                                onClick = { menuExpanded = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                            }

                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                if (isReceived) {
                                    DropdownMenuItem(
                                        text = { Text(strings.viewReceiptBtn) },
                                        onClick = {
                                            menuExpanded = false
                                            onViewReceipt(donation)
                                        },
                                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF1A237E)) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.shareReceiptBtn) },
                                        onClick = {
                                            menuExpanded = false
                                            PdfReceiptGenerator.sharePdfReceipt(context, donation, societyInfo)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Share Receipt Image") },
                                        onClick = {
                                            menuExpanded = false
                                            PdfReceiptGenerator.shareImageReceipt(context, donation, societyInfo)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Send Receipt on WhatsApp") },
                                        onClick = {
                                            menuExpanded = false
                                            WhatsAppReminderHelper.sendPaymentConfirmationReceipt(context, donation, societyInfo)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = { Text(strings.sendWhatsAppReminder) },
                                        onClick = {
                                            menuExpanded = false
                                            WhatsAppReminderHelper.sendPendingReminder(context, donation, societyInfo)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                                    )
                                    if (isAdmin) {
                                        DropdownMenuItem(
                                            text = { Text(strings.markAsPaidBtn) },
                                            onClick = {
                                                menuExpanded = false
                                                onMarkAsPaid(donation)
                                            },
                                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                                        )
                                    }
                                }

                                if (isAdmin) {
                                    DropdownMenuItem(
                                        text = { Text(strings.editBtn) },
                                        onClick = {
                                            menuExpanded = false
                                            onEdit(donation)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(strings.deleteBtn, color = Color.Red) },
                                        onClick = {
                                            menuExpanded = false
                                            onDelete(donation)
                                        },
                                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                        modifier = Modifier.testTag("delete_donation_menu_item")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isDeleted && !donation.deletionReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFCDD2), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "❌ Void Reason: ${donation.deletionReason} (By ${donation.deletedByName ?: "Admin"})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFB71C1C)
                    )
                }
            }

            if (isModified && !isDeleted && !donation.modificationReason.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFECB3), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✏️ Modified Reason: ${donation.modificationReason} (By ${donation.modifiedByName ?: "Admin"})",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Donor Name and Phone
            Text(
                text = donation.donorName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📞 +91 ${donation.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFormat.format(Date(donation.paymentDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (donation.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${strings.noteLabel}: ${donation.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Footer: Payment Mode chip & Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mode Chip
                if (isReceived) {
                    val (modeColor, modeBg, modeIcon) = when {
                        donation.paymentMode.contains("UPI", ignoreCase = true) -> Triple(ColorUPI, ColorUPIBg, Icons.Default.CreditCard)
                        donation.paymentMode.contains("Cash", ignoreCase = true) -> Triple(ColorCash, ColorCashBg, Icons.Default.Payment)
                        donation.paymentMode.contains("Bank", ignoreCase = true) -> Triple(ColorBank, ColorBankBg, Icons.Default.AccountBalance)
                        else -> Triple(ColorCheque, ColorChequeBg, Icons.Default.Receipt)
                    }

                    Box(
                        modifier = Modifier
                            .background(modeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = modeIcon, contentDescription = null, tint = modeColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = donation.paymentMode,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = modeColor
                            )
                        }
                    }
                } else {
                    Text(
                        text = "${strings.recordedByLabel}: ${donation.recordedByName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isReceived) {
                        OutlinedButton(
                            onClick = { onViewReceipt(donation) },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(strings.viewReceiptBtn, style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        Button(
                            onClick = {
                                WhatsAppReminderHelper.sendPendingReminder(context, donation, societyInfo, appLanguage)
                            },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(strings.sendWhatsAppReminder, style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }

                        if (isAdmin) {
                            Button(
                                onClick = { onMarkAsPaid(donation) },
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorReceivedGreen),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(strings.markAsPaidBtn, style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
