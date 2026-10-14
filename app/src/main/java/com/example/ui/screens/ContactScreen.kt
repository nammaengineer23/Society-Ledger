package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SocietyInfo
import com.example.ui.components.ContactSupportDialog
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

data class EmergencyContact(
    val title: String,
    val person: String,
    val phone: String,
    val tag: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    societyInfo: SocietyInfo,
    onBack: () -> Unit,
    onSubmitComplaint: ((com.example.data.model.AppComplaint) -> Unit)? = null,
    onNavigateToFaq: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var inquiryName by remember { mutableStateOf("") }
    var inquiryPhone by remember { mutableStateOf("") }
    var inquiryCategory by remember { mutableStateOf("Donation Clarification") }
    var inquiryMessage by remember { mutableStateOf("") }
    var inquirySent by remember { mutableStateOf(false) }
    var showContactSupportDialog by remember { mutableStateOf(false) }

    val emergencyList = remember(societyInfo) {
        val list = mutableListOf<EmergencyContact>()
        if (societyInfo.securityGatePhone.isNotBlank()) {
            list.add(EmergencyContact("Society Security Gate", "Chief Security Officer", societyInfo.securityGatePhone, "24/7 Security", Color(0xFF1E88E5)))
        }
        if (societyInfo.maintenanceLeadPhone.isNotBlank()) {
            list.add(EmergencyContact("Maintenance Helpline", "Technician Team", societyInfo.maintenanceLeadPhone, "Lift / Electrical", Color(0xFFE53935)))
        }
        if (societyInfo.plumberPhone.isNotBlank()) {
            list.add(EmergencyContact("Water & Plumbing Support", "Society Plumber", societyInfo.plumberPhone, "Sanitary / Tank", Color(0xFF00897B)))
        }
        if (societyInfo.emergencyHelplinePhone.isNotBlank()) {
            list.add(EmergencyContact("Local Emergency Helpline", "Emergency Services", societyInfo.emergencyHelplinePhone, "Police / Ambulance", Color(0xFF5E35B1)))
        }
        list
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("contact_screen"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Contact & Clarifications",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showContactSupportDialog = true },
                        modifier = Modifier.testTag("contact_support_action_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Contact Master Admin",
                            tint = SapphireNavy
                        )
                    }
                    if (onNavigateToFaq != null) {
                        IconButton(onClick = onNavigateToFaq, modifier = Modifier.testTag("contact_faq_action_btn")) {
                            Icon(Icons.Default.HelpOutline, contentDescription = "FAQ", tint = SapphireNavy)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Master Admin Support Banner Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showContactSupportDialog = true }
                        .testTag("contact_support_banner"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.07f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(SapphireNavy, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = IndianSaffron,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Contact App Support",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = SapphireNavy)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(IndianSaffron.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "DIRECT",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SapphireNavy
                                        )
                                    }
                                }
                                Text(
                                    text = "Send feedback, requests, or bug reports directly to Master Admin (nammaengineer23@gmail.com)",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { showContactSupportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("Open", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            if (onNavigateToFaq != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToFaq() }
                            .testTag("contact_faq_banner"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.08f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(SapphireNavy.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Looking for Quick Answers?", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = SapphireNavy))
                                    Text("Browse our searchable Frequently Asked Questions (FAQ)", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                                }
                            }
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = SapphireNavy, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            // RWA Office & Official Desk Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SapphireNavy),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .background(IndianSaffron, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "OFFICIAL DESK",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = societyInfo.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        Text(
                            text = "Registration No: ${societyInfo.registrationNo}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        val addressText = listOfNotNull(
                            societyInfo.address.takeIf { it.isNotBlank() },
                            societyInfo.city.takeIf { it.isNotBlank() },
                            societyInfo.state.takeIf { it.isNotBlank() },
                            societyInfo.pinCode.takeIf { it.isNotBlank() }
                        ).joinToString(", ")
                        if (addressText.isNotBlank()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = addressText, style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Office Hours: Mon - Sat: 9:00 AM - 7:00 PM | Sun: 10:00 AM - 2:00 PM", style = MaterialTheme.typography.bodySmall.copy(color = Color.White))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val phone = societyInfo.contactPhone
                                    if (phone.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Contact phone not yet entered by Admin", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Call Admin", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val email = societyInfo.adminEmail.ifBlank { societyInfo.contactEmail }
                                    if (email.isNotBlank()) {
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:$email")
                                            putExtra(Intent.EXTRA_SUBJECT, "Inquiry: ${societyInfo.name}")
                                        }
                                        context.startActivity(intent)
                                    } else {
                                        Toast.makeText(context, "Admin email not yet entered by Admin", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Email, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Email Admin", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Emergency Contacts Directory
            item {
                Text(
                    text = "Emergency & Maintenance Helpline",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (emergencyList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No emergency helpline numbers configured.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Emergency phone numbers can be entered by the Admin in Society Settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            } else {
                items(emergencyList) { emergency ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(emergency.color.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = null,
                                    tint = emergency.color,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = emergency.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${emergency.person} • ${emergency.tag}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${emergency.phone}"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = emergency.color),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

            // Inquiry & Clarification Ticket Form
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = SocietyTealPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Submit Clarification or Audit Inquiry",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Text(
                            text = "Residents and members can ask clarifications regarding receipts, pending pledges, or audited balance sheets.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (inquirySent) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "✅ Inquiry Submitted to Committee",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    )
                                    Text(
                                        text = "Your request has been logged. The committee secretary / treasurer will clarify within 24 business hours.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = inquiryName,
                                onValueChange = { inquiryName = it },
                                label = { Text("Your Full Name / Flat No") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = inquiryPhone,
                                onValueChange = { inquiryPhone = it },
                                label = { Text("Contact Phone / WhatsApp Number") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = inquiryMessage,
                                onValueChange = { inquiryMessage = it },
                                label = { Text("Clarification Question or Feedback") },
                                placeholder = { Text("Enter clarification question or feedback...") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (inquiryName.isNotBlank() && inquiryMessage.isNotBlank()) {
                                        inquirySent = true
                                        val newComplaint = com.example.data.model.AppComplaint(
                                            id = "TKT-${System.currentTimeMillis() % 100000}",
                                            societyName = societyInfo.name,
                                            submittedByName = inquiryName.trim(),
                                            contactPhone = inquiryPhone.ifBlank { societyInfo.contactPhone },
                                            contactEmail = societyInfo.adminEmail,
                                            category = inquiryCategory,
                                            subject = "Resident Inquiry: $inquiryCategory",
                                            description = inquiryMessage.trim(),
                                            status = "OPEN",
                                            priority = "MEDIUM",
                                            createdAt = System.currentTimeMillis()
                                        )
                                        onSubmitComplaint?.invoke(newComplaint)
                                        Toast.makeText(context, "Clarification #${newComplaint.id} logged with App Desk!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please enter your name and message.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send Clarification Request", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Copyright & Clarifications Policy Footer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "© 2026 Society Ledger Transparency Systems. All rights reserved.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Governed under State Societies Registration Act & RWA Bylaws.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (showContactSupportDialog) {
            ContactSupportDialog(
                societyInfo = societyInfo,
                onDismiss = { showContactSupportDialog = false },
                onSubmitTicket = onSubmitComplaint
            )
        }
    }
}
