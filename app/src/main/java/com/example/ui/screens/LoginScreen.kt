package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.data.model.User
import com.example.ui.components.AuthButtonLottieLoader
import com.example.ui.components.AuthLoadingFeedbackBanner
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.SocietyTealDark
import com.example.ui.theme.SocietyTealPrimary
import com.example.ui.viewmodel.UserViewModel

/**
 * Public resident authentication login options supported by the screen:
 * Google Sign-In or Email / Password credentials.
 * OTP verification is removed.
 */
enum class AuthMethodOption {
    GOOGLE,
    EMAIL
}

/**
 * Integrated User Authentication screen connecting to [UserViewModel].
 * Handles Google Sign-In and Email & Password sign-in / registration.
 * Automatically ensures all users register and land in Public Resident view.
 */
@Composable
fun UserAuthScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel(),
    societyInfo: SocietyInfo? = null,
    availableMembers: List<Member> = emptyList(),
    onAuthSuccess: (User) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()
    val successMessage by userViewModel.successMessage.collectAsState()

    // Trigger success callback when current user is authenticated
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            onAuthSuccess(user)
        }
    }

    LoginScreen(
        modifier = modifier,
        societyInfo = societyInfo,
        availableMembers = availableMembers,
        isLoading = isLoading,
        errorMessage = errorMessage,
        successMessage = successMessage,
        onGoogleSignInSubmit = { email, displayName, role, phone, password ->
            userViewModel.loginWithGoogle(email, displayName, "Public Resident", phone, password)
        },
        onLoginSubmit = { email, password ->
            userViewModel.loginWithEmail(email, password)
        },
        onRegisterSubmit = { name, email, phone, password ->
            userViewModel.register(
                email = email,
                password = password,
                name = name,
                phone = phone
            )
        },
        onClearMessages = {
            userViewModel.clearMessages()
        },
        onBack = onBack
    )
}

/**
 * Public Authentication screen providing:
 * 1. Google Sign-In: Continue with Google, prompts for password, and on first-time account prompts for Name and unique Phone.
 * 2. Email Sign-In / Register: Email and Password with optional first-time account Name and unique Phone.
 * OTP verification is removed; no phone/email verification required.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    societyInfo: SocietyInfo? = null,
    availableMembers: List<Member> = emptyList(),
    initialMethod: AuthMethodOption = AuthMethodOption.GOOGLE,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onLoginSubmit: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleSignInSubmit: (email: String, displayName: String, role: String, phone: String, password: String) -> Unit = { _, _, _, _, _ -> },
    onRegisterSubmit: (name: String, email: String, phone: String, password: String) -> Unit = { _, _, _, _ -> },
    onResetPassword: ((email: String) -> Unit)? = null,
    onClearMessages: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    var selectedMethod by remember { mutableStateOf(initialMethod) }

    // Google Sign-In State
    var googleAccountEmail by remember { mutableStateOf("") }
    var googleAccountName by remember { mutableStateOf("") }
    var googleAccountPhone by remember { mutableStateOf("") }
    var googleAccountPassword by remember { mutableStateOf("") }
    var isGoogleAccountSelected by remember { mutableStateOf(false) }
    var isGooglePasswordVisible by remember { mutableStateOf(false) }

    // Email Sign-In / Register State
    var isEmailRegisterMode by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var emailPassword by remember { mutableStateOf("") }
    var emailResidentName by remember { mutableStateOf("") }
    var emailPhoneNumber by remember { mutableStateOf("") }
    var isEmailPasswordVisible by remember { mutableStateOf(false) }

    // Feedback & UI State
    var localError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val displayedError = errorMessage ?: localError

    // Helper to check for duplicate phone number in backend
    fun isPhoneNumberDuplicate(rawPhone: String): Boolean {
        val clean = rawPhone.filter { it.isDigit() }.takeLast(10)
        if (clean.length < 10) return false
        return availableMembers.any { member ->
            val memberClean = member.phone.filter { it.isDigit() }.takeLast(10)
            memberClean.isNotBlank() && memberClean == clean
        }
    }

    // Launcher for device account picker (lists Google accounts on the phone)
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedEmail = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!selectedEmail.isNullOrBlank()) {
                googleAccountEmail = selectedEmail
                val displayName = selectedEmail.substringBefore("@")
                    .replace(".", " ")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    }
                googleAccountName = displayName
                isGoogleAccountSelected = true
                localError = null
                onClearMessages()
            }
        }
    }

    fun launchGoogleAccountChooser() {
        localError = null
        onClearMessages()
        try {
            val intent = AccountManager.newChooseAccountIntent(
                null,
                null,
                arrayOf("com.google"),
                null,
                null,
                null,
                null
            )
            accountPickerLauncher.launch(intent)
        } catch (e: Exception) {
            // If device cannot open account picker, fallback gracefully to manual entry
            isGoogleAccountSelected = true
            if (googleAccountEmail.isBlank()) {
                localError = "Please enter your Google account email and password below."
            }
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity
    BackHandler(enabled = onBack == null) {
        activity?.finish()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Bar Navigation & Trust Badge
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("auth_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SapphireNavy
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Surface(
                        color = ColorReceivedGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, ColorReceivedGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = ColorReceivedGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Citizen Transparency Portal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ColorReceivedGreen
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Official Header Hero Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(SapphireNavy, SocietyTealDark)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Society Ledger",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Transparent Public Governance • Instant Audited Accounts",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Portal",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Segmented Tabs: Google Sign-In & Email Sign-In
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Slate200.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, Slate200)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        // Google Tab
                        val isGoogle = selectedMethod == AuthMethodOption.GOOGLE
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGoogle) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedMethod = AuthMethodOption.GOOGLE
                                    localError = null
                                    onClearMessages()
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_continue_with_google"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "G",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                color = Color(0xFF4285F4),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Continue with Google",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isGoogle) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isGoogle) SapphireNavy else Slate600
                                    )
                                )
                            }
                        }

                        // Email Sign-In Tab
                        val isEmail = selectedMethod == AuthMethodOption.EMAIL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isEmail) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedMethod = AuthMethodOption.EMAIL
                                    localError = null
                                    onClearMessages()
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_email_signin"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = if (isEmail) SocietyTealPrimary else Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Email Sign In",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isEmail) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isEmail) SocietyTealPrimary else Slate600
                                    )
                                )
                            }
                        }
                    }
                }

                // 4. Main Authentication Container Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Slate200),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        AnimatedContent(
                            targetState = selectedMethod,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { it / 8 })
                                    .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutHorizontally(animationSpec = tween(160)) { -it / 8 })
                            },
                            label = "auth_method_view"
                        ) { method ->
                            when (method) {
                                // -------------------------------------------------------------
                                // A. CONTINUE WITH GOOGLE FLOW
                                // -------------------------------------------------------------
                                AuthMethodOption.GOOGLE -> {
                                    Column {
                                        Text(
                                            text = "Continue with Google",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                        )
                                        Text(
                                            text = "Sign in with your Google account credentials to access all resident societies in transparent view.",
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                                        )

                                        // Public Registration Notice Badge
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = IndianSaffron.copy(alpha = 0.10f),
                                            border = BorderStroke(1.dp, IndianSaffron.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 14.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Info,
                                                    contentDescription = null,
                                                    tint = IndianSaffron,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Every user first registers as Public. You will access all societies in transparent public view. Society committees can assign you member privileges later.",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }

                                        // Primary Google Sign-In Button
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.5.dp, Slate200),
                                            shadowElevation = 1.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(52.dp)
                                                .clickable {
                                                    launchGoogleAccountChooser()
                                                }
                                                .testTag("google_sign_in_primary_button")
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color.White,
                                                    border = BorderStroke(1.dp, Slate200),
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = "G",
                                                            style = MaterialTheme.typography.titleSmall.copy(
                                                                fontWeight = FontWeight.Black,
                                                                color = Color(0xFF4285F4)
                                                            )
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = "Continue with Google",
                                                    style = MaterialTheme.typography.bodyLarge.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Slate900
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Account Credentials & Password Section
                                        Text(
                                            text = "Google Account Credentials & Security:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate600,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        // Google Email Input
                                        OutlinedTextField(
                                            value = googleAccountEmail,
                                            onValueChange = {
                                                googleAccountEmail = it
                                                isGoogleAccountSelected = true
                                            },
                                            label = { Text("Google Account Email *") },
                                            placeholder = { Text("e.g. resident@gmail.com") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Email, contentDescription = null, tint = SapphireNavy)
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("google_email_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = SapphireNavy,
                                                unfocusedBorderColor = Slate200
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Password Required (Points 9 & 10)
                                        OutlinedTextField(
                                            value = googleAccountPassword,
                                            onValueChange = { googleAccountPassword = it },
                                            label = { Text("Account Password *") },
                                            placeholder = { Text("Enter account password") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = SapphireNavy)
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { isGooglePasswordVisible = !isGooglePasswordVisible }) {
                                                    Icon(
                                                        imageVector = if (isGooglePasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = "Toggle password"
                                                    )
                                                }
                                            },
                                            visualTransformation = if (isGooglePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("google_password_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = SapphireNavy,
                                                unfocusedBorderColor = Slate200
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // First-Time Registration Fields: Name and Unique Phone (Point 11)
                                        Text(
                                            text = "Profile & Phone Details (First-Time Setup):",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Slate600,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                                        )

                                        OutlinedTextField(
                                            value = googleAccountName,
                                            onValueChange = { googleAccountName = it },
                                            label = { Text("Full Name (Display Name) *") },
                                            placeholder = { Text("Enter your full name") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = SapphireNavy)
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("google_name_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = SapphireNavy,
                                                unfocusedBorderColor = Slate200
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        val googleCleanPhone = googleAccountPhone.filter { it.isDigit() }
                                        val isGooglePhoneDuplicate = isPhoneNumberDuplicate(googleCleanPhone)
                                        OutlinedTextField(
                                            value = googleCleanPhone,
                                            onValueChange = { input ->
                                                val digits = input.filter { it.isDigit() }
                                                if (digits.length <= 10) googleAccountPhone = digits
                                            },
                                            label = { Text("10-Digit Mobile Phone *") },
                                            placeholder = { Text("9876543210") },
                                            prefix = { Text("+91 ") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Phone, contentDescription = null, tint = SapphireNavy)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            isError = isGooglePhoneDuplicate,
                                            supportingText = {
                                                if (isGooglePhoneDuplicate) {
                                                    Text("Phone number is already registered in backend! Please use a unique number.", color = MaterialTheme.colorScheme.error)
                                                } else {
                                                    Text("Must be unique from previous numbers in backend")
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("google_phone_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = SapphireNavy,
                                                unfocusedBorderColor = Slate200
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Submit Button
                                        Button(
                                            onClick = {
                                                val trimmedEmail = googleAccountEmail.trim()
                                                val trimmedPwd = googleAccountPassword.trim()
                                                val trimmedPhone = googleAccountPhone.trim()

                                                if (trimmedEmail.isBlank()) {
                                                    localError = "Please select or enter your Google email address."
                                                } else if (trimmedPwd.isBlank()) {
                                                    localError = "Please enter your account password."
                                                } else if (trimmedPhone.isNotBlank() && trimmedPhone.length != 10) {
                                                    localError = "Phone number must be exactly 10 digits."
                                                } else if (isPhoneNumberDuplicate(trimmedPhone)) {
                                                    localError = "This phone number is already registered in the backend. Please use a unique number."
                                                } else {
                                                    localError = null
                                                    onClearMessages()
                                                    onGoogleSignInSubmit(
                                                        trimmedEmail,
                                                        googleAccountName.trim(),
                                                        "Public Resident",
                                                        trimmedPhone,
                                                        trimmedPwd
                                                    )
                                                }
                                            },
                                            enabled = !isLoading,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("confirm_google_sign_in_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                                        ) {
                                            if (isLoading) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    AuthButtonLottieLoader(size = 22.dp, tint = Color.White)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Signing in...", fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Sign In with Google Account", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                // -------------------------------------------------------------
                                // B. EMAIL SIGN-IN & REGISTRATION FLOW (NO OTP)
                                // -------------------------------------------------------------
                                AuthMethodOption.EMAIL -> {
                                    Column {
                                        Text(
                                            text = if (isEmailRegisterMode) "Create Resident Account" else "Email Sign In",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                        )
                                        Text(
                                            text = if (isEmailRegisterMode) {
                                                "Enter your details to create a free citizen account. No OTP verification required."
                                            } else {
                                                "Enter your registered email and password to enter the portal."
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                                            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
                                        )

                                        // Toggle between Sign In and Register
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            FilterChip(
                                                selected = !isEmailRegisterMode,
                                                onClick = {
                                                    isEmailRegisterMode = false
                                                    localError = null
                                                },
                                                label = { Text("Sign In", fontWeight = FontWeight.SemiBold) },
                                                modifier = Modifier.weight(1f).testTag("chip_email_login_mode")
                                            )
                                            FilterChip(
                                                selected = isEmailRegisterMode,
                                                onClick = {
                                                    isEmailRegisterMode = true
                                                    localError = null
                                                },
                                                label = { Text("Create Account", fontWeight = FontWeight.SemiBold) },
                                                modifier = Modifier.weight(1f).testTag("chip_email_register_mode")
                                            )
                                        }

                                        if (isEmailRegisterMode) {
                                            // Full Name Input
                                            OutlinedTextField(
                                                value = emailResidentName,
                                                onValueChange = { emailResidentName = it },
                                                label = { Text("Full Name *") },
                                                placeholder = { Text("e.g. Anand Kumar") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = SocietyTealPrimary)
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("email_reg_name_input"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = SocietyTealPrimary,
                                                    unfocusedBorderColor = Slate200
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Unique 10-Digit Phone Number (Point 11)
                                            val cleanEmailPhone = emailPhoneNumber.filter { it.isDigit() }
                                            val isEmailPhoneDuplicate = isPhoneNumberDuplicate(cleanEmailPhone)
                                            OutlinedTextField(
                                                value = cleanEmailPhone,
                                                onValueChange = { input ->
                                                    val digits = input.filter { it.isDigit() }
                                                    if (digits.length <= 10) emailPhoneNumber = digits
                                                },
                                                label = { Text("10-Digit Mobile Phone *") },
                                                placeholder = { Text("9876543210") },
                                                prefix = { Text("+91 ") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Phone, contentDescription = null, tint = SocietyTealPrimary)
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                                isError = isEmailPhoneDuplicate,
                                                supportingText = {
                                                    if (isEmailPhoneDuplicate) {
                                                        Text("Phone number is already registered in backend! Please use a unique number.", color = MaterialTheme.colorScheme.error)
                                                    } else {
                                                        Text("Must be unique from previous numbers in backend")
                                                    }
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("email_reg_phone_input"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = SocietyTealPrimary,
                                                    unfocusedBorderColor = Slate200
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))
                                        }

                                        // Email Input
                                        OutlinedTextField(
                                            value = emailInput,
                                            onValueChange = { emailInput = it },
                                            label = { Text("Email Address *") },
                                            placeholder = { Text("resident@society.org") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Email, contentDescription = null, tint = SocietyTealPrimary)
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("email_address_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = SocietyTealPrimary,
                                                unfocusedBorderColor = Slate200
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Password Input
                                        OutlinedTextField(
                                            value = emailPassword,
                                            onValueChange = { emailPassword = it },
                                            label = { Text("Password *") },
                                            placeholder = { Text("Enter password (min 6 characters)") },
                                            leadingIcon = {
                                                Icon(Icons.Default.Lock, contentDescription = null, tint = SocietyTealPrimary)
                                            },
                                            trailingIcon = {
                                                IconButton(onClick = { isEmailPasswordVisible = !isEmailPasswordVisible }) {
                                                    Icon(
                                                        imageVector = if (isEmailPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                        contentDescription = "Toggle password"
                                                    )
                                                }
                                            },
                                            visualTransformation = if (isEmailPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("email_password_input"),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = SocietyTealPrimary,
                                                unfocusedBorderColor = Slate200
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Action Button: Sign In or Create Account
                                        Button(
                                            onClick = {
                                                val trimmedEmail = emailInput.trim()
                                                val trimmedPwd = emailPassword.trim()
                                                val trimmedPhone = emailPhoneNumber.trim()
                                                val trimmedName = emailResidentName.trim()

                                                if (trimmedEmail.isBlank()) {
                                                    localError = "Please enter your email address."
                                                } else if (trimmedPwd.length < 6) {
                                                    localError = "Password must be at least 6 characters long."
                                                } else if (isEmailRegisterMode) {
                                                    if (trimmedName.isBlank()) {
                                                        localError = "Please enter your full name."
                                                    } else if (trimmedPhone.length != 10) {
                                                        localError = "Please enter a valid 10-digit mobile phone number."
                                                    } else if (isPhoneNumberDuplicate(trimmedPhone)) {
                                                        localError = "This phone number is already registered in the backend. Please use a unique number."
                                                    } else {
                                                        localError = null
                                                        onClearMessages()
                                                        onRegisterSubmit(trimmedName, trimmedEmail, trimmedPhone, trimmedPwd)
                                                    }
                                                } else {
                                                    localError = null
                                                    onClearMessages()
                                                    onLoginSubmit(trimmedEmail, trimmedPwd)
                                                }
                                            },
                                            enabled = !isLoading,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("email_submit_button"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                                        ) {
                                            if (isLoading) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    AuthButtonLottieLoader(size = 22.dp, tint = Color.White)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(if (isEmailRegisterMode) "Creating account..." else "Signing in...", fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        if (isEmailRegisterMode) "Create Account & Sign In" else "Sign In",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Error Banner
                        AnimatedVisibility(visible = !displayedError.isNullOrBlank()) {
                            Surface(
                                color = ColorExpenseRed.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ColorExpenseRed.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Error",
                                        tint = ColorExpenseRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = displayedError.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ColorExpenseRed,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.testTag("error_message_text")
                                    )
                                }
                            }
                        }

                        // Success Banner
                        AnimatedVisibility(visible = !successMessage.isNullOrBlank()) {
                            Surface(
                                color = ColorReceivedGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, ColorReceivedGreen.copy(alpha = 0.30f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = ColorReceivedGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = successMessage.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = ColorReceivedGreen,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.testTag("success_message_text")
                                    )
                                }
                            }
                        }

                        // Animated Loading Feedback
                        AuthLoadingFeedbackBanner(
                            visible = isLoading,
                            isRegister = isEmailRegisterMode
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Mandatory Sign-In Security Notice
                Surface(
                    color = Slate200.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Secure Portal Access",
                            tint = SapphireNavy,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mandatory Sign-In: Continue with Google or Email credentials to view society accounts.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
