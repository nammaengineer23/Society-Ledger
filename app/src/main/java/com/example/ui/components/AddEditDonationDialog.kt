package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Donation
import com.example.data.model.Member
import com.example.data.model.SocietyInfo
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddEditDonationDialog(
    existingDonation: Donation? = null,
    currentMember: Member?,
    societyInfo: SocietyInfo = SocietyInfo(),
    onSave: (
        donorName: String,
        phone: String,
        amount: Double,
        status: String,
        paymentMode: String,
        paymentDate: Long,
        notes: String,
        modificationReason: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var donorName by remember { mutableStateOf(existingDonation?.donorName ?: "") }
    var phone by remember { mutableStateOf(existingDonation?.phone ?: "") }
    var amountText by remember { mutableStateOf(existingDonation?.amount?.toLong()?.toString() ?: "") }
    var status by remember { mutableStateOf(existingDonation?.status ?: "received") }
    var paymentMode by remember { mutableStateOf(existingDonation?.paymentMode ?: "UPI/Online") }
    var notes by remember { mutableStateOf(existingDonation?.notes ?: "") }
    var upiReferenceNo by remember { mutableStateOf("") }
    var modificationReason by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(existingDonation?.paymentDate ?: System.currentTimeMillis()) }

    val context = LocalContext.current
    val calendar = remember(paymentDate) {
        Calendar.getInstance().apply { timeInMillis = paymentDate }
    }
    val datePickerDialog = remember(paymentDate, context) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                paymentDate = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    var donorNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var reasonError by remember { mutableStateOf<String?>(null) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val modes = listOf("UPI/Online", "Cash", "Bank Transfer", "Cheque")

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("add_edit_donation_dialog"),
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
                        Icon(
                            imageVector = Icons.Default.VolunteerActivism,
                            contentDescription = null,
                            tint = SocietyTealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingDonation != null) "Edit Donation" else "Record New Donation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Donor Full Name
                OutlinedTextField(
                    value = donorName,
                    onValueChange = {
                        donorName = it
                        if (donorNameError != null && it.isNotBlank()) {
                            donorNameError = null
                        }
                    },
                    label = { Text("Donor Full Name *") },
                    placeholder = { Text("Enter donor full name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = donorNameError != null,
                    supportingText = {
                        if (donorNameError != null) {
                            Text(donorNameError!!, color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donor_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Phone Number (10-digit validation)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        if (digitsOnly.length <= 10) {
                            phone = digitsOnly
                            if (phoneError != null && digitsOnly.length == 10) {
                                phoneError = null
                            }
                        }
                    },
                    label = { Text("Donor Phone Number (10 Digits) *") },
                    placeholder = { Text("9810011223") },
                    prefix = { Text("+91 ") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = phoneError != null,
                    supportingText = {
                        if (phoneError != null) {
                            Text(phoneError!!, color = Color.Red)
                        } else if (phone.isNotEmpty() && phone.length < 10) {
                            Text("${10 - phone.length} more digits needed for 10-digit phone", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            Text("Used for instant receipt and WhatsApp updates")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donor_phone_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Amount in INR (Strict Positive Integer Validation)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        // Only allow whole digits
                        val digitsOnly = input.filter { it.isDigit() }
                        amountText = digitsOnly
                        if (amountError != null) {
                            val v = digitsOnly.toLongOrNull()
                            if (v != null && v > 0) {
                                amountError = null
                            }
                        }
                    },
                    label = { Text("Donation Amount (INR ₹ - Positive Integer) *") },
                    placeholder = { Text("5000") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError != null,
                    supportingText = {
                        if (amountError != null) {
                            Text(amountError!!, color = Color.Red)
                        } else {
                            Text("Enter whole rupee amount (must be positive integer > ₹0)")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donor_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Status Toggle: Received vs Pending
                Text(
                    text = "Payment Status *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Received Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (status == "received") ColorReceivedGreen else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { status = "received" }
                            .padding(vertical = 10.dp)
                            .testTag("status_received_option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ Received",
                            fontWeight = FontWeight.Bold,
                            color = if (status == "received") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Pending Option
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (status == "pending") ColorPendingOrange else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { status = "pending" }
                            .padding(vertical = 10.dp)
                            .testTag("status_pending_option"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏳ Pending (Pledge)",
                            fontWeight = FontWeight.Bold,
                            color = if (status == "pending") Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Payment Mode Selection
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (status == "received") "Payment Mode *" else "Preferred Payment Mode *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    modes.forEach { mode ->
                        val isSelected = paymentMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) {
                                        if (mode == "UPI/Online") Color(0xFF5E35B1) else SocietyTealPrimary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { paymentMode = mode }
                                .padding(vertical = 8.dp)
                                .testTag("payment_mode_${mode.replace("/", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (mode == "UPI/Online") "UPI / QR" else mode.replace("/Online", ""),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Dynamic Society UPI QR Code display when UPI/Online is selected
                if (paymentMode == "UPI/Online") {
                    Spacer(modifier = Modifier.height(12.dp))
                    val parsedCurrentAmount = amountText.trim().toDoubleOrNull()
                    SocietyUpiQrCard(
                        societyInfo = societyInfo,
                        amount = parsedCurrentAmount,
                        donorName = donorName,
                        upiReferenceNo = upiReferenceNo,
                        onUpiReferenceChange = { upiReferenceNo = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date Selection Field
                Text(
                    text = "Donation Date *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = dateFormat.format(Date(paymentDate)),
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Donation Date",
                            tint = SocietyTealPrimary
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { datePickerDialog.show() },
                            modifier = Modifier.testTag("donor_date_picker_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Pick Date",
                                tint = SocietyTealPrimary
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("donor_date_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { paymentDate = System.currentTimeMillis() },
                        label = { Text("Today", fontSize = 11.sp) },
                        modifier = Modifier.testTag("chip_dialog_today")
                    )
                    FilterChip(
                        selected = false,
                        onClick = { paymentDate = System.currentTimeMillis() - 86400000L },
                        label = { Text("Yesterday", fontSize = 11.sp) },
                        modifier = Modifier.testTag("chip_dialog_yesterday")
                    )
                    FilterChip(
                        selected = false,
                        onClick = { datePickerDialog.show() },
                        label = { Text("Change Date 📅", fontSize = 11.sp) },
                        modifier = Modifier.testTag("chip_dialog_custom")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes / Purpose / Flat No
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Purpose / Flat No / Reference (Optional)") },
                    placeholder = { Text("Enter notes or reference") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("donor_notes_input"),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Meta Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Date: ${dateFormat.format(Date(paymentDate))}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "By: ${currentMember?.name ?: "Admin"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (existingDonation != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = modificationReason,
                        onValueChange = {
                            modificationReason = it
                            if (reasonError != null) reasonError = null
                        },
                        label = { Text("Compulsory Reason for Modification *") },
                        placeholder = { Text("e.g. Corrected payment mode per bank receipt...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("modification_reason_input"),
                        minLines = 2,
                        maxLines = 3,
                        isError = reasonError != null,
                        supportingText = {
                            if (reasonError != null) {
                                Text(reasonError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("Required for Admin Audit Trail & Reports", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
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
                            var hasError = false
                            if (donorName.trim().isBlank()) {
                                donorNameError = "Donor full name is required"
                                hasError = true
                            }
                            val cleanPhone = phone.trim().filter { it.isDigit() }
                            if (cleanPhone.length != 10) {
                                phoneError = "Phone number must be exactly 10 digits"
                                hasError = true
                            }
                            val parsedAmount = amountText.trim().toLongOrNull()
                            if (parsedAmount == null || parsedAmount <= 0) {
                                amountError = "Amount must be a positive integer whole number (> ₹0)"
                                hasError = true
                            }
                            if (existingDonation != null && modificationReason.trim().length < 5) {
                                reasonError = "Compulsory modification reason (min 5 chars) is required"
                                hasError = true
                            }

                            if (!hasError && parsedAmount != null) {
                                val effectiveNotes = buildString {
                                    append(notes.trim())
                                    if (upiReferenceNo.isNotBlank() && !notes.contains(upiReferenceNo.trim())) {
                                        if (isNotEmpty()) append(" | ")
                                        append("UPI UTR: ").append(upiReferenceNo.trim())
                                    }
                                }.toString()
                                onSave(
                                    donorName.trim(),
                                    cleanPhone,
                                    parsedAmount.toDouble(),
                                    status,
                                    if (status == "received") paymentMode else "Pending",
                                    paymentDate,
                                    effectiveNotes,
                                    modificationReason.trim()
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_donation_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                    ) {
                        Text(
                            text = if (existingDonation != null) "Update & Audit Log" else "Save Donation",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
