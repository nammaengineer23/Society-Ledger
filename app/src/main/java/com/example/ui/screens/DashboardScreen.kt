package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryExpenseSummary
import com.example.data.model.CommitteeAccountCalculator
import com.example.data.model.CommitteeMemberAccount
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.FinancialKPIs
import com.example.data.model.Member
import com.example.data.model.MonthlyFlowSummary
import com.example.data.model.PaymentModeSummary
import com.example.data.model.SocietyInfo
import com.example.ui.components.ExpensesCategoryDonutChart
import com.example.ui.components.ExportHelper
import com.example.ui.components.KPICard
import com.example.ui.components.MonthlyFlowBarChart
import com.example.ui.components.PaymentModeBreakdownCard
import com.example.ui.theme.ColorBalanceBgDark
import com.example.ui.theme.ColorBalanceBlueDark
import com.example.ui.theme.ColorExpenseBg
import com.example.ui.theme.ColorExpenseBgDark
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.components.RoleBasedAccessCard
import com.example.ui.theme.ColorExpenseRedDark
import com.example.ui.theme.ColorPendingBg
import com.example.ui.theme.ColorPendingBgDark
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorPendingOrangeDark
import com.example.ui.theme.ColorReceivedBg
import com.example.ui.theme.ColorReceivedBgDark
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.ColorReceivedGreenDark
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealContainer
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun DashboardScreen(
    kpis: FinancialKPIs,
    societyInfo: SocietyInfo,
    monthlyData: List<MonthlyFlowSummary>,
    categoryExpenses: List<CategoryExpenseSummary>,
    paymentModeData: List<PaymentModeSummary>,
    donations: List<Donation>,
    expenses: List<Expense>,
    isAdmin: Boolean,
    activeMember: Member? = null,
    members: List<Member> = emptyList(),
    onNavigateToReports: () -> Unit = {},
    onNavigateToDonations: () -> Unit,
    onNavigateToExpenses: () -> Unit,
    onNavigateToPledges: () -> Unit = {},
    onNavigateToDonors: () -> Unit = {},
    onOpenAddDonation: () -> Unit,
    onOpenAddExpense: () -> Unit,
    onEditSocietyClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onOpenAuthDialog: () -> Unit = {},
    onTogglePublicMode: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val isDark = LocalThemeIsDark.current

    val receivedAccent = if (isDark) ColorReceivedGreenDark else ColorReceivedGreen
    val receivedBg = if (isDark) ColorReceivedBgDark else ColorReceivedBg
    val pendingAccent = if (isDark) ColorPendingOrangeDark else ColorPendingOrange
    val pendingBg = if (isDark) ColorPendingBgDark else ColorPendingBg
    val expenseAccent = if (isDark) ColorExpenseRedDark else ColorExpenseRed
    val expenseBg = if (isDark) ColorExpenseBgDark else ColorExpenseBg
    val balanceAccent = if (isDark) ColorBalanceBlueDark else SapphireNavy
    val balanceBg = if (isDark) ColorBalanceBgDark else Color(0xFFE3F2FD)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        val isWideScreen = maxWidth >= 680.dp
        val isTabletExpanded = maxWidth >= 900.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = if (isTabletExpanded) 24.dp else 16.dp,
                vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Top Banner: Subtle animated motion transitions when switching between Public View and Member Workspace
        item {
            AnimatedContent(
                targetState = Pair(isAdmin, activeMember),
                transitionSpec = {
                    (fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                            slideInVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) { -it / 5 })
                        .togetherWith(
                            fadeOut(animationSpec = tween(180)) +
                                    slideOutVertically(animationSpec = tween(180)) { -it / 5 }
                        )
                },
                label = "dashboard_mode_banner_motion"
            ) { (adminMode, member) ->
                if (adminMode && member != null) {
                    val isSuperAdmin = member.isSuperAdmin || member.role.contains("President", ignoreCase = true)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("committee_officer_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuperAdmin) SapphireNavy.copy(alpha = 0.08f) else SocietyTealPrimary.copy(alpha = 0.08f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSuperAdmin) SapphireNavy.copy(alpha = 0.25f) else SocietyTealPrimary.copy(alpha = 0.25f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (isSuperAdmin) SapphireNavy else SocietyTealPrimary,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = member.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Logged in: ${member.name}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${member.role} • Committee Workspace",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSuperAdmin) IndianSaffron else SocietyTealPrimary,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isSuperAdmin) "SUPER ADMIN" else "COMMITTEE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        // Officer's Personal Cumulative Custodial Position
                        val officerAccount = remember(member, donations, expenses) {
                            CommitteeAccountCalculator.calculateAccount(member, donations, expenses)
                        }
                        val officerSign = if (officerAccount.netBalance >= 0) "+" else ""
                        val officerNetColor = when {
                            officerAccount.netBalance > 0 -> Color(0xFF1565C0)
                            officerAccount.netBalance < 0 -> Color(0xFFC62828)
                            else -> Color(0xFF2E7D32)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Your Account: Collected ₹${String.format(java.util.Locale.US, "%,.0f", officerAccount.totalReceivedAmount)} (${officerAccount.receivedCount}) • Spent ₹${String.format(java.util.Locale.US, "%,.0f", officerAccount.totalExpensesAmount)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Net Position: $officerSign₹${String.format(java.util.Locale.US, "%,.0f", officerAccount.netBalance)} (${officerAccount.balanceStatusText})",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = officerNetColor
                            )
                        }
                    }
                } else {
                    // Public Transparency Mode Banner with subtle motion styling
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("public_transparency_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E293B).copy(alpha = 0.8f) else Color(0xFFF1F8E9)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) Color(0xFF334155) else Color(0xFFC8E6C9)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (isDark) Color(0xFF2E7D32).copy(alpha = 0.3f) else Color(0xFFE8F5E9),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = strings.publicAuditMode,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDark) Color.White else Color(0xFF1B5E20)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF2E7D32).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = strings.publicViewReadOnly,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                                                )
                                            )
                                        }
                                    }
                                    Text(
                                        text = strings.homePortalSubtitle,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }

                            // Enter as Member Quick Chip
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = IndianSaffron,
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .clickable { onOpenAuthDialog() }
                                    .testTag("banner_enter_member_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.filterByMember,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        // Hero Card: Net Available Society Balance (Adaptive Layout)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_balance_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SapphireNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    SapphireNavy,
                                    Color(0xFF004D40),
                                    SocietyTealPrimary
                                )
                            )
                        )
                        .padding(if (isTabletExpanded) 24.dp else 20.dp)
                ) {
                    if (isWideScreen) {
                        // Wide screen: Horizontal split between Net Balance and Quick Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.2f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = strings.netAvailableBalance.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 1.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = societyInfo.financialYear,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AnimatedContent(
                                        targetState = isAdmin,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { -it / 2 })
                                                .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { it / 2 })
                                        },
                                        label = "hero_mode_badge_wide"
                                    ) { admin ->
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (admin) IndianSaffron.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.18f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 7.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = if (admin) strings.filterByMember.uppercase() else strings.publicViewReadOnly.uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = societyInfo.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                val locSummary = listOfNotNull(
                                    societyInfo.city.takeIf { it.isNotBlank() },
                                    societyInfo.pinCode.takeIf { it.isNotBlank() }?.let { "PIN $it" }
                                ).joinToString(" • ")
                                if (locSummary.isNotBlank()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.85f),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = locSummary,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = Color.White.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "₹ ${String.format("%,.2f", kpis.netBalance)}",
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 32.sp
                                    ),
                                    color = Color.White
                                )
                                if (societyInfo.openingBalance > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${strings.startingBankReserve}: ₹ ${String.format("%,.0f", societyInfo.openingBalance)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = null,
                                                tint = Color(0xFF69F0AE),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(strings.totalReceivedCollections, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                                                Text("₹${String.format("%,.0f", kpis.totalCollected)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                            }
                                        }
                                    }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color(0xFFFF8A80),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(strings.totalExpenses, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                                            Text("₹${String.format("%,.0f", kpis.totalExpenses)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Compact screen layout
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f, fill = false)) {
                                    Text(
                                        text = strings.netAvailableBalance.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.85f),
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = societyInfo.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = societyInfo.financialYear,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    AnimatedContent(
                                        targetState = isAdmin,
                                        transitionSpec = {
                                            (fadeIn(animationSpec = tween(220)) + slideInVertically(animationSpec = tween(220)) { -it / 2 })
                                                .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(160)) { it / 2 })
                                        },
                                        label = "hero_mode_badge_compact"
                                    ) { admin ->
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (admin) IndianSaffron.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.18f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 7.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = if (admin) strings.filterByMember.uppercase() else strings.publicViewReadOnly.uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "₹ ${String.format("%,.2f", kpis.netBalance)}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 32.sp
                                ),
                                color = Color.White
                            )

                            if (societyInfo.openingBalance > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${strings.startingBankReserve}: ₹ ${String.format("%,.0f", societyInfo.openingBalance)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Quick stats pill row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Total Inflow Pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = Color(0xFF69F0AE),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(strings.totalReceivedCollections, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                                            Text("₹${String.format("%,.0f", kpis.totalCollected)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }

                                // Total Outflow Pill
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = Color(0xFFFF8A80),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(strings.totalExpenses, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = Color.White.copy(alpha = 0.8f))
                                            Text("₹${String.format("%,.0f", kpis.totalExpenses)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Role-Based Access Control Component: Conditionally displays admin-only features for committee members versus regular member views
        item {
            RoleBasedAccessCard(
                isAdmin = isAdmin,
                currentMember = activeMember,
                onUnlockAdmin = onOpenAuthDialog,
                onSwitchToRegularMember = { onTogglePublicMode(true) },
                onRecordDonation = onOpenAddDonation,
                onRecordExpense = onOpenAddExpense,
                onManageCommittee = onNavigateToReports,
                onExportData = onNavigateToReports,
                onViewDonationsLedger = onNavigateToDonations,
                onViewExpensesLedger = onNavigateToExpenses,
                onViewFinancialReports = onNavigateToReports
            )
        }

        // WhatsApp Donors Broadcast Quick Action Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDonors() }
                    .testTag("dashboard_donors_broadcast_tile"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1B382B) else Color(0xFF25D366).copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color(0xFF25D366), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "WhatsApp Donor Broadcast",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
                            )
                            Text(
                                text = "${kpis.receivedCount + kpis.pendingCount} Donors in society register • Tap to broadcast",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
                            )
                        }
                    }
                    Text(
                        text = "Open >",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32)
                    )
                }
            }
        }

        // 4 KPI Summary Cards Grid (Adaptive Layout: 4 columns on wide screens, 2x2 on compact screens)
        item {
            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KPICard(
                        title = strings.totalReceivedCollections,
                        amount = kpis.totalCollected,
                        subtitle = "${kpis.receivedCount} donations",
                        icon = Icons.Default.CheckCircle,
                        accentColor = receivedAccent,
                        containerColor = receivedBg,
                        modifier = Modifier.weight(1f).clickable { onNavigateToDonations() },
                        testTag = "kpi_total_collected"
                    )

                    KPICard(
                        title = strings.pendingPledges,
                        amount = kpis.totalPending,
                        subtitle = "${kpis.pendingCount} pending donors",
                        icon = Icons.Default.PendingActions,
                        accentColor = pendingAccent,
                        containerColor = pendingBg,
                        modifier = Modifier.weight(1f).clickable { onNavigateToPledges() },
                        testTag = "kpi_total_pending"
                    )

                    KPICard(
                        title = strings.totalExpenses,
                        amount = kpis.totalExpenses,
                        subtitle = "${kpis.expenseCount} payments",
                        icon = Icons.Default.ReceiptLong,
                        accentColor = expenseAccent,
                        containerColor = expenseBg,
                        modifier = Modifier.weight(1f).clickable { onNavigateToExpenses() },
                        testTag = "kpi_total_expenses"
                    )

                    KPICard(
                        title = strings.netAvailableBalance,
                        amount = kpis.netBalance,
                        subtitle = if (kpis.netBalance >= 0) "Surplus / अधिशेष" else "Deficit / घाटा",
                        icon = Icons.Default.AccountBalance,
                        accentColor = balanceAccent,
                        containerColor = balanceBg,
                        modifier = Modifier.weight(1f),
                        testTag = "kpi_net_balance"
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KPICard(
                            title = strings.totalReceivedCollections,
                            amount = kpis.totalCollected,
                            subtitle = "${kpis.receivedCount} donations",
                            icon = Icons.Default.CheckCircle,
                            accentColor = receivedAccent,
                            containerColor = receivedBg,
                            modifier = Modifier.weight(1f).clickable { onNavigateToDonations() },
                            testTag = "kpi_total_collected"
                        )

                        KPICard(
                            title = strings.pendingPledges,
                            amount = kpis.totalPending,
                            subtitle = "${kpis.pendingCount} pending donors",
                            icon = Icons.Default.PendingActions,
                            accentColor = pendingAccent,
                            containerColor = pendingBg,
                            modifier = Modifier.weight(1f).clickable { onNavigateToPledges() },
                            testTag = "kpi_total_pending"
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KPICard(
                            title = strings.totalExpenses,
                            amount = kpis.totalExpenses,
                            subtitle = "${kpis.expenseCount} payments",
                            icon = Icons.Default.ReceiptLong,
                            accentColor = expenseAccent,
                            containerColor = expenseBg,
                            modifier = Modifier.weight(1f).clickable { onNavigateToExpenses() },
                            testTag = "kpi_total_expenses"
                        )

                        KPICard(
                            title = strings.netAvailableBalance,
                            amount = kpis.netBalance,
                            subtitle = if (kpis.netBalance >= 0) "Surplus / अधिशेष" else "Deficit / घाटा",
                            icon = Icons.Default.AccountBalance,
                            accentColor = balanceAccent,
                            containerColor = balanceBg,
                            modifier = Modifier.weight(1f),
                            testTag = "kpi_net_balance"
                        )
                    }
                }
            }
        }

        // Charts Layout: Side-by-Side on wide screens, stacked on compact screens
        if (isWideScreen) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    MonthlyFlowBarChart(
                        monthlyData = monthlyData,
                        modifier = Modifier.weight(1.1f)
                    )
                    ExpensesCategoryDonutChart(
                        categories = categoryExpenses,
                        modifier = Modifier.weight(0.9f)
                    )
                }
            }
        } else {
            // Monthly Inflow vs Outflow Bar Chart
            item {
                MonthlyFlowBarChart(monthlyData = monthlyData)
            }

            // Category Expenses Donut Chart
            item {
                ExpensesCategoryDonutChart(categories = categoryExpenses)
            }
        }

        // Payment Mode Breakdown
        item {
            PaymentModeBreakdownCard(modeData = paymentModeData)
        }

        // Committee Member Cumulative Accounts Summary Card
        if (members.isNotEmpty()) {
            item {
                val memberAccounts = remember(members, donations, expenses) {
                    CommitteeAccountCalculator.calculateAllAccounts(members, donations, expenses)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_committee_accounts_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(SapphireNavy.copy(alpha = 0.1f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = SapphireNavy,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = strings.committeeAccountsTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${members.size} Officers tracking society collections & expenditures",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = strings.fullLedgerBtn,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy,
                                modifier = Modifier
                                    .clickable { onNavigateToReports() }
                                    .padding(start = 4.dp)
                                    .testTag("btn_dashboard_to_committee_ledger")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Compact scannable list of each committee member's cumulative status
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            memberAccounts.forEach { acc ->
                                val mem = acc.member
                                val netColor = when {
                                    acc.netBalance > 0 -> Color(0xFF1565C0)
                                    acc.netBalance < 0 -> Color(0xFFC62828)
                                    else -> Color(0xFF2E7D32)
                                }
                                val sign = if (acc.netBalance >= 0) "+" else ""

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                        .clickable { onNavigateToReports() }
                                        .padding(horizontal = 10.dp, vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1.3f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .background(
                                                        if (mem.isSuperAdmin) SapphireNavy else SocietyTealPrimary,
                                                        CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = mem.name.take(1).uppercase(java.util.Locale.ROOT),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = mem.name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = mem.role,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "+₹${String.format(java.util.Locale.US, "%,.0f", acc.totalReceivedAmount)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF2E7D32)
                                                )
                                                Text(
                                                    text = "-₹${String.format(java.util.Locale.US, "%,.0f", acc.totalExpensesAmount)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFC62828)
                                                )
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .background(netColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "$sign₹${String.format(java.util.Locale.US, "%,.0f", acc.netBalance)}",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                    fontWeight = FontWeight.Bold,
                                                    color = netColor
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Audit & Transparency Share Bar


        // -------------------------------------------------------------------------
        // Home / All Society Directory - Prominent Large Action Card & Button
        // -------------------------------------------------------------------------
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_all_societies_directory_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE8F5E9)
                ),
                border = BorderStroke(1.dp, SocietyTealPrimary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(SocietyTealPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = SocietyTealPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strings.allSocietiesDirectory,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else SapphireNavy
                            )
                            Text(
                                text = strings.homeDirectoryDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onNavigateToHome,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("dashboard_open_directory_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SapphireNavy
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = strings.allSocietiesDirectory,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    }
}
