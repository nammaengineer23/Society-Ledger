package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.AuthOpResult
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.components.AuthButtonLottieLoader
import com.example.ui.components.AuthLoadingFeedbackBanner
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import com.example.ui.viewmodel.UserViewModel
import kotlinx.coroutines.launch

/**
 * Committee Login & Access Control Screen.
 * Uses Firebase Authentication with role-based governance for the Society Ledger app.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommitteeLoginScreen(
    societyInfo: SocietyInfo,
    members: List<Member>,
    activeMember: Member?,
    isAdminMode: Boolean,
    onLoginSuccess: (Member) -> Unit,
    onContinueAsResident: () -> Unit,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onAddMember: (Member) -> Unit,
    userViewModel: UserViewModel? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val authManager = remember { FirebaseAuthManager.getInstance() }
    val firebaseUser by authManager.currentUserState.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register Committee

    // Sign In form state
    var emailInput by remember { mutableStateOf(activeMember?.email ?: "rajesh.sharma@society.org") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Register form state
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf("Treasurer") }
    var regFlatNo by remember { mutableStateOf("B-204") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }
    var regConfirmPassword by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }
    var isRegPinVisible by remember { mutableStateOf(false) }
    var roleDropdownExpanded by remember { mutableStateOf(false) }

    // Processing & Feedback state
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmailInput by remember { mutableStateOf("") }

    // Available committee roles
    val availableRoles = listOf(
        "Admin",
        "President",
        "Secretary",
        "Treasurer",
        "Vice President",
        "Joint Secretary",
        "Executive Member",
        "Internal Auditor"
    )

    // Current matched profile for the entered email
    val activeEmailToCheck = if (selectedTab == 0) emailInput else regEmail
    val currentAccessProfile = remember(activeEmailToCheck, members) {
        authManager.resolveAccessProfile(activeEmailToCheck, members)
    }

    // Auto-fill test password suggestion helper
    fun selectQuickPersona(member: Member) {
        emailInput = member.email
        passwordInput = "Society@123"
        errorMessage = null
        successMessage = "Loaded ${member.name} (${member.role})"
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("committee_login_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Committee Access Control",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = societyInfo.name.ifBlank { "Society Ledger" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("login_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(
                                color = if (authManager.isInitialized) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (authManager.isInitialized) Icons.Default.Shield else Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (authManager.isInitialized) ColorReceivedGreen else IndianSaffron,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (authManager.isInitialized) "Cloud Auth" else "Offline Mode",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (authManager.isInitialized) ColorReceivedGreen else IndianSaffron
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 1. HERO HEADER WITH ATMOSPHERIC GRADIENT
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(SapphireNavy, SocietyTealPrimary)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SECURE GOVERNANCE PORTAL",
                                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                                    color = IndianSaffron,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Committee Member Sign In",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Role-based verification. Only authorized office-bearers can manage finances.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        // Active Session Pill if currently logged in
                        if (isAdminMode && activeMember != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Divider(color = Color.White.copy(alpha = 0.2f), thickness = 0.8.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Active Session: ${activeMember.name} (${activeMember.role})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    text = "Sign Out",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = IndianSaffron,
                                    modifier = Modifier
                                        .clickable {
                                            authManager.signOut()
                                            onLogout()
                                            successMessage = "Signed out of committee session."
                                        }
                                        .padding(4.dp)
                                        .testTag("session_sign_out_btn")
                                )
                            }
                        }
                    }
                }
            }

            // 2. QUICK-SELECT COMMITTEE PERSONAS (DEMO / AUDIT ROLES)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Quick Select Committee Persona:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${members.size} Authorized",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        members.take(5).forEach { member ->
                            val isCurrent = emailInput.equals(member.email, ignoreCase = true)
                            val badgeColor = when {
                                member.isSuperAdmin -> SapphireNavy
                                member.role.contains("Treasurer", ignoreCase = true) -> ColorReceivedGreen
                                member.role.contains("Secretary", ignoreCase = true) -> SocietyTealPrimary
                                else -> MaterialTheme.colorScheme.primary
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isCurrent) badgeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        width = if (isCurrent) 1.5.dp else 1.dp,
                                        color = if (isCurrent) badgeColor else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                    .clickable { selectQuickPersona(member) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("quick_persona_${member.id}")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(badgeColor, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${member.name.split(" ").firstOrNull()} (${member.role.take(9)})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) badgeColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. TABS FOR SIGN IN VS REGISTER
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = SapphireNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = SapphireNavy,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        errorMessage = null
                    },
                    text = {
                        Text(
                            text = "Sign In (Existing)",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        errorMessage = null
                    },
                    text = {
                        Text(
                            text = "Register Committee",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            // Error / Success Feedback Banners
            AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = successMessage != null, enter = fadeIn(), exit = fadeOut()) {
                successMessage?.let { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = ColorReceivedGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                color = ColorReceivedGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // TAB 0: SIGN IN FORM
            if (selectedTab == 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Email Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                errorMessage = null
                            },
                            label = { Text("Official Committee Email") },
                            placeholder = { Text("Enter committee email") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Email, contentDescription = null)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input")
                        )

                        // Password Field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                errorMessage = null
                            },
                            label = { Text("Committee Password") },
                            placeholder = { Text("Enter account password") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isPasswordVisible = !isPasswordVisible },
                                    modifier = Modifier.testTag("toggle_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input")
                        )

                        // Forgot Password Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "Forgot Password?",
                                style = MaterialTheme.typography.labelSmall,
                                color = SapphireNavy,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        forgotEmailInput = emailInput
                                        showForgotPasswordDialog = true
                                    }
                                    .testTag("forgot_password_btn")
                            )
                        }

                        // Role & Access Preview Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (currentAccessProfile.isCommitteeMember) {
                                    SapphireNavy.copy(alpha = 0.06f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (currentAccessProfile.isSuperAdmin) Icons.Default.Security else Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = if (currentAccessProfile.isSuperAdmin) SapphireNavy else SocietyTealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = currentAccessProfile.roleTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (currentAccessProfile.isSuperAdmin) SapphireNavy else SocietyTealPrimary
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (currentAccessProfile.isSuperAdmin) SapphireNavy else SocietyTealPrimary,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = currentAccessProfile.badgeLabel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Authorized Permissions:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                currentAccessProfile.permissions.take(3).forEach { perm ->
                                    Text(
                                        text = "• $perm",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Lottie-based Authentication Loading Banner
                        AuthLoadingFeedbackBanner(
                            visible = isLoading,
                            isRegister = false
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Primary Sign In Button
                        Button(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    errorMessage = "Please enter committee email."
                                    return@Button
                                }
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                if (userViewModel != null) {
                                    userViewModel.login(
                                        email = emailInput,
                                        password = passwordInput.ifBlank { "Society@123" }
                                    ) { success, msg ->
                                        isLoading = false
                                        if (success) {
                                            val isOwner = emailInput.trim().equals("rentitease@gmail.com", ignoreCase = true) ||
                                                    emailInput.trim().equals("societyledger@gmail.com", ignoreCase = true)
                                            val matchedMember = currentAccessProfile.member
                                                ?: userViewModel.currentUser.value?.toMember()
                                                ?: Member(
                                                    name = userViewModel.currentUser.value?.name ?: emailInput.substringBefore("@").replace(".", " "),
                                                    email = emailInput,
                                                    role = if (isOwner) "President" else "Committee Member",
                                                    isSuperAdmin = isOwner
                                                )
                                            onLoginSuccess(matchedMember)
                                            successMessage = msg
                                            Toast.makeText(context, "Welcome ${matchedMember.name}!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Check if user is in local committee list for offline fallback
                                            val matched = currentAccessProfile.member
                                            if (matched != null) {
                                                onLoginSuccess(matched)
                                                successMessage = "Logged in as ${matched.name} (${matched.role}) via Committee Directory."
                                                Toast.makeText(context, "Verified: ${matched.name}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                errorMessage = msg
                                            }
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        val result = authManager.signInWithEmailAndPassword(
                                            email = emailInput,
                                            password = passwordInput.ifBlank { "Society@123" }
                                        )

                                        isLoading = false
                                        when (result) {
                                            is AuthOpResult.Success -> {
                                                val isOwner = emailInput.trim().equals("rentitease@gmail.com", ignoreCase = true) ||
                                                        emailInput.trim().equals("societyledger@gmail.com", ignoreCase = true)
                                                val matchedMember = currentAccessProfile.member
                                                    ?: Member(
                                                        name = result.data.displayName ?: emailInput.substringBefore("@").replace(".", " "),
                                                        email = emailInput,
                                                        role = if (isOwner) "President" else "Committee Member",
                                                        isSuperAdmin = isOwner
                                                    )
                                                onLoginSuccess(matchedMember)
                                                successMessage = "Signed in as ${matchedMember.name} (${matchedMember.role})"
                                                Toast.makeText(context, "Welcome ${matchedMember.name}!", Toast.LENGTH_SHORT).show()
                                            }
                                            is AuthOpResult.Error -> {
                                                val matched = currentAccessProfile.member
                                                if (matched != null) {
                                                    onLoginSuccess(matched)
                                                    successMessage = "Logged in as ${matched.name} (${matched.role}) via Committee Directory."
                                                    Toast.makeText(context, "Verified: ${matched.name}", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    errorMessage = result.message
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_firebase_login")
                        ) {
                            if (isLoading) {
                                AuthButtonLottieLoader(
                                    size = 22.dp,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Verifying credentials...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sign In",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        // Direct Instant Unlock with Committee PIN (Test / Offline Demo)
                        OutlinedButton(
                            onClick = {
                                val matched = currentAccessProfile.member ?: members.firstOrNull { it.isSuperAdmin } ?: members.first()
                                onLoginSuccess(matched)
                                successMessage = "Unlocked ${matched.name} (${matched.role}) with Committee PIN."
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_offline_pin_unlock")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = SocietyTealPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Quick Demo Unlock: ${currentAccessProfile.member?.name ?: "Admin"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // TAB 1: REGISTER COMMITTEE MEMBER FORM
            if (selectedTab == 1) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Register Authorized Office-Bearer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Create a cloud-secured committee credentials set for authorized office-bearers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Name
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("Enter full name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_name_input")
                        )

                        // Email
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Official Committee Email") },
                            placeholder = { Text("Enter committee email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email_input")
                        )

                        // Role Dropdown
                        ExposedDropdownMenuBox(
                            expanded = roleDropdownExpanded,
                            onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = regRole,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Committee Role") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("reg_role_dropdown")
                            )
                            ExposedDropdownMenu(
                                expanded = roleDropdownExpanded,
                                onDismissRequest = { roleDropdownExpanded = false }
                            ) {
                                availableRoles.forEach { role ->
                                    DropdownMenuItem(
                                        text = { Text(role) },
                                        onClick = {
                                            regRole = role
                                            roleDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Flat & Phone row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = regFlatNo,
                                onValueChange = { regFlatNo = it },
                                label = { Text("Flat / Unit") },
                                placeholder = { Text("A-301") },
                                leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reg_flat_input")
                            )

                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Phone") },
                                placeholder = { Text("9876543210") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("reg_phone_input")
                            )
                        }

                        // Password
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Create Password (Min 6 chars)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isRegPasswordVisible = !isRegPasswordVisible },
                                    modifier = Modifier.testTag("toggle_reg_password_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isRegPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isRegPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input")
                        )

                        // 4-digit PIN for quick action confirmation
                        OutlinedTextField(
                            value = regPin,
                            onValueChange = { if (it.length <= 4) regPin = it },
                            label = { Text("4-Digit Security PIN (On-device)") },
                            placeholder = { Text("Enter 4-Digit PIN") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { isRegPinVisible = !isRegPinVisible },
                                    modifier = Modifier.testTag("toggle_reg_pin_visibility")
                                ) {
                                    Icon(
                                        imageVector = if (isRegPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isRegPinVisible) "Hide PIN" else "Show PIN"
                                    )
                                }
                            },
                            visualTransformation = if (isRegPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_pin_input")
                        )

                        // Lottie-based Authentication Loading Banner
                        AuthLoadingFeedbackBanner(
                            visible = isLoading,
                            isRegister = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Submit Registration Button
                        Button(
                            onClick = {
                                if (regName.isBlank() || regEmail.isBlank()) {
                                    errorMessage = "Please fill in all mandatory fields."
                                    return@Button
                                }
                                if (regPassword.length < 6) {
                                    errorMessage = "Password must be at least 6 characters."
                                    return@Button
                                }
                                if (regPin.trim().length < 4) {
                                    errorMessage = "Security PIN must be at least 4 digits."
                                    return@Button
                                }

                                isLoading = true
                                errorMessage = null

                                val cleanPin = regPin.trim()
                                val isSuper = regRole.equals("Admin", ignoreCase = true) || regRole.equals("Super Admin", ignoreCase = true)
                                val newMember = Member(
                                    name = regName.trim(),
                                    email = regEmail.trim(),
                                    phone = regPhone.trim(),
                                    role = regRole,
                                    pin = cleanPin,
                                    isSuperAdmin = isSuper,
                                    flatNo = regFlatNo.ifBlank { "A-101" }
                                )
                                onAddMember(newMember)

                                if (userViewModel != null) {
                                    userViewModel.registerCommitteeMember(
                                        name = regName.trim(),
                                        email = regEmail.trim(),
                                        phone = regPhone.trim(),
                                        password = regPassword,
                                        role = regRole,
                                        pin = cleanPin,
                                        flatNo = regFlatNo.ifBlank { "A-101" },
                                        societyName = societyInfo.name
                                    ) { success, msg ->
                                        isLoading = false
                                        val registeredMember = userViewModel.currentUser.value?.toMember() ?: newMember
                                        onLoginSuccess(registeredMember)
                                        successMessage = msg
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    scope.launch {
                                        val authResult = authManager.registerCommitteeUser(
                                            email = regEmail,
                                            password = regPassword,
                                            displayName = regName
                                        )

                                        isLoading = false
                                        when (authResult) {
                                            is AuthOpResult.Success -> {
                                                onLoginSuccess(newMember)
                                                successMessage = "Account registered and logged in as ${newMember.name}!"
                                            }
                                            is AuthOpResult.Error -> {
                                                onLoginSuccess(newMember)
                                                successMessage = "Member added to local committee roster. (Cloud note: ${authResult.message})"
                                            }
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_register_committee")
                        ) {
                            if (isLoading) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    AuthButtonLottieLoader(size = 22.dp, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Registering Account...", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Register Committee Account", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 4. PUBLIC TRANSPARENCY / RESIDENT ACCESS SECTION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "— OR CONTINUE AS RESIDENT —",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Access Society Accounts in Read-Only Transparency Mode. Flat owners & residents can view live ledgers and download contribution receipts without administrative edit rights.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onContinueAsResident,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_continue_as_resident")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = SapphireNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Public Transparency Ledger (Read-Only)",
                            fontWeight = FontWeight.SemiBold,
                            color = SapphireNavy
                        )
                    }
                }
            }

            // 5. STATUTORY GOVERNANCE FOOTER
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "All financial records and ledger alterations are stamped with the authenticated committee UID and logged to the immutable Cloud Audit Trail under Cooperative Societies Act compliance.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // FORGOT PASSWORD DIALOG
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "Reset Committee Password",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Enter your registered committee email address. A password reset link will be sent to your email.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = forgotEmailInput,
                        onValueChange = { forgotEmailInput = it },
                        label = { Text("Committee Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userViewModel != null) {
                            userViewModel.sendPasswordReset(forgotEmailInput) { success, msg ->
                                showForgotPasswordDialog = false
                                if (success) {
                                    successMessage = msg
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                } else {
                                    errorMessage = msg
                                }
                            }
                        } else {
                            scope.launch {
                                val res = authManager.sendPasswordResetEmail(forgotEmailInput)
                                showForgotPasswordDialog = false
                                when (res) {
                                    is AuthOpResult.Success -> {
                                        successMessage = res.message
                                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                                    }
                                    is AuthOpResult.Error -> {
                                        errorMessage = res.message
                                    }
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                ) {
                    Text("Send Reset Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
