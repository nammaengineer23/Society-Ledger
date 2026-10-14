package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.data.model.Donation
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.components.DonationItemCard
import com.example.ui.components.WhatsAppReminderHelper
import com.example.ui.theme.ColorPendingBg
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun PendingPledgesScreen(
    pendingDonations: List<Donation>,
    searchQuery: String,
    memberFilter: Long? = null,
    members: List<Member> = emptyList(),
    activeMember: Member? = null,
    societyInfo: SocietyInfo,
    isAdmin: Boolean,
    onSearchChange: (String) -> Unit,
    onMemberFilterChange: (Long?) -> Unit = {},
    onAddPledgeClick: () -> Unit,
    onMarkAsPaid: (Donation) -> Unit,
    onEditDonation: (Donation) -> Unit,
    onDeleteDonation: (Donation) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val totalPendingAmount = pendingDonations.sumOf { it.amount }

    Scaffold(
        modifier = modifier.testTag("pending_pledges_screen"),
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddPledgeClick,
                    containerColor = ColorPendingOrange,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_pledge")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = strings.addDonationFab)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Pending Counter Banner Top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pending_summary_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColorPendingBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.pendingPledgesTitle.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorPendingOrange,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "₹ ${String.format("%,.0f", totalPendingAmount)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ColorPendingOrange
                        )
                        Text(
                            text = "${pendingDonations.size} pending contributors awaiting collection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(ColorPendingOrange.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PendingActions,
                            contentDescription = null,
                            tint = ColorPendingOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
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
                    .testTag("pending_search_input"),
                singleLine = true
            )

            // Member Filter Chips Row
            if (members.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
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
                            .testTag("filter_pending_all_members")
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
                                .testTag("filter_pending_my_records")
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
                                .testTag("filter_pending_member_${member.id}")
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

            Spacer(modifier = Modifier.height(12.dp))

            if (pendingDonations.isEmpty()) {
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
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ColorReceivedGreen,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = strings.noPendingPledges,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ColorReceivedGreen
                        )
                        Text(
                            text = "All society contributions have been collected and verified.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(pendingDonations, key = { it.id }) { donation ->
                        DonationItemCard(
                            donation = donation,
                            societyInfo = societyInfo,
                            isAdmin = isAdmin,
                            onViewReceipt = {},
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
