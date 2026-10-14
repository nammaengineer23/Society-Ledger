package com.example.ui.components

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
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
import com.example.data.model.CommitteeMemberAccount
import com.example.data.model.Member
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommitteeMemberAccountCard(
    account: CommitteeMemberAccount,
    societyName: String,
    isCurrent: Boolean,
    isAdminMode: Boolean,
    isSuperAdmin: Boolean,
    onFilterDonations: (Long) -> Unit = {},
    onFilterExpenses: (Long) -> Unit = {},
    onChangePinClick: (member: Member, isSuperAdminReset: Boolean) -> Unit = { _, _ -> },
    onEditMemberClick: (Member) -> Unit = {},
    onDeleteMemberClick: (Member) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }
    val member = account.member

    val balanceColor = when {
        account.netBalance > 0 -> Color(0xFF1565C0) // Deep Blue for custodial surplus
        account.netBalance < 0 -> Color(0xFFC62828) // Red for society payable / reimbursement
        else -> Color(0xFF2E7D32) // Green for settled zero balance
    }

    val balanceStatusBadge = when {
        account.netBalance > 0 -> "In Custody"
        account.netBalance < 0 -> "Society Payable"
        else -> "Settled"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("committee_member_account_card_${member.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) Color(0xFFE8F5E9).copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, SocietyTealPrimary) else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar, Name, Role, and Action Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (member.isSuperAdmin) SapphireNavy else if (isCurrent) SocietyTealPrimary else Color(0xFF78909C),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = member.name.take(1).uppercase(Locale.ROOT),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (member.isSuperAdmin) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(SapphireNavy, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "SUPER ADMIN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = buildString {
                                append(member.role)
                                if (member.flatNo.isNotBlank()) append(" • Flat ${member.flatNo}")
                                if (member.phone.isNotBlank()) append(" • 📞 ${member.phone}")
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Status Badges & Admin buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrent) {
                        Box(
                            modifier = Modifier
                                .background(ColorReceivedGreen, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Active",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Member PIN reset / change (Admin only)
                    if (isSuperAdmin) {
                        IconButton(
                            onClick = { onChangePinClick(member, true) },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("change_pin_member_${member.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = "Reset PIN (Admin Only)",
                                tint = SapphireNavy,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Edit & Delete Icons in Header (Admin Only)
                    if (isSuperAdmin) {
                        IconButton(
                            onClick = { onEditMemberClick(member) },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("edit_member_${member.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Member",
                                tint = SapphireNavy,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = { onDeleteMemberClick(member) },
                            modifier = Modifier
                                .size(30.dp)
                                .testTag("delete_member_${member.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Member",
                                tint = ColorExpenseRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dedicated Quick Management Action Row (Modify Member, PIN Reset, Delete) - Strictly Admin Only
            if (isSuperAdmin) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onEditMemberClick(member) },
                        modifier = Modifier
                            .weight(1.2f)
                            .testTag("btn_modify_member_${member.id}"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SapphireNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Modify Member",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SapphireNavy
                        )
                    }

                    OutlinedButton(
                        onClick = { onChangePinClick(member, true) },
                        modifier = Modifier
                            .weight(0.9f)
                            .testTag("btn_change_pin_${member.id}"),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LockReset,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SapphireNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reset PIN",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = SapphireNavy
                        )
                    }

                    IconButton(
                        onClick = { onDeleteMemberClick(member) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_member_${member.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Member",
                            tint = ColorExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CUMULATIVE ACCOUNT METRIC GRID
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cumulative Collections
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onFilterDonations(member.id) }
                    ) {
                        Text(
                            text = "COLLECTIONS",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ColorReceivedGreen,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%,.0f", account.totalReceivedAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = ColorReceivedGreen
                        )
                        Text(
                            text = "${account.receivedCount} receipts",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Cumulative Expenses
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onFilterExpenses(member.id) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXPENSES",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = ColorExpenseRed,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%,.0f", account.totalExpensesAmount)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = ColorExpenseRed
                        )
                        Text(
                            text = "${account.expensesCount} vouchers",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Cumulative Net Balance
                    Column(
                        modifier = Modifier.weight(1.1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "NET ACCOUNT",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = balanceColor,
                            fontWeight = FontWeight.Bold
                        )
                        val sign = if (account.netBalance > 0) "+" else ""
                        Text(
                            text = "$sign₹${String.format(Locale.US, "%,.0f", account.netBalance)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = balanceColor
                        )
                        Box(
                            modifier = Modifier
                                .background(balanceColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = balanceStatusBadge,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = balanceColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Expand / Collapse Bar & Quick Stats
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = SapphireNavy,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isExpanded) "Hide Account Breakdown" else "View Account Breakdown (${account.totalTransactionsCount} records)",
                        style = MaterialTheme.typography.labelSmall,
                        color = SapphireNavy,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (account.cashInHand != 0.0) {
                    Text(
                        text = "Cash In-Hand: ₹${String.format(Locale.US, "%,.0f", account.cashInHand)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = if (account.cashInHand >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // EXPANDED ITEMIZATION
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mode Breakdown Section
                    Text(
                        text = "Payment Mode Distribution:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💵 Cash Collections",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.0f", account.cashReceived)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "📱 UPI / Online",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.0f", account.onlineReceived)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🏦 Cheque / Bank",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.0f", account.chequeReceived)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (account.totalPendingAmount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(IndianSaffron.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⏳ Pending Pledges Follow-up:",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ColorPendingOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${String.format(Locale.US, "%,.0f", account.totalPendingAmount)} (${account.pendingCount} donors)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ColorPendingOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Recent Recorded Entries Preview
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Transactions Handled:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    val recentDonations = account.donations.take(3)
                    val recentExpenses = account.expenses.take(3)

                    if (recentDonations.isEmpty() && recentExpenses.isEmpty()) {
                        Text(
                            text = "No individual records logged yet for this member.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

                        // Recent collections
                        recentDonations.forEach { d ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• [Donation] ${d.donorName}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${dateFormat.format(Date(d.paymentDate))})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "+₹${d.amount.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = ColorReceivedGreen
                                )
                            }
                        }

                        // Recent expenses
                        recentExpenses.forEach { e ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "• [Expense] ${e.title}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "(${dateFormat.format(Date(e.expenseDate))})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "-₹${e.amount.toInt()}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = ColorExpenseRed
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Filter Donations, Filter Expenses, Share Statement
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onFilterDonations(member.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_filter_member_donations_${member.id}")
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Collections", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }

                        OutlinedButton(
                            onClick = { onFilterExpenses(member.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_filter_member_expenses_${member.id}")
                        ) {
                            Icon(Icons.Default.FilterAlt, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Expenses", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }

                        Button(
                            onClick = {
                                val statement = account.toShareableStatement(societyName)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, statement)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Member Account Statement")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("btn_share_member_statement_${member.id}")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Ledger", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                        }
                    }
                }
            }
        }
    }
}
