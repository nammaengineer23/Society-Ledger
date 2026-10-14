package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Member
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun ChangePinDialog(
    member: Member,
    isSuperAdminPerformingReset: Boolean = false,
    onSaveNewPin: (newPin: String) -> Unit,
    onResetToDefaultPin: () -> Unit,
    onDismiss: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showOldPin by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("change_pin_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuperAdminPerformingReset) Icons.Default.Security else Icons.Default.LockReset,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isSuperAdminPerformingReset) "Admin PIN Reset" else "Change My PIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${member.name} (${member.role})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // If not Super Admin reset, require Old PIN verification
                if (!isSuperAdminPerformingReset) {
                    OutlinedTextField(
                        value = oldPin,
                        onValueChange = {
                            if (it.length <= 6) {
                                oldPin = it
                                errorMessage = null
                            }
                        },
                        label = { Text("Current PIN *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showOldPin = !showOldPin }) {
                                Icon(
                                    imageVector = if (showOldPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (showOldPin) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("old_pin_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // New PIN input
                OutlinedTextField(
                    value = newPin,
                    onValueChange = {
                        if (it.length <= 6) {
                            newPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("New PIN (4 to 6 Digits) *") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showNewPin = !showNewPin }) {
                            Icon(
                                imageVector = if (showNewPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_pin_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Confirm New PIN
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = {
                        if (it.length <= 6) {
                            confirmPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm New PIN *") },
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null) },
                    trailingIcon = {
                        IconButton(
                            onClick = { showConfirmPin = !showConfirmPin },
                            modifier = Modifier.testTag("toggle_confirm_pin_visibility")
                        ) {
                            Icon(
                                imageVector = if (showConfirmPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showConfirmPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(errorMessage!!, color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_new_pin_input"),
                    singleLine = true
                )

                // Super Admin quick reset card
                if (isSuperAdminPerformingReset) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Admin Power Action:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy
                            )
                            Text(
                                text = "Instantly restore this committee member's access by resetting their PIN back to default.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onResetToDefaultPin()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("reset_to_default_pin_btn")
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reset Member PIN to Default", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (!isSuperAdminPerformingReset && member.pin.isNotBlank() && oldPin != member.pin) {
                                errorMessage = "Current PIN is incorrect"
                                return@Button
                            }
                            if (newPin.length < 4) {
                                errorMessage = "New PIN must be at least 4 digits"
                                return@Button
                            }
                            if (newPin != confirmPin) {
                                errorMessage = "New PIN and Confirm PIN do not match"
                                return@Button
                            }
                            onSaveNewPin(newPin)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("save_new_pin_btn")
                    ) {
                        Text("Save New PIN", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
