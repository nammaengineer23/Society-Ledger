package com.example.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.data.remote.FirestoreService
import com.example.ui.theme.ColorExpenseBg
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.ThemeToggleIconButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Jetpack Compose input form for adding new expense records.
 * Includes fields for description, category, amount, date (with interactive picker),
 * and vendor (payee/contractor), and directly saves to the Firestore 'expenses'
 * collection under the society.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    societyName: String = "",
    categoryBudgets: Map<String, Double> = emptyMap(),
    existingExpensesForCategory: Double = 0.0,
    isDarkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onExpenseSaved: (Expense) -> Unit = {},
    committeeMembers: List<Member> = emptyList(),
    firestoreService: FirestoreService = remember { FirestoreService() },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val resolvedSociety = societyName
    val dateFormat = remember { SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()) }

    val categories = listOf(
        "Events & Festivals",
        "Maintenance",
        "Repairs",
        "Utilities",
        "Refreshments",
        "Admin & Legal",
        "Miscellaneous"
    )

    // Form Field States
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories[1]) } // Default to Maintenance
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf("") }
    var expenseDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var vendor by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("UPI") } // UPI, Cash, Bank Transfer, Cheque
    var receiptRef by remember { mutableStateOf("") }
    var paidByCommitteeMemberId by remember { mutableStateOf(0L) }
    var payerDropdownExpanded by remember { mutableStateOf(false) }
    var receiptImageUri by remember { mutableStateOf<String?>(null) }

    // Validation & Loading States
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var vendorError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var saveSuccessMessage by remember { mutableStateOf<String?>(null) }
    var savedDocId by remember { mutableStateOf<String?>(null) }

    // Zero-permission Android Photo Picker for receipt photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            receiptImageUri = it.toString()
        }
    }

    // Date Picker Dialog setup
    val calendar = remember(expenseDate) {
        Calendar.getInstance().apply { timeInMillis = expenseDate }
    }
    val datePickerDialog = remember(expenseDate, context) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val chosenCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                expenseDate = chosenCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Budget Calculations
    val currentBudget = categoryBudgets[category] ?: 0.0
    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val projectedSpent = existingExpensesForCategory + enteredAmount
    val isOverBudget = currentBudget > 0 && projectedSpent > currentBudget

    data class PaymentMethodOption(val id: String, val title: String, val icon: ImageVector)
    val paymentMethods = listOf(
        PaymentMethodOption("UPI", "UPI / QR", Icons.Default.QrCode),
        PaymentMethodOption("Cash", "Cash", Icons.Default.Money),
        PaymentMethodOption("Bank Transfer", "NetBanking", Icons.Default.AccountBalance),
        PaymentMethodOption("Cheque", "Cheque", Icons.Default.Receipt)
    )

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("expense_screen_container"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "New Expense Entry",
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
                        modifier = Modifier.testTag("expense_screen_back_button")
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
                        testTag = "expense_screen_theme_toggle"
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
                    // Header Banner: Target Firestore Collection Indicator
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
                                            listOf(ColorExpenseRed, Color(0xFFE53935))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = "Expense Form",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Record Society Expenditure",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = if (resolvedSociety.isNotBlank()) "Society: $resolvedSociety" else "Official Society Expenditure Log",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = ColorExpenseRed,
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
                            // 1. Description Field
                            Text(
                                text = "Description / Purpose *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = {
                                    description = it
                                    if (descriptionError != null && it.isNotBlank()) {
                                        descriptionError = null
                                    }
                                },
                                placeholder = { Text("Enter expense title") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = "Description",
                                        tint = ColorExpenseRed
                                    )
                                },
                                isError = descriptionError != null,
                                supportingText = {
                                    if (descriptionError != null) {
                                        Text(descriptionError!!, color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_expense_description"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorExpenseRed,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Category Field (Dropdown)
                            Text(
                                text = "Category *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ExposedDropdownMenuBox(
                                expanded = categoryDropdownExpanded,
                                onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = category,
                                    onValueChange = {},
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = "Category",
                                            tint = ColorExpenseRed
                                        )
                                    },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("input_expense_category"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ColorExpenseRed,
                                        unfocusedBorderColor = Slate200
                                    )
                                )

                                ExposedDropdownMenu(
                                    expanded = categoryDropdownExpanded,
                                    onDismissRequest = { categoryDropdownExpanded = false }
                                ) {
                                    categories.forEach { cat ->
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(cat, fontWeight = if (cat == category) FontWeight.Bold else FontWeight.Normal)
                                                    if (cat == category) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = null,
                                                            tint = ColorExpenseRed,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                category = cat
                                                categoryDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 3. Amount Field
                            Text(
                                text = "Amount Spent (INR ₹) *",
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
                                        tint = ColorExpenseRed
                                    )
                                },
                                isError = amountError != null,
                                supportingText = {
                                    if (amountError != null) {
                                        Text(amountError!!, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Enter total expense amount (must be positive number > ₹0)")
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_expense_amount"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorExpenseRed,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            // Budget Warning Alert (if applicable)
                            if (isOverBudget) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = Color(0xFFE65100),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Budget Alert: Exceeds monthly budget ₹${String.format("%,.0f", currentBudget)} for '$category' (Projected: ₹${String.format("%,.0f", projectedSpent)}).",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFE65100)
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 4. Date Field (with Interactive Picker & Quick Chips)
                            Text(
                                text = "Expense Date *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = dateFormat.format(Date(expenseDate)),
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Expense Date",
                                        tint = ColorExpenseRed
                                    )
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { datePickerDialog.show() },
                                        modifier = Modifier.testTag("btn_pick_expense_date")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EditCalendar,
                                            contentDescription = "Select Date from Calendar",
                                            tint = ColorExpenseRed
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { datePickerDialog.show() }
                                    .testTag("input_expense_date"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorExpenseRed,
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
                                    onClick = { expenseDate = System.currentTimeMillis() },
                                    label = { Text("Today", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Slate100),
                                    modifier = Modifier.testTag("chip_expense_date_today")
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { expenseDate = System.currentTimeMillis() - 86400000L },
                                    label = { Text("Yesterday", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Slate100),
                                    modifier = Modifier.testTag("chip_expense_date_yesterday")
                                )
                                FilterChip(
                                    selected = false,
                                    onClick = { datePickerDialog.show() },
                                    label = { Text("Choose Date 📅", style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Slate100),
                                    modifier = Modifier.testTag("chip_expense_date_custom")
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 5. Vendor / Payee Field
                            Text(
                                text = "Vendor / Payee Name *",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = vendor,
                                onValueChange = {
                                    vendor = it
                                    if (vendorError != null && it.isNotBlank()) {
                                        vendorError = null
                                    }
                                },
                                placeholder = { Text("Enter vendor or payee name") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Vendor Name",
                                        tint = ColorExpenseRed
                                    )
                                },
                                isError = vendorError != null,
                                supportingText = {
                                    if (vendorError != null) {
                                        Text(vendorError!!, color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Contractor, shopkeeper, or technician paid")
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_expense_vendor"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorExpenseRed,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Paid from",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Choose a committee member only if they paid from personal funds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            ExposedDropdownMenuBox(
                                expanded = payerDropdownExpanded,
                                onExpandedChange = { payerDropdownExpanded = it }
                            ) {
                                val selectedPayer = committeeMembers.firstOrNull { it.id == paidByCommitteeMemberId }
                                OutlinedTextField(
                                    value = selectedPayer?.let { "${it.name} — ${it.role}" } ?: "Trust funds",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Funding source") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = payerDropdownExpanded) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = payerDropdownExpanded,
                                    onDismissRequest = { payerDropdownExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Trust funds") },
                                        onClick = { paidByCommitteeMemberId = 0L; payerDropdownExpanded = false }
                                    )
                                    committeeMembers.filter { it.role != "Resident Member" && it.role != "Donor" }.forEach { committeeMember ->
                                        DropdownMenuItem(
                                            text = { Text("${committeeMember.name} — ${committeeMember.role}") },
                                            onClick = { paidByCommitteeMemberId = committeeMember.id; payerDropdownExpanded = false }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            // 6. Payment Method Selector
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
                                    .testTag("expense_payment_method_selector"),
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
                                                    if (isSelected) ColorExpenseRed else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { paymentMode = method.id }
                                                .padding(vertical = 12.dp, horizontal = 10.dp)
                                                .testTag("expense_payment_mode_${method.id}"),
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
                                                    if (isSelected) ColorExpenseRed else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { paymentMode = method.id }
                                                .padding(vertical = 12.dp, horizontal = 10.dp)
                                                .testTag("expense_payment_mode_${method.id}"),
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

                            Spacer(modifier = Modifier.height(14.dp))

                            // 7. Optional Bill / Receipt Reference No & Photo
                            Text(
                                text = "Bill / Invoice Ref (Optional)",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = receiptRef,
                                onValueChange = { receiptRef = it },
                                placeholder = { Text("Enter bill or voucher reference") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Receipt,
                                        contentDescription = "Bill Ref",
                                        tint = Slate300
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_expense_receipt_ref"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ColorExpenseRed,
                                    unfocusedBorderColor = Slate200
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Optional Receipt Photo Upload
                            if (receiptImageUri != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, Slate200, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = receiptImageUri,
                                        contentDescription = "Receipt Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { receiptImageUri = null },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                            .size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove receipt",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_upload_expense_bill"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorExpenseRed)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Attach Bill / Invoice Photo (Optional)")
                                }
                            }

                            // Success Message Banner
                            AnimatedVisibility(visible = saveSuccessMessage != null) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFE8F5E9)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFF81C784))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Success",
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = saveSuccessMessage.orEmpty(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    color = Color(0xFF2E7D32),
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

                                    if (description.trim().isBlank()) {
                                        descriptionError = "Expense description is required"
                                        hasError = true
                                    }
                                    if (vendor.trim().isBlank()) {
                                        vendorError = "Vendor or payee name is required"
                                        hasError = true
                                    }
                                    if (parsedAmount == null || parsedAmount <= 0.0) {
                                        amountError = "Please enter a valid amount greater than 0"
                                        hasError = true
                                    }

                                    if (!hasError && parsedAmount != null) {
                                        isSaving = true
                                        saveSuccessMessage = null
                                        coroutineScope.launch {
                                            val finalReceipt = if (!receiptImageUri.isNullOrBlank()) {
                                                receiptImageUri.orEmpty()
                                            } else {
                                                receiptRef.trim()
                                            }

                                            val newExpense = Expense(
                                                title = description.trim(),
                                                category = category,
                                                amount = parsedAmount,
                                                expenseDate = expenseDate,
                                                paidTo = vendor.trim(),
                                                paymentMode = paymentMode,
                                                receiptUrl = finalReceipt,
                                                paidByCommitteeMemberId = paidByCommitteeMemberId,
                                                recordedById = 1,
                                                recordedByName = "Admin Committee",
                                                societyId = resolvedSociety
                                            )

                                            // Save directly to Firestore 'expenses' collection under the society
                                            val docId = firestoreService.saveExpense(
                                                expense = newExpense,
                                                fallbackUserId = "admin_1",
                                                fallbackUserEmail = "committee@society.org",
                                                societyName = resolvedSociety
                                            )
                                            isSaving = false
                                            savedDocId = docId

                                            val finalExpense = if (docId != null) {
                                                newExpense.copy(firestoreId = docId)
                                            } else {
                                                newExpense
                                            }

                                            if (docId != null) {
                                                saveSuccessMessage = "Expense saved successfully under $resolvedSociety"
                                                snackbarHostState.showSnackbar("Expense recorded successfully")
                                            } else {
                                                saveSuccessMessage = "Expense recorded successfully (offline mode active)"
                                                snackbarHostState.showSnackbar("Expense recorded successfully")
                                            }
                                            onExpenseSaved(finalExpense)
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_expense_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ColorExpenseRed
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
                                            text = "Save Expense",
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
