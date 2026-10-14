package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

data class CommunityAdItem(
    val id: String,
    val title: String,
    val category: String,
    val offerTag: String,
    val description: String,
    val vendorName: String,
    val contactPhone: String,
    val contactEmail: String,
    val badgeColor: Color = SapphireNavy,
    val icon: ImageVector = Icons.Default.Verified
)

object CommunityAdRepository {
    val ALL_ADS = listOf(
        CommunityAdItem(
            id = "ad_audit_ca",
            title = "Apex Society Audit & CA Partners",
            category = "RWA Audit & Legal",
            offerTag = "Flat 25% Off Annual Audit Package",
            description = "Govt-registered CAs specializing in RWA & Apartment balance sheet audit, society bylaws compliance, tax filing & AGM financial reports.",
            vendorName = "Apex Chartered & Advisory Services",
            contactPhone = "+91 98860 11223",
            contactEmail = "audits@apexadvisors.in",
            badgeColor = SapphireNavy,
            icon = Icons.Default.AccountBalance
        ),
        CommunityAdItem(
            id = "ad_solar_rooftop",
            title = "GreenVolt Solar for Apartments",
            category = "Clean Energy & Subsidies",
            offerTag = "Save 70% on Common Electricity",
            description = "Zero-capex rooftop solar installation for housing societies with PM Surya Ghar subsidy approval & net-metering setup.",
            vendorName = "GreenVolt Clean Energy Pvt Ltd",
            contactPhone = "+91 98450 33445",
            contactEmail = "solar@greenvoltenergy.in",
            badgeColor = IndianSaffron,
            icon = Icons.Default.Lightbulb
        ),
        CommunityAdItem(
            id = "ad_smart_security",
            title = "SecureNest AI Boom Barrier & CCTV",
            category = "Security & Access Automation",
            offerTag = "Free 30-Day On-Site Trial for RWAs",
            description = "Automated ANPR vehicle license plate reading, digital visitor logbook, HD perimeter surveillance, and 24/7 security control room.",
            vendorName = "SecureNest Technologies",
            contactPhone = "+91 99001 55667",
            contactEmail = "sales@securenest.io",
            badgeColor = Color(0xFF2E7D32),
            icon = Icons.Default.Security
        ),
        CommunityAdItem(
            id = "ad_tank_water",
            title = "PureAqua Mechanized Tank Cleaning",
            category = "Water & Hygiene AMC",
            offerTag = "Free Water Quality & TDS Report",
            description = "6-stage German UV & high-pressure mechanized water sump and overhead tank cleaning. Scheduled AMC with hygiene compliance certificate.",
            vendorName = "PureAqua Facility Hygiene",
            contactPhone = "+91 97400 77889",
            contactEmail = "care@pureaquaclean.com",
            badgeColor = SocietyTealPrimary,
            icon = Icons.Default.CheckCircle
        ),
        CommunityAdItem(
            id = "ad_lift_amc",
            title = "ElevateCare Lift & Elevator AMC",
            category = "Facility Maintenance & Safety",
            offerTag = "24/7 Emergency Breakdown Support",
            description = "ISO-certified elevator maintenance, annual safety fitness certification, and monthly preventative servicing for residential high-rises.",
            vendorName = "ElevateCare Engineering Systems",
            contactPhone = "+91 98800 44556",
            contactEmail = "support@elevatecare.in",
            badgeColor = SapphireNavy,
            icon = Icons.Default.Handyman
        )
    )

    fun getAdsForCategory(categoryHint: String): List<CommunityAdItem> {
        val matches = ALL_ADS.filter { it.category.contains(categoryHint, ignoreCase = true) || it.title.contains(categoryHint, ignoreCase = true) }
        return if (matches.isNotEmpty()) matches else ALL_ADS
    }
}

/**
 * Standard Multi-Ad Banner for Home & Main Screens.
 * Automatically hidden if user is a paid/Pro subscriber.
 */
@Composable
fun CommunityAdBanner(
    isProSubscriber: Boolean,
    modifier: Modifier = Modifier,
    preferredCategory: String? = null,
    onNavigateToSubscription: (() -> Unit)? = null
) {
    // Zero-display rule for subscribed members
    if (isProSubscriber) {
        return
    }

    val context = LocalContext.current
    val ads = remember(preferredCategory) {
        if (preferredCategory != null) CommunityAdRepository.getAdsForCategory(preferredCategory)
        else CommunityAdRepository.ALL_ADS
    }

    var currentAdIndex by remember { mutableIntStateOf(0) }
    var showAdInquiryDialog by remember { mutableStateOf(false) }
    var selectedAdForInquiry by remember { mutableStateOf<CommunityAdItem?>(null) }
    var showAdvertiseWithUsDialog by remember { mutableStateOf(false) }

    val currentAd = ads[currentAdIndex % ads.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("community_ad_banner_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Ad Badge, Title & Switch Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = IndianSaffron.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(0.5.dp, IndianSaffron)
                    ) {
                        Text(
                            text = "SPONSORED",
                            color = IndianSaffron,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Society Partner Network",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Next / Prev ad controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(currentAdIndex % ads.size) + 1}/${ads.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = {
                            currentAdIndex = if (currentAdIndex > 0) currentAdIndex - 1 else ads.size - 1
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Ad",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            currentAdIndex = (currentAdIndex + 1) % ads.size
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Ad",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ad Body Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(currentAd.badgeColor.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentAd.icon,
                        contentDescription = null,
                        tint = currentAd.badgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentAd.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = currentAd.offerTag,
                            color = Color(0xFF1B5E20),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentAd.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "By ${currentAd.vendorName}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Inquire & Run Ads Here
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        selectedAdForInquiry = currentAd
                        showAdInquiryDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SapphireNavy),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_ad_inquire"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Inquire / Avail Offer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = { showAdvertiseWithUsDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_run_ads_here"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Advertise Here", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showAdInquiryDialog && selectedAdForInquiry != null) {
        AdInquiryDialog(
            ad = selectedAdForInquiry!!,
            onDismiss = { showAdInquiryDialog = false }
        )
    }

    if (showAdvertiseWithUsDialog) {
        AdvertiseWithUsDialog(
            onDismiss = { showAdvertiseWithUsDialog = false }
        )
    }
}

/**
 * Compact Inline Ad Card for list headers/footers in Expenses, Donors, Reports.
 * Zero-display rule when isProSubscriber is true.
 */
@Composable
fun CommunityCompactAdCard(
    isProSubscriber: Boolean,
    adCategory: String,
    modifier: Modifier = Modifier,
    onNavigateToSubscription: (() -> Unit)? = null
) {
    if (isProSubscriber) {
        return
    }

    val context = LocalContext.current
    val ads = remember(adCategory) { CommunityAdRepository.getAdsForCategory(adCategory) }
    val ad = ads.firstOrNull() ?: CommunityAdRepository.ALL_ADS.first()

    var showInquiry by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("compact_ad_${ad.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ad.badgeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ad.icon,
                        contentDescription = null,
                        tint = ad.badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = IndianSaffron.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(3.dp)
                        ) {
                            Text(
                                text = "AD",
                                color = IndianSaffron,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 8.sp),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = ad.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${ad.offerTag} • ${ad.vendorName}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = SapphireNavy,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.clickable { showInquiry = true }
            ) {
                Text(
                    text = "Inquire",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }

    if (showInquiry) {
        AdInquiryDialog(
            ad = ad,
            onDismiss = { showInquiry = false }
        )
    }
}

/**
 * Reusable Ad Inquiry Dialog with WhatsApp, Direct Dial, and Email options.
 */
@Composable
fun AdInquiryDialog(
    ad: CommunityAdItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(ad.badgeColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ad.icon,
                    contentDescription = null,
                    tint = ad.badgeColor,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = ad.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "OFFER: ${ad.offerTag}",
                        color = Color(0xFF1B5E20),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = ad.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "VERIFIED PARTNER DETAILS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SapphireNavy
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vendor: ${ad.vendorName}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )
                        Text(
                            text = "Helpline: ${ad.contactPhone}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "Email: ${ad.contactEmail}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                // WhatsApp Action
                Button(
                    onClick = {
                        val msg = "Hello, I am contacting you from Society Ledger regarding your offer: '${ad.offerTag}' for our apartment/society."
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=${ad.contactPhone.replace(" ", "").replace("+", "")}&text=${Uri.encode(msg)}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(msg)}"))
                            context.startActivity(browserIntent)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_ad_whatsapp_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Connect on WhatsApp", fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Call Action
                OutlinedButton(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.contactPhone}"))
                        context.startActivity(dialIntent)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_ad_call_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Call Partner (${ad.contactPhone})", fontWeight = FontWeight.SemiBold)
                }

                // Email Action
                OutlinedButton(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:${ad.contactEmail}")
                            putExtra(Intent.EXTRA_SUBJECT, "Inquiry for ${ad.title} from Society Ledger")
                            putExtra(Intent.EXTRA_TEXT, "Hello,\n\nWe are interested in availing '${ad.offerTag}' for our society.\nPlease share your brochure and pricing quotation.\n\nThank you.")
                        }
                        context.startActivity(emailIntent)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_ad_email_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Email Inquiry", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

/**
 * Reusable "Advertise With Us" Dialog.
 */
@Composable
fun AdvertiseWithUsDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    KeyboardAwareAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(IndianSaffron.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddBusiness,
                    contentDescription = null,
                    tint = IndianSaffron,
                    modifier = Modifier.size(26.dp)
                )
            }
        },
        title = {
            Text(
                text = "Advertise with Society Ledger",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Showcase your professional society services directly to Apartment Presidents, RWA Treasurers, and Facility Committees.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "POPULAR ADVERTISER CATEGORIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = SapphireNavy
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "• Solar Rooftop & EV Charging Stations", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                        Text(text = "• Boom Barriers, ANPR & CCTV Security", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                        Text(text = "• Chartered Accountants & Society Audit", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                        Text(text = "• Water Sump & STP Maintenance AMC", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                        Text(text = "• Lift AMC, Painting & Waterproofing", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                    }
                }

                Button(
                    onClick = {
                        val msg = "Hello Society Ledger Team, I am interested in running advertisements & partnering for society vendor services."
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://api.whatsapp.com/send?phone=919886012345&text=${Uri.encode(msg)}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=${Uri.encode(msg)}"))
                            context.startActivity(browserIntent)
                        }
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_ad_desk_whatsapp_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WhatsApp Ad Desk", fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ads@societyledger.org")
                            putExtra(Intent.EXTRA_SUBJECT, "Advertising Inquiry for Society Ledger App")
                            putExtra(Intent.EXTRA_TEXT, "Hello Team,\n\nI would like to publish an advertisement / partnership banner in Society Ledger.\n\nBusiness Name:\nContact Phone:\nServices Offered:\n\nThank you.")
                        }
                        context.startActivity(emailIntent)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_ad_desk_email_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Email: ads@societyledger.org", fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
