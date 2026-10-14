package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Compulsory Reason Dialog for Super Admin Deletion / Voiding.
 * Strictly required by statutory audit & financial transparency bylaws.
 */
@Composable
fun SuperAdminDeleteConfirmDialog(
    itemTitle: String,
    itemAmount: Double,
    itemRef: String,
    isExpense: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmDelete: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("super_admin_delete_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (isExpense) "Void Expense Transaction" else "Void Donation Transaction",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Item: $itemTitle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Amount: ₹${String.format("%,.2f", itemAmount)} | Ref: $itemRef",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "⚠️ Audit Notice: This transaction will be highlighted in RED, excluded from accounts/totals, and logged permanently with your reason in the Statutory Audit Trail.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mandatory Reason for Deletion / Void *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = { Text("e.g., Duplicate voucher #104, bounced cheque verified by bank, wrong ledger entry...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("super_admin_delete_reason_input"),
                    minLines = 3,
                    maxLines = 4,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = "Required for Committee & Resident audit reports (min 5 characters)")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().length < 5) {
                        errorMessage = "Please enter a valid justification (minimum 5 characters)."
                    } else {
                        onConfirmDelete(reason.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_super_admin_delete_button")
            ) {
                Text("Confirm Void & Log Audit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Compulsory Reason Dialog for Super Admin Transaction Modification.
 * Strictly required by audit & transparency bylaws.
 */
@Composable
fun SuperAdminModifyConfirmDialog(
    itemTitle: String,
    itemAmount: Double,
    itemRef: String,
    isExpense: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmModify: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("super_admin_modify_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.EditNote,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = if (isExpense) "Modify Expense Transaction" else "Modify Donation Transaction",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Item: $itemTitle",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "Amount: ₹${String.format("%,.2f", itemAmount)} | Ref: $itemRef",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "✏️ Audit Notice: Changes will be highlighted with an AMBER badge and logged with your reason in the Statutory Audit Trail.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mandatory Reason for Modification *",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = {
                        reason = it
                        if (errorMessage != null) errorMessage = null
                    },
                    placeholder = { Text("e.g., Corrected payment mode from Cash to UPI per bank statement...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("super_admin_modify_reason_input"),
                    minLines = 3,
                    maxLines = 4,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(text = "Required for Committee & Resident audit reports (min 5 characters)")
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.trim().length < 5) {
                        errorMessage = "Please enter a valid justification (minimum 5 characters)."
                    } else {
                        onConfirmModify(reason.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                modifier = Modifier.testTag("confirm_super_admin_modify_button")
            ) {
                Text("Proceed with Modification", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Access Restriction Dialog shown when a standard Committee Member attempts to Modify or Delete.
 */
@Composable
fun CommitteeRestrictedActionDialog(
    actionName: String, // "Modify" or "Delete"
    onDismiss: () -> Unit
) {
    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("committee_restricted_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Admin Authority Required",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "As per Society Financial Governance Bylaws:",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Committee members are strictly authorized to ENTER donations and expenses.\n" +
                            "• Only the Admin can $actionName transactions.\n" +
                            "• All modifications and deletions require a compulsory audit reason and are logged in the public Transparency Reports.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Understood")
            }
        }
    )
}
