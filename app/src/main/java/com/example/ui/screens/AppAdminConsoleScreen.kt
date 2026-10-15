package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.KeyboardAwareDialog
import com.example.data.model.AppComplaint
import com.example.data.model.Member
import com.example.data.model.Society
import com.example.data.model.SocietyInfo
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealDark
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppAdminConsoleScreen(
    currentSociety: SocietyInfo,
    allSocieties: List<Society>,
    complaints: List<AppComplaint>,
    isCurrentProSubscriber: Boolean,
    subscriptionExpiry: Long,
    razorpayKeyId: String,
    globalAnnouncement: String,
    isAnnouncementEnabled: Boolean,
    initialAuthenticated: Boolean = false,
    onVerifyMasterAdminAccess: ((String, String, (Boolean, String) -> Unit) -> Unit)? = null,
    onVerifyMasterPin: (String) -> Boolean = { false },
    onGrantPro: (Society, Int, String, String, (Boolean, String) -> Unit) -> Unit,
    onRevokePro: (Society, String, (Boolean, String) -> Unit) -> Unit,
    onUpdateComplaintStatus: (String, String, String, String, (Boolean, String) -> Unit) -> Unit,
    onSubmitComplaint: (AppComplaint, (Boolean, String) -> Unit) -> Unit,
    onUpdateAnnouncement: (String, Boolean, (Boolean, String) -> Unit) -> Unit,
    onUpdateRazorpayKey: (String) -> Unit,
    onUpdateMasterPin: (String, (Boolean, String) -> Unit) -> Unit,
    onDeleteSociety: ((Society, String, (Boolean, String) -> Unit) -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onSwitchSociety: (Society) -> Unit,
    onNavigateToSubscription: (() -> Unit)? = null,
    members: List<Member> = emptyList(),
    onAddMember: () -> Unit = {},
    onEditMember: (Member) -> Unit = {},
    onDeleteMember: (Member) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isMasterAuthenticated by remember { mutableStateOf(initialAuthenticated) }

    LaunchedEffect(initialAuthenticated) {
        isMasterAuthenticated = initialAuthenticated
    }

    var enteredEmail by remember { mutableStateOf("societyledger@gmail.com") }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Access Denied: Only authorized Master Admin") }
    var isVerifying by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Premium & Approvals, 1: Complaints Desk, 2: Global Controls

    // Modals
    var showGrantProDialogForSociety by remember { mutableStateOf<Society?>(null) }
    var showRevokeDialogForSociety by remember { mutableStateOf<Society?>(null) }
    var showTicketDetailDialog by remember { mutableStateOf<AppComplaint?>(null) }
    var showCreateTicketDialog by remember { mutableStateOf(false) }
    var showLicenseCertificateSociety by remember { mutableStateOf<Society?>(null) }
    var showDeleteSocietyDialog by remember { mutableStateOf<Society?>(null) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_admin_console_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = IndianSaffron,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "App Master Admin Console",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (isMasterAuthenticated) "Developer & Owner Control Hub • v1.0.0" else "Authentication Required",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    if (isMasterAuthenticated) {
                        IconButton(
                            onClick = {
                                isMasterAuthenticated = false
                                enteredPin = ""
                                onLogout?.invoke()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Console",
                                tint = Color.White
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .background(IndianSaffron, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "PIN LOCKED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SapphireNavy
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = SapphireNavy)
            )
        }
    ) { innerPadding ->
        if (!isMasterAuthenticated) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_auth_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Master Admin Access",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SapphireNavy
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Restricted to Application Owner (societyledger@gmail.com). Enter your authorized email and Master PIN to unlock the console.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = enteredEmail,
                            onValueChange = {
                                enteredEmail = it
                                pinError = false
                            },
                            label = { Text("Master Admin Email") },
                            placeholder = { Text("societyledger@gmail.com") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = SapphireNavy)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("master_admin_console_email_input"),
                            enabled = !isVerifying
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = enteredPin,
                            onValueChange = {
                                if (it.length <= 6) {
                                    enteredPin = it
                                    pinError = false
                                }
                            },
                            label = { Text("Enter Master Admin PIN") },
                            placeholder = { Text("Enter Master Admin PIN") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SapphireNavy)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showPin = !showPin }) {
                                    Icon(
                                        imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null
                                    )
                                }
                            },
                            visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = pinError,
                            supportingText = {
                                if (pinError) {
                                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                                } else {
                                    Text("Security Active")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("master_pin_input_field"),
                            singleLine = true,
                            enabled = !isVerifying
                        )

                        if (isVerifying) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = SapphireNavy
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Verifying credentials...",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SapphireNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (enteredEmail.isBlank() || enteredPin.isBlank()) {
                                    pinError = true
                                    errorMessage = "Email and Master PIN are required"
                                    return@Button
                                }
                                if (onVerifyMasterAdminAccess != null) {
                                    isVerifying = true
                                    pinError = false
                                    onVerifyMasterAdminAccess(enteredEmail.trim(), enteredPin.trim()) { success, msg ->
                                        isVerifying = false
                                        if (success) {
                                            isMasterAuthenticated = true
                                            pinError = false
                                        } else {
                                            pinError = true
                                            errorMessage = if (msg.isNotBlank()) msg else "Access Denied: Only authorized Master Admin"
                                        }
                                    }
                                } else {
                                    val isAuthorizedEmail = enteredEmail.trim().equals("societyledger@gmail.com", ignoreCase = true) ||
                                            enteredEmail.trim().equals("rentitease@gmail.com", ignoreCase = true)
                                    if (!isAuthorizedEmail) {
                                        pinError = true
                                        errorMessage = "Access Denied: Only authorized Master Admin"
                                    } else if (onVerifyMasterPin(enteredPin)) {
                                        isMasterAuthenticated = true
                                        pinError = false
                                    } else {
                                        pinError = true
                                        errorMessage = "Access Denied: Incorrect Master Admin PIN."
                                    }
                                }
                            },
                            enabled = !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_unlock_admin_console"),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = IndianSaffron)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Admin Console", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_cancel_admin_auth"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel & Return to Public View")
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
            // Master Admin Owner Identity Banner
            Surface(
                color = SapphireNavy.copy(alpha = 0.95f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(IndianSaffron, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Namma Engineer (Master App Owner)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "societyledger@gmail.com | 9986385925",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:societyledger@gmail.com?subject=Society%20Ledger%20Admin%20Inquiry")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Email: societyledger@gmail.com", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:9986385925")
                                }
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Call: 9986385925", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Tab Navigation Header
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SocietyTealPrimary,
                edgePadding = 12.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SocietyTealPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Premium & Approvals", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Complaints & Help Desk (${complaints.count { it.status != "RESOLVED" }})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Global Controls & PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Committee (${members.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> PremiumManagementTab(
                    currentSociety = currentSociety,
                    allSocieties = allSocieties,
                    isCurrentProSubscriber = isCurrentProSubscriber,
                    subscriptionExpiry = subscriptionExpiry,
                    onOpenGrantDialog = { showGrantProDialogForSociety = it },
                    onOpenRevokeDialog = { showRevokeDialogForSociety = it },
                    onViewCertificate = { showLicenseCertificateSociety = it },
                    onOpenDeleteDialog = { showDeleteSocietyDialog = it },
                    onSwitchSociety = onSwitchSociety,
                    onNavigateToSubscription = onNavigateToSubscription
                )
                1 -> ComplaintsManagementTab(
                    complaints = complaints,
                    onOpenTicketDetail = { showTicketDetailDialog = it },
                    onOpenCreateTicket = { showCreateTicketDialog = true }
                )
                2 -> GlobalControlsTab(
                    globalAnnouncement = globalAnnouncement,
                    isAnnouncementEnabled = isAnnouncementEnabled,
                    razorpayKeyId = razorpayKeyId,
                    onUpdateAnnouncement = onUpdateAnnouncement,
                    onUpdateRazorpayKey = onUpdateRazorpayKey,
                    onUpdateMasterPin = onUpdateMasterPin,
                    onNavigateToSubscription = onNavigateToSubscription
                )
                3 -> CommitteeOfficersTab(
                    societyName = currentSociety.name,
                    members = members,
                    onAddMember = onAddMember,
                    onEditMember = onEditMember,
                    onDeleteMember = onDeleteMember
                )
            }
        }
    }
}

    // Dialog: Delete Society from Console
    showDeleteSocietyDialog?.let { society ->
        MasterAdminDeleteSocietyDialog(
            society = society,
            isCurrentSociety = society.name.equals(currentSociety.name, ignoreCase = true),
            onDismiss = { showDeleteSocietyDialog = null },
            onConfirmDelete = { reason ->
                onDeleteSociety?.invoke(society, reason) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    if (success) {
                        showDeleteSocietyDialog = null
                    }
                }
            }
        )
    }

    // Dialog: Grant Pro Subscription
    showGrantProDialogForSociety?.let { society ->
        GrantProApprovalDialog(
            society = society,
            onDismiss = { showGrantProDialogForSociety = null },
            onConfirm = { days, txnId, note ->
                onGrantPro(society, days, txnId, note) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    if (success) showGrantProDialogForSociety = null
                }
            }
        )
    }

    // Dialog: Revoke Pro Subscription
    showRevokeDialogForSociety?.let { society ->
        RevokeProDialog(
            society = society,
            onDismiss = { showRevokeDialogForSociety = null },
            onConfirm = { reason ->
                onRevokePro(society, reason) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) showRevokeDialogForSociety = null
                }
            }
        )
    }

    // Dialog: Ticket Details & Resolution
    showTicketDetailDialog?.let { ticket ->
        TicketDetailAdminDialog(
            ticket = ticket,
            onDismiss = { showTicketDetailDialog = null },
            onUpdateStatus = { newStatus, resolution, adminNotes ->
                onUpdateComplaintStatus(ticket.id, newStatus, resolution, adminNotes) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) showTicketDetailDialog = null
                }
            }
        )
    }

    // Dialog: Create Manual Complaint Ticket
    if (showCreateTicketDialog) {
        CreateTicketAdminDialog(
            societies = allSocieties,
            onDismiss = { showCreateTicketDialog = false },
            onSubmit = { newTicket ->
                onSubmitComplaint(newTicket) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) showCreateTicketDialog = false
                }
            }
        )
    }

    // Dialog: Digital License Certificate
    showLicenseCertificateSociety?.let { society ->
        DigitalLicenseCertificateDialog(
            society = society,
            isCurrentProSubscriber = isCurrentProSubscriber,
            subscriptionExpiry = subscriptionExpiry,
            onDismiss = { showLicenseCertificateSociety = null }
        )
    }
}

// -----------------------------------------------------------------------------------------
// TAB 0: PREMIUM CREATION, APPROVALS & SOCIETY LICENSING
// -----------------------------------------------------------------------------------------
@Composable
private fun PremiumManagementTab(
    currentSociety: SocietyInfo,
    allSocieties: List<Society>,
    isCurrentProSubscriber: Boolean,
    subscriptionExpiry: Long,
    onOpenGrantDialog: (Society) -> Unit,
    onOpenRevokeDialog: (Society) -> Unit,
    onViewCertificate: (Society) -> Unit,
    onOpenDeleteDialog: (Society) -> Unit,
    onSwitchSociety: (Society) -> Unit,
    onNavigateToSubscription: (() -> Unit)? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("ALL") } // "ALL", "PRO", "TRIAL"

    val filteredSocieties = remember(allSocieties, searchQuery, filterMode) {
        allSocieties.filter { soc ->
            val matchesSearch = soc.name.contains(searchQuery, ignoreCase = true) ||
                    soc.registrationNo.contains(searchQuery, ignoreCase = true) ||
                    soc.city.contains(searchQuery, ignoreCase = true) ||
                    soc.adminEmail.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (filterMode) {
                "PRO" -> soc.name == currentSociety.name && isCurrentProSubscriber
                "TRIAL" -> !(soc.name == currentSociety.name && isCurrentProSubscriber)
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Overview Summary Cards
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Premium Licensing Overview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Annual Pro Plan @ ₹499/Year • First 3 Months Free",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Razorpay Live",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Metric 1: Total Registered Societies
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Total Societies",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${allSocieties.size}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SapphireNavy
                                    )
                                )
                            }
                        }

                        // Metric 2: Pro Subscriptions
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Active Pro (₹499)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCurrentProSubscriber) "1 Active" else "0 Active",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                )
                            }
                        }

                        // Metric 3: Active Trials
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE0F2F1),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "3-Mo Free Trials",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SocietyTealDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isCurrentProSubscriber) "${allSocieties.size - 1}" else "${allSocieties.size}",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SocietyTealDark
                                    )
                                )
                            }
                        }
                    }

                    if (onNavigateToSubscription != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onNavigateToSubscription,
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_admin_manage_plans"),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Configure Subscription Plans & Payment Gateway", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Manual Grant / Create License Action Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SapphireNavy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardMembership, contentDescription = null, tint = IndianSaffron, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Manual License Approval Desk",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Approve bank transfer / UPI payments & grant 1-Year or Lifetime Pro access.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val target = allSocieties.firstOrNull { it.name == currentSociety.name } ?: allSocieties.firstOrNull()
                            if (target != null) onOpenGrantDialog(target)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndianSaffron),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Grant Pro", color = SapphireNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by society name, reg no, city...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterMode == "ALL",
                    onClick = { filterMode = "ALL" },
                    label = { Text("All (${allSocieties.size})") }
                )
                FilterChip(
                    selected = filterMode == "PRO",
                    onClick = { filterMode = "PRO" },
                    label = { Text("Pro Active") }
                )
                FilterChip(
                    selected = filterMode == "TRIAL",
                    onClick = { filterMode = "TRIAL" },
                    label = { Text("Free Trial (3 Mo)") }
                )
            }
        }

        // Society Cards List
        items(filteredSocieties, key = { it.id }) { society ->
            val isCurrent = society.name == currentSociety.name
            val isPro = isCurrent && isCurrentProSubscriber

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_society_card_${society.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header: Society Name & License Status Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = society.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(SocietyTealPrimary, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "Reg: ${society.registrationNo.ifBlank { "DRAFT / UNREGISTERED" }} • ${society.city.ifBlank { "Location N/A" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // License Status Badge & Quick Actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isPro) {
                                Box(
                                    modifier = Modifier
                                        .background(IndianSaffron, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "PRO ACTIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = SapphireNavy
                                            )
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFE0F2F1), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "3-MO TRIAL ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = SocietyTealDark
                                        )
                                    )
                                }
                            }

                            // Quick Delete Society Icon Button
                            IconButton(
                                onClick = { onOpenDeleteDialog(society) },
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                                    .testTag("admin_delete_society_icon_${society.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Society",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Contact & License Expiry Details
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Admin Email: ${society.adminEmail.ifBlank { "Not Configured" }}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Phone: ${society.contactPhone.ifBlank { "Not Configured" }}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isPro && subscriptionExpiry > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pro License Expiry: ${SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(subscriptionExpiry))}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ColorReceivedGreen
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Admin Action Buttons - Row 1: Licensing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Grant / Upgrade Pro Button
                        Button(
                            onClick = { onOpenGrantDialog(society) },
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isPro) "Extend Pro" else "Approve Pro", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // Digital Certificate
                        OutlinedButton(
                            onClick = { onViewCertificate(society) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.CardMembership, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Certificate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Admin Action Buttons - Row 2: Management & Deletion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Switch Society (if not current)
                        if (!isCurrent) {
                            OutlinedButton(
                                onClick = { onSwitchSociety(society) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Switch", fontSize = 11.sp)
                            }
                        } else if (isPro) {
                            OutlinedButton(
                                onClick = { onOpenRevokeDialog(society) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                            ) {
                                Text("Revoke Pro", fontSize = 11.sp)
                            }
                        }

                        // Master Admin Delete Society Button
                        OutlinedButton(
                            onClick = { onOpenDeleteDialog(society) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_delete_society_btn_${society.id}"),
                            contentPadding = PaddingValues(vertical = 6.dp, horizontal = 6.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Delete Society", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 1: COMPLAINTS & HELP DESK
// -----------------------------------------------------------------------------------------
@Composable
private fun ComplaintsManagementTab(
    complaints: List<AppComplaint>,
    onOpenTicketDetail: (AppComplaint) -> Unit,
    onOpenCreateTicket: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "OPEN", "IN_PROGRESS", "RESOLVED"

    val filteredComplaints = remember(complaints, searchQuery, filterStatus) {
        complaints.filter { ticket ->
            val matchesSearch = ticket.subject.contains(searchQuery, ignoreCase = true) ||
                    ticket.submittedByName.contains(searchQuery, ignoreCase = true) ||
                    ticket.societyName.contains(searchQuery, ignoreCase = true) ||
                    ticket.id.contains(searchQuery, ignoreCase = true) ||
                    ticket.category.contains(searchQuery, ignoreCase = true)

            val matchesStatus = when (filterStatus) {
                "OPEN" -> ticket.status == "OPEN"
                "IN_PROGRESS" -> ticket.status == "IN_PROGRESS"
                "RESOLVED" -> ticket.status == "RESOLVED"
                else -> true
            }

            matchesSearch && matchesStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Summary & Log New Ticket Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "App Complaints & Service Requests",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Direct Helpdesk from Society Admins & Residents",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onOpenCreateTicket,
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ Log Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Open Issues", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE65100))
                                Text(
                                    text = "${complaints.count { it.status == "OPEN" }}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("In Progress", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1976D2))
                                Text(
                                    text = "${complaints.count { it.status == "IN_PROGRESS" }}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Resolved", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32))
                                Text(
                                    text = "${complaints.count { it.status == "RESOLVED" }}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search & Filters
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by ticket #, subject, society...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("All (${complaints.size})") }
                )
                FilterChip(
                    selected = filterStatus == "OPEN",
                    onClick = { filterStatus = "OPEN" },
                    label = { Text("Open") }
                )
                FilterChip(
                    selected = filterStatus == "IN_PROGRESS",
                    onClick = { filterStatus = "IN_PROGRESS" },
                    label = { Text("In Progress") }
                )
                FilterChip(
                    selected = filterStatus == "RESOLVED",
                    onClick = { filterStatus = "RESOLVED" },
                    label = { Text("Resolved") }
                )
            }
        }

        // Complaint Cards List
        if (filteredComplaints.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ColorReceivedGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pending complaints in this filter!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "All society service requests are in order.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredComplaints, key = { it.id }) { ticket ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenTicketDetail(ticket) }
                        .testTag("complaint_ticket_card_${ticket.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Ticket ID, Priority and Status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "#${ticket.id}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SapphireNavy
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(ticket.priorityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                    Text(
                                        text = ticket.priority,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ticket.priorityColor
                                        )
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(ticket.statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = ticket.status.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ticket.statusColor
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ticket.subject,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Complainant & Society Info
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${ticket.submittedByName} • ${ticket.societyName}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "${ticket.contactPhone} • ${ticket.formattedDate}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Text(
                                    text = "Manage >",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SocietyTealPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// TAB 2: GLOBAL SYSTEM CONTROLS, ANNOUNCEMENTS & PIN
// -----------------------------------------------------------------------------------------
@Composable
private fun GlobalControlsTab(
    globalAnnouncement: String,
    isAnnouncementEnabled: Boolean,
    razorpayKeyId: String,
    onUpdateAnnouncement: (String, Boolean, (Boolean, String) -> Unit) -> Unit,
    onUpdateRazorpayKey: (String) -> Unit,
    onUpdateMasterPin: (String, (Boolean, String) -> Unit) -> Unit,
    onNavigateToSubscription: (() -> Unit)? = null
) {
    val context = LocalContext.current

    var announcementText by remember { mutableStateOf(globalAnnouncement) }
    var announcementEnabled by remember { mutableStateOf(isAnnouncementEnabled) }

    var rzpKeyText by remember { mutableStateOf(razorpayKeyId) }

    var newPinText by remember { mutableStateOf("") }
    var confirmPinText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Section 1: Global Broadcast Announcement
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = IndianSaffron)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Global App Announcement Banner",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Broadcasted to all society home & report screens",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = announcementEnabled,
                            onCheckedChange = { announcementEnabled = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = announcementText,
                        onValueChange = { announcementText = it },
                        label = { Text("Announcement Message") },
                        placeholder = { Text("e.g., Annual Pro Plan now available at ₹499/year! Contact 9986385925.") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onUpdateAnnouncement(announcementText, announcementEnabled) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Broadcast App-Wide", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Razorpay Gateway Credentials
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, tint = SapphireNavy)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Razorpay Merchant Integration",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Live Payment Gateway Key for ₹499/Year Subscriptions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = rzpKeyText,
                        onValueChange = { rzpKeyText = it },
                        label = { Text("Razorpay Key ID (Live / Test)") },
                        placeholder = { Text("rzp_live_...") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            onUpdateRazorpayKey(rzpKeyText)
                            Toast.makeText(context, "Razorpay Key ID saved!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Razorpay Key", fontWeight = FontWeight.Bold)
                    }

                    if (onNavigateToSubscription != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToSubscription,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Subscription Console", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section 3: Master Super Admin PIN Update
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndianSaffron)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Master Admin Security PIN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Controls access to this Master Admin Console",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPinText,
                            onValueChange = { if (it.length <= 6) newPinText = it },
                            label = { Text("New PIN (4-6 digits)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = confirmPinText,
                            onValueChange = { if (it.length <= 6) confirmPinText = it },
                            label = { Text("Confirm PIN") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (newPinText.length < 4) {
                                Toast.makeText(context, "PIN must be at least 4 digits.", Toast.LENGTH_SHORT).show()
                            } else if (newPinText != confirmPinText) {
                                Toast.makeText(context, "PINs do not match.", Toast.LENGTH_SHORT).show()
                            } else {
                                onUpdateMasterPin(newPinText) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        newPinText = ""
                                        confirmPinText = ""
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save New Master PIN", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 4: Developer & Support Desk Info Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Society Ledger Enterprise Engine v1.0.0",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Master Developer Desk: societyledger@gmail.com",
                        style = MaterialTheme.typography.bodySmall.copy(color = SapphireNavy, fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Direct Support Hotline: +91 9986385925",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: GRANT / APPROVE PRO SUBSCRIPTION
// -----------------------------------------------------------------------------------------
@Composable
private fun GrantProApprovalDialog(
    society: Society,
    onDismiss: () -> Unit,
    onConfirm: (Int, String, String) -> Unit
) {
    var selectedPlanDays by remember { mutableIntStateOf(365) } // 365: 1 Year, 730: 2 Years, 3650: 10 Years
    var txnId by remember { mutableStateOf("RZP_OFFLINE_${System.currentTimeMillis() % 100000}") }
    var approvalNote by remember { mutableStateOf("Approved via Master Admin Desk") }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(IndianSaffron, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Approve / Grant Pro",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = society.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Pro License Duration:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPlanDays == 365,
                        onClick = { selectedPlanDays = 365 },
                        label = { Text("1 Year (₹499)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedPlanDays == 730,
                        onClick = { selectedPlanDays = 730 },
                        label = { Text("2 Years") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedPlanDays == 3650,
                        onClick = { selectedPlanDays = 3650 },
                        label = { Text("Lifetime") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = txnId,
                    onValueChange = { txnId = it },
                    label = { Text("Payment / Reference Txn ID") },
                    placeholder = { Text("pay_rzp_... or UPI Ref") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = approvalNote,
                    onValueChange = { approvalNote = it },
                    label = { Text("Approval / Verification Note") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onConfirm(selectedPlanDays, txnId, approvalNote) },
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Grant License", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: REVOKE PRO
// -----------------------------------------------------------------------------------------
@Composable
private fun RevokeProDialog(
    society: Society,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var reason by remember { mutableStateOf("Subscription expired or cancelled by admin.") }

    KeyboardAwareDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Revoke Pro License?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "This will revert ${society.name} to the Free Trial tier.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Revocation") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { onConfirm(reason) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Revoke", color = Color.White)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: TICKET DETAIL & RESOLUTION
// -----------------------------------------------------------------------------------------
@Composable
private fun TicketDetailAdminDialog(
    ticket: AppComplaint,
    onDismiss: () -> Unit,
    onUpdateStatus: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var currentStatus by remember { mutableStateOf(ticket.status) }
    var resolutionText by remember { mutableStateOf(ticket.resolutionNotes) }
    var adminNotesText by remember { mutableStateOf(ticket.adminNotes) }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Ticket #${ticket.id}",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(ticket.statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = currentStatus,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = ticket.statusColor
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Category: ${ticket.category} • ${ticket.formattedDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Complainant Details Card
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Complainant Details",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = SapphireNavy)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${ticket.submittedByName} • ${ticket.societyName}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Phone: ${ticket.contactPhone} | Email: ${ticket.contactEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // One-Tap Contact Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val cleanPhone = ticket.contactPhone.replace("+", "").replace(" ", "").trim()
                                        val waPhone = if (cleanPhone.length == 10) "91$cleanPhone" else cleanPhone
                                        val message = "Hello ${ticket.submittedByName}, regarding your Society Ledger ticket #${ticket.id} (${ticket.subject}): "
                                        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$waPhone&text=${Uri.encode(message)}")
                                        try {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "WhatsApp not available.", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Text("WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:${ticket.contactPhone}")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Phone: ${ticket.contactPhone}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Subject: ${ticket.subject}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = ticket.description,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Change Chips
                    Text(
                        text = "Update Status:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = currentStatus == "OPEN",
                            onClick = { currentStatus = "OPEN" },
                            label = { Text("Open", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = currentStatus == "IN_PROGRESS",
                            onClick = { currentStatus = "IN_PROGRESS" },
                            label = { Text("In Progress", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = currentStatus == "RESOLVED",
                            onClick = { currentStatus = "RESOLVED" },
                            label = { Text("Resolved", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = currentStatus == "REJECTED",
                            onClick = { currentStatus = "REJECTED" },
                            label = { Text("Reject", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = resolutionText,
                        onValueChange = { resolutionText = it },
                        label = { Text("Official Resolution Steps / Response") },
                        placeholder = { Text("e.g., Verified payment txn. Pro activated until 2027.") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = adminNotesText,
                        onValueChange = { adminNotesText = it },
                        label = { Text("Internal Admin Private Notes") },
                        placeholder = { Text("e.g., Spoke with secretary on phone.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Back")
                        }

                        Button(
                            onClick = { onUpdateStatus(currentStatus, resolutionText, adminNotesText) },
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("Save Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: CREATE MANUAL TICKET
// -----------------------------------------------------------------------------------------
@Composable
private fun CreateTicketAdminDialog(
    societies: List<Society>,
    onDismiss: () -> Unit,
    onSubmit: (AppComplaint) -> Unit
) {
    var selectedSocietyName by remember { mutableStateOf(societies.firstOrNull()?.name ?: "") }
    var complainantName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Payment & Razorpay") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("MEDIUM") }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            LazyColumn(modifier = Modifier.padding(20.dp)) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Log Support Ticket",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = complainantName,
                        onValueChange = { complainantName = it },
                        label = { Text("Complainant Name / Designation") },
                        placeholder = { Text("Enter complainant name / designation") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone / WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Ticket Subject / Issue Summary") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Detailed Description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (complainantName.isNotBlank() && subject.isNotBlank()) {
                                    val newTicket = AppComplaint(
                                        id = "TKT-${System.currentTimeMillis() % 100000}",
                                        societyName = selectedSocietyName,
                                        submittedByName = complainantName.trim(),
                                        contactPhone = phone.ifBlank { "9986385925" },
                                        category = category,
                                        subject = subject.trim(),
                                        description = description.trim(),
                                        priority = priority,
                                        status = "OPEN",
                                        createdAt = System.currentTimeMillis()
                                    )
                                    onSubmit(newTicket)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("Submit Ticket", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: DIGITAL PRO LICENSE CERTIFICATE
// -----------------------------------------------------------------------------------------
@Composable
private fun DigitalLicenseCertificateDialog(
    society: Society,
    isCurrentProSubscriber: Boolean,
    subscriptionExpiry: Long,
    onDismiss: () -> Unit
) {
    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(IndianSaffron, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = SapphireNavy,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CERTIFICATE OF LICENSE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SapphireNavy,
                        letterSpacing = 1.sp
                    )
                )

                Text(
                    text = "Official Society Ledger Enterprise Authentication",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFE)),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Granted To:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = society.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy
                            )
                        )
                        Text(
                            text = "Registration: ${society.registrationNo.ifBlank { "REG-2026-CERTIFIED" }}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Plan Tier", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("ANNUAL PRO", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = IndianSaffron))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Authorized By", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Namma Engineer Desk", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = SapphireNavy))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Support: societyledger@gmail.com | 9986385925",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Certificate")
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// DIALOG: MASTER ADMIN PERMANENT SOCIETY DELETION
// -----------------------------------------------------------------------------------------
@Composable
private fun MasterAdminDeleteSocietyDialog(
    society: Society,
    isCurrentSociety: Boolean,
    onDismiss: () -> Unit,
    onConfirmDelete: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("Purged by Master Admin from Admin Console") }
    var isDeleting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
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
                text = "Delete Society?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Are you sure you want to permanently delete '${society.name}'?",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "• Society: ${society.name}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "• Registration No: ${society.registrationNo.ifBlank { "Not Registered" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "• Location: ${society.city.ifBlank { "N/A" }}, ${society.state.ifBlank { "N/A" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (isCurrentSociety) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "⚠️ Notice: This is currently the active society on this device. Its local records will be purged and the app will switch to another society.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "This will permanently remove the society and all its nested records (members, expenses, donations, transactions, audit logs) from the database and device storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Deletion") },
                    singleLine = false,
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("master_delete_reason_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isDeleting = true
                    onConfirmDelete(reason.trim())
                },
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("confirm_master_delete_btn")
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text("Delete Society")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CommitteeOfficersTab(
    societyName: String,
    members: List<Member>,
    onAddMember: () -> Unit,
    onEditMember: (Member) -> Unit,
    onDeleteMember: (Member) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$societyName Officers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy
                            )
                            Text(
                                text = "Registered committee administrators & custodial members (${members.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onAddMember,
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_console_add_member_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Member", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (members.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No committee members registered in $societyName.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddMember,
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add First Committee Member", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(members) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_console_member_${member.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (member.isSuperAdmin) IndianSaffron.copy(alpha = 0.2f) else SocietyTealPrimary.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.name.take(2).uppercase(Locale.getDefault()),
                                    fontWeight = FontWeight.Bold,
                                    color = if (member.isSuperAdmin) IndianSaffron else SocietyTealPrimary,
                                    fontSize = 14.sp
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
                                                .background(IndianSaffron.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Super Admin",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = IndianSaffron
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${member.role} • PIN Protected",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val contactDetails = listOfNotNull(
                                    member.phone.takeIf { it.isNotBlank() },
                                    member.email.takeIf { it.isNotBlank() }
                                ).joinToString(" | ")
                                if (contactDetails.isNotBlank()) {
                                    Text(
                                        text = contactDetails,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { onEditMember(member) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("admin_console_modify_member_${member.id}")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Modify", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { onDeleteMember(member) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Member",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


