package com.example.ui.screens

import android.accounts.AccountManager
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
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
 * Google-only authentication screen. Firebase validates the Google ID token;
 * the authenticated identity is then checked against the configured access roster.
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
        onGoogleIdTokenSubmit = { idToken ->
            userViewModel.loginWithGoogleIdToken(idToken)
        },
        onClearMessages = {
            userViewModel.clearMessages()
        },
        onBack = onBack
    )
}

/**
 * Google-only authentication. No email/password, registration, or OTP forms are shown.
 * Authorization is handled after Firebase verifies the Google ID token.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    societyInfo: SocietyInfo? = null,
    availableMembers: List<Member> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onGoogleIdTokenSubmit: (idToken: String) -> Unit = {},
    onClearMessages: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    // Local UI feedback; authentication is handled only by verified Google ID tokens.
    var localError by remember { mutableStateOf<String?>(null) }
    val displayedError = errorMessage ?: localError

    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    fun launchVerifiedGoogleSignIn() {
        localError = null
        onClearMessages()
        coroutineScope.launch {
            try {
                val serverClientId = com.example.BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
                require(serverClientId.isNotEmpty() && !serverClientId.startsWith("YOUR_")) {
                    "Google sign-in is not configured. Set GOOGLE_WEB_CLIENT_ID to the Web client ID from Firebase Console."
                }
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val googleCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                onGoogleIdTokenSubmit(googleCredential.idToken)
            } catch (e: GetCredentialException) {
                localError = e.message ?: "Google sign-in was cancelled or unavailable. Please try again."
            } catch (e: Exception) {
                localError = e.message ?: "Unable to verify your Google account. Please try again."
            }
        }
    }
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
                        Column {
                            Text(
                                text = "Sign in to Temple Trust Ledger",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                            )
                            Text(
                                text = "Use your Google account to securely access the temple trust accounts. Firebase verifies your Google ID token before access is granted.",
                                style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.5.dp, Slate200),
                                shadowElevation = 1.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable(enabled = !isLoading) { launchVerifiedGoogleSignIn() }
                                    .testTag("google_sign_in_primary_button")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "G",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            color = Color(0xFF4285F4)
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = if (isLoading) "Signing in…" else "Continue with Google",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
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
                            text = "Secure sign-in: Firebase verifies your Google account before temple trust access is granted.",
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
