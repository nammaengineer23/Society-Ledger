package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.CommitteeMemberAccount
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommitteeCumulativeAccountsDialog(
    accounts: List<CommitteeMemberAccount>,
    societyName: String,
    onDismiss: () -> Unit,
    onFilterMemberDonations: (Long) -> Unit = {},
    onFilterMemberExpenses: (Long) -> Unit = {}
) {
    val context = LocalContext.current

    val totalCommitteeCollections = accounts.sumOf { it.totalReceivedAmount }
    val totalCommitteeExpenses = accounts.sumOf { it.totalExpensesAmount }
    val totalNetCommitteeBalance = totalCommitteeCollections - totalCommitteeExpenses

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .testTag("committee_cumulative_accounts_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SapphireNavy.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Committee Cumulative Accounts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$societyName • General Ledger Audit",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Grand Total KPI Strip
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.07f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TOTAL COLLECTED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = ColorReceivedGreen, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.US, "%,.0f", totalCommitteeCollections)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ColorReceivedGreen)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TOTAL EXPENSED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = ColorExpenseRed, fontWeight = FontWeight.Bold)
                            Text("₹${String.format(Locale.US, "%,.0f", totalCommitteeExpenses)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ColorExpenseRed)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("NET IN CUSTODY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = SapphireNavy, fontWeight = FontWeight.Bold)
                            val sign = if (totalNetCommitteeBalance >= 0) "+" else ""
                            Text("$sign₹${String.format(Locale.US, "%,.0f", totalNetCommitteeBalance)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = SapphireNavy)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List of Member Accounts
                Text(
                    text = "Member-wise Breakdown (${accounts.size} Officers):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { acc ->
                        val member = acc.member
                        val netColor = when {
                            acc.netBalance > 0 -> Color(0xFF1565C0)
                            acc.netBalance < 0 -> Color(0xFFC62828)
                            else -> Color(0xFF2E7D32)
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.2f)) {
                                        Text(
                                            text = member.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = member.role,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        val sign = if (acc.netBalance > 0) "+" else ""
                                        Text(
                                            text = "$sign₹${String.format(Locale.US, "%,.0f", acc.netBalance)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = netColor
                                        )
                                        Text(
                                            text = acc.balanceStatusText,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = netColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Collected: ₹${String.format(Locale.US, "%,.0f", acc.totalReceivedAmount)} (${acc.receivedCount})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = ColorReceivedGreen
                                    )
                                    Text(
                                        text = "Expensed: ₹${String.format(Locale.US, "%,.0f", acc.totalExpensesAmount)} (${acc.expensesCount})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = ColorExpenseRed
                                    )
                                    Text(
                                        text = "Cash: ₹${String.format(Locale.US, "%,.0f", acc.cashInHand)}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                            val statement = buildString {
                                appendLine("📋 *SOCIETY COMMITTEE CUMULATIVE ACCOUNTS REPORT*")
                                appendLine("🏛️ Society: $societyName")
                                appendLine("─────────────────────────")
                                appendLine("💰 Total Collected: ₹${String.format(Locale.US, "%,.0f", totalCommitteeCollections)}")
                                appendLine("💸 Total Expensed: ₹${String.format(Locale.US, "%,.0f", totalCommitteeExpenses)}")
                                val sign = if (totalNetCommitteeBalance >= 0) "+" else ""
                                appendLine("⚖️ Net Committee Balance: $sign₹${String.format(Locale.US, "%,.0f", totalNetCommitteeBalance)}")
                                appendLine("─────────────────────────")
                                appendLine("MEMBER-WISE CUMULATIVE BREAKDOWN:")
                                accounts.forEachIndexed { idx, acc ->
                                    val memberSign = if (acc.netBalance >= 0) "+" else ""
                                    appendLine("${idx + 1}. *${acc.member.name}* (${acc.member.role})")
                                    appendLine("   • Collected: ₹${String.format(Locale.US, "%,.0f", acc.totalReceivedAmount)} (${acc.receivedCount} receipts)")
                                    appendLine("   • Expensed: ₹${String.format(Locale.US, "%,.0f", acc.totalExpensesAmount)} (${acc.expensesCount} vouchers)")
                                    appendLine("   • Net Balance: $memberSign₹${String.format(Locale.US, "%,.0f", acc.netBalance)} (${acc.balanceStatusText})")
                                    appendLine("   • Cash in Hand: ₹${String.format(Locale.US, "%,.0f", acc.cashInHand)}")
                                }
                                appendLine("─────────────────────────")
                                appendLine("📅 Generated on ${dateFormat.format(Date())}")
                            }

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, statement)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Committee Accounts Statement"))
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Statement", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
