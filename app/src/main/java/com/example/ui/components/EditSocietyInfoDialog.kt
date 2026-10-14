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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SocietyInfo
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

@Composable
fun EditSocietyInfoDialog(
    societyInfo: SocietyInfo,
    onDismiss: () -> Unit,
    onSave: (SocietyInfo) -> Unit
) {
    var name by remember { mutableStateOf(societyInfo.name) }
    var registrationNo by remember { mutableStateOf(societyInfo.registrationNo) }
    var upiId by remember { mutableStateOf(societyInfo.upiId) }
    var bankName by remember { mutableStateOf(societyInfo.bankName) }
    var accountNumber by remember { mutableStateOf(societyInfo.accountNumber) }
    var ifscCode by remember { mutableStateOf(societyInfo.ifscCode) }
    var branchName by remember { mutableStateOf(societyInfo.branchName) }
    var address by remember { mutableStateOf(societyInfo.address) }
    var city by remember { mutableStateOf(societyInfo.city) }
    var state by remember { mutableStateOf(societyInfo.state) }
    var pinCode by remember { mutableStateOf(societyInfo.pinCode) }
    var contactPhone by remember { mutableStateOf(societyInfo.contactPhone.filter { it.isDigit() }) }
    var contactEmail by remember { mutableStateOf(societyInfo.contactEmail) }
    var financialYear by remember { mutableStateOf(societyInfo.financialYear) }
    var openingBalanceText by remember { mutableStateOf(societyInfo.openingBalance.toLong().toString()) }
    var presidentName by remember { mutableStateOf(societyInfo.presidentName) }
    var secretaryName by remember { mutableStateOf(societyInfo.secretaryName) }
    var treasurerName by remember { mutableStateOf(societyInfo.treasurerName) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var balanceError by remember { mutableStateOf<String?>(null) }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .testTag("edit_society_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
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
                                .size(42.dp)
                                .background(SapphireNavy.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Society & Bank Master Details",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Admin Configuration & Official Info",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Society Identity
                    Text(
                        text = "1. SOCIETY IDENTIFICATION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SapphireNavy
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            if (nameError != null && it.isNotBlank()) nameError = null
                        },
                        label = { Text("Society / RWA Full Name *") },
                        placeholder = { Text("Enter society name") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                        isError = nameError != null,
                        supportingText = {
                            if (nameError != null) Text(nameError!!, color = Color.Red)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("society_name_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = registrationNo,
                            onValueChange = { registrationNo = it },
                            label = { Text("Registration No.") },
                            placeholder = { Text("Enter registration number") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("society_reg_input")
                        )

                        OutlinedTextField(
                            value = financialYear,
                            onValueChange = { financialYear = it },
                            label = { Text("Financial Year") },
                            placeholder = { Text("FY 2026-2027") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("society_fy_input")
                        )
                    }

                    AutoFetchLocationCard(
                        onLocationFetched = { details ->
                            if (details.city.isNotBlank()) city = details.city
                            if (details.state.isNotBlank()) state = details.state
                            if (details.address.isNotBlank()) address = details.address
                            if (details.pinCode.isNotBlank()) pinCode = details.pinCode
                        },
                        testTagPrefix = "edit_society_location"
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Society Office Address / Locality") },
                        placeholder = { Text("Enter society office address") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("society_address_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("City *") },
                            placeholder = { Text("Enter city") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("society_city_input")
                        )

                        OutlinedTextField(
                            value = state,
                            onValueChange = { state = it },
                            label = { Text("State") },
                            placeholder = { Text("Enter state") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("society_state_input")
                        )
                    }

                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { input ->
                            if (input.length <= 6 && input.all { it.isDigit() }) pinCode = input
                        },
                        label = { Text("PIN Code (6 digits)") },
                        placeholder = { Text("Enter PIN code") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("society_pincode_input")
                    )

                    // 2. Official Bank Details (Optional)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. OFFICIAL SOCIETY BANK ACCOUNT DETAILS (OPTIONAL)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SocietyTealPrimary
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = bankName,
                                onValueChange = { bankName = it },
                                label = { Text("Bank Name (Optional)") },
                                placeholder = { Text("Enter bank name") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("society_bank_name_input")
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = accountNumber,
                                    onValueChange = { input ->
                                        accountNumber = input.filter { it.isLetterOrDigit() }
                                    },
                                    label = { Text("Account Number (Optional)") },
                                    placeholder = { Text("Enter account number") },
                                    leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("society_account_input")
                                )

                                OutlinedTextField(
                                    value = ifscCode,
                                    onValueChange = { input ->
                                        ifscCode = input.uppercase().trim()
                                    },
                                    label = { Text("IFSC Code (Optional)") },
                                    placeholder = { Text("Enter IFSC code") },
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .testTag("society_ifsc_input")
                                )
                            }

                            OutlinedTextField(
                                value = branchName,
                                onValueChange = { branchName = it },
                                label = { Text("Branch Name & City (Optional)") },
                                placeholder = { Text("Enter branch name") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("society_branch_input")
                            )

                            OutlinedTextField(
                                value = upiId,
                                onValueChange = { upiId = it.trim() },
                                label = { Text("Official Society UPI ID (Optional)") },
                                placeholder = { Text("Enter UPI ID") },
                                leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("society_upi_input")
                            )
                        }
                    }

                    // 3. Opening Financial Baseline & Present Balance
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. PRESENT STARTING BALANCE / OPENING RESERVE",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SapphireNavy
                    )

                    OutlinedTextField(
                        value = openingBalanceText,
                        onValueChange = { input ->
                            val digits = input.filter { it.isDigit() }
                            openingBalanceText = digits
                            if (balanceError != null) balanceError = null
                        },
                        label = { Text("Opening Reserve / Bank Balance (INR ₹) *") },
                        placeholder = { Text("0") },
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = balanceError != null,
                        supportingText = {
                            if (balanceError != null) {
                                Text(balanceError!!, color = Color.Red)
                            } else {
                                Text("Starting bank surplus at beginning of financial year (Whole integer ₹)")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("society_opening_balance_input")
                    )

                    // 4. Official Contact Info
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "4. OFFICIAL CONTACT INFO & SIGNATORIES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = SapphireNavy
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = contactPhone,
                            onValueChange = { input ->
                                val digits = input.filter { it.isDigit() }
                                if (digits.length <= 10) {
                                    contactPhone = digits
                                    if (phoneError != null && digits.length == 10) {
                                        phoneError = null
                                    }
                                }
                            },
                            label = { Text("Helpline Phone (10 Digits) *") },
                            placeholder = { Text("Enter phone number") },
                            prefix = { Text("+91 ") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            isError = phoneError != null,
                            supportingText = {
                                if (phoneError != null) Text(phoneError!!, color = Color.Red)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("society_phone_input")
                        )

                        OutlinedTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it.trim() },
                            label = { Text("Official Email") },
                            placeholder = { Text("Enter contact email") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("society_email_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = presidentName,
                            onValueChange = { presidentName = it },
                            label = { Text("President") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = secretaryName,
                            onValueChange = { secretaryName = it },
                            label = { Text("Secretary") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = treasurerName,
                            onValueChange = { treasurerName = it },
                            label = { Text("Treasurer") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            var hasError = false
                            if (name.trim().isBlank()) {
                                nameError = "Society name is required"
                                hasError = true
                            }
                            val cleanPhone = contactPhone.trim().filter { it.isDigit() }
                            if (cleanPhone.isNotEmpty() && cleanPhone.length != 10) {
                                phoneError = "Phone number must be 10 digits"
                                hasError = true
                            }
                            val parsedOpeningBalance = openingBalanceText.trim().toLongOrNull()
                            if (parsedOpeningBalance == null || parsedOpeningBalance < 0) {
                                balanceError = "Enter a valid positive integer whole amount (or 0)"
                                hasError = true
                            }

                            if (!hasError && parsedOpeningBalance != null) {
                                val generatedBankAccount = if (bankName.isNotBlank() && accountNumber.isNotBlank()) {
                                    "$bankName A/C: $accountNumber (IFSC: $ifscCode${if (branchName.isNotBlank()) ", $branchName" else ""})"
                                } else {
                                    societyInfo.bankAccount
                                }

                                val updatedInfo = societyInfo.copy(
                                    name = name.trim(),
                                    registrationNo = registrationNo.trim(),
                                    upiId = upiId.trim(),
                                    bankName = bankName.trim(),
                                    accountNumber = accountNumber.trim(),
                                    ifscCode = ifscCode.trim(),
                                    branchName = branchName.trim(),
                                    bankAccount = generatedBankAccount,
                                    address = address.trim(),
                                    city = city.trim(),
                                    state = state.trim(),
                                    pinCode = pinCode.trim(),
                                    contactPhone = cleanPhone,
                                    contactEmail = contactEmail.trim(),
                                    financialYear = financialYear.trim().ifBlank { "FY 2026-2027" },
                                    openingBalance = parsedOpeningBalance.toDouble(),
                                    presidentName = presidentName.trim(),
                                    secretaryName = secretaryName.trim(),
                                    treasurerName = treasurerName.trim()
                                )
                                onSave(updatedInfo)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        modifier = Modifier.testTag("save_society_info_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Master Details")
                    }
                }
            }
        }
    }
}
