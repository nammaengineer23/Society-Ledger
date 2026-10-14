package com.example.ui.components

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun AuthMemberDialog(
    members: List<Member>,
    currentMember: Member?,
    isAdminMode: Boolean,
    onSelectMember: (Member) -> Unit,
    onTogglePublicMode: (Boolean) -> Unit,
    onOpenFirebaseLogin: () -> Unit = {},
    onLogout: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedMemberForAuth by remember { mutableStateOf<Member?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("auth_member_dialog"),
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
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (!isAdminMode) "Enter as Member" else "Committee Access Control",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (!isAdminMode) "Select your profile and enter PIN to enter as member" else "${members.size} Authorized Committee Members",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Public Transparency View Toggle Row
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!isAdminMode) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Public Transparency Mode (Read-Only)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (!isAdminMode) "Active: Public read-only view. Enter PIN below to login as Admin." else "Disabled: Committee Admin Mode Active",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = !isAdminMode,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    onTogglePublicMode(true)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.testTag("public_mode_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Firebase Cloud Auth Direct Action Button
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenFirebaseLogin()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_firebase_auth_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SocietyTealPrimary
                    ),
                    border = BorderStroke(1.dp, SocietyTealPrimary.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign In with Firebase Auth (Cloud)",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if ((currentMember != null || isAdminMode) && onLogout != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onLogout.invoke()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_dialog_logout_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "Log Out (Sign Out of Account)",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Direct PIN-based role authentication with smooth motion transition
                AnimatedContent(
                    targetState = selectedMemberForAuth,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220)) + slideInHorizontally(animationSpec = tween(220)) { if (targetState != null) it / 6 else -it / 6 })
                            .togetherWith(fadeOut(animationSpec = tween(160)) + slideOutHorizontally(animationSpec = tween(160)) { if (targetState != null) -it / 6 else it / 6 })
                    },
                    label = "auth_dialog_page_transition"
                ) { targetMember ->
                    if (targetMember == null) {
                        Column {
                            Text(
                                text = "Select Committee Member Profile:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 340.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(members) { member ->
                                    val isCurrent = currentMember?.id == member.id && isAdminMode
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedMemberForAuth = member
                                                enteredPin = ""
                                                pinError = false
                                            }
                                            .testTag("member_select_${member.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isCurrent) Color(0xFFE0F2F1) else MaterialTheme.colorScheme.surface
                                        ),
                                        border = if (isCurrent) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SocietyTealPrimary), width = 1.5.dp) else CardDefaults.outlinedCardBorder()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(38.dp)
                                                        .background(SocietyTealPrimary.copy(alpha = 0.12f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = member.name.take(1),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = SocietyTealPrimary
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = member.name,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        if (member.isSuperAdmin) {
                                                            Spacer(modifier = Modifier.width(6.dp))
                                                            Box(
                                                                modifier = Modifier
                                                                    .background(SapphireNavy, RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                                                            ) {
                                                                Text(
                                                                    text = "SUPER ADMIN",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                    color = Color.White,
                                                                    fontWeight = FontWeight.Bold
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = "${member.role} • ${member.phone}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            if (isCurrent) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Active",
                                                        tint = ColorReceivedGreen,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Active",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ColorReceivedGreen,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            } else {
                                                Text(
                                                    text = "Switch >",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SocietyTealPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // PIN Verification View for Selected Member
                        val member = targetMember
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Authenticating:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${member.role} • ${member.email}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = {
                                        if (it.length <= 6) {
                                            enteredPin = it
                                            pinError = false
                                        }
                                    },
                                    label = { Text("Enter 4-Digit Committee PIN") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
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
                                            val errorMsg = if (!member.isSuperAdmin && member.pin.isBlank()) {
                                                "No PIN allotted yet. Admin must allot a PIN in Committee Roster."
                                            } else {
                                                "Invalid PIN for ${member.name}. Please enter unique assigned 4-digit PIN."
                                            }
                                            Text(errorMsg, color = MaterialTheme.colorScheme.error)
                                        } else if (!member.isSuperAdmin && member.pin.isBlank()) {
                                            Text("No PIN allotted yet • Contact Admin", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        } else {
                                            Text("Enter unique 4-digit PIN assigned to ${member.name}")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("pin_input_field"),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { selectedMemberForAuth = null },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Back")
                                    }

                                    Button(
                                        onClick = {
                                            val trimmed = enteredPin.trim()
                                            val isValid = if (member.isSuperAdmin) {
                                                trimmed.isNotBlank() && trimmed == member.pin
                                            } else {
                                                member.pin.isNotBlank() && trimmed == member.pin
                                            }
                                            if (isValid) {
                                                onSelectMember(member)
                                                onDismiss()
                                            } else {
                                                pinError = true
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("confirm_login_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                                    ) {
                                        Text("Unlock & Login", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
