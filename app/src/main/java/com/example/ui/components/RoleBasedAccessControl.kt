package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Member
import com.example.ui.theme.ColorExpenseRed
import com.example.ui.theme.ColorExpenseRedDark
import com.example.ui.theme.ColorReceivedGreen
import com.example.ui.theme.ColorReceivedGreenDark
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.SapphireNavy
import com.example.ui.theme.SocietyTealPrimary

/**
 * Generic Composable that conditionally renders admin-only content for committee members
 * versus regular member view-only content for residents.
 */
@Composable
fun RoleGuardedContent(
    isAdmin: Boolean,
    adminContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    regularMemberContent: (@Composable () -> Unit)? = null
) {
    Box(modifier = modifier) {
        if (isAdmin) {
            adminContent()
        } else {
            regularMemberContent?.invoke()
        }
    }
}

/**
 * A dedicated, interactive role-based UI component that conditionally displays
 * admin-only management features for committee members (President, Treasurer, Secretary)
 * versus regular member read-only transparency features for society residents.
 */
@Composable
fun RoleBasedAccessCard(
    isAdmin: Boolean,
    currentMember: Member? = null,
    onUnlockAdmin: () -> Unit = {},
    onSwitchToRegularMember: () -> Unit = {},
    onRecordDonation: () -> Unit = {},
    onRecordExpense: () -> Unit = {},
    onManageCommittee: () -> Unit = {},
    onExportData: () -> Unit = {},
    onViewDonationsLedger: () -> Unit = {},
    onViewExpensesLedger: () -> Unit = {},
    onViewFinancialReports: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current

    val isSuperAdmin = currentMember?.isSuperAdmin == true ||
            currentMember?.role?.contains("Super Admin", ignoreCase = true) == true ||
            currentMember?.role?.contains("President", ignoreCase = true) == true

    // Card styling colors based on role & dark theme
    val cardBg = if (isDark) {
        if (isAdmin) Color(0xFF162432) else Color(0xFF1E242B)
    } else {
        if (isAdmin) Color(0xFFF0F7FF) else Color(0xFFF8FAF9)
    }

    val cardBorderColor = if (isDark) {
        if (isAdmin) Color(0xFF254B6E) else Color(0xFF334155)
    } else {
        if (isAdmin) Color(0xFFBBDEFB) else Color(0xFFE2E8F0)
    }

    val roleBadgeBg = if (isAdmin) {
        if (isSuperAdmin) SapphireNavy else SocietyTealPrimary
    } else {
        if (isDark) Color(0xFF2E4035) else Color(0xFFE8F5E9)
    }

    val roleBadgeTextColor = if (isAdmin) {
        Color.White
    } else {
        if (isDark) ColorReceivedGreenDark else ColorReceivedGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("role_based_access_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Role Status Badge & Switcher Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Role Badge & Identity
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = if (isAdmin) {
                                    if (isSuperAdmin) SapphireNavy.copy(alpha = 0.15f) else SocietyTealPrimary.copy(alpha = 0.15f)
                                } else {
                                    ColorReceivedGreen.copy(alpha = 0.15f)
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdmin) {
                                if (isSuperAdmin) Icons.Default.Security else Icons.Default.AdminPanelSettings
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (isAdmin) "Admin Role" else "Member Role",
                            tint = if (isAdmin) {
                                if (isSuperAdmin) SapphireNavy else SocietyTealPrimary
                            } else {
                                if (isDark) ColorReceivedGreenDark else ColorReceivedGreen
                            },
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = roleBadgeBg,
                                modifier = Modifier.testTag("role_badge")
                            ) {
                                Text(
                                    text = if (isAdmin) {
                                        if (isSuperAdmin) "SUPER ADMIN" else "COMMITTEE ADMIN"
                                    } else {
                                        "REGULAR MEMBER"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = roleBadgeTextColor
                                    ),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isAdmin && currentMember != null) {
                                "${currentMember.name} (${currentMember.role})"
                            } else if (isAdmin) {
                                "Authorized Committee Member"
                            } else {
                                "Society Resident / Member View"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Toggle / Switch Action
                if (isAdmin) {
                    // Button to toggle into regular member view for verification
                    OutlinedButton(
                        onClick = onSwitchToRegularMember,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("role_toggle_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwitchAccount,
                            contentDescription = "Switch to Member View",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Member View",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                } else {
                    // Button to unlock Committee Admin privileges
                    Button(
                        onClick = onUnlockAdmin,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("unlock_admin_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndianSaffron
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Unlock Admin",
                            modifier = Modifier.size(14.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Unlock Admin",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // CONDITIONAL FEATURE AREA
            AnimatedContent(
                targetState = isAdmin,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "role_based_features_motion"
            ) { adminMode ->
                if (adminMode) {
                    // ==========================================
                    // ADMIN-ONLY FEATURES FOR COMMITTEE MEMBERS
                    // ==========================================
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF64B5F6) else SapphireNavy,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Controls • Write, Edit & Reconciliation Enabled",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = if (isDark) Color(0xFF90CAF9) else SapphireNavy
                                )
                            )
                        }

                        // Primary Action Buttons Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onRecordDonation,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("admin_feature_record_donation"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SocietyTealPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Donation",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Button(
                                onClick = onRecordExpense,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp)
                                    .testTag("admin_feature_record_expense"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) ColorExpenseRedDark else ColorExpenseRed
                                )
                            ) {
                                Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+ Expense",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Secondary Admin Management Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onManageCommittee,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("admin_feature_manage_committee"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Committee",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = onExportData,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("admin_feature_export_ledger"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Export Ledger",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // REGULAR MEMBER / RESIDENT TRANSPARENCY VIEW
                    // ==========================================
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Informative transparency banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isDark) Color(0xFF263238) else Color(0xFFE8F5E9)
                                )
                                .border(
                                    1.dp,
                                    if (isDark) Color(0xFF37474F) else Color(0xFFC8E6C9),
                                    RoundedCornerShape(10.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isDark) ColorReceivedGreenDark else ColorReceivedGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Read-Only Transparency Mode. Financial entries are locked for audit integrity.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        color = if (isDark) Color(0xFFCFD8DC) else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Regular Member Accessible Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onViewDonationsLedger,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("regular_feature_view_donations"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = SocietyTealPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Collections",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = onViewExpensesLedger,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("regular_feature_view_expenses"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isDark) ColorExpenseRedDark else ColorExpenseRed
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Expenses",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = onViewFinancialReports,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("regular_feature_financial_summary"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp),
                                    tint = if (isDark) Color(0xFF90CAF9) else SapphireNavy
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Summary",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
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
