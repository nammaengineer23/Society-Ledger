package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.components.DonationItemCard
import com.example.ui.components.SocietyUpiQrDialog
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun DonationsScreen(
    donations: List<Donation>,
    searchQuery: String,
    statusFilter: String,
    modeFilter: String,
    timePeriodFilter: String,
    memberFilter: Long? = null,
    members: List<Member> = emptyList(),
    activeMember: Member? = null,
    societyInfo: SocietyInfo,
    isAdmin: Boolean,
    onSearchChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onModeFilterChange: (String) -> Unit,
    onTimePeriodChange: (String) -> Unit,
    onMemberFilterChange: (Long?) -> Unit = {},
    onAddDonationClick: () -> Unit,
    onViewReceipt: (Donation) -> Unit,
    onMarkAsPaid: (Donation) -> Unit,
    onEditDonation: (Donation) -> Unit,
    onDeleteDonation: (Donation) -> Unit,
    onNavigateToDonors: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current
    val modes = listOf(
        "ALL" to strings.filterAll,
        "UPI/Online" to strings.modeUpi,
        "Cash" to strings.modeCash,
        "Bank Transfer" to strings.modeBank,
        "Cheque" to strings.modeCheque
    )
    val statuses = listOf("ALL", "received", "pending")
    val periods = listOf(
        "ALL" to strings.periodAllTime,
        "THIS_MONTH" to strings.periodThisMonth,
        "LAST_MONTH" to strings.periodLastMonth
    )

    val totalAmount = donations.sumOf { it.amount }
    var showUpiQrDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("donations_screen"),
        floatingActionButton = {
            AnimatedVisibility(
                visible = isAdmin || activeMember != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                label = "fab_add_donation_visibility"
            ) {
                FloatingActionButton(
                    onClick = onAddDonationClick,
                    containerColor = SocietyTealPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_donation")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = strings.addDonationFab)
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 680.dp
            val isTabletExpanded = maxWidth >= 900.dp
            val gridColumns = when {
                isTabletExpanded -> 3
                isWideScreen -> 2
                else -> 1
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = if (isWideScreen) 24.dp else 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Adaptive Search & Broadcast Banner Row
                if (isWideScreen) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            placeholder = { Text(strings.searchDonationsPlaceholder) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChange("") }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("donation_search_input"),
                            singleLine = true
                        )

                        // Donors Directory & WhatsApp Broadcast shortcut button
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF25D366).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .clickable { onNavigateToDonors() }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                                .testTag("donations_to_donors_broadcast_banner"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "WhatsApp Broadcast",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.donorsBroadcastBtn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        // Society UPI Payment QR shortcut button
                        Row(
                            modifier = Modifier
                                .background(Color(0xFF5E35B1).copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                .clickable { showUpiQrDialog = true }
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                                .testTag("donations_society_upi_qr_banner"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Society UPI QR",
                                tint = Color(0xFF5E35B1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.societyUpiQrBtn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E35B1)
                            )
                        }
                    }
                } else {
                    // Standard Mobile Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text(strings.searchDonationsPlaceholder) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donation_search_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mobile Quick Actions Row: Donors Broadcast & Society UPI QR
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF25D366).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .clickable { onNavigateToDonors() }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("donations_to_donors_broadcast_banner_mobile"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.donorsBroadcastBtn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .background(Color(0xFF5E35B1).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .clickable { showUpiQrDialog = true }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("donations_society_upi_qr_button_mobile"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color(0xFF5E35B1),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.societyUpiQrBtn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E35B1)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Status & Mode Filter Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    statuses.forEach { status ->
                        val isSelected = statusFilter.equals(status, ignoreCase = true)
                        val label = when (status) {
                            "received" -> "✓ ${strings.filterReceived}"
                            "pending" -> "⏳ ${strings.filterPending}"
                            else -> strings.filterAll
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) SocietyTealPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { onStatusFilterChange(status) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Modes
                    modes.drop(1).forEach { (modeKey, modeLabel) ->
                        val isSelected = modeFilter.equals(modeKey, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable {
                                    onModeFilterChange(if (isSelected) "ALL" else modeKey)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = modeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Committee Member Filter Chips Row
                if (members.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 ${strings.filterByMember}:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // All Members chip
                        val isAllSelected = memberFilter == null || memberFilter == 0L
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isAllSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onMemberFilterChange(null) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("filter_all_members")
                        ) {
                            Text(
                                text = strings.allMembersFilter,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // My Records chip (if activeMember is present)
                        if (activeMember != null) {
                            val isMyRecords = memberFilter == activeMember.id
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isMyRecords) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        onMemberFilterChange(if (isMyRecords) null else activeMember.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("filter_my_records")
                            ) {
                                Text(
                                    text = "⭐ ${strings.myEntriesOnly}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isMyRecords) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isMyRecords) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Individual Committee Members
                        members.forEach { member ->
                            val isSelected = memberFilter == member.id
                            val shortName = member.name.split(" ").firstOrNull() ?: member.name
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable {
                                        onMemberFilterChange(if (isSelected) null else member.id)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("filter_member_${member.id}")
                            ) {
                                Text(
                                    text = "$shortName (${member.role.split(" ").firstOrNull() ?: "Member"})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Summary row: Filtered count and total sum
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${donations.size} ${strings.donationsTitle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${strings.totalLabel}: ₹ ${String.format("%,.0f", totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorReceivedGreen
                    )
                }

                if (!isWideScreen) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Donors Directory & WhatsApp Broadcast shortcut banner (Mobile)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF25D366).copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            .clickable { onNavigateToDonors() }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("donations_to_donors_broadcast_banner"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "WhatsApp Broadcast",
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.donorsBroadcastBtn,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }
                        Text(
                            text = ">",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF25D366)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (donations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = strings.noDonationsFound,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = strings.tryClearingFilters,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(donations, key = { it.id }) { donation ->
                            DonationItemCard(
                                donation = donation,
                                societyInfo = societyInfo,
                                isAdmin = isAdmin,
                                onViewReceipt = onViewReceipt,
                                onMarkAsPaid = onMarkAsPaid,
                                onEdit = onEditDonation,
                                onDelete = onDeleteDonation
                            )
                        }
                    }
                }
            }
        }
    }

    if (showUpiQrDialog) {
        SocietyUpiQrDialog(
            societyInfo = societyInfo,
            onDismiss = { showUpiQrDialog = false },
            onRecordDonation = { _ ->
                showUpiQrDialog = false
                onAddDonationClick()
            }
        )
    }
}
