package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DonorSummary
import com.example.data.model.SocietyInfo
import com.example.ui.components.CommunityCompactAdCard
import com.example.ui.components.ExportHelper
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsBroadcastScreen(
    societyInfo: SocietyInfo,
    donors: List<DonorSummary>,
    allDonorsCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    filterType: String,
    onFilterTypeChange: (String) -> Unit,
    onAddCustomDonor: (name: String, phone: String, email: String, flat: String, notes: String) -> Unit,
    onNavigateBack: () -> Unit,
    isProSubscriber: Boolean = false,
    onNavigateToSubscription: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    var showBroadcastDialog by remember { mutableStateOf(false) }
    var showAddDonorDialog by remember { mutableStateOf(false) }
    var selectedDonorForChat by remember { mutableStateOf<DonorSummary?>(null) }

    // Broadcast composer state
    var broadcastTarget by remember { mutableStateOf("ALL") } // "ALL", "RECEIVED", "PENDING", "HIGH"
    var selectedTemplateIndex by remember { mutableIntStateOf(4) }
    var customBroadcastText by remember { mutableStateOf("") }

    val validPhoneDonors = remember(donors) {
        donors.filter { it.phone.replace(Regex("[^0-9]"), "").length >= 10 }
    }

    val totalLifetimeDonated = remember(donors) { donors.sumOf { it.totalDonated } }
    val totalPendingPledges = remember(donors) { donors.sumOf { it.totalPending } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Donors List & Broadcast",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${societyInfo.name} • ${donors.size} Donors",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val numbers = validPhoneDonors.map { it.phone }
                            ExportHelper.copyPhoneNumbersToClipboard(context, numbers, "${societyInfo.name} Donors")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Numbers",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { showAddDonorDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Donor",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SapphireNavy
                )
            )
        },
        modifier = modifier.testTag("donors_broadcast_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. WhatsApp Broadcast Action Hub Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("broadcast_action_hub_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                        .size(42.dp)
                                        .background(Color(0xFF25D366), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = "WhatsApp Broadcast",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "WhatsApp Donor Broadcast",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Send society notices, receipts & updates",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Stats Highlights
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Donors", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("${donors.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Phone Connected", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("${validPhoneDonors.size}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF81C784))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Lifetime Inflow", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f))
                                Text("₹ ${String.format("%,.0f", totalLifetimeDonated)}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = IndianSaffron)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Broadcast Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showBroadcastDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("open_broadcast_composer_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Compose Broadcast", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val numbers = validPhoneDonors.map { it.phone }
                                    ExportHelper.copyPhoneNumbersToClipboard(context, numbers, "${societyInfo.name} Donors")
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(0.9f)
                                    .testTag("copy_all_numbers_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Numbers", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // 2. Search & Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("donor_search_input"),
                        placeholder = { Text("Search donor by name, phone or flat...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = SapphireNavy)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Filter Chips Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf(
                            "ALL" to "All Donors (${allDonorsCount})",
                            "RECEIVED" to "Received Donors",
                            "PENDING" to "Pending Pledges",
                            "HIGH_CONTRIBUTOR" to "High Donors (₹5K+)"
                        )
                        filters.forEach { (key, label) ->
                            val isSelected = filterType == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { onFilterTypeChange(key) },
                                label = {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 12.sp
                                    )
                                },
                                modifier = Modifier.testTag("donor_filter_$key")
                            )
                        }
                    }
                }
            }

            // 4. Donor List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Society Donors Directory (${donors.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SapphireNavy
                    )
                    Text(
                        text = "Auto-synced with donations",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 5. Donors List Items
            if (donors.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolunteerActivism,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No donors found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add a donation or pre-register a donor to start building your broadcast list.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            } else {
                items(donors, key = { it.phone.ifBlank { it.name } }) { donor ->
                    DonorCardItem(
                        donor = donor,
                        dateFormat = dateFormat,
                        societyInfo = societyInfo,
                        onWhatsAppClick = {
                            selectedDonorForChat = donor
                        },
                        onCallClick = {
                            try {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${donor.phone}"))
                                context.startActivity(callIntent)
                            } catch (e: Exception) {
                                // fallback
                            }
                        }
                    )
                }
            }
        }
    }

    // Individual Donor WhatsApp Chat Dialog
    selectedDonorForChat?.let { donor ->
        DonorWhatsAppOptionsDialog(
            donor = donor,
            societyInfo = societyInfo,
            onDismiss = { selectedDonorForChat = null },
            onSendWhatsApp = { message ->
                ExportHelper.openWhatsAppChat(context, donor.phone, message)
                selectedDonorForChat = null
            }
        )
    }

    // Full Society WhatsApp Broadcast Composer Dialog
    if (showBroadcastDialog) {
        SocietyBroadcastComposerDialog(
            societyInfo = societyInfo,
            donors = donors,
            validPhoneDonors = validPhoneDonors,
            target = broadcastTarget,
            onTargetChange = { broadcastTarget = it },
            selectedTemplateIndex = selectedTemplateIndex,
            onTemplateIndexChange = { selectedTemplateIndex = it },
            customText = customBroadcastText,
            onCustomTextChange = { customBroadcastText = it },
            onDismiss = { showBroadcastDialog = false },
            onSendBroadcast = { message ->
                ExportHelper.shareWhatsAppBroadcast(context, message)
                showBroadcastDialog = false
            },
            onCopyNumbers = { numbers ->
                ExportHelper.copyPhoneNumbersToClipboard(context, numbers, "${societyInfo.name} Broadcast")
            }
        )
    }

    // Add New Donor Contact Dialog
    if (showAddDonorDialog) {
        AddCustomDonorDialog(
            onDismiss = { showAddDonorDialog = false },
            onAdd = { name, phone, email, flat, notes ->
                onAddCustomDonor(name, phone, email, flat, notes)
                showAddDonorDialog = false
            }
        )
    }
}

@Composable
fun DonorCardItem(
    donor: DonorSummary,
    dateFormat: SimpleDateFormat,
    societyInfo: SocietyInfo,
    onWhatsAppClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("donor_card_${donor.phone}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(SocietyTealPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = donor.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SocietyTealPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = donor.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (donor.flatOrAddress.isNotBlank()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = donor.flatOrAddress,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (donor.phone.isNotBlank()) "📞 +91 ${donor.phone}" else "No phone recorded",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = if (donor.phone.isNotBlank()) SapphireNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Total Donated Chip
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹ ${String.format("%,.0f", donor.totalDonated)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (donor.totalReceived > 0) ColorReceivedGreen else ColorPendingOrange
                    )
                    Text(
                        text = "${donor.donationCount} Contribution${if (donor.donationCount != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Sub-metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (donor.totalReceived > 0) {
                        Box(
                            modifier = Modifier
                                .background(ColorReceivedGreen.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Paid: ₹${String.format("%,.0f", donor.totalReceived)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ColorReceivedGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (donor.totalPending > 0) {
                        Box(
                            modifier = Modifier
                                .background(ColorPendingOrange.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Pending: ₹${String.format("%,.0f", donor.totalPending)}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = ColorPendingOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (donor.lastDonationDate > 0) {
                    Text(
                        text = "Last: ${dateFormat.format(Date(donor.lastDonationDate))}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (donor.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📝 ${donor.notes}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row: WhatsApp & Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWhatsAppClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("whatsapp_donor_${donor.phone}")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Message", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                if (donor.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = onCallClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("call_donor_${donor.phone}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(14.dp),
                            tint = SapphireNavy
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", fontSize = 12.sp, color = SapphireNavy)
                    }
                }
            }
        }
    }
}

@Composable
fun DonorWhatsAppOptionsDialog(
    donor: DonorSummary,
    societyInfo: SocietyInfo,
    onDismiss: () -> Unit,
    onSendWhatsApp: (String) -> Unit
) {
    var selectedOption by remember { mutableIntStateOf(0) }
    var customMsg by remember { mutableStateOf("") }

    val options = listOf(
        "🙏 Thank You & Contribution Acknowledgment",
        "📢 General Society Notice & Meeting Update",
        "🔔 Pending Contribution / Pledge Follow-up",
        "✍️ Custom Message"
    )

    val prefilledText = remember(selectedOption, customMsg, donor, societyInfo) {
        when (selectedOption) {
            0 -> buildString {
                append("Dear *${donor.name}*,\n\n")
                append("Thank you sincerely for your generous contribution of *₹ ${String.format("%,.0f", donor.totalReceived)}* to *${societyInfo.name}*.\n\n")
                append("Your support helps us maintain and develop our society facilities. If you need a formal 80G or digital receipt, please let us know.\n\n")
                append("Regards,\n*Managing Committee*\n${societyInfo.name}")
            }
            1 -> buildString {
                append("📢 *IMPORTANT SOCIETY NOTICE*\n")
                append("*${societyInfo.name.uppercase()}*\n\n")
                append("Dear *${donor.name}*,\n\n")
                append("This is an official update regarding our upcoming society general body meeting and community development initiatives.\n\n")
                append("🏛️ *Society:* ${societyInfo.name}\n")
                append("🆔 *Reg No:* ${societyInfo.registrationNo}\n")
                append("📲 *UPI ID:* `${societyInfo.upiId}`\n\n")
                append("Thank you for your continued cooperation!\n\n")
                append("Regards,\n*Secretary / Committee*\n${societyInfo.name}")
            }
            2 -> buildString {
                append("Dear *${donor.name}*,\n\n")
                append("Warm greetings from *${societyInfo.name}*!\n\n")
                append("This is a gentle reminder regarding your pledged contribution of *₹ ${String.format("%,.0f", if (donor.totalPending > 0) donor.totalPending else 1000.0)}* for society maintenance and funds.\n\n")
                append("📲 *Pay via UPI:* `${societyInfo.upiId}`\n")
                append("🏦 *Bank Account:* ${societyInfo.fullBankDetails}\n\n")
                append("Kindly share the transaction screenshot once completed so we can issue your official receipt.\n\n")
                append("Thank you,\n*Treasurer / Managing Committee*\n${societyInfo.name}")
            }
            else -> customMsg.ifBlank { "Dear ${donor.name},\n\nGreetings from ${societyInfo.name}." }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp Message to ${donor.name}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select template or compose a message for ${donor.phone}:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Template selector radio chips
                options.forEachIndexed { index, option ->
                    val isSelected = selectedOption == index
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) SapphireNavy.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = if (isSelected) SapphireNavy else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedOption = index }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) SapphireNavy else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (selectedOption == 3) {
                    OutlinedTextField(
                        value = customMsg,
                        onValueChange = { customMsg = it },
                        placeholder = { Text("Write your message here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        maxLines = 4
                    )
                } else {
                    // Preview Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = prefilledText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSendWhatsApp(prefilledText) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SocietyBroadcastComposerDialog(
    societyInfo: SocietyInfo,
    donors: List<DonorSummary>,
    validPhoneDonors: List<DonorSummary>,
    target: String,
    onTargetChange: (String) -> Unit,
    selectedTemplateIndex: Int,
    onTemplateIndexChange: (Int) -> Unit,
    customText: String,
    onCustomTextChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSendBroadcast: (String) -> Unit,
    onCopyNumbers: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val targetDonors = remember(target, donors) {
        when (target) {
            "RECEIVED" -> donors.filter { it.totalReceived > 0 }
            "PENDING" -> donors.filter { it.totalPending > 0 }
            "HIGH" -> donors.filter { it.totalDonated >= 5000 }
            else -> donors
        }
    }

    val targetPhoneNumbers = remember(targetDonors) {
        targetDonors.map { it.phone.replace(Regex("[^0-9]"), "") }.filter { it.length >= 10 }
    }

    val templates = listOf(
        "📢 AGM / Society General Meeting Announcement",
        "🪔 Festival & Community Celebration Appeal",
        "📊 Annual Financial Transparency Statement",
        "🔔 Maintenance & Pledge Contribution Reminder"
    )

    val isCustomMode = selectedTemplateIndex == 4

    val broadcastMessage = remember(selectedTemplateIndex, customText, societyInfo, targetDonors) {
        when (selectedTemplateIndex) {
            0 -> buildString {
                append("🏛️ *OFFICIAL NOTICE: ${societyInfo.name.uppercase()}*\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("📢 *Annual General Body Meeting (AGM) Announcement*\n\n")
                append("Dear Valued Members & Donors,\n\n")
                append("You are cordially invited to the General Body Meeting of *${societyInfo.name}*.\n\n")
                append("📋 *Agenda:*\n")
                append("1. Annual Financial Statement & Audit Review\n")
                append("2. Society Development & Maintenance Initiatives\n")
                append("3. Election / Committee Updates & Resident Q&A\n\n")
                append("🆔 *Reg No:* ${societyInfo.registrationNo}\n")
                append("📲 *Society Helpline:* +91 ${societyInfo.contactPhone}\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n")
                append("Managing Committee, ${societyInfo.name}")
            }
            1 -> buildString {
                append("🪔 *FESTIVAL & CELEBRATION APPEAL*\n")
                append("*${societyInfo.name.uppercase()}*\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
                append("Dear Residents & Generous Donors,\n\n")
                append("Our society is organizing the grand community festival and cultural celebrations! We invite all residents and well-wishers to contribute generously to make this event vibrant and memorable.\n\n")
                append("💰 *Contribution Modes:*\n")
                append("📲 *UPI ID:* `${societyInfo.upiId}`\n")
                append("🏦 *Bank Account:* ${societyInfo.fullBankDetails}\n\n")
                append("Please send the transaction details to +91 ${societyInfo.contactPhone} for your official digital receipt.\n\n")
                append("Warm regards,\n*Festival Organizing Committee*\n${societyInfo.name}")
            }
            2 -> buildString {
                append("📊 *ANNUAL FINANCIAL TRANSPARENCY STATEMENT*\n")
                append("*${societyInfo.name.uppercase()}*\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
                append("Dear Society Donors & Members,\n\n")
                append("In our commitment to 100% financial transparency, here is our current fiscal overview:\n\n")
                append("• Financial Year: ${societyInfo.financialYear}\n")
                append("• Total Donors: ${targetDonors.size} Contributors\n")
                append("• Total Inflow Raised: ₹ ${String.format("%,.0f", targetDonors.sumOf { it.totalDonated })}\n\n")
                append("Detailed audited accounts and vouchers are available for review on the Society Ledger Portal.\n\n")
                append("Thank you for your continued trust and support!\n\n")
                append("Managing Committee, ${societyInfo.name}")
            }
            3 -> buildString {
                append("🔔 *MAINTENANCE & CONTRIBUTION REMINDER*\n")
                append("*${societyInfo.name.uppercase()}*\n")
                append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n")
                append("Dear Members & Donors,\n\n")
                append("This is a gentle reminder regarding pending society maintenance contributions and pledges for *${societyInfo.financialYear}*.\n\n")
                append("Your timely support enables uninterrupted security, water, garden, and community amenities.\n\n")
                append("📲 *Pay via UPI:* `${societyInfo.upiId}`\n")
                append("🏦 *Bank Details:* ${societyInfo.bankAccount}\n\n")
                append("Thank you for your prompt cooperation!\n\n")
                append("Treasury Department, ${societyInfo.name}")
            }
            else -> {
                if (customText.isNotBlank()) {
                    customText.trim()
                } else {
                    "📢 *NOTICE FROM ${societyInfo.name.uppercase()}*\n\nDear Donors & Residents,\n\n[Write or paste your announcement here]\n\nRegards,\nManaging Committee, ${societyInfo.name}"
                }
            }
        }
    }

    fun pasteFromClipboard() {
        var pasted: String? = null
        try {
            pasted = clipboardManager.getText()?.text
        } catch (_: Exception) {
            // fallback
        }
        if (pasted.isNullOrBlank()) {
            try {
                val sysClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                pasted = sysClipboard?.primaryClip?.getItemAt(0)?.text?.toString()
            } catch (_: Exception) {
                // ignore
            }
        }
        if (!pasted.isNullOrBlank()) {
            onCustomTextChange(pasted)
            Toast.makeText(context, "Pasted message from clipboard (${pasted.length} chars)", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Clipboard is empty. Copy your message first.", Toast.LENGTH_SHORT).show()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    tint = Color(0xFF25D366),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("WhatsApp Broadcast Console", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Target recipient selector
                item {
                    Text("1. Target Audience for this Society:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val targets = listOf(
                            "ALL" to "All Donors (${donors.size})",
                            "RECEIVED" to "Paid (${donors.count { it.totalReceived > 0 }})",
                            "PENDING" to "Pledges (${donors.count { it.totalPending > 0 }})",
                            "HIGH" to "High Donors (${donors.count { it.totalDonated >= 5000 }})"
                        )
                        targets.forEach { (tKey, tLabel) ->
                            val isSel = target == tKey
                            FilterChip(
                                selected = isSel,
                                onClick = { onTargetChange(tKey) },
                                label = { Text(tLabel, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                }

                // 2. Broadcast Message Source
                item {
                    Text("2. Broadcast Message Source:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    TabRow(
                        selectedTabIndex = if (isCustomMode) 0 else 1,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        Tab(
                            selected = isCustomMode,
                            onClick = { onTemplateIndexChange(4) },
                            modifier = Modifier.testTag("tab_custom_broadcast"),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("✍️ Custom Message", fontSize = 11.sp, fontWeight = if (isCustomMode) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                        Tab(
                            selected = !isCustomMode,
                            onClick = { if (isCustomMode) onTemplateIndexChange(0) },
                            modifier = Modifier.testTag("tab_ready_templates"),
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("📋 Ready Templates", fontSize = 11.sp, fontWeight = if (!isCustomMode) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        )
                    }
                }

                // 3. Custom Message Composer or Preset Templates
                if (isCustomMode) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "3. Compose / Paste Message:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Dedicated Paste from Clipboard Button
                                    Button(
                                        onClick = { pasteFromClipboard() },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                                        modifier = Modifier.testTag("btn_paste_custom_broadcast")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    if (customText.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = { onCustomTextChange("") },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("btn_clear_custom_broadcast")
                                        ) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Clear", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = customText,
                                onValueChange = onCustomTextChange,
                                placeholder = {
                                    Text(
                                        text = "Paste or type your announcement here...\n\n💡 Tip: Tap 'Paste' above to insert text copied from WhatsApp, Notes, or SMS.",
                                        fontSize = 11.sp
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .testTag("custom_broadcast_text_input"),
                                trailingIcon = {
                                    if (customText.isBlank()) {
                                        IconButton(onClick = { pasteFromClipboard() }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = "Paste from Clipboard",
                                                tint = SapphireNavy
                                            )
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            )

                            // Quick Format & Detail Insert Chips
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Quick Add:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                AssistChip(
                                    onClick = {
                                        val header = "🏛️ *OFFICIAL NOTICE: ${societyInfo.name.uppercase()}*\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n"
                                        onCustomTextChange(if (customText.isBlank()) header else "$header\n$customText")
                                    },
                                    label = { Text("+ Society Header", fontSize = 10.sp) }
                                )
                                AssistChip(
                                    onClick = {
                                        val upiDetails = "\n\n📲 *UPI ID:* `${societyInfo.upiId}`\n🏦 *Bank Account:* ${societyInfo.bankAccount}"
                                        onCustomTextChange(customText + upiDetails)
                                    },
                                    label = { Text("+ UPI & Bank", fontSize = 10.sp) }
                                )
                                AssistChip(
                                    onClick = {
                                        val contactDetails = "\n\n📞 *Society Helpline:* +91 ${societyInfo.contactPhone}"
                                        onCustomTextChange(customText + contactDetails)
                                    },
                                    label = { Text("+ Helpline", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                } else {
                    // Pre-made template list
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("3. Choose Pre-set Society Notice:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            templates.forEachIndexed { idx, tName ->
                                val isSel = selectedTemplateIndex == idx
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSel) SapphireNavy.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = if (isSel) 1.5.dp else 0.dp,
                                            color = if (isSel) SapphireNavy else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { onTemplateIndexChange(idx) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = tName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) SapphireNavy else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Option to load template into custom message editor
                            OutlinedButton(
                                onClick = {
                                    onCustomTextChange(broadcastMessage)
                                    onTemplateIndexChange(4)
                                    Toast.makeText(context, "Loaded template into Custom Message editor for editing/pasting!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_customize_template"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Customize & Edit in Custom Message", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // 4. Live Message Preview
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "4. Broadcast Preview (WhatsApp):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${broadcastMessage.length} chars",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🟢 WhatsApp Broadcast Ready",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "${targetPhoneNumbers.size} Recipients",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = broadcastMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }

                // 5. Copy numbers helper
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${targetPhoneNumbers.size} Phone Numbers Ready",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(
                                onClick = { onCopyNumbers(targetPhoneNumbers) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy All", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isCustomMode && customText.isBlank()) {
                        // Attempt auto-paste if clipboard has text
                        val clip = clipboardManager.getText()?.text
                            ?: (context.getSystemService(Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager)
                                ?.primaryClip?.getItemAt(0)?.text?.toString()
                        if (!clip.isNullOrBlank()) {
                            onCustomTextChange(clip)
                            onSendBroadcast(clip.trim())
                        } else {
                            Toast.makeText(context, "Please paste or enter your custom broadcast message first", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        onSendBroadcast(broadcastMessage)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                modifier = Modifier.testTag("send_broadcast_whatsapp_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isCustomMode && customText.isNotBlank()) "Send Custom Broadcast" else "Share to WhatsApp",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AddCustomDonorDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, phone: String, email: String, flat: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var flat by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = SapphireNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Donor to Directory", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add donor details to the society broadcast contact directory:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = { Text("Donor Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; error = null },
                    label = { Text("Phone / WhatsApp (10 digits) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = flat,
                        onValueChange = { flat = it },
                        label = { Text("Flat / Unit #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                error?.let {
                    Text(text = it, color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanPhone = phone.filter { it.isDigit() }
                    if (name.isBlank()) {
                        error = "Please enter donor name"
                    } else if (cleanPhone.length != 10) {
                        error = "Please enter a valid 10-digit mobile number"
                    } else {
                        onAdd(name, cleanPhone, email, flat, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
            ) {
                Text("Add to Donors List", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
