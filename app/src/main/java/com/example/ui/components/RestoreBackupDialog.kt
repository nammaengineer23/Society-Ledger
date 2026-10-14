package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.SapphireNavy

@Composable
fun RestoreBackupDialog(
    onRestore: (restoreData: ExportHelper.RestoreBackupData, mergeMode: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var jsonContent by remember { mutableStateOf("") }
    var parsedData by remember { mutableStateOf<ExportHelper.RestoreBackupData?>(null) }
    var parseError by remember { mutableStateOf<String?>(null) }
    var isMergeMode by remember { mutableStateOf(false) }
    var showPasteField by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val text = inputStream?.bufferedReader().use { reader -> reader?.readText() } ?: ""
                jsonContent = text
                val parsed = ExportHelper.parseJsonBackup(text)
                if (parsed.success) {
                    parsedData = parsed
                    parseError = null
                } else {
                    parsedData = null
                    parseError = parsed.errorMessage ?: "Failed to read backup file."
                }
            } catch (e: Exception) {
                parseError = "Error reading file: ${e.localizedMessage}"
            }
        }
    }

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .testTag("restore_backup_dialog")
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
                                imageVector = Icons.Default.Restore,
                                contentDescription = null,
                                tint = SapphireNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Restore Society Records",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Import JSON Backup into Ledger",
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

                // Action to Pick File
                Button(
                    onClick = { filePickerLauncher.launch("application/json") },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_pick_backup_file")
                ) {
                    Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Backup File (.json)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Toggle Paste JSON option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { showPasteField = !showPasteField },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showPasteField) "Hide JSON Text Input" else "Or Paste JSON Text")
                    }
                }

                if (showPasteField) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = jsonContent,
                        onValueChange = {
                            jsonContent = it
                            if (it.trim().startsWith("{")) {
                                val parsed = ExportHelper.parseJsonBackup(it)
                                if (parsed.success) {
                                    parsedData = parsed
                                    parseError = null
                                } else {
                                    parsedData = null
                                    parseError = parsed.errorMessage
                                }
                            }
                        },
                        label = { Text("Paste JSON Backup Content") },
                        placeholder = { Text("{\"export_metadata\": ...}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("paste_json_field"),
                        maxLines = 6
                    )
                }

                // Error Banner
                parseError?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = err, color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Parsed Data Preview Card
                parsedData?.let { data ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SapphireNavy.copy(alpha = 0.06f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SapphireNavy.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ColorReceivedGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VALID BACKUP DETECTED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SapphireNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            data.societyInfo?.let { soc ->
                                Text(
                                    text = soc.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Financial Year: ${soc.financialYear} • Opening: ₹${String.format("%,.0f", soc.openingBalance)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }

                            HorizontalDivider(color = SapphireNavy.copy(alpha = 0.15f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Donations:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${data.donations.size} (₹${String.format("%,.0f", data.totalReceived)})",
                                        fontWeight = FontWeight.Bold,
                                        color = ColorReceivedGreen
                                    )
                                }
                                Column {
                                    Text("Expenses:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${data.expenses.size} (₹${String.format("%,.0f", data.totalExpenses)})",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD32F2F)
                                    )
                                }
                                Column {
                                    Text("Committee:", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        "${data.members.size} Members",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Restore Mode Selection
                    Text(
                        text = "SELECT RESTORATION MODE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    // Option 1: Complete Overwrite
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (!isMergeMode) SapphireNavy.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { isMergeMode = false }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !isMergeMode,
                            onClick = { isMergeMode = false },
                            colors = RadioButtonDefaults.colors(selectedColor = SapphireNavy)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Complete Records Replacement", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Replaces all current records with the contents of this backup.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Option 2: Merge Mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isMergeMode) SapphireNavy.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { isMergeMode = true }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isMergeMode,
                            onClick = { isMergeMode = true },
                            colors = RadioButtonDefaults.colors(selectedColor = SapphireNavy)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("Merge with Existing Data", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("Retains current transactions and appends backup records.", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Confirm Action Button
                    Button(
                        onClick = {
                            onRestore(data, isMergeMode)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_confirm_restore_json")
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMergeMode) "Confirm & Merge Backup" else "Confirm & Restore Complete Records",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
