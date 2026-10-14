package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.Donation
import com.example.data.model.Expense
import com.example.data.model.SocietyInfo
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary
import java.util.Calendar

data class BulkDonationRow(
    var donorName: String = "",
    var phone: String = "",
    var amountText: String = "",
    var status: String = "received", // "received", "pending"
    var paymentMode: String = "UPI/Online",
    var notes: String = ""
)

data class BulkExpenseRow(
    var title: String = "",
    var category: String = "Stage & Sound Setup",
    var amountText: String = "",
    var paidTo: String = "",
    var paymentMode: String = "Online"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasterBulkDataEntryDialog(
    initialSocietyInfo: SocietyInfo,
    onDismiss: () -> Unit,
    onSaveAll: (
        updatedSocietyInfo: SocietyInfo,
        donations: List<Donation>,
        expenses: List<Expense>,
        replaceExisting: Boolean
    ) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Society & Bank Info State
    var societyName by remember { mutableStateOf(initialSocietyInfo.name) }
    var registrationNo by remember { mutableStateOf(initialSocietyInfo.registrationNo) }
    var bankName by remember { mutableStateOf(initialSocietyInfo.bankName) }
    var accountNumber by remember { mutableStateOf(initialSocietyInfo.accountNumber) }
    var ifscCode by remember { mutableStateOf(initialSocietyInfo.ifscCode) }
    var branchName by remember { mutableStateOf(initialSocietyInfo.branchName) }
    var upiId by remember { mutableStateOf(initialSocietyInfo.upiId) }
    var address by remember { mutableStateOf(initialSocietyInfo.address) }
    var city by remember { mutableStateOf(initialSocietyInfo.city) }
    var state by remember { mutableStateOf(initialSocietyInfo.state) }
    var pinCode by remember { mutableStateOf(initialSocietyInfo.pinCode) }
    var contactPhone by remember { mutableStateOf(initialSocietyInfo.contactPhone.filter { it.isDigit() }) }
    var contactEmail by remember { mutableStateOf(initialSocietyInfo.contactEmail) }
    var financialYear by remember { mutableStateOf(initialSocietyInfo.financialYear) }
    var openingBalanceText by remember { mutableStateOf(initialSocietyInfo.openingBalance.toLong().toString()) }
    var presidentName by remember { mutableStateOf(initialSocietyInfo.presidentName) }
    var secretaryName by remember { mutableStateOf(initialSocietyInfo.secretaryName) }
    var treasurerName by remember { mutableStateOf(initialSocietyInfo.treasurerName) }

    // Bulk Rows State
    val donationRows = remember {
        mutableStateListOf(
            BulkDonationRow("Ramesh Sharma (Flat 101)", "9810011223", "5000", "received", "UPI/Online", "Annual Festival"),
            BulkDonationRow("Pooja Verma (Flat 204)", "9820033445", "3000", "received", "Cash", "Society Maintenance"),
            BulkDonationRow("Amit Khurana (Flat 302)", "9830055667", "5000", "pending", "UPI/Online", "Temple Pledged")
        )
    }

    val expenseRows = remember {
        mutableStateListOf(
            BulkExpenseRow("Grand Festival Stage & Audio", "Stage & Sound Setup", "18000", "Balaji Sound & Light", "Online"),
            BulkExpenseRow("Temple Decoration & Floral", "Decoration & Flowers", "8000", "Vrindavan Florists", "Cash")
        )
    }

    var replaceExistingData by remember { mutableStateOf(true) }
    var generalError by remember { mutableStateOf<String?>(null) }

    val expenseCategories = listOf(
        "Stage & Sound Setup",
        "Electricity, Generator & Lights",
        "Catering & Mahaprasad",
        "Decoration & Flowers",
        "Panditji & Poojan Samagri",
        "Security, Guard & Cleaning",
        "Printing & Mementos",
        "General Society Fund"
    )

    val paymentModes = listOf("UPI/Online", "Cash", "Bank Transfer", "Cheque")

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .padding(vertical = 12.dp)
                .testTag("master_bulk_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Dialog Title & Quick Stats
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
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Admin Master Setup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure society, bank details & present values at once",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Navigation Tabs for Master Entry
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = SapphireNavy,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "1. Society & Bank",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        icon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("master_setup_tab_society")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "2. Donors (${donationRows.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("master_setup_tab_donations")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Text(
                                "3. Expenses (${expenseRows.size})",
                                fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("master_setup_tab_expenses")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Content Body (Scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // TAB 1: Society Info & Bank Details
                            Text(
                                text = "OFFICIAL SOCIETY & BANK INFORMATION",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SapphireNavy
                            )

                            OutlinedTextField(
                                value = societyName,
                                onValueChange = { societyName = it },
                                label = { Text("Society / RWA Name *") },
                                placeholder = { Text("Enter society name") },
                                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = registrationNo,
                                    onValueChange = { registrationNo = it },
                                    label = { Text("Registration No.") },
                                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = financialYear,
                                    onValueChange = { financialYear = it },
                                    label = { Text("Financial Year") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Bank Details Card (Optional)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Official Society Bank Account & UPI (Optional)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SocietyTealPrimary
                                    )

                                    OutlinedTextField(
                                        value = bankName,
                                        onValueChange = { bankName = it },
                                        label = { Text("Bank Name (Optional)") },
                                        placeholder = { Text("e.g. State Bank of India / HDFC Bank") },
                                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = accountNumber,
                                            onValueChange = { accountNumber = it.filter { c -> c.isLetterOrDigit() } },
                                            label = { Text("Account Number (Optional)") },
                                            leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) },
                                            modifier = Modifier.weight(1.2f)
                                        )
                                        OutlinedTextField(
                                            value = ifscCode,
                                            onValueChange = { ifscCode = it.uppercase().trim() },
                                            label = { Text("IFSC Code (Optional)") },
                                            modifier = Modifier.weight(0.8f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = branchName,
                                        onValueChange = { branchName = it },
                                        label = { Text("Branch Name & City (Optional)") },
                                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = upiId,
                                        onValueChange = { upiId = it.trim() },
                                        label = { Text("Official Society UPI ID (Optional)") },
                                        placeholder = { Text("society@upi") },
                                        leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }

                            // Present Opening Balance
                            OutlinedTextField(
                                value = openingBalanceText,
                                onValueChange = { openingBalanceText = it.filter { c -> c.isDigit() } },
                                label = { Text("Present Opening Bank Balance / Surplus (INR ₹) *") },
                                placeholder = { Text("0") },
                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                supportingText = { Text("Starting cash & bank surplus at the beginning of the year") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Contact info
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = contactPhone,
                                    onValueChange = {
                                        val digits = it.filter { c -> c.isDigit() }
                                        if (digits.length <= 10) contactPhone = digits
                                    },
                                    label = { Text("Helpline Phone") },
                                    prefix = { Text("+91 ") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Society Address / Locality") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            AutoFetchLocationCard(
                                onLocationFetched = { details ->
                                    if (details.city.isNotBlank()) city = details.city
                                    if (details.state.isNotBlank()) state = details.state
                                    if (details.address.isNotBlank()) address = details.address
                                    if (details.pinCode.isNotBlank()) pinCode = details.pinCode
                                },
                                testTagPrefix = "bulk_society_location"
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
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = state,
                                    onValueChange = { state = it },
                                    label = { Text("State") },
                                    placeholder = { Text("Enter state") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = pinCode,
                                    onValueChange = { input ->
                                        if (input.length <= 6 && input.all { it.isDigit() }) pinCode = input
                                    },
                                    label = { Text("PIN Code") },
                                    placeholder = { Text("560001") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f)
                                )
                            }
                        }

                        1 -> {
                            // TAB 2: Batch Add Present Donors & Contributions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PRESENT DONORS & CONTRIBUTIONS LIST",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SapphireNavy
                                )
                                Button(
                                    onClick = {
                                        donationRows.add(
                                            BulkDonationRow(
                                                donorName = "",
                                                phone = "",
                                                amountText = "5000",
                                                status = "received",
                                                paymentMode = "UPI/Online",
                                                notes = ""
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("add_bulk_donation_row_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Donor Row", fontSize = 12.sp)
                                }
                            }

                            if (donationRows.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No donor rows added. Click 'Add Donor Row' above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            donationRows.forEachIndexed { index, row ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Donor #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SapphireNavy)
                                            IconButton(
                                                onClick = { donationRows.removeAt(index) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = row.donorName,
                                                onValueChange = {
                                                    donationRows[index] = row.copy(donorName = it)
                                                },
                                                label = { Text("Full Name *") },
                                                placeholder = { Text("Ramesh Kumar") },
                                                modifier = Modifier.weight(1.2f)
                                            )
                                            OutlinedTextField(
                                                value = row.phone,
                                                onValueChange = { input ->
                                                    val digits = input.filter { it.isDigit() }
                                                    if (digits.length <= 10) {
                                                        donationRows[index] = row.copy(phone = digits)
                                                    }
                                                },
                                                label = { Text("10-Digit Phone *") },
                                                placeholder = { Text("9810011223") },
                                                prefix = { Text("+91 ") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = row.amountText,
                                                onValueChange = { input ->
                                                    donationRows[index] = row.copy(amountText = input.filter { it.isDigit() })
                                                },
                                                label = { Text("Amount (₹) *") },
                                                placeholder = { Text("5000") },
                                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f)
                                            )

                                            // Status Toggle (Received vs Pending)
                                            Row(
                                                modifier = Modifier
                                                    .weight(1.2f)
                                                    .align(Alignment.CenterVertically),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(
                                                            if (row.status == "received") Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .clickable {
                                                            donationRows[index] = row.copy(status = "received")
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "Received",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (row.status == "received") Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .background(
                                                            if (row.status == "pending") Color(0xFFE65100) else MaterialTheme.colorScheme.surfaceVariant,
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .clickable {
                                                            donationRows[index] = row.copy(status = "pending")
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        "Pending",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (row.status == "pending") Color.White else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = row.notes,
                                            onValueChange = {
                                                donationRows[index] = row.copy(notes = it)
                                            },
                                            label = { Text("Remarks / Purpose") },
                                            placeholder = { Text("Enter remarks or purpose") },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 3: Batch Add Present Expenses
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "PRESENT SOCIETY EXPENSES LIST",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SapphireNavy
                                )
                                Button(
                                    onClick = {
                                        expenseRows.add(
                                            BulkExpenseRow(
                                                title = "",
                                                category = expenseCategories[0],
                                                amountText = "5000",
                                                paidTo = "",
                                                paymentMode = "Online"
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("add_bulk_expense_row_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Expense Row", fontSize = 12.sp)
                                }
                            }

                            if (expenseRows.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No expense rows added. Click 'Add Expense Row' above.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            expenseRows.forEachIndexed { index, row ->
                                var categoryDropdownExpanded by remember { mutableStateOf(false) }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Expense #${index + 1}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SapphireNavy)
                                            IconButton(
                                                onClick = { expenseRows.removeAt(index) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        OutlinedTextField(
                                            value = row.title,
                                            onValueChange = {
                                                expenseRows[index] = row.copy(title = it)
                                            },
                                            label = { Text("Expense Purpose / Title *") },
                                            placeholder = { Text("Enter expense title") },
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        ExposedDropdownMenuBox(
                                            expanded = categoryDropdownExpanded,
                                            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            OutlinedTextField(
                                                value = row.category,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Expense Category") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillMaxWidth()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = categoryDropdownExpanded,
                                                onDismissRequest = { categoryDropdownExpanded = false }
                                            ) {
                                                expenseCategories.forEach { cat ->
                                                    DropdownMenuItem(
                                                        text = { Text(cat) },
                                                        onClick = {
                                                            expenseRows[index] = row.copy(category = cat)
                                                            categoryDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedTextField(
                                                value = row.amountText,
                                                onValueChange = { input ->
                                                    expenseRows[index] = row.copy(amountText = input.filter { it.isDigit() })
                                                },
                                                label = { Text("Amount (₹) *") },
                                                placeholder = { Text("12000") },
                                                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.weight(1f)
                                            )

                                            OutlinedTextField(
                                                value = row.paidTo,
                                                onValueChange = {
                                                    expenseRows[index] = row.copy(paidTo = it)
                                                },
                                                label = { Text("Paid To / Vendor *") },
                                                placeholder = { Text("Balaji Electricals") },
                                                modifier = Modifier.weight(1.2f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (generalError != null) {
                        Text(generalError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Replace vs Append Checkbox
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable { replaceExistingData = !replaceExistingData },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = replaceExistingData,
                                onCheckedChange = { replaceExistingData = it },
                                colors = CheckboxDefaults.colors(checkedColor = SapphireNavy)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Start Fresh with this Present Dataset",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Replaces old sample transactions and populates this new master data",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (societyName.trim().isBlank()) {
                                generalError = "Society name cannot be blank"
                                selectedTab = 0
                                return@Button
                            }
                            val parsedOpening = openingBalanceText.trim().toLongOrNull() ?: 0L

                            // Validate donations
                            val validDonations = mutableListOf<Donation>()
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            var receiptCounter = 101

                            for (r in donationRows) {
                                if (r.donorName.trim().isNotBlank() && r.amountText.trim().isNotBlank()) {
                                    val amt = r.amountText.trim().toLongOrNull() ?: 0L
                                    val cleanPhone = r.phone.trim().filter { it.isDigit() }
                                    if (amt > 0) {
                                        val status = if (r.status == "pending") "pending" else "received"
                                        validDonations.add(
                                            Donation(
                                                donorName = r.donorName.trim(),
                                                phone = cleanPhone.ifBlank { "9876543210" },
                                                amount = amt.toDouble(),
                                                status = status,
                                                paymentMode = if (status == "received") r.paymentMode else "Pending",
                                                paymentDate = System.currentTimeMillis(),
                                                receiptNo = if (status == "received") "RCP-$currentYear-${receiptCounter++}" else "",
                                                notes = r.notes.trim(),
                                                recordedById = 1,
                                                recordedByName = "Admin"
                                            )
                                        )
                                    }
                                }
                            }

                            // Validate expenses
                            val validExpenses = mutableListOf<Expense>()
                            for (e in expenseRows) {
                                if (e.title.trim().isNotBlank() && e.amountText.trim().isNotBlank()) {
                                    val amt = e.amountText.trim().toLongOrNull() ?: 0L
                                    if (amt > 0) {
                                        validExpenses.add(
                                            Expense(
                                                title = e.title.trim(),
                                                category = e.category,
                                                amount = amt.toDouble(),
                                                expenseDate = System.currentTimeMillis(),
                                                paidTo = e.paidTo.trim().ifBlank { "General Vendor" },
                                                paymentMode = e.paymentMode,
                                                recordedById = 1,
                                                recordedByName = "Admin"
                                            )
                                        )
                                    }
                                }
                            }

                            val fullBank = if (bankName.isNotBlank() && accountNumber.isNotBlank()) {
                                "$bankName A/C: $accountNumber (IFSC: $ifscCode${if (branchName.isNotBlank()) ", $branchName" else ""})"
                            } else {
                                initialSocietyInfo.bankAccount
                            }

                            val updatedInfo = initialSocietyInfo.copy(
                                name = societyName.trim(),
                                registrationNo = registrationNo.trim(),
                                bankName = bankName.trim(),
                                accountNumber = accountNumber.trim(),
                                ifscCode = ifscCode.trim(),
                                branchName = branchName.trim(),
                                bankAccount = fullBank,
                                upiId = upiId.trim(),
                                address = address.trim(),
                                city = city.trim(),
                                state = state.trim(),
                                pinCode = pinCode.trim(),
                                contactPhone = contactPhone.trim().ifBlank { "9876543210" },
                                contactEmail = contactEmail.trim().ifBlank { "contact@society.org" },
                                financialYear = financialYear.trim().ifBlank { "FY 2026-2027" },
                                openingBalance = parsedOpening.toDouble(),
                                presidentName = presidentName.trim(),
                                secretaryName = secretaryName.trim(),
                                treasurerName = treasurerName.trim()
                            )

                            onSaveAll(
                                updatedInfo,
                                validDonations,
                                validExpenses,
                                replaceExistingData
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        modifier = Modifier.testTag("save_all_master_data_btn")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Apply All Values at Once")
                    }
                }
            }
        }
    }
}
