package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SocietyInfo
import com.example.ui.theme.SocietyTealPrimary
import java.util.Locale

/**
 * Builds standard NPCI BHIM/UPI URI for payments.
 */
fun buildUpiPaymentUri(
    upiId: String,
    societyName: String,
    amount: Double?,
    note: String = "Donation"
): String {
    val cleanUpiId = upiId.trim().ifBlank { "greenvalleysociety@upi" }
    val encodedPayee = try {
        java.net.URLEncoder.encode(societyName.trim().take(25).ifBlank { "Society Treasury" }, "UTF-8").replace("+", "%20")
    } catch (e: Exception) {
        "Society"
    }
    val encodedNote = try {
        java.net.URLEncoder.encode(note.trim().take(25).ifBlank { "Donation" }, "UTF-8").replace("+", "%20")
    } catch (e: Exception) {
        "Donation"
    }

    return buildString {
        append("upi://pay?")
        append("pa=").append(cleanUpiId)
        append("&pn=").append(encodedPayee)
        if (amount != null && amount > 0) {
            append("&am=").append(String.format(Locale.US, "%.2f", amount))
        }
        append("&cu=INR")
        append("&tn=").append(encodedNote)
    }
}

/**
 * Interactive Society UPI QR Card displayed when UPI payment mode is selected.
 * Dynamically encodes the society's configured UPI ID and the entered donation amount.
 */
@Composable
fun SocietyUpiQrCard(
    societyInfo: SocietyInfo,
    amount: Double?,
    donorName: String = "",
    upiReferenceNo: String = "",
    onUpiReferenceChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val effectiveUpiId = societyInfo.upiId.trim().ifBlank { "greenvalleysociety@upi" }
    val isDefaultUpi = societyInfo.upiId.trim().isBlank()

    val upiUri = remember(effectiveUpiId, societyInfo.name, amount) {
        buildUpiPaymentUri(
            upiId = effectiveUpiId,
            societyName = societyInfo.name,
            amount = amount,
            note = if (donorName.isNotBlank()) "Donation_${donorName.take(12).filter { it.isLetterOrDigit() }}" else "Society_Donation"
        )
    }

    val qrBitmap = remember(upiUri) {
        try {
            QrCodeGenerator.generateQrBitmap(
                content = upiUri,
                size = 512,
                foregroundColor = android.graphics.Color.rgb(18, 18, 18),
                backgroundColor = android.graphics.Color.WHITE
            )
        } catch (e: Exception) {
            null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("society_upi_qr_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5E35B1).copy(alpha = 0.07f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF5E35B1).copy(alpha = 0.45f)),
            width = 1.5.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF5E35B1), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "UPI QR Code",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Society UPI Payment QR",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5E35B1)
                        )
                        Text(
                            text = "Scan to pay directly into Society Bank Account",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Society Payee & UPI ID Badge with Copy Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, Color(0xFF5E35B1).copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Payee: ${societyInfo.name}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    Text(
                        text = effectiveUpiId,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5E35B1),
                            fontSize = 13.sp
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(effectiveUpiId))
                            Toast.makeText(context, "Society UPI ID copied: $effectiveUpiId", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("copy_society_upi_id_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy UPI ID",
                            tint = Color(0xFF5E35B1),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // QR Code Presentation Canvas
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFD1C4E9), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Society UPI QR Code for payment",
                        modifier = Modifier
                            .size(174.dp)
                            .testTag("upi_qr_code_image")
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Generating QR Code...",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Amount Indicator Badge
            if (amount != null && amount > 0) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE8F5E9))
                        .border(1.dp, Color(0xFF81C784), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Amount encoded: ₹${String.format(Locale.US, "%,.0f", amount)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }
            } else {
                Text(
                    text = "💡 Type donation amount above to set exact amount in QR",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Supported Payment Apps Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Accepts: GPay • PhonePe • Paytm • BHIM • Cred • All UPI Apps",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                    color = Color(0xFF5E35B1).copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Intent Trigger: Open installed UPI apps on device
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(upiUri))
                            val chooser = Intent.createChooser(intent, "Pay via UPI App")
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "No UPI app found on device. Scan QR code using Google Pay, PhonePe or Paytm on your phone.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_upi_app_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF5E35B1)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Open UPI App",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Copy UPI Payment Link
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(upiUri))
                        Toast.makeText(context, "UPI Payment Link copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_upi_link_button"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Copy Link",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transaction UTR / Reference No Field
            OutlinedTextField(
                value = upiReferenceNo,
                onValueChange = onUpiReferenceChange,
                label = { Text("UPI Ref / UTR / Txn ID (Optional)") },
                placeholder = { Text("e.g. 482910492810") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(0xFF5E35B1)
                    )
                },
                supportingText = {
                    Text(
                        text = "12-digit UTR from donor's GPay/PhonePe screen for audit verification",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_upi_reference_input"),
                singleLine = true
            )

            if (isDefaultUpi) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Society UPI ID is using default ('$effectiveUpiId'). Admin can customize this under Society Settings.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Dedicated Full-screen/Popup Dialog to display Society UPI QR code directly,
 * allowing instant scanning without opening the complete donation form first.
 */
@Composable
fun SocietyUpiQrDialog(
    societyInfo: SocietyInfo,
    onDismiss: () -> Unit,
    onRecordDonation: (amount: Double?) -> Unit = {}
) {
    var customAmountText by remember { mutableStateOf("") }
    val parsedAmount = customAmountText.toDoubleOrNull()

    KeyboardAwareDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp)
                .testTag("society_upi_qr_dialog"),
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
                                .size(36.dp)
                                .background(Color(0xFF5E35B1), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Society Payment QR",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = societyInfo.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Amount Pre-fill Field
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = { input ->
                        val digits = input.filter { it.isDigit() }
                        customAmountText = digits
                    },
                    label = { Text("Pre-fill Amount in QR (Optional)") },
                    placeholder = { Text("e.g. 5000 (leave blank for open donation)") },
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_qr_custom_amount_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // The UPI QR Card
                SocietyUpiQrCard(
                    societyInfo = societyInfo,
                    amount = parsedAmount,
                    donorName = ""
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action to jump into Recording Donation
                Button(
                    onClick = {
                        onDismiss()
                        onRecordDonation(parsedAmount)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("record_donation_from_qr_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Record Donation from this Payment >",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
