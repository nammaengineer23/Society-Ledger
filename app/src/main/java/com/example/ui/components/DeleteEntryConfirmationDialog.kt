package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.LocalThemeIsDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Confirmation dialog before deleting any donation or expense entry
 * to prevent accidental data loss and maintain statutory audit transparency.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeleteEntryConfirmationDialog(
    itemType: String, // "Donation" or "Expense"
    itemTitle: String,
    itemAmount: Double,
    itemRef: String,
    categoryOrNotes: String = "",
    dateTimestamp: Long = 0L,
    onDismiss: () -> Unit,
    onConfirmDelete: (reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val isExpense = itemType.equals("Expense", ignoreCase = true)

    val quickReasons = if (isExpense) {
        listOf("Duplicate voucher", "Wrong amount entered", "Invoice voided / cancelled", "Test expense entry", "Entry made in error")
    } else {
        listOf("Duplicate entry", "Wrong amount entered", "Cheque bounced / cancelled", "Test donation entry", "Entry made in error")
    }

    var selectedReason by remember { mutableStateOf(quickReasons.first()) }
    var customReason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val formattedDate = remember(dateTimestamp) {
        if (dateTimestamp > 0L) {
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dateTimestamp))
        } else {
            ""
        }
    }

    val finalReason = if (customReason.isNotBlank()) customReason.trim() else selectedReason

    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.testTag("delete_confirmation_dialog"),
        shape = RoundedCornerShape(24.dp),
        icon = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = if (isDark) 0.35f else 0.7f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Delete Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Delete $itemType Entry?",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("delete_dialog_title")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Confirm deletion to prevent accidental data loss",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error
                    ),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Summary Card displaying the item details about to be deleted
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_item_summary_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF231C1D) else Color(0xFFFFF5F5)
                    ),
                    border = BorderStroke(1.dp, if (isDark) Color(0xFF4D2628) else Color(0xFFFFCDD2))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = itemTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("delete_entry_title")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%,.2f", itemAmount)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = ColorExpenseRed
                                ),
                                modifier = Modifier.testTag("delete_entry_amount")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = itemRef,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (formattedDate.isNotBlank()) {
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        if (categoryOrNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = categoryOrNotes,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Reason Selector Chips
                Text(
                    text = "Reason for deletion (Audit record):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickReasons.forEach { reason ->
                        val isSelected = selectedReason == reason && customReason.isBlank()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedReason = reason
                                customReason = ""
                                errorMessage = null
                            },
                            label = {
                                Text(
                                    text = reason,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Optional Custom Reason Input
                OutlinedTextField(
                    value = customReason,
                    onValueChange = {
                        customReason = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = {
                        Text(
                            text = "Or type custom reason (min 5 chars)...",
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delete_reason_input"),
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (finalReason.length < 5) {
                        errorMessage = "Please enter or select a valid reason (min 5 characters)."
                    } else {
                        onConfirmDelete(finalReason)
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.testTag("confirm_delete_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Delete Entry",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("cancel_delete_button")
            ) {
                Text(
                    text = "Cancel",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/**
 * Specialized Confirmation Dialog for Donation Deletion.
 */
@Composable
fun DeleteDonationConfirmationDialog(
    donation: Donation,
    onDismiss: () -> Unit,
    onConfirmDelete: (reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    DeleteEntryConfirmationDialog(
        itemType = "Donation",
        itemTitle = donation.donorName.ifBlank { "Anonymous Donor" },
        itemAmount = donation.amount,
        itemRef = "Receipt #${donation.receiptNo}",
        categoryOrNotes = if (donation.notes.isNotBlank()) donation.notes else "Payment: ${donation.paymentMode} (${donation.status})",
        dateTimestamp = donation.paymentDate,
        onDismiss = onDismiss,
        onConfirmDelete = onConfirmDelete,
        modifier = modifier
    )
}

/**
 * Specialized Confirmation Dialog for Expense Deletion.
 */
@Composable
fun DeleteExpenseConfirmationDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirmDelete: (reason: String) -> Unit,
    modifier: Modifier = Modifier
) {
    DeleteEntryConfirmationDialog(
        itemType = "Expense",
        itemTitle = expense.title,
        itemAmount = expense.amount,
        itemRef = "Voucher #${expense.id} • ${expense.category}",
        categoryOrNotes = "Paid to: ${expense.paidTo} via ${expense.paymentMode}",
        dateTimestamp = expense.expenseDate,
        onDismiss = onDismiss,
        onConfirmDelete = onConfirmDelete,
        modifier = modifier
    )
}
