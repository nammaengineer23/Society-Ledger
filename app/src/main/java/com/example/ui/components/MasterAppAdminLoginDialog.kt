package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.viewmodel.AdminViewModel

/**
 * Master App Admin Login Modal Dialog.
 *
 * Provides a secure authorization dialog for the Master App Admin Console.
 * Requires:
 * 1. Master Admin Email (authorized: nammaengineer23@gmail.com)
 * 2. Master PIN (cloud-synced via Firestore app_config/master_admin, or bootstrap 9999)
 *
 * Includes submit button that triggers the ViewModel's authentication function.
 */
@Composable
fun MasterAppAdminLoginDialog(
    adminViewModel: AdminViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val isAuthenticating by adminViewModel.isAuthenticating.collectAsState()
    val authError by adminViewModel.authErrorMessage.collectAsState()

    MasterAppAdminLoginDialogContent(
        isAuthenticating = isAuthenticating,
        externalErrorMessage = authError,
        onDismiss = onDismiss,
        onAuthenticate = { email, pin, onResult ->
            adminViewModel.authenticate(email, pin, onResult)
        },
        onSuccess = onSuccess
    )
}

/**
 * Overload accepting authentication callback directly for clean composition and testing.
 */
@Composable
fun MasterAppAdminLoginDialog(
    onDismiss: () -> Unit,
    onAuthenticate: (email: String, pin: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onSuccess: () -> Unit,
    initialEmail: String = "nammaengineer23@gmail.com"
) {
    MasterAppAdminLoginDialogContent(
        isAuthenticating = false,
        externalErrorMessage = null,
        onDismiss = onDismiss,
        onAuthenticate = onAuthenticate,
        onSuccess = onSuccess,
        initialEmail = initialEmail
    )
}

@Composable
private fun MasterAppAdminLoginDialogContent(
    isAuthenticating: Boolean,
    externalErrorMessage: String?,
    onDismiss: () -> Unit,
    onAuthenticate: (email: String, pin: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onSuccess: () -> Unit,
    initialEmail: String = "nammaengineer23@gmail.com"
) {
    var emailInput by remember { mutableStateOf(initialEmail) }
    var pinInput by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    var inProgress by remember { mutableStateOf(false) }

    val activeError = localError ?: externalErrorMessage
    val isBusy = isAuthenticating || inProgress

    KeyboardAwareAlertDialog(
        onDismissRequest = {
            if (!isBusy) onDismiss()
        },
        modifier = Modifier.testTag("master_app_admin_dialog"),
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Master Admin Security",
                tint = IndianSaffron,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Master Admin Authorization",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = SapphireNavy
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Access to the Master Admin Console is restricted to the authorized Cloud Master Admin ($initialEmail).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Email Text Field
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        localError = null
                    },
                    label = { Text("Master Admin Email") },
                    placeholder = { Text("nammaengineer23@gmail.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("master_admin_email_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Admin Email",
                            tint = SapphireNavy
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireNavy,
                        focusedLabelColor = SapphireNavy
                    ),
                    enabled = !isBusy
                )

                // 2. Master PIN Text Field
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = {
                        pinInput = it
                        localError = null
                    },
                    label = { Text("Master PIN") },
                    placeholder = { Text("Enter Master PIN") },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("master_admin_pin_input"),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Master PIN",
                            tint = SapphireNavy
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SapphireNavy,
                        focusedLabelColor = SapphireNavy
                    ),
                    enabled = !isBusy
                )

                // Error Message Display
                if (activeError != null) {
                    Text(
                        text = activeError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("master_admin_error_text")
                    )
                }

                // Authenticating Progress Spinner
                if (isBusy) {
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

                Text(
                    text = "Cloud Security Verification",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (emailInput.isBlank() || pinInput.isBlank()) {
                        localError = "Email and Master PIN are required"
                        return@Button
                    }
                    inProgress = true
                    localError = null
                    onAuthenticate(emailInput.trim(), pinInput.trim()) { success, msg ->
                        inProgress = false
                        if (success) {
                            onSuccess()
                        } else {
                            localError = if (msg.isNotBlank()) msg else "Access Denied: Authorization failed."
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                shape = RoundedCornerShape(10.dp),
                enabled = !isBusy,
                modifier = Modifier.testTag("authorize_open_console_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = IndianSaffron,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Authorize & Open Console",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isBusy,
                modifier = Modifier.testTag("cancel_admin_auth_btn")
            ) {
                Text(
                    text = "Cancel",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
