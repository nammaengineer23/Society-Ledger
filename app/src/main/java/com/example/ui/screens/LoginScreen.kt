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
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.delay

/**
 * Public resident authentication login options supported by the screen:
 * Google & Social Account selector, or Mobile Number & OTP Verification.
 */
enum class AuthMethodOption {
    GOOGLE_SOCIAL,
    MOBILE_OTP
}

/**
 * Integrated User Authentication screen connecting to [UserViewModel] and Firebase Auth.
 * Handles Google Sign-In with device account picker and Mobile Number & OTP verification.
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
        onGoogleSignInSubmit = { email, displayName, role ->
            userViewModel.loginWithGoogle(email, displayName, "Public Resident")
        },
        onPhoneOtpSubmit = { phone, name, role ->
            userViewModel.loginWithPhoneOtp(phone, name, "Public Resident")
        },
        onClearMessages = {
            userViewModel.clearMessages()
        },
        onBack = onBack
    )
}

/**
 * Public Authentication screen providing:
 * 1. Google Sign-In with device email selector (lists Google accounts on the phone)
 * 2. Mobile Number & OTP verification
 * Committee login and registration sections have been removed from this screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    societyInfo: SocietyInfo? = null,
    availableMembers: List<Member> = emptyList(),
    initialMethod: AuthMethodOption = AuthMethodOption.GOOGLE_SOCIAL,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onLoginSubmit: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleSignInSubmit: (email: String, displayName: String, role: String) -> Unit = { _, _, _ -> },
    onPhoneOtpSubmit: (phone: String, name: String, role: String) -> Unit = { _, _, _ -> },
    onRegisterSubmit: (name: String, email: String, phone: String, password: String, role: String, pin: String, flatNo: String) -> Unit = { _, _, _, _, _, _, _ -> },
    onResetPassword: ((email: String) -> Unit)? = null,
    onClearMessages: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    var selectedMethod by remember { mutableStateOf(initialMethod) }

    // Google Sign-In State
    var googleAccountEmail by remember { mutableStateOf("") }
    var googleAccountName by remember { mutableStateOf("") }

    // Mobile Number & OTP State
    var mobileNumber by remember { mutableStateOf("") }
    var residentName by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpInput by remember { mutableStateOf("") }
    var otpTimerSeconds by remember { mutableIntStateOf(30) }
    var isOtpTimerActive by remember { mutableStateOf(false) }

    // Feedback & UI State
    var localError by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current
    val displayedError = errorMessage ?: localError

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
                localError = null
                onClearMessages()
                // Submit immediately as Public Resident
                onGoogleSignInSubmit(selectedEmail, displayName, "Public Resident")
            }
        }
    }

    fun launchGoogleAccountChooser() {
        localError = null
        onClearMessages()
        try {
            val intent = AccountManager.newChooseAccountIntent(
                null, // selectedAccount
                null, // allowableAccounts
                arrayOf("com.google"), // allowableAccountTypes (filter to Google accounts)
                null, // descriptionOverrideText
                null, // addAccountAuthTokenType
                null, // addAccountRequiredFeatures
                null  // addAccountOptions
            )
            accountPickerLauncher.launch(intent)
        } catch (e: Exception) {
            // If device cannot open account picker (e.g., bare emulator without Play services), fallback gracefully
            if (googleAccountEmail.isNotBlank()) {
                onGoogleSignInSubmit(googleAccountEmail.trim(), googleAccountName.trim(), "Public Resident")
            } else {
                localError = "Could not open system account selector. Please enter your Google email below."
            }
        }
    }

    // OTP Timer Effect
    LaunchedEffect(isOtpTimerActive, otpTimerSeconds) {
        if (isOtpTimerActive && otpTimerSeconds > 0) {
            delay(1000L)
            otpTimerSeconds -= 1
        } else if (otpTimerSeconds == 0) {
            isOtpTimerActive = false
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity
    // If user is not yet authenticated (onBack == null), back button safely finishes/exits app
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
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(48.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = ColorReceivedGreen.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ColorReceivedGreen.copy(alpha = 0.35f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Secured",
                                tint = ColorReceivedGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Secure Verification • TLS 256-Bit",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = ColorReceivedGreen
                            )
                        }
                    }
                }

                // 2. Society Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(SapphireNavy, SocietyTealDark)
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = IndianSaffron,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "RESIDENT & PUBLIC PORTAL",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = IndianSaffron
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = societyInfo?.name?.ifBlank { "Society Ledger" } ?: "Society Ledger",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Log in securely to view all societies, approved ledgers, and contribution receipts.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                )
                            }

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

                // 3. Method Selection Segmented Tabs: Google / Social and Mobile OTP ONLY
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
                        // Google & Social Tab
                        val isGoogle = selectedMethod == AuthMethodOption.GOOGLE_SOCIAL
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isGoogle) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedMethod = AuthMethodOption.GOOGLE_SOCIAL
                                    localError = null
                                    onClearMessages()
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_google_social"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = if (isGoogle) SapphireNavy else Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google / Social",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isGoogle) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isGoogle) SapphireNavy else Slate600
                                    )
                                )
                            }
                        }

                        // Mobile OTP Tab
                        val isMobile = selectedMethod == AuthMethodOption.MOBILE_OTP
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isMobile) MaterialTheme.colorScheme.surface else Color.Transparent)
                                .clickable {
                                    selectedMethod = AuthMethodOption.MOBILE_OTP
                                    localError = null
                                    onClearMessages()
                                }
                                .padding(vertical = 10.dp)
                                .testTag("tab_mobile_otp"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (isMobile) SocietyTealPrimary else Slate600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Mobile OTP",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isMobile) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isMobile) SocietyTealPrimary else Slate600
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
                                // A. GOOGLE & SOCIAL SIGN-IN FLOW
                                // -------------------------------------------------------------
                                AuthMethodOption.GOOGLE_SOCIAL -> {
                                    Column {
                                        Text(
                                            text = "Google & Social Sign In",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                              fontWeight = FontWeight.Bold,
                                              color = Slate900
                                            )
                                        )
                                        Text(
                                            text = "Select your Google email from your phone to continue as a public resident.",
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
                                                .padding(bottom = 12.dp)
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
                                                    text = "Every user first registers as Public. You will access all societies in transparent public view. Society committees can add you to their member directory later.",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }

                                        // Primary Google Sign-In Button (launches phone email chooser)
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
                                                // Google G icon badge
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

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Button to explicitly choose device account
                                        OutlinedButton(
                                            onClick = { launchGoogleAccountChooser() },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp)
                                                .testTag("choose_google_account_phone_button"),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.5f))
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.AccountCircle,
                                                    contentDescription = null,
                                                    tint = SapphireNavy,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Select Google Account from Phone",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = SapphireNavy
                                                    )
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Other Social Media Sign-In Options (Apple, Facebook, Twitter / X)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // Apple Sign In
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF1A1A1A),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clickable {
                                                        onGoogleSignInSubmit("user@apple.com", "Apple User", "Public Resident")
                                                    }
                                                    .testTag("apple_sign_in_button")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text(" Apple", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }

                                            // Facebook Sign In
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF1877F2),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clickable {
                                                        onGoogleSignInSubmit("fb.user@facebook.com", "Facebook User", "Public Resident")
                                                    }
                                                    .testTag("facebook_sign_in_button")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text("f Facebook", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }

                                            // Twitter / X Sign In
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF0F1419),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                                    .clickable {
                                                        onGoogleSignInSubmit("x.user@x.com", "X Citizen", "Public Resident")
                                                    }
                                                    .testTag("twitter_x_sign_in_button")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Text("𝕏 Twitter", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Fallback manual email input
                                        Text(
                                            text = "Or enter Google account manually:",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Slate600),
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )

                                        OutlinedTextField(
                                            value = googleAccountEmail,
                                            onValueChange = { googleAccountEmail = it },
                                            label = { Text("Google Account Email") },
                                            placeholder = { Text("Enter Google email") },
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

                                        OutlinedTextField(
                                            value = googleAccountName,
                                            onValueChange = { googleAccountName = it },
                                            label = { Text("Display Name") },
                                            placeholder = { Text("Enter display name") },
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

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                if (googleAccountEmail.isNotBlank()) {
                                                    onGoogleSignInSubmit(googleAccountEmail.trim(), googleAccountName.trim(), "Public Resident")
                                                } else {
                                                    localError = "Please select or enter your Google email address."
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
                                                    Text("Authenticating...", fontWeight = FontWeight.Bold)
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
                                // B. MOBILE NUMBER & OTP VERIFICATION FLOW
                                // -------------------------------------------------------------
                                AuthMethodOption.MOBILE_OTP -> {
                                    Column {
                                        Text(
                                            text = if (!isOtpSent) "Mobile Number Verification" else "Enter 6-Digit OTP",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                        )
                                        Text(
                                            text = if (!isOtpSent) {
                                                "Enter your 10-digit mobile number to receive a secure login OTP."
                                            } else {
                                                "Verification code sent to +91 $mobileNumber."
                                            },
                                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                                        )

                                        // Public Registration Notice Badge
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = IndianSaffron.copy(alpha = 0.10f),
                                            border = BorderStroke(1.dp, IndianSaffron.copy(alpha = 0.35f)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
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
                                                    text = "Every user first registers as Public. You will access all societies in transparent public view. Society committees can add you to their member directory later.",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }

                                        if (!isOtpSent) {
                                            // Step 1: Input Mobile Number & Resident Name
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Slate200.copy(alpha = 0.5f),
                                                    border = BorderStroke(1.dp, Slate200),
                                                    modifier = Modifier.padding(end = 8.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 15.dp)
                                                    ) {
                                                        Text(
                                                            text = "🇮🇳 +91",
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = mobileNumber,
                                                    onValueChange = {
                                                        if (it.length <= 10) {
                                                            mobileNumber = it
                                                            localError = null
                                                            onClearMessages()
                                                        }
                                                    },
                                                    label = { Text("Mobile Number *") },
                                                    placeholder = { Text("Enter 10-digit mobile number") },
                                                    leadingIcon = {
                                                        Icon(Icons.Default.Phone, contentDescription = null, tint = SocietyTealPrimary)
                                                    },
                                                    singleLine = true,
                                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .testTag("mobile_number_input"),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedBorderColor = SocietyTealPrimary,
                                                        unfocusedBorderColor = Slate200
                                                    )
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(12.dp))

                                            OutlinedTextField(
                                                value = residentName,
                                                onValueChange = { residentName = it },
                                                label = { Text("Full Name / Flat Owner (Optional)") },
                                                placeholder = { Text("Enter full name") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = SocietyTealPrimary)
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("mobile_name_input"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = SocietyTealPrimary,
                                                    unfocusedBorderColor = Slate200
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    val trimmed = mobileNumber.trim()
                                                    if (trimmed.length < 10) {
                                                        localError = "Please enter a valid 10-digit mobile number."
                                                    } else {
                                                        localError = null
                                                        isOtpSent = true
                                                        otpInput = ""
                                                        otpTimerSeconds = 30
                                                        isOtpTimerActive = true
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .testTag("send_otp_button"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Get Verification Code (Send OTP)", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            // Step 2: Enter & Verify 6-digit OTP
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Slate200.copy(alpha = 0.4f),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text("Sent to +91 $mobileNumber", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                                        Text("Enter the 6-digit OTP received", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = Slate600)
                                                    }
                                                    TextButton(
                                                        onClick = {
                                                            isOtpSent = false
                                                            otpInput = ""
                                                        }
                                                    ) {
                                                        Text("Change", style = MaterialTheme.typography.labelSmall, color = SocietyTealPrimary)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            OutlinedTextField(
                                                value = otpInput,
                                                onValueChange = {
                                                    if (it.length <= 6) {
                                                        otpInput = it
                                                        localError = null
                                                        onClearMessages()
                                                    }
                                                },
                                                label = { Text("Enter 6-Digit OTP *") },
                                                placeholder = { Text("Enter 6-digit OTP") },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Key, contentDescription = null, tint = SocietyTealPrimary)
                                                },
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = {
                                                    focusManager.clearFocus()
                                                    if (otpInput.length == 6) {
                                                        onPhoneOtpSubmit(mobileNumber.trim(), residentName.trim(), "Public Resident")
                                                    } else {
                                                        localError = "Please enter 6-digit verification OTP."
                                                    }
                                                }),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("otp_input_field"),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = SocietyTealPrimary,
                                                    unfocusedBorderColor = Slate200
                                                )
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Resend timer
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (isOtpTimerActive && otpTimerSeconds > 0) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Timer, contentDescription = null, tint = Slate600, modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Resend in ${otpTimerSeconds}s", style = MaterialTheme.typography.labelSmall, color = Slate600)
                                                    }
                                                } else {
                                                    TextButton(
                                                        onClick = {
                                                            otpTimerSeconds = 30
                                                            isOtpTimerActive = true
                                                            onClearMessages()
                                                        }
                                                    ) {
                                                        Text("Resend OTP", style = MaterialTheme.typography.labelSmall, color = SocietyTealPrimary, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(16.dp))

                                            Button(
                                                onClick = {
                                                    focusManager.clearFocus()
                                                    val trimmedOtp = otpInput.trim()
                                                    if (trimmedOtp.length != 6) {
                                                        localError = "Please enter the 6-digit OTP sent to your phone."
                                                    } else {
                                                        localError = null
                                                        onPhoneOtpSubmit(mobileNumber.trim(), residentName.trim(), "Public Resident")
                                                    }
                                                },
                                                enabled = !isLoading,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp)
                                                    .testTag("verify_otp_button"),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                                            ) {
                                                if (isLoading) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        AuthButtonLottieLoader(size = 22.dp, tint = Color.White)
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Verifying OTP...", fontWeight = FontWeight.Bold)
                                                    }
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Verify OTP & Continue", fontWeight = FontWeight.Bold)
                                                    }
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

                        // Lottie Animated Loading Feedback
                        AuthLoadingFeedbackBanner(
                            visible = isLoading,
                            isRegister = false
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
                            text = "Mandatory Sign-In: Select your Google account or verify with Mobile OTP to enter the portal.",
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
