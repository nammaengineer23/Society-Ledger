package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditMemberDialog(
    existingMember: Member? = null,
    onSave: (name: String, email: String, phone: String, role: String, pin: String, isSuperAdmin: Boolean, openingBalance: Double, openingBalanceNotes: String) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = existingMember != null

    var name by remember { mutableStateOf(existingMember?.name ?: "") }
    var email by remember { mutableStateOf(existingMember?.email ?: "") }
    var phone by remember { mutableStateOf(existingMember?.phone ?: "") }
    var role by remember { mutableStateOf(existingMember?.role ?: "Committee Member") }
    var pin by remember { mutableStateOf(existingMember?.pin ?: "") }
    var isSuperAdmin by remember { mutableStateOf(existingMember?.isSuperAdmin ?: false) }
    var openingBalance by remember { mutableStateOf("0") }
    var openingBalanceNotes by remember { mutableStateOf("") }
    var openingBalanceError by remember { mutableStateOf(false) }
    var showPin by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    val roleSuggestions = listOf(
        "President",
        "General Secretary",
        "Treasurer",
        "Vice President",
        "Joint Secretary",
        "Maintenance Lead",
        "Cultural Lead",
        "Committee Member"
    )

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("add_edit_member_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuperAdmin) Icons.Default.Security else Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isEdit) "Modify Member" else "Add Committee Member",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isEdit) "Update member details & privileges" else "Add a new member to committee",
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

                // Full Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Full Name *") },
                    placeholder = { Text("e.g. Ramesh Kumar") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = SapphireNavy)
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Member name cannot be empty", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Role Input
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role / Designation *") },
                    placeholder = { Text("e.g. General Secretary") },
                    leadingIcon = {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = SapphireNavy)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_role_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role quick selection chips
                Text(
                    text = "Quick Roles:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roleSuggestions.forEach { suggestion ->
                        val isSelected = role.equals(suggestion, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) SapphireNavy else MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { role = suggestion }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Phone Input
                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                            phone = it
                            phoneError = false
                        }
                    },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("10-digit mobile number") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = SapphireNavy)
                    },
                    prefix = { Text("+91 ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = phoneError,
                    supportingText = if (phoneError) {
                        { Text("Enter a valid 10-digit phone number", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_phone_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Email Input
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("member@society.org") },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = SapphireNavy)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_email_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Security PIN Input
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            pin = it
                            pinError = false
                        }
                    },
                    label = { Text("Security Login PIN (4-6 digits) *") },
                    placeholder = { Text("Enter 4-6 digit PIN") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = SapphireNavy)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPin = !showPin }) {
                            Icon(
                                imageVector = if (showPin) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = pinError,
                    supportingText = if (pinError) {
                        { Text("PIN must be at least 4 digits", color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_pin_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (!isEdit) {
                    Text(
                        text = "Opening balance / advance (₹)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = {
                            if (it.isBlank() || it.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                openingBalance = it
                                openingBalanceError = false
                            }
                        },
                        label = { Text("Amount already held / advanced") },
                        supportingText = {
                            Text(if (openingBalanceError) "Enter a valid non-negative amount" else "Use 0 if there is no opening advance")
                        },
                        isError = openingBalanceError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("member_opening_balance_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = openingBalanceNotes,
                        onValueChange = { openingBalanceNotes = it },
                        label = { Text("Opening balance notes / reference") },
                        supportingText = { Text("Optional note for the audit trail") },
                        modifier = Modifier.fillMaxWidth().testTag("member_opening_balance_notes")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Super Admin Privileges Switch Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSuperAdmin) SapphireNavy.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
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
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isSuperAdmin) SapphireNavy else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Admin Access",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSuperAdmin) SapphireNavy else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Grants exclusive authority to add, modify, and delete other committee members.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isSuperAdmin,
                            onCheckedChange = { isSuperAdmin = it },
                            modifier = Modifier.testTag("super_admin_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            var valid = true
                            if (name.isBlank()) {
                                nameError = true
                                valid = false
                            }
                            if (phone.length < 10) {
                                phoneError = true
                                valid = false
                            }
                            if (pin.length < 4) {
                                pinError = true
                                valid = false
                            }

                            if (valid) {
                                onSave(
                                    name.trim(),
                                    email.trim(),
                                    phone.trim(),
                                    role.trim(),
                                    pin.trim(),
                                    isSuperAdmin
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_member_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy)
                    ) {
                        Text(if (isEdit) "Update" else "Add Member", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
