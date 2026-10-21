package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CommitteeMemberAccountBalance
import com.example.data.model.Member
import com.example.data.model.Transaction
import java.util.Locale

@Composable
fun CommitteeMemberSettlementDialog(
    member: Member,
    transactions: List<Transaction>,
    onDismiss: () -> Unit,
    onSave: (amount: Double, trustPaysMember: Boolean, paymentMode: String, referenceNo: String, notes: String) -> Unit
) {
    var amountText by remember(member.id) { mutableStateOf("") }
    var trustPaysMember by remember(member.id, transactions) {
        mutableStateOf(CommitteeMemberAccountBalance.calculate(member.id, transactions).netAmountDueToTrust < 0.0)
    }
    var paymentMode by remember(member.id) { mutableStateOf("UPI/Online") }
    var referenceNo by remember(member.id) { mutableStateOf("") }
    var notes by remember(member.id) { mutableStateOf("") }
    var error by remember(member.id) { mutableStateOf<String?>(null) }

    val balance = remember(member.id, transactions) {
        CommitteeMemberAccountBalance.calculate(member.id, transactions)
    }
    val outstanding = if (trustPaysMember) balance.amountTrustMustReimburse else balance.amountMemberMustReturn
    val amount = amountText.toDoubleOrNull()
    val validAmount = amount != null && amount.isFinite() && amount > 0.0 && amount <= outstanding + 0.005
    val ledger = remember(member.id, transactions) {
        transactions.filter { it.committeeMemberId == member.id && !it.isDeleted }
            .sortedByDescending { it.paymentDate }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Committee account — ${member.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val balanceText = when {
                    balance.isSettled -> "Settled balance: ₹0.00"
                    balance.netAmountDueToTrust > 0 -> "Member must return: ₹${"%.2f".format(Locale.ROOT, balance.amountMemberMustReturn)}"
                    else -> "Trust must reimburse: ₹${"%.2f".format(Locale.ROOT, balance.amountTrustMustReimburse)}"
                }
                Text(balanceText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("Settlement direction", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { trustPaysMember = false; amountText = ""; error = null },
                        enabled = balance.amountMemberMustReturn > 0.0,
                        modifier = Modifier.weight(1f)
                    ) { Text("Member → Trust") }
                    OutlinedButton(
                        onClick = { trustPaysMember = true; amountText = ""; error = null },
                        enabled = balance.amountTrustMustReimburse > 0.0,
                        modifier = Modifier.weight(1f)
                    ) { Text("Trust → Member") }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        if (it.isBlank() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                            amountText = it
                            error = null
                        }
                    },
                    label = { Text("Settlement amount (₹)") },
                    supportingText = { Text("Available to settle: ₹${"%.2f".format(Locale.ROOT, outstanding)}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Payment method", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Cash", "UPI/Online", "Bank Transfer", "Cheque").forEach { mode ->
                        if (paymentMode == mode) {
                            Button(onClick = { paymentMode = mode }, modifier = Modifier.weight(1f)) { Text(mode, maxLines = 1) }
                        } else {
                            OutlinedButton(onClick = { paymentMode = mode }, modifier = Modifier.weight(1f)) { Text(mode, maxLines = 1) }
                        }
                    }
                }
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("Bank / UPI / cheque reference (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Reason / audit note *") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                Text("Recent ledger entries", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 120.dp)) {
                    items(ledger.take(8), key = { it.id }) { entry ->
                        Column(modifier = Modifier.padding(vertical = 3.dp)) {
                            Text(entry.title, style = MaterialTheme.typography.bodySmall)
                            Text(
                                "${entry.status} • ₹${"%.2f".format(Locale.ROOT, entry.amount)} • ${entry.paymentMode}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (ledger.isEmpty()) item { Text("No linked ledger entries yet.", style = MaterialTheme.typography.bodySmall) }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when {
                    !validAmount -> error = "Enter an amount greater than zero and no more than the outstanding balance."
                    notes.isBlank() -> error = "Add a reason so the settlement is auditable."
                    else -> {
                        onSave(amount!!, trustPaysMember, paymentMode, referenceNo.trim(), notes.trim())
                        onDismiss()
                    }
                }
            }) { Text("Record settlement") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
