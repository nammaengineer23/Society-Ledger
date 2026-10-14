package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Donation
import com.example.data.remote.FirestoreService
import com.example.ui.theme.ColorPendingOrange
import com.example.ui.theme.ColorPendingOrangeDark
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.ColorReceivedGreenDark
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.SocietyTealDark
import com.example.ui.theme.SocietyTealPrimary
import com.example.ui.theme.ThemeToggleIconButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Jetpack Compose input form for adding new donation records.
 * Includes fields for donor name, donation amount, date (with calendar picker),
 * and payment method (UPI, Cash, Bank Transfer, Cheque).
 * Directly saves data to the Firestore 'donations' collection under the society.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    societyName: String = "",
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onDonationSaved: (Donation) -> Unit = {},
    firestoreService: FirestoreService = remember { FirestoreService() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val resolvedSociety = societyName
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    // Form Field States
    var donorName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var paymentMode by remember { mutableStateOf("UPI/Online") } // "UPI/Online", "Cash", "Bank Transfer", "Cheque"
    var status by remember { mutableStateOf("received") } // "received" or "pending"
    var notes by remember { mutableStateOf("") }

    // Validation & Loading States
    var donorNameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var savedDocId by remember { mutableStateOf<String?>(null) }

    // Date Picker Dialog setup
    val calendar = remember(paymentDate) {
        Calendar.getInstance().apply { timeInMillis = paymentDate }
    }
    val datePickerDialog = remember(paymentDate, context) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                paymentDate = chosenCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    data class PaymentMethodOption(val id: String, val title: String, val icon: ImageVector)
    val paymentMethods = listOf(
        PaymentMethodOption("UPI/Online", "UPI / QR", Icons.Default.QrCode),
        PaymentMethodOption("Cash", "Cash", Icons.Default.Money),
        PaymentMethodOption("Bank Transfer", "NetBanking", Icons.Default.AccountBalance),
        PaymentMethodOption("Cheque", "Cheque", Icons.Default.Receipt)
    )

    val statuses = listOf(
        "received" to "Received (Paid)",
        "pending" to "Pledged (Pending)"
    )

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("donation_screen_container"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Donation Entry",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Society: $resolvedSociety",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("donation_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    ThemeToggleIconButton(
                        isDark = isDarkTheme,
                        onToggle = onToggleTheme,
                        modifier = Modifier.padding(end = 6.dp),
                        testTag = "donation_screen_theme_toggle"
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 640.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header Banner: Firestore Destination Indicator
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(SocietyTealDark, SocietyTealPrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = "Donation Form",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Record Society Donation",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (resolvedSociety.isNotBlank()) "Society: $resolvedSociety" else "Official Society Donation Receipt",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = SocietyTealPrimary,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }

                    // Main Form Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Slate200),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // 1. Donor Name Field
                            Text(
                                text = "Donor Name *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = donorName,
                                onValueChange = {
                                    donorName = it
                                    if (donorNameError != null && it.isNotBlank()) {
                                        donorNameError = null
                                    }
                                },
                                placeholder = { Text("Enter donor name or flat number") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Donor Name",
                                        tint = SocietyTealPrimary
                                    )
                                },
                                isError = donorNameError != null,
                                supportingText = {
                                    if (donorNameError != null) {
                                        Text(donorNameError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_donor_name"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SocietyTealPrimary,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Donation Amount Field
                            Text(
                                text = "Donation Amount (INR ₹) *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = amountText,
                                onValueChange = { input ->
                                    val digitsOnly = input.filter { it.isDigit() || it == '.' }
                                    amountText = digitsOnly
                                    if (amountError != null) {
                                        val v = digitsOnly.toDoubleOrNull()
                                        if (v != null && v > 0.0) {
                                            amountError = null
                                        }
                                    }
                                },
                                placeholder = { Text("Enter amount") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyRupee,
                                        contentDescription = "Amount",
                                        tint = ColorReceivedGreen
                                    )
                                },
                                isError = amountError != null,
                                supportingText = {
                                    if (amountError != null) {
                                        Text(amountError!!, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Enter contribution amount (must be positive number > ₹0)")
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_donation_amount"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SocietyTealPrimary,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Donation Date Field (with Interactive Picker & Quick Chips)
                            Text(
                                text = "Donation Date *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
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
                                        modifier = Modifier.testTag("btn_pick_date")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Select Date from Calendar",
                                            tint = SocietyTealPrimary
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                                    .testTag("input_donation_date"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SocietyTealPrimary,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            // Quick Date Selector Chips
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = false,
                                    onClick = { paymentDate = System.currentTimeMillis() },
                                    label = { Text("Today", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Slate100),
                                    modifier = Modifier.testTag("chip_date_today")
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { paymentDate = System.currentTimeMillis() - 86400000L },
                                    label = { Text("Yesterday", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Slate100),
                                    modifier = Modifier.testTag("chip_date_yesterday")
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { datePickerDialog.show() },
                                    label = { Text("Choose Date 📅", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Slate100),
                                    modifier = Modifier.testTag("chip_date_custom")
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 4. Payment Method Selector (UPI/Online, Cash, Bank Transfer, Cheque)
                            Text(
                                text = "Payment Method *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("payment_method_selector"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val topPair = paymentMethods.take(2)
                                val bottomPair = paymentMethods.drop(2)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    topPair.forEach { method ->
                                        val isSelected = paymentMode == method.id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) SocietyTealPrimary else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { paymentMode = method.id }
                                                .padding(vertical = 12.dp, horizontal = 10.dp)
                                                .testTag("payment_mode_${method.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = method.icon,
                                                    contentDescription = method.title,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = method.title,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    bottomPair.forEach { method ->
                                        val isSelected = paymentMode == method.id
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isSelected) SocietyTealPrimary else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { paymentMode = method.id }
                                                .padding(vertical = 12.dp, horizontal = 10.dp)
                                                .testTag("payment_mode_${method.id}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.Center
                                            ) {
                                                Icon(
                                                    imageVector = method.icon,
                                                    contentDescription = method.title,
                                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = method.title,
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 5. Payment Status Selector (Received / Pending)
                            Text(
                                text = "Donation Status",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                statuses.forEach { (key, label) ->
                                    val isSelected = status == key
                                    val activeColor = if (key == "received") (if (isDarkTheme) ColorReceivedGreenDark else ColorReceivedGreen) else (if (isDarkTheme) ColorPendingOrangeDark else ColorPendingOrange)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { status = key }
                                            .padding(vertical = 10.dp, horizontal = 8.dp)
                                            .testTag("status_option_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 6. Optional Phone Number Field
                            Text(
                                text = "Donor Phone Number (Optional)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { input ->
                                    val digitsOnly = input.filter { it.isDigit() }
                                    if (digitsOnly.length <= 10) {
                                        phone = digitsOnly
                                        if (phoneError != null) phoneError = null
                                    }
                                },
                                placeholder = { Text("10-digit mobile number for WhatsApp receipt") },
                                prefix = { if (phone.isNotEmpty()) Text("+91 ") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = "Phone Number",
                                        tint = SocietyTealPrimary
                                    )
                                },
                                isError = phoneError != null,
                                supportingText = {
                                    if (phoneError != null) {
                                        Text(phoneError!!, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Used for instant PDF receipt and WhatsApp acknowledgment")
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_phone_number"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SocietyTealPrimary,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 7. Notes / Purpose Field
                            Text(
                                text = "Purpose / Notes (Optional)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                placeholder = { Text("Enter notes or contribution details") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Notes,
                                        contentDescription = "Notes",
                                        tint = Slate300
                                    )
                                },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_donation_notes"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SocietyTealPrimary,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            // Success Message Banner
                            AnimatedVisibility(visible = saveSuccessMessage != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = ColorReceivedGreen.copy(alpha = 0.12f)
                                    ),
                                    border = BorderStroke(1.dp, ColorReceivedGreen.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = ColorReceivedGreen,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = saveSuccessMessage.orEmpty(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = ColorReceivedGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                            if (savedDocId != null) {
                                                Text(
                                                    text = "Document ID: $savedDocId",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = Slate600,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Submit to Firestore Button
                            Button(
                                onClick = {
                                    val parsedAmount = amountText.toDoubleOrNull()
                                    var hasError = false

                                    if (donorName.trim().isBlank()) {
                                        donorNameError = "Donor name is required"
                                        hasError = true
                                    }
                                    if (parsedAmount == null || parsedAmount <= 0.0) {
                                        amountError = "Please enter a valid amount greater than 0"
                                        hasError = true
                                    }
                                    val cleanPhone = phone.trim().filter { it.isDigit() }
                                    if (cleanPhone.isNotEmpty() && cleanPhone.length != 10) {
                                        phoneError = "Phone number must be exactly 10 digits"
                                        hasError = true
                                    }

                                    if (!hasError && parsedAmount != null) {
                                        isSaving = true
                                        saveSuccessMessage = null
                                        coroutineScope.launch {
                                            val year = Calendar.getInstance().get(Calendar.YEAR)
                                            val randomSuffix = (100..999).random()
                                            val prefix = if (status == "received") "RCP" else "PND"
                                            val generatedReceipt = "$prefix-$year-$randomSuffix"

                                            val newDonation = Donation(
                                                donorName = donorName.trim(),
                                                phone = cleanPhone,
                                                amount = parsedAmount,
                                                status = status,
                                                paymentMode = if (status == "received") paymentMode else "Pending",
                                                paymentDate = paymentDate,
                                                receiptNo = generatedReceipt,
                                                notes = notes.trim(),
                                                recordedById = 1,
                                                recordedByName = "Admin Committee",
                                                societyId = resolvedSociety
                                            )

                                            // Save directly to Firestore 'donations' collection under the society
                                            val docId = firestoreService.saveDonation(
                                                donation = newDonation,
                                                societyName = resolvedSociety
                                            )
                                            isSaving = false
                                            savedDocId = docId

                                            val finalDonation = if (docId != null) {
                                                newDonation.copy(firestoreId = docId)
                                            } else {
                                                newDonation
                                            }

                                            if (docId != null) {
                                                saveSuccessMessage = "Donation recorded successfully under $resolvedSociety"
                                                snackbarHostState.showSnackbar("Donation recorded successfully")
                                            } else {
                                                saveSuccessMessage = "Donation recorded successfully (offline mode active)"
                                                snackbarHostState.showSnackbar("Donation recorded successfully")
                                            }
                                            onDonationSaved(finalDonation)
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_donation_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SocietyTealPrimary
                                )
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = Color.White,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Save Donation",
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
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
