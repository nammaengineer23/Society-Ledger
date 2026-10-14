package com.example.ui.components

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Expense
import com.example.data.model.Member
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseDialog(
    existingExpense: Expense? = null,
    currentMember: Member?,
    categoryBudgets: Map<String, Double> = emptyMap(),
    existingExpensesForCategory: Double = 0.0,
    onSave: (
        title: String,
        category: String,
        amount: Double,
        expenseDate: Long,
        paidTo: String,
        paymentMode: String,
        receiptUrl: String,
        modificationReason: String
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val standardCategories = listOf(
        "Events & Festivals",
        "Maintenance",
        "Repairs",
        "Utilities",
        "Refreshments",
        "Admin & Legal",
        "Miscellaneous"
    )

    val existingCat = existingExpense?.category ?: standardCategories[0]
    val isCustomInitial = existingCat.isNotBlank() && !standardCategories.contains(existingCat)

    var title by remember { mutableStateOf(existingExpense?.title ?: "") }
    var selectedCategoryOption by remember { mutableStateOf(if (isCustomInitial) "Other / Custom" else existingCat) }
    var customCategoryText by remember { mutableStateOf(if (isCustomInitial) existingCat else "") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var expenseReason by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(existingExpense?.amount?.toLong()?.toString() ?: "") }
    var paidTo by remember { mutableStateOf(existingExpense?.paidTo ?: "") }
    var paymentMode by remember { mutableStateOf(existingExpense?.paymentMode ?: "Online") }
    var receiptUrl by remember { mutableStateOf(existingExpense?.receiptUrl ?: "") }
    var modificationReason by remember { mutableStateOf("") }
    val context = LocalContext.current
    var expenseDate by remember { mutableStateOf(existingExpense?.expenseDate ?: System.currentTimeMillis()) }

    val resolvedCategory = if (selectedCategoryOption == "Other / Custom") {
        customCategoryText.trim()
    } else {
        selectedCategoryOption.trim()
    }

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

    var titleError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var paidToError by remember { mutableStateOf<String?>(null) }
    var reasonError by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            receiptUrl = it.toString()
        }
    }

    val currentBudget = categoryBudgets[resolvedCategory] ?: 0.0
    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val projectedSpent = existingExpensesForCategory + enteredAmount - (existingExpense?.amount ?: 0.0)
    val isOverBudget = currentBudget > 0 && projectedSpent > currentBudget

    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val modes = listOf("UPI", "Cash", "Bank Transfer", "Cheque")

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 20.dp)
                .testTag("add_edit_expense_dialog"),
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
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = ColorExpenseRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (existingExpense != null) "Edit Expense" else "Record New Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Expense Title
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError != null && it.isNotBlank()) {
                            titleError = null
                        }
                    },
                    label = { Text("Expense Purpose / Title *") },
                    placeholder = { Text("e.g. Festival Stage Lighting & Sound") },
                    leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                    isError = titleError != null,
                    supportingText = {
                        if (titleError != null) {
                            Text(titleError!!, color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategoryOption,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expense Category *") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("expense_category_selector")
                    )

                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        standardCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategoryOption = cat
                                    categoryDropdownExpanded = false
                                    categoryError = null
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Other / Custom Category...", fontWeight = FontWeight.SemiBold)
                                }
                            },
                            onClick = {
                                selectedCategoryOption = "Other / Custom"
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }

                // If Custom Category is selected, display free-text input
                if (selectedCategoryOption == "Other / Custom") {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customCategoryText,
                        onValueChange = {
                            customCategoryText = it
                            if (categoryError != null && it.isNotBlank()) {
                                categoryError = null
                            }
                        },
                        label = { Text("Specify Custom Category *") },
                        placeholder = { Text("e.g. Tree Plantation, Security Gear, Legal Notice...") },
                        leadingIcon = { Icon(Icons.Default.EditCalendar, contentDescription = null) },
                        isError = categoryError != null,
                        supportingText = {
                            if (categoryError != null) {
                                Text(categoryError!!, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_custom_category_input"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Amount (Positive Integer Validation)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        val digitsOnly = input.filter { it.isDigit() }
                        amountText = digitsOnly
                        if (amountError != null) {
                            val v = digitsOnly.toLongOrNull()
                            if (v != null && v > 0) {
                                amountError = null
                            }
                        }
                    },
                    label = { Text("Amount Spent (INR ₹ - Positive Integer) *") },
                    placeholder = { Text("12000") },
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
                        .testTag("expense_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Paid To (Vendor / Person)
                OutlinedTextField(
                    value = paidTo,
                    onValueChange = {
                        paidTo = it
                        if (paidToError != null && it.isNotBlank()) {
                            paidToError = null
                        }
                    },
                    label = { Text("Paid To (Vendor / Contractor / Person) *") },
                    placeholder = { Text("e.g. Balaji Electricals") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = paidToError != null,
                    supportingText = {
                        if (paidToError != null) {
                            Text(paidToError!!, color = Color.Red)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_vendor_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Expense Date Field
                OutlinedTextField(
                    value = dateFormat.format(Date(expenseDate)),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Expense Date *") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ColorExpenseRed) },
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.EditCalendar, contentDescription = "Pick Date", tint = ColorExpenseRed)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                        .testTag("expense_date_input")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = { expenseDate = System.currentTimeMillis() },
                        label = { Text("Today", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { expenseDate = System.currentTimeMillis() - 86400000L },
                        label = { Text("Yesterday", style = MaterialTheme.typography.labelSmall) }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { datePickerDialog.show() },
                        label = { Text("Pick Date 📅", style = MaterialTheme.typography.labelSmall) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment Mode Selection
                Text(
                    text = "Payment Method *",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modes.take(2).forEach { mode ->
                            val isSelected = paymentMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ColorExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { paymentMode = mode }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modes.drop(2).forEach { mode ->
                            val isSelected = paymentMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) ColorExpenseRed else MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { paymentMode = mode }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Budget Warning Alert
                if (isOverBudget) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Budget Warning: This expense will exceed the monthly ₹${String.format("%,.0f", currentBudget)} budget for '$resolvedCategory' (Projected: ₹${String.format("%,.0f", projectedSpent)}).",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bill / Receipt Reference No
                OutlinedTextField(
                    value = receiptUrl,
                    onValueChange = { receiptUrl = it },
                    label = { Text("Bill / Invoice Ref / Receipt Note") },
                    placeholder = { Text("e.g. INV-2026-891 or Cash voucher #12") },
                    leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_receipt_ref_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Vendor Bill / Receipt Photo Attachment
                Text(
                    text = "Attach Vendor Bill / Receipt Photo",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (receiptUrl.startsWith("content://") || receiptUrl.startsWith("file://") || receiptUrl.contains("/")) {
                    // Preview Photo
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AsyncImage(
                                model = receiptUrl,
                                contentDescription = "Receipt Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Change Photo", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                IconButton(
                                    onClick = { receiptUrl = "" },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Photo", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_attach_receipt_photo")
                    ) {
                        Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload / Capture Bill Photo")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reason / Notes (similar to Donations)
                OutlinedTextField(
                    value = expenseReason,
                    onValueChange = { expenseReason = it },
                    label = { Text("Reason / Notes / Justification") },
                    placeholder = { Text("e.g. Approved by secretary for annual general meeting refreshments...") },
                    leadingIcon = { Icon(Icons.Default.Comment, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_reason_input"),
                    minLines = 2,
                    maxLines = 3
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
                            text = "Date: ${dateFormat.format(Date(expenseDate))}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Approved by: ${currentMember?.name ?: "Admin"}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (existingExpense != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = modificationReason,
                        onValueChange = {
                            modificationReason = it
                            if (reasonError != null) reasonError = null
                        },
                        label = { Text("Compulsory Reason for Modification *") },
                        placeholder = { Text("e.g. Corrected invoice amount per audited vendor bill...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expense_modification_reason_input"),
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
                            if (title.trim().isBlank()) {
                                titleError = "Expense purpose / title is required"
                                hasError = true
                            }
                            if (resolvedCategory.isBlank()) {
                                categoryError = "Expense category is required"
                                hasError = true
                            }
                            if (paidTo.trim().isBlank()) {
                                paidToError = "Vendor or recipient name is required"
                                hasError = true
                            }
                            val parsedAmount = amountText.trim().toLongOrNull()
                            if (parsedAmount == null || parsedAmount <= 0) {
                                amountError = "Amount must be a positive integer whole number (> ₹0)"
                                hasError = true
                            }
                            if (existingExpense != null && modificationReason.trim().length < 5) {
                                reasonError = "Compulsory modification reason (min 5 chars) is required"
                                hasError = true
                            }

                            if (!hasError && parsedAmount != null) {
                                val effectiveModReason = if (existingExpense != null) {
                                    modificationReason.trim()
                                } else {
                                    expenseReason.trim()
                                }
                                onSave(
                                    title.trim(),
                                    resolvedCategory,
                                    parsedAmount.toDouble(),
                                    expenseDate,
                                    paidTo.trim(),
                                    paymentMode,
                                    receiptUrl.trim(),
                                    effectiveModReason
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("save_expense_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorExpenseRed)
                    ) {
                        Text(
                            text = if (existingExpense != null) "Update & Audit Log" else "Record Expense",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
