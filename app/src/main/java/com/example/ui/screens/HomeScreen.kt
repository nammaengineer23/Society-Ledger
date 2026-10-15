package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Person
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.data.model.SearchableSociety
import com.example.data.model.SocietyInfo
import com.example.ui.components.AutoFetchLocationCard
import com.example.ui.components.CommunityAdBanner
import com.example.ui.components.ContactSupportDialog
import com.example.ui.components.ExportHelper
import com.example.ui.components.KeyboardAwareAlertDialog
import com.example.ui.components.MasterAppAdminLoginDialog
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.LocalAppStrings
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun HomeScreen(
    currentSociety: SocietyInfo,
    searchableSocieties: List<SearchableSociety>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onEnterSocietyWithPin: (society: SearchableSociety, pin: String) -> Boolean,
    onEnterSocietyAsMember: (society: SearchableSociety, member: Member, pin: String) -> Boolean = { soc, _, pin -> onEnterSocietyWithPin(soc, pin) },
    getMembersForSociety: (society: SearchableSociety) -> List<Member> = { emptyList() },
    onEnterSocietyPublic: (society: SearchableSociety) -> Unit,
    onCreateSociety: (society: SearchableSociety, pin: String, switchToNew: Boolean) -> Unit = { _, _, _ -> },
    onNavigateToDashboard: () -> Unit,
    onNavigateToSubscription: () -> Unit = {},
    isProSubscriber: Boolean = false,
    loggedInUserName: String = "",
    loggedInUserRole: String = "",
    onLogout: (() -> Unit)? = null,
    onNavigateToContact: () -> Unit,
    onNavigateToAboutUs: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToUserDataCollection: () -> Unit,
    onNavigateToDeleteSociety: () -> Unit,
    onNavigateToAdminConsole: (() -> Unit)? = null,
    onVerifyMasterAdminAccess: (email: String, pin: String, onResult: (Boolean, String) -> Unit) -> Unit = { _, _, cb -> cb(false, "") },
    onSubmitComplaint: ((com.example.data.model.AppComplaint) -> Unit)? = null,
    onNavigateToFaq: () -> Unit = {},
    onNavigateToChatbot: () -> Unit = {},
    isCloudSyncing: Boolean = false,
    onRefreshCloud: () -> Unit = {},
    onForceSyncWithFirestore: () -> Unit = onRefreshCloud,
    onUploadAllToCloud: ((onResult: (Boolean, String) -> Unit) -> Unit)? = null,
    onInitializeSchema: ((onResult: (Boolean, String) -> Unit) -> Unit)? = null,
    onRecreateAllData: ((onResult: (Boolean, String) -> Unit) -> Unit)? = null,
    onTestCloudConnection: ((onResult: (Boolean, String) -> Unit) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val strings = LocalAppStrings.current
    val appShareUrl = ExportHelper.OFFICIAL_WEB_APP_URL
    val playStoreAppUrl = ExportHelper.PLAYSTORE_APP_URL

    var selectedSocietyForPin by remember { mutableStateOf<SearchableSociety?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var isPinVisible by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf<String?>(null) }

    // Dialog state for Creating a New Society / Draft Society
    var showCreateSocietyDialog by remember { mutableStateOf(false) }
    var showPinGuideDialog by remember { mutableStateOf(false) }
    var showShareAppDialog by remember { mutableStateOf(false) }
    var showMasterAdminAuthDialog by remember { mutableStateOf(false) }
    var showContactSupportDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sync_rotation")
    val syncRotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_spin"
    )

    val filteredSocieties = remember(searchableSocieties, searchQuery) {
        if (searchQuery.isBlank()) {
            searchableSocieties
        } else {
            val q = searchQuery.trim().lowercase()
            searchableSocieties.filter { soc ->
                soc.name.lowercase().contains(q) ||
                        soc.registrationNo.lowercase().contains(q) ||
                        soc.city.lowercase().contains(q) ||
                        soc.state.lowercase().contains(q) ||
                        soc.contactPhone.lowercase().contains(q) ||
                        soc.contactEmail.lowercase().contains(q) ||
                        soc.presidentName.lowercase().contains(q) ||
                        soc.secretaryName.lowercase().contains(q) ||
                        soc.treasurerName.lowercase().contains(q) ||
                        getMembersForSociety(soc).any { member ->
                            member.name.lowercase().contains(q) ||
                                    member.phone.lowercase().contains(q) ||
                                    member.role.lowercase().contains(q) ||
                                    member.flatNo.lowercase().contains(q) ||
                                    member.email.lowercase().contains(q)
                        }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header: Society Portal & Create Action
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_hero_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SapphireNavy
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFF2E7D32), RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = strings.publicAuditActive,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = strings.homePortalTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = strings.homePortalSubtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons in Hero: Create New Society, PIN Info & Share App
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showCreateSocietyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndianSaffron),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("btn_create_new_society"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.newSocietyBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = { showPinGuideDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("btn_how_pin_works"),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.pinGuideBtn, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }

                        Button(
                            onClick = { showShareAppDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(0.9f)
                                .testTag("btn_hero_share_apk"),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share APK", modifier = Modifier.size(15.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(strings.shareApkBtn, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Current Active Society Pill
                    Surface(
                        color = Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDashboard() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.currentSocietyLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndianSaffron
                                    )
                                )
                                Text(
                                    text = currentSociety.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1
                                )
                            }

                            Button(
                                onClick = onNavigateToDashboard,
                                colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(strings.enterSocietyBtn, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }

        // Public Resident Logged-In Status Banner & Role Information
        if (loggedInUserName.isNotBlank() || loggedInUserRole.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("public_user_status_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, IndianSaffron.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = CircleShape,
                                    color = IndianSaffron.copy(alpha = 0.15f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = IndianSaffron,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = loggedInUserName.ifBlank { "Public Resident" },
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = IndianSaffron.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "PUBLIC VIEW • READ-ONLY AUDIT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFE65100)
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            if (onLogout != null) {
                                OutlinedButton(
                                    onClick = onLogout,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorExpenseRed),
                                    border = BorderStroke(1.dp, ColorExpenseRed.copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("public_logout_button")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You are logged in with Public Resident view. You can inspect all society collections, view certified balance sheets, and audit donations. If you reside in any society, the management committee can add your flat and grant you member access.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // Search Bar for Societies
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("society_search_input"),
                placeholder = { Text(strings.searchSocietiesPlaceholder) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SocietyTealPrimary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Registered Societies List Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "${strings.registeredSocietiesTitle} (${filteredSocieties.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = { onForceSyncWithFirestore() },
                        enabled = !isCloudSyncing,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("force_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync records",
                            tint = if (isCloudSyncing) SocietyTealPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isCloudSyncing) syncRotationAngle else 0f)
                        )
                    }
                }
                Text(
                    text = "PIN to Enter Admin Mode",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of Societies
        if (filteredSocieties.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isBlank()) "No societies registered yet. Tap '+ Register Society' to create your society." else "${strings.noSocietiesFound}: \"$searchQuery\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showCreateSocietyDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.newSocietyBtn)
                        }
                    }
                }
            }
        } else {
            items(filteredSocieties, key = { it.id.ifBlank { "${it.name}_${it.registrationNo}" } }) { society ->
                val isCurrent = (society.registrationNo.isNotBlank() && society.registrationNo == currentSociety.registrationNo) ||
                        society.id == currentSociety.registrationNo ||
                        society.name.equals(currentSociety.name, ignoreCase = true)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEnterSocietyPublic(society)
                            onNavigateToDashboard()
                        }
                        .testTag("society_item_${society.id}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isCurrent) androidx.compose.foundation.BorderStroke(1.5.dp, SocietyTealPrimary) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
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
                                        .size(38.dp)
                                        .background(SocietyTealPrimary.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = SocietyTealPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = society.name,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(SocietyTealPrimary, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = "ACTIVE",
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "Reg: ${society.registrationNo.ifBlank { "DRAFT / UNREGISTERED" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Top Right Corner: Option to enter as Member
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = SapphireNavy.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .clickable {
                                        selectedSocietyForPin = society
                                        pinInput = ""
                                        pinError = null
                                    }
                                    .testTag("card_member_btn_${society.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = strings.enterAsMember,
                                        tint = SapphireNavy,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.filterByMember,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = SapphireNavy
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (society.city.isNotBlank()) {
                                        listOfNotNull(
                                            society.city,
                                            society.state.takeIf { it.isNotBlank() },
                                            society.pinCode.takeIf { it.isNotBlank() }?.let { "PIN $it" }
                                        ).joinToString(", ")
                                    } else "Location not specified",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = society.financialYear.ifBlank { "FY 2026-2027" },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        // First entry as public without PIN requirement
                        Button(
                            onClick = {
                                onEnterSocietyPublic(society)
                                onNavigateToDashboard()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("public_enter_btn_${society.id}"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${strings.enterSocietyBtn} (${strings.publicViewReadOnly})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Quick Navigation & Policy Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_policy_hub_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Official Governance & Policy Pages",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Access society bylaws, contact desk, terms of service, and resident data collection transparency.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Row 1: Contact Us & About Us
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToContact,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_contact_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ContactSupport, contentDescription = null, tint = SocietyTealPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Contact Desk", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToAboutUs,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_about_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = SapphireNavy)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("About Us", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Terms & Data Collection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onNavigateToTerms,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_terms_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFFE65100))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Terms & Bylaws", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedButton(
                            onClick = onNavigateToUserDataCollection,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_data_collection_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Policy, contentDescription = null, tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Data Collection", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToFaq,
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_faq_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = SapphireNavy)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("FAQ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SapphireNavy)
                        }

                        Button(
                            onClick = onNavigateToChatbot,
                            colors = ButtonDefaults.buttonColors(containerColor = IndianSaffron.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("nav_chatbot_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = IndianSaffron)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask AI Chatbot", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC85A00))
                        }
                    }

                    Button(
                        onClick = { showContactSupportDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_contact_support_dialog_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Contact Master Admin Support", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (onNavigateToAdminConsole != null) {
                        Button(
                            onClick = { showMasterAdminAuthDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nav_admin_console_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = IndianSaffron)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Master Admin Console", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // App Admin Contact & Support Footer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showContactSupportDialog = true }
                    .testTag("copyright_support_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Society Ledger Transparency System v1.0.0",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Master Admin: societyledger@gmail.com | WhatsApp Help: +91 9986385925",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = SapphireNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tap to Contact Master Admin Support • © 2026",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // ==========================================
    // 0A. CONTACT SUPPORT MODAL DIALOG
    // ==========================================
    if (showContactSupportDialog) {
        val currentSocInfo = searchableSocieties.firstOrNull()?.toSocietyInfo() ?: currentSociety
        ContactSupportDialog(
            societyInfo = currentSocInfo,
            onDismiss = { showContactSupportDialog = false },
            onSubmitTicket = onSubmitComplaint
        )
    }

    // ==========================================
    // 0. MASTER APP ADMIN AUTHORIZATION MODAL DIALOG
    // ==========================================
    if (showMasterAdminAuthDialog) {
        MasterAppAdminLoginDialog(
            onDismiss = { showMasterAdminAuthDialog = false },
            onAuthenticate = onVerifyMasterAdminAccess,
            onSuccess = {
                showMasterAdminAuthDialog = false
                onNavigateToAdminConsole?.invoke()
            }
        )
    }

    // ==========================================
    // 1. CREATE NEW SOCIETY / DRAFT SOCIETY DIALOG
    // ==========================================
    if (showCreateSocietyDialog) {
        var newSocName by remember { mutableStateOf("") }
        var newRegNo by remember { mutableStateOf("") }
        var newCity by remember { mutableStateOf("") }
        var newState by remember { mutableStateOf("") }
        var newAddress by remember { mutableStateOf("") }
        var newPinCode by remember { mutableStateOf("") }
        var newUpiId by remember { mutableStateOf("") }
        var newOpeningBalance by remember { mutableStateOf("") }
        var newFinancialYear by remember { mutableStateOf("FY 2026-2027") }
        var newSuperAdminName by remember { mutableStateOf("") }
        var newAdminPin by remember { mutableStateOf("") }
        var newSocietyPhone by remember { mutableStateOf("") }
        var newSocietyEmail by remember { mutableStateOf("") }
        var isDraftStatus by remember { mutableStateOf(false) }
        var createError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showCreateSocietyDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AddBusiness,
                    contentDescription = null,
                    tint = IndianSaffron,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Create New Society",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Set up a new society ledger. The society will be saved to your Home list and can be entered immediately using your custom PIN.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (createError != null) {
                        Text(
                            text = createError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    OutlinedTextField(
                        value = newSocName,
                        onValueChange = { newSocName = it },
                        label = { Text("Society / RWA Name *") },
                        placeholder = { Text("Enter society name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_society_name_input")
                    )

                    OutlinedTextField(
                        value = newRegNo,
                        onValueChange = { newRegNo = it },
                        label = { Text("Registration Number") },
                        placeholder = { Text("Enter registration number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("new_society_reg_input")
                    )

                    AutoFetchLocationCard(
                        onLocationFetched = { details ->
                            if (details.city.isNotBlank()) newCity = details.city
                            if (details.state.isNotBlank()) newState = details.state
                            if (details.address.isNotBlank()) newAddress = details.address
                            if (details.pinCode.isNotBlank()) newPinCode = details.pinCode
                        },
                        testTagPrefix = "create_society_location"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCity,
                            onValueChange = { newCity = it },
                            label = { Text("City *") },
                            placeholder = { Text("Enter city") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("new_society_city_input")
                        )

                        OutlinedTextField(
                            value = newState,
                            onValueChange = { newState = it },
                            label = { Text("State") },
                            placeholder = { Text("Enter state") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("new_society_state_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAddress,
                            onValueChange = { newAddress = it },
                            label = { Text("Society Address") },
                            placeholder = { Text("Enter society address") },
                            singleLine = true,
                            modifier = Modifier.weight(1.3f)
                        )

                        OutlinedTextField(
                            value = newPinCode,
                            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newPinCode = it },
                            label = { Text("PIN Code") },
                            placeholder = { Text("Enter PIN code") },
                            singleLine = true,
                            modifier = Modifier.weight(0.7f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newUpiId,
                            onValueChange = { newUpiId = it },
                            label = { Text("UPI ID") },
                            placeholder = { Text("Enter UPI ID") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = newOpeningBalance,
                            onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) newOpeningBalance = it },
                            label = { Text("Opening Reserve (₹)") },
                            placeholder = { Text("0") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Society Official Contact Info (Optional)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSocietyPhone,
                            onValueChange = { newSocietyPhone = it },
                            label = { Text("Society Phone (Opt)") },
                            placeholder = { Text("Enter phone number") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("new_society_phone_input")
                        )

                        OutlinedTextField(
                            value = newSocietyEmail,
                            onValueChange = { newSocietyEmail = it },
                            label = { Text("Society Email (Opt)") },
                            placeholder = { Text("Enter email address") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("new_society_email_input")
                        )
                    }

                    // Admin Setup Section (Only Admin is created initially)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Password, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Admin Setup (Only Admin Created)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SapphireNavy)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Only the Admin will be created with this society. All other committee members are created, allotted PINs, and modified later by the Admin.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newSuperAdminName,
                                onValueChange = { newSuperAdminName = it },
                                label = { Text("Admin Name") },
                                placeholder = { Text("Enter admin name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("new_society_admin_name_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newAdminPin,
                                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) newAdminPin = it },
                                label = { Text("President PIN (Required) *") },
                                placeholder = { Text("Enter 4-digit PIN") },
                                singleLine = true,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword),
                                modifier = Modifier.fillMaxWidth().testTag("new_society_pin_input")
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSocName.isBlank()) {
                            createError = "Please enter a valid Society / RWA Name."
                            return@Button
                        }
                        if (newAdminPin.trim().length < 4) {
                            createError = "Please set a President PIN (at least 4 digits)."
                            return@Button
                        }
                        val cleanBalance = newOpeningBalance.toDoubleOrNull() ?: 0.0
                        val societyToSave = SearchableSociety(
                            id = "soc_" + System.currentTimeMillis(),
                            name = newSocName.trim(),
                            registrationNo = if (newRegNo.isNotBlank()) newRegNo.trim() else "DRAFT/${System.currentTimeMillis() % 10000}",
                            city = newCity.trim(),
                            state = newState.trim(),
                            address = newAddress.trim(),
                            pinCode = newPinCode.trim(),
                            upiId = newUpiId.trim(),
                            financialYear = newFinancialYear.trim(),
                            openingBalance = cleanBalance,
                            contactEmail = newSocietyEmail.trim(),
                            adminEmail = newSocietyEmail.trim(),
                            contactPhone = newSocietyPhone.trim(),
                            presidentName = if (newSuperAdminName.isNotBlank()) newSuperAdminName.trim() else "Admin",
                            secretaryName = "",
                            treasurerName = ""
                        )
                        onCreateSociety(societyToSave, newAdminPin.trim(), true)
                        showCreateSocietyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndianSaffron),
                    modifier = Modifier.testTag("save_and_open_society_btn")
                ) {
                    Text("Save & Open Society", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showCreateSocietyDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // ==========================================
    // 2. PIN EXPLANATION / GUIDE DIALOG
    // ==========================================
    if (showPinGuideDialog) {
        AlertDialog(
            onDismissRequest = { showPinGuideDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = SapphireNavy,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "How Society PIN to Enter is Decided",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "1. Creating a New Society",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SapphireNavy)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "When creating a society, the creator sets a custom 4-digit Admin PIN. Only the Admin PIN is configured on creation.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "2. Committee Member PINs (No Defaults)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SocietyTealPrimary)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "There are no default PINs for regular members. The Admin can allot unique PINs to officers (Secretary, Treasurer) via the Committee Roster.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "3. Master Admin Access",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Master Admin Console is restricted to authorized app administrators and protected by admin security PIN.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "4. Public Resident Mode (No PIN Required)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Residents and public auditors can tap 'Public View' to inspect accounts, vouchers, and statements in 100% transparent read-only mode without any PIN.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPinGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                ) {
                    Text("Got It")
                }
            }
        )
    }

    // ==========================================
    // 3. PIN TO ENTER (MEMBER PROFILE AUTHENTICATION) DIALOG
    // ==========================================
    selectedSocietyForPin?.let { targetSociety ->
        val societyMembers = remember(targetSociety) {
            val list = getMembersForSociety(targetSociety)
            if (list.isNotEmpty()) list else listOf(
                Member(id = 1, name = targetSociety.presidentName.ifBlank { "President" }, role = "President & Admin", pin = "", isSuperAdmin = true, email = targetSociety.adminEmail, phone = targetSociety.contactPhone),
                Member(id = 2, name = targetSociety.secretaryName.ifBlank { "General Secretary" }, role = "General Secretary", pin = "", isSuperAdmin = false, email = "", phone = ""),
                Member(id = 3, name = targetSociety.treasurerName.ifBlank { "Treasurer" }, role = "Treasurer", pin = "", isSuperAdmin = false, email = "", phone = "")
            )
        }
        var selectedMember by remember(targetSociety) { mutableStateOf<Member?>(societyMembers.firstOrNull()) }

        KeyboardAwareAlertDialog(
            onDismissRequest = {
                selectedSocietyForPin = null
                pinInput = ""
                pinError = null
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = null,
                        tint = SapphireNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Enter with Member PIN",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = targetSociety.name,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = SocietyTealPrimary),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "Reg: ${targetSociety.registrationNo.ifBlank { "DRAFT" }} • ${targetSociety.city}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Select your committee profile and enter your allotted 4-digit PIN to access your workspace based on your designated role:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Members List in the Society
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        societyMembers.forEach { member ->
                            val isSelected = selectedMember?.id == member.id || (selectedMember == null && member == societyMembers.firstOrNull())
                            val isSuperAdminProfile = member.isSuperAdmin ||
                                    member.role.contains("President", ignoreCase = true) ||
                                    member.role.contains("Admin", ignoreCase = true) ||
                                    member.role.contains("Super Admin", ignoreCase = true)

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMember = member
                                        pinInput = ""
                                        pinError = null
                                    }
                                    .testTag("member_select_${member.id}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SapphireNavy) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedMember = member
                                                pinInput = ""
                                                pinError = null
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = SapphireNavy)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(
                                                if (isSuperAdminProfile) SapphireNavy else SocietyTealPrimary,
                                                CircleShape
                                            ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = member.name.take(1).uppercase(),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Column {
                                            Text(
                                                text = member.name,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = member.role,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                )
                                                if (isSuperAdminProfile) {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(IndianSaffron, RoundedCornerShape(3.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "Admin",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        )
                                                    }
                                                } else {
                                                    Box(
                                                        modifier = Modifier
                                                            .background(SocietyTealPrimary.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
                                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                                    ) {
                                                        Text(
                                                            text = "Member Rights",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = SocietyTealPrimary)
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

                    // PIN Verification Input Section
                    val currentTargetMember = selectedMember ?: societyMembers.firstOrNull()
                    if (currentTargetMember != null) {
                        val isSuperAdmin = currentTargetMember.isSuperAdmin || currentTargetMember.role.contains("President", ignoreCase = true)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSuperAdmin) SapphireNavy.copy(alpha = 0.08f) else SocietyTealPrimary.copy(alpha = 0.08f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSuperAdmin) SapphireNavy.copy(alpha = 0.3f) else SocietyTealPrimary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = if (isSuperAdmin) SapphireNavy else SocietyTealPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Enter Allotted PIN for ${currentTargetMember.name}:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = {
                                        if (it.length <= 6 && it.all { c -> c.isDigit() }) {
                                            pinInput = it
                                            if (pinError != null) pinError = null
                                        }
                                    },
                                    placeholder = { Text("4-digit PIN") },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { isPinVisible = !isPinVisible },
                                            modifier = Modifier.testTag("toggle_pin_visibility")
                                        ) {
                                            Icon(
                                                imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = if (isPinVisible) "Hide PIN" else "Show PIN"
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    isError = pinError != null,
                                    singleLine = true,
                                    supportingText = {
                                        if (pinError != null) {
                                            Text(pinError!!, color = MaterialTheme.colorScheme.error)
                                        } else if (currentTargetMember.isSuperAdmin) {
                                            Text("Role: ${currentTargetMember.role} • Requires Admin PIN set on creation")
                                        } else if (currentTargetMember.pin.isBlank()) {
                                            Text("No PIN allotted yet • Admin can set PIN in Committee Roster", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Text("Role: ${currentTargetMember.role} • Allotted PIN configured")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pin_to_enter_input")
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val currentTargetMember = selectedMember ?: societyMembers.firstOrNull()
                Button(
                    onClick = {
                        if (currentTargetMember == null) {
                            pinError = "Please select a member profile first."
                            return@Button
                        }
                        if (pinInput.isBlank()) {
                            pinError = "Please enter the allotted 4-digit PIN for ${currentTargetMember.name}."
                            return@Button
                        }
                        val success = onEnterSocietyAsMember(targetSociety, currentTargetMember, pinInput.trim())
                        if (success) {
                            selectedSocietyForPin = null
                            onNavigateToDashboard()
                        } else {
                            if (!currentTargetMember.isSuperAdmin && currentTargetMember.pin.isBlank()) {
                                pinError = "No PIN has been allotted for ${currentTargetMember.name}. Admin must set a PIN in the Committee Roster."
                            } else if (currentTargetMember.isSuperAdmin) {
                                pinError = "Incorrect Admin PIN for ${currentTargetMember.name}. Please enter the 4-digit PIN set during creation."
                            } else {
                                pinError = "Incorrect PIN for ${currentTargetMember.name}. Please enter their unique assigned PIN."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    modifier = Modifier.testTag("confirm_pin_enter_btn")
                ) {
                    val roleLabel = currentTargetMember?.role?.split(" ")?.firstOrNull() ?: "Member"
                    Text("Unlock & Enter as $roleLabel", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = {
                            onEnterSocietyPublic(targetSociety)
                            selectedSocietyForPin = null
                            onNavigateToDashboard()
                        }
                    ) {
                        Text("Public View", fontSize = 11.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            selectedSocietyForPin = null
                            pinInput = ""
                            pinError = null
                        }
                    ) {
                        Text("Cancel", fontSize = 11.sp)
                    }
                }
            }
        )
    }

    // ==========================================
    // 3. SHARE PRESENT APK / APP LINK DIALOG
    // ==========================================
    if (showShareAppDialog) {
        AlertDialog(
            onDismissRequest = { showShareAppDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFF25D366).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = Color(0xFF25D366),
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Share Society Ledger App",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Share the present installed APK package or instant access link with other society members, committee executives, and auditors.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Link Display Box: Google Play Store & Web
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "GOOGLE PLAY STORE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        color = SapphireNavy
                                    )
                                )
                                Text(
                                    text = "Official Release",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 9.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                )
                            }
                            Text(
                                text = playStoreAppUrl,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Share Action 1: Send Present APK File Directly
                    Button(
                        onClick = {
                            ExportHelper.shareAppApk(context, appShareUrl, playStoreAppUrl)
                            showShareAppDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_share_apk_file_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Present APK File", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Share Action 2: Open / Download on Google Play Store
                    Button(
                        onClick = {
                            ExportHelper.openPlayStore(context, playStoreAppUrl)
                            showShareAppDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_open_playstore_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Google Play Store", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // Share Action 3: Share App Links via WhatsApp / Social Apps
                    OutlinedButton(
                        onClick = {
                            ExportHelper.shareAppLinkOnly(context, appShareUrl, playStoreAppUrl)
                            showShareAppDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1B5E20)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25D366)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_share_app_link_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF1B5E20))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Play Store Link via WhatsApp / Apps", fontWeight = FontWeight.SemiBold)
                    }

                    // Share Action 4: Copy Play Store & Web Links to Clipboard
                    OutlinedButton(
                        onClick = {
                            val allLinks = "📲 Download on Google Play Store:\n$playStoreAppUrl\n\n🌐 Web App Portal:\n$appShareUrl"
                            ExportHelper.copyAppLinkToClipboard(context, allLinks)
                            showShareAppDialog = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_copy_app_link_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Store & Web Links", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = { showShareAppDialog = false }
                ) {
                    Text("Close")
                }
            }
        )
    }
}
